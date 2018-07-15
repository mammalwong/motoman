package com.marcowong.motoman.track;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class TrackGenerator {
	public IRandom random = new BasicRandom(100000);
	public int turnAngleSmoothFactor = 2;
	public float turnAngleZeroFactor = 0.2f;
	public float sharpAngleReference = 75;
	public float trackLen = 100;
	public float segLen = 1;
	public float segWidth = 1;
	public float segPad = 0.5f;
	
	private static class UpdateTurnAngleContext {
		public int retryCount = 0;
	}
	
	private float lastTurnAngle = 0;
	private int updateTurnAngleCounter = 0;
	private float updateTurnAngle(UpdateTurnAngleContext context) {
		if (++context.retryCount > 10) return Float.NaN;
		if (((++updateTurnAngleCounter) % turnAngleSmoothFactor) == 0) {
			return lastTurnAngle;
		}
		if (Math.abs(lastTurnAngle) > 60 &&
			random.next() < 0.8) {
			return lastTurnAngle;
		}
		if (Math.abs(lastTurnAngle) > 30 &&
			random.next() < 0.2) {
			return lastTurnAngle;
		}
		float a = random.next();
		//a = (float)Math.pow(a, 1);
		a = Math.max(0, -turnAngleZeroFactor + a * (1 + turnAngleZeroFactor)) * 90;
		float signedA = a * (random.next() > 0.5f ? 1 : -1);
		if (a < sharpAngleReference) {
			lastTurnAngle = signedA;
		}
		return signedA;
	}
	
	public List<TrackSegment> generate() {
		List<TrackSegment> tss = new ArrayList<TrackSegment>();
		float tTLen = 0;
		
		final String turnAngleContextKey = "turnAngleContext";
		TrackSegment lastSeg = new TrackSegment();
		lastSeg.x1 = 0;
		lastSeg.y1 = 0;
		lastSeg.l1 = -segWidth;
		lastSeg.r1 = segWidth;
		lastSeg.w1 = 0;
		lastSeg.x2 = 0;
		lastSeg.y2 = segLen;
		lastSeg.attributes.put(turnAngleContextKey, new UpdateTurnAngleContext());
		tss.add(lastSeg);
		tTLen += segLen;
		
		Vector3 vec = new Vector3();
		Matrix4 mat = new Matrix4();
		IsLineIntersectST st = new IsLineIntersectST();
		TrackSegLine[] linesA = new TrackSegLine[4];
		TrackSegLine[] linesB = new TrackSegLine[4];
		genLoop: while (tTLen < trackLen) {
			float turnAngle = updateTurnAngle((UpdateTurnAngleContext)lastSeg.attributes.get(turnAngleContextKey));
			if (turnAngle != turnAngle) {
				if (tss.size() < 2) {
					lastSeg.attributes.put(turnAngleContextKey, new UpdateTurnAngleContext());
				} else {
					tTLen -= vec.set(lastSeg.x2 - lastSeg.x1, lastSeg.y2 - lastSeg.y1, 0).len();
					lastSeg = tss.get(tss.size() - 2);
					tss.remove(tss.size() - 1);
				}
				System.out.println("pop " + tss.size());
				continue genLoop;
			}
			
			TrackSegment curSeg = new TrackSegment();
			curSeg.x1 = lastSeg.x2;
			curSeg.y1 = lastSeg.y2;
			curSeg.attributes.put(turnAngleContextKey, new UpdateTurnAngleContext());
			
			vec.set(lastSeg.x2 - lastSeg.x1, lastSeg.y2 - lastSeg.y1, 0).nor();
			float lastTurnAngle = (float)(Math.atan2(vec.y, vec.x) * (180 / Math.PI)) - 90;
			
			curSeg.w1 = lastTurnAngle + turnAngle;
			vec.set(0, segLen, 0);
			mat.idt();
			mat.translate(curSeg.x1, curSeg.y1, 0);
			mat.rotate(0, 0, 1, curSeg.w1);
			vec.mul(mat);
			curSeg.x2 = vec.x;
			curSeg.y2 = vec.y;
			curSeg.l1 = -segWidth;
			curSeg.r1 = segWidth;
			
			TrackSegLines lastSegLines = getLinesByPathAndHead(lastSeg);
			TrackSegLines curSegLines = getLinesByPathAndHead(curSeg);
			doLineIntersect(
					lastSegLines.l.x1, lastSegLines.l.y1, lastSegLines.l.x2, lastSegLines.l.y2,
					curSegLines.l.x2, curSegLines.l.y2, curSegLines.l.x1, curSegLines.l.y1, st);
			float lx, ly;
			if (st.t == st.t) {
				lx = lastSegLines.l.x1 + (lastSegLines.l.x2 - lastSegLines.l.x1) * st.t;
				ly = lastSegLines.l.y1 + (lastSegLines.l.y2 - lastSegLines.l.y1) * st.t;
			} else {
				lx = curSegLines.l.x2;
				ly = curSegLines.l.y2;
			}
			doLineIntersect(
					lastSegLines.r.x2, lastSegLines.r.y2, lastSegLines.r.x1, lastSegLines.r.y1,
					curSegLines.r.x1, curSegLines.r.y1, curSegLines.r.x2, curSegLines.r.y2, st);
			float rx, ry;
			if (st.t == st.t) {
				rx = lastSegLines.r.x2 + (lastSegLines.r.x1 - lastSegLines.r.x2) * st.t;
				ry = lastSegLines.r.y2 + (lastSegLines.r.y1 - lastSegLines.r.y2) * st.t;
			} else {
				rx = curSegLines.r.x1;
				ry = curSegLines.r.y1;
			}
			float lLen = vec.set(lx - curSeg.x1, ly - curSeg.y1, 0).len();
			float rLen = vec.set(rx - curSeg.x1, ry - curSeg.y1, 0).len();
			vec.set(ry - ly, rx - lx, 0).nor();
			float ang = (float)(Math.atan2(vec.x, vec.y) * (180 / Math.PI));
			lastSeg.l2 = -lLen;
			lastSeg.r2 = rLen;
			lastSeg.w2 = ang;
			curSeg.l1 = -lLen;
			curSeg.r1 = rLen;
			curSeg.w1 = ang;
			
			linesA[0] = curSegLines.hp;
			linesA[1] = curSegLines.lp;
			linesA[2] = curSegLines.rp;
			linesA[3] = curSegLines.tp;
			boolean noCollision = true;
			trackSegCollsionTest1: for (int i = 0; noCollision && i < tss.size() - 1; ++i) {
				TrackSegment ts = tss.get(i);
				TrackSegLines tsLines = getLinesByPathAndAll(ts);
				if (ts == lastSeg) continue;
				linesB[0] = tsLines.hp;
				linesB[1] = tsLines.lp;
				linesB[2] = tsLines.rp;
				linesB[3] = tsLines.tp;
				for (TrackSegLine lineA : linesA) {
					for (TrackSegLine lineB : linesB) {
						if (isLineIntersect(lineA.x1, lineA.y1, lineA.x2, lineA.y2,
								lineB.x1, lineB.y1, lineB.x2, lineB.y2)) {
							noCollision = false;
							break trackSegCollsionTest1;
						}
					}
				}
			}
			TrackSegLines semiFinalizedLastSegLines = getLinesByPathAndAll(lastSeg);
			linesA[0] = semiFinalizedLastSegLines.hp;
			linesA[1] = semiFinalizedLastSegLines.lp;
			linesA[2] = semiFinalizedLastSegLines.rp;
			linesA[3] = semiFinalizedLastSegLines.tp;
			trackSegCollsionTest2: for (int i = 0; noCollision && i < tss.size() - 2; ++i) {
				TrackSegment ts = tss.get(i);
				TrackSegLines tsLines = getLinesByPathAndAll(ts);
				if (ts == lastSeg) continue;
				linesB[0] = tsLines.hp;
				linesB[1] = tsLines.lp;
				linesB[2] = tsLines.rp;
				linesB[3] = tsLines.tp;
				for (TrackSegLine lineA : linesA) {
					for (TrackSegLine lineB : linesB) {
						if (isLineIntersect(lineA.x1, lineA.y1, lineA.x2, lineA.y2,
								lineB.x1, lineB.y1, lineB.x2, lineB.y2)) {
							noCollision = false;
							break trackSegCollsionTest2;
						}
					}
				}
			}
			
			if (noCollision) {
				tTLen += vec.set(curSeg.x2 - curSeg.x1, curSeg.y2 - curSeg.y1, 0).len();
				tss.add(curSeg);
				lastSeg = curSeg;
			}
		}
		
		if (tTLen < trackLen) return null;
		
		TrackSegment finalSeg = lastSeg;
		TrackSegLines finalSegLines = getLinesByPathAndHead(finalSeg);
		float lLen = vec.set(finalSegLines.l.x1 - finalSeg.x2, finalSegLines.l.y1 - finalSeg.y2, 0).len();
		float rLen = vec.set(finalSegLines.r.x2 - finalSeg.x2, finalSegLines.r.y2 - finalSeg.y2, 0).len();
		vec.set(finalSegLines.r.y2 - finalSegLines.l.y1, finalSegLines.r.x2 - finalSegLines.l.x1, 0).nor();
		float ang = (float)(Math.atan2(vec.x, vec.y) * (180 / Math.PI));
		finalSeg.l2 = -lLen;
		finalSeg.r2 = rLen;
		finalSeg.w2 = ang;
		
		TrackSegment prevTS = null;
		for (TrackSegment ts : tss) {
			ts.attributes.remove(turnAngleContextKey);
			ts.attributes.put("lines", getLinesByPathAndAll(ts));
			ts.prev = prevTS;
			if (prevTS != null) prevTS.next = ts;
			prevTS = ts;
		}
		
		return tss;
	}
	
	private TrackSegLines getLinesByPathAndHead(TrackSegment ts) {
		return getLinesOfTrackSegment(ts, true);
	}
	
	private TrackSegLines getLinesByPathAndAll(TrackSegment ts) {
		return getLinesOfTrackSegment(ts, false);
	}
	
	private Vector3 getLinesByPathAndHeadTmpVec = new Vector3();
	private Vector3 getLinesByPathAndHeadTmpVec2 = new Vector3();
	private Vector3 getLinesByPathAndHeadTmpVec3 = new Vector3();
	private Matrix4 getLinesByPathAndHeadTmpMat = new Matrix4();
	private IsLineIntersectST getLinesByPathAndHeadTmpST = new IsLineIntersectST();
	private TrackSegLines getLinesOfTrackSegment(TrackSegment ts, boolean isEndUndefined) {
		TrackSegLines tsl = new TrackSegLines();
		
		getLinesByPathAndHeadTmpMat.idt();
		getLinesByPathAndHeadTmpMat.translate(ts.x1, ts.y1, 0);
		getLinesByPathAndHeadTmpMat.rotate(0, 0, 1, ts.w1);
		
		getLinesByPathAndHeadTmpVec.set(ts.l1, 0, 0);
		getLinesByPathAndHeadTmpVec.mul(getLinesByPathAndHeadTmpMat);
		tsl.l.x2 = tsl.h.x1 = getLinesByPathAndHeadTmpVec.x;
		tsl.l.y2 = tsl.h.y1 = getLinesByPathAndHeadTmpVec.y;
		getLinesByPathAndHeadTmpVec.set(ts.r1, 0, 0);
		getLinesByPathAndHeadTmpVec.mul(getLinesByPathAndHeadTmpMat);
		tsl.r.x1 = tsl.h.x2 = getLinesByPathAndHeadTmpVec.x;
		tsl.r.y1 = tsl.h.y2 = getLinesByPathAndHeadTmpVec.y;
		
		getLinesByPathAndHeadTmpVec.set(ts.x2 - ts.x1, ts.y2 - ts.y1, 0);
		getLinesByPathAndHeadTmpVec.crs(0, 0, 1);
		getLinesByPathAndHeadTmpVec.nor(); 
		getLinesByPathAndHeadTmpVec2.set(ts.l1, 0, 0);
		getLinesByPathAndHeadTmpVec2.mul(getLinesByPathAndHeadTmpMat);
		getLinesByPathAndHeadTmpVec3.set(getLinesByPathAndHeadTmpVec);
		getLinesByPathAndHeadTmpVec3.mul(-(segWidth + segPad));
		getLinesByPathAndHeadTmpVec3.add(ts.x1, ts.y1, 0);
		doLineIntersect(ts.x1, ts.y1,
				getLinesByPathAndHeadTmpVec2.x,
				getLinesByPathAndHeadTmpVec2.y,
				getLinesByPathAndHeadTmpVec3.x,
				getLinesByPathAndHeadTmpVec3.y,
				getLinesByPathAndHeadTmpVec3.x + (ts.x2 - ts.x1),
				getLinesByPathAndHeadTmpVec3.y + (ts.y2 - ts.y1),
				getLinesByPathAndHeadTmpST);
		tsl.lp.x2 = tsl.hp.x1 = ts.x1 + (getLinesByPathAndHeadTmpVec2.x - ts.x1) * getLinesByPathAndHeadTmpST.t;
		tsl.lp.y2 = tsl.hp.y1 = ts.y1 + (getLinesByPathAndHeadTmpVec2.y - ts.y1) * getLinesByPathAndHeadTmpST.t;
		getLinesByPathAndHeadTmpVec3.set(getLinesByPathAndHeadTmpVec);
		getLinesByPathAndHeadTmpVec3.mul((segWidth + segPad));
		getLinesByPathAndHeadTmpVec3.add(ts.x1, ts.y1, 0);
		doLineIntersect(ts.x1, ts.y1,
				getLinesByPathAndHeadTmpVec2.x,
				getLinesByPathAndHeadTmpVec2.y,
				getLinesByPathAndHeadTmpVec3.x,
				getLinesByPathAndHeadTmpVec3.y,
				getLinesByPathAndHeadTmpVec3.x + (ts.x2 - ts.x1),
				getLinesByPathAndHeadTmpVec3.y + (ts.y2 - ts.y1),
				getLinesByPathAndHeadTmpST);
		tsl.rp.x1 = tsl.hp.x2 = ts.x1 + (getLinesByPathAndHeadTmpVec2.x - ts.x1) * getLinesByPathAndHeadTmpST.t;
		tsl.rp.y1 = tsl.hp.y2 = ts.y1 + (getLinesByPathAndHeadTmpVec2.y - ts.y1) * getLinesByPathAndHeadTmpST.t;
		
		if (isEndUndefined) {
			getLinesByPathAndHeadTmpVec.set(ts.x2 - ts.x1, ts.y2 - ts.y1, 0);
			getLinesByPathAndHeadTmpVec.crs(0, 0, 1);
			getLinesByPathAndHeadTmpVec.nor();
			
			tsl.l.x1 = tsl.t.x2 = ts.x2 + getLinesByPathAndHeadTmpVec.x * -segWidth;
			tsl.l.y1 = tsl.t.y2 = ts.y2 + getLinesByPathAndHeadTmpVec.y * -segWidth;
			tsl.r.x2 = tsl.t.x1 = ts.x2 + getLinesByPathAndHeadTmpVec.x * segWidth;
			tsl.r.y2 = tsl.t.y1 = ts.y2 + getLinesByPathAndHeadTmpVec.y * segWidth;
			
			tsl.lp.x1 = tsl.tp.x2 = ts.x2 + getLinesByPathAndHeadTmpVec.x * -(segWidth + segPad);
			tsl.lp.y1 = tsl.tp.y2 = ts.y2 + getLinesByPathAndHeadTmpVec.y * -(segWidth + segPad);
			tsl.rp.x2 = tsl.tp.x1 = ts.x2 + getLinesByPathAndHeadTmpVec.x * (segWidth + segPad);
			tsl.rp.y2 = tsl.tp.y1 = ts.y2 + getLinesByPathAndHeadTmpVec.y * (segWidth + segPad);
		} else {
			getLinesByPathAndHeadTmpMat.idt();
			getLinesByPathAndHeadTmpMat.translate(ts.x2, ts.y2, 0);
			getLinesByPathAndHeadTmpMat.rotate(0, 0, 1, ts.w2);
			
			getLinesByPathAndHeadTmpVec.set(ts.l2, 0, 0);
			getLinesByPathAndHeadTmpVec.mul(getLinesByPathAndHeadTmpMat);
			tsl.l.x1 = tsl.t.x2 = getLinesByPathAndHeadTmpVec.x;
			tsl.l.y1 = tsl.t.y2 = getLinesByPathAndHeadTmpVec.y;
			getLinesByPathAndHeadTmpVec.set(ts.r2, 0, 0);
			getLinesByPathAndHeadTmpVec.mul(getLinesByPathAndHeadTmpMat);
			tsl.r.x2 = tsl.t.x1 = getLinesByPathAndHeadTmpVec.x;
			tsl.r.y2 = tsl.t.y1 = getLinesByPathAndHeadTmpVec.y;
			
			getLinesByPathAndHeadTmpVec.set(ts.x2 - ts.x1, ts.y2 - ts.y1, 0);
			getLinesByPathAndHeadTmpVec.crs(0, 0, 1);
			getLinesByPathAndHeadTmpVec.nor();
			getLinesByPathAndHeadTmpVec2.set(ts.l2, 0, 0);
			getLinesByPathAndHeadTmpVec2.mul(getLinesByPathAndHeadTmpMat);
			getLinesByPathAndHeadTmpVec3.set(getLinesByPathAndHeadTmpVec);
			getLinesByPathAndHeadTmpVec3.mul(-(segWidth + segPad));
			getLinesByPathAndHeadTmpVec3.add(ts.x2, ts.y2, 0);
			doLineIntersect(ts.x2, ts.y2,
					getLinesByPathAndHeadTmpVec2.x,
					getLinesByPathAndHeadTmpVec2.y,
					getLinesByPathAndHeadTmpVec3.x,
					getLinesByPathAndHeadTmpVec3.y,
					getLinesByPathAndHeadTmpVec3.x + (ts.x2 - ts.x1),
					getLinesByPathAndHeadTmpVec3.y + (ts.y2 - ts.y1),
					getLinesByPathAndHeadTmpST);
			tsl.lp.x1 = tsl.tp.x2 = ts.x2 + (getLinesByPathAndHeadTmpVec2.x - ts.x2) * getLinesByPathAndHeadTmpST.t;
			tsl.lp.y1 = tsl.tp.y2 = ts.y2 + (getLinesByPathAndHeadTmpVec2.y - ts.y2) * getLinesByPathAndHeadTmpST.t;
			getLinesByPathAndHeadTmpVec3.set(getLinesByPathAndHeadTmpVec);
			getLinesByPathAndHeadTmpVec3.mul((segWidth + segPad));
			getLinesByPathAndHeadTmpVec3.add(ts.x2, ts.y2, 0);
			doLineIntersect(ts.x2, ts.y2,
					getLinesByPathAndHeadTmpVec2.x,
					getLinesByPathAndHeadTmpVec2.y,
					getLinesByPathAndHeadTmpVec3.x,
					getLinesByPathAndHeadTmpVec3.y,
					getLinesByPathAndHeadTmpVec3.x + (ts.x2 - ts.x1),
					getLinesByPathAndHeadTmpVec3.y + (ts.y2 - ts.y1),
					getLinesByPathAndHeadTmpST);
			tsl.rp.x2 = tsl.tp.x1 = ts.x2 + (getLinesByPathAndHeadTmpVec2.x - ts.x2) * getLinesByPathAndHeadTmpST.t;
			tsl.rp.y2 = tsl.tp.y1 = ts.y2 + (getLinesByPathAndHeadTmpVec2.y - ts.y2) * getLinesByPathAndHeadTmpST.t;
		}
		
		return tsl;
	}
	
	private static class IsLineIntersectST {
		public float t;
		@SuppressWarnings("unused")
		public float s;
	}
	
	private static boolean isLineIntersect(
			float ax1, float ay1, float ax2, float ay2,
			float bx1, float by1, float bx2, float by2) {
		return doLineIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2, null);
	}
	
	private static final double doLineIntersectEpslion = 0.0001;
	private static boolean doLineIntersect(
			float ax1, float ay1, float ax2, float ay2,
			float bx1, float by1, float bx2, float by2,
			IsLineIntersectST st) {
		float axd = ax2 - ax1;
		float ayd = ay2 - ay1;
		float bxd = bx2 - bx1;
		float byd = by2 - by1;
		
		float d = bxd * ayd - axd * byd;
		if (-doLineIntersectEpslion <= d && d <= doLineIntersectEpslion) {
			if (st != null) {
				st.t = Float.NaN;
				st.s = Float.NaN;
			}
			return false;
		}
		
		float di = 1 / d;
		float s = di * ((ax1 - bx1) * ayd - (ay1 - by1) * axd);
		float t = di * -(-(ax1 - bx1) * byd + (ay1 - by1) * bxd);
		
		if (st != null) {
			st.t = t;
			st.s = s;
		}
		return 0 <= s && s <= 1 && 0 <= t && t <= 1;
	}
}
