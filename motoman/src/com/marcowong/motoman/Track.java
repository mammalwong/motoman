package com.marcowong.motoman;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.marcowong.motoman.track.TrackData;
import com.marcowong.motoman.track.TrackDirection;
import com.marcowong.motoman.track.TrackSegLine;
import com.marcowong.motoman.track.TrackSegLines;
import com.marcowong.motoman.track.TrackSegment;

public class Track {
	private static Model trackModel;
	private static IMeshContext trackModelMeshContext;
	private static Matrix4 trackScaleMat = new Matrix4();
	private static float trackModelLen = 1;
	private static float trackModelRot = -90;
	private static float trackSegHeight;
	private static float trackScaleFactor;
	private static Model lampModel;
	private static IMeshContext lampModelMeshContext;
	private static Matrix4 lampScaleMat = new Matrix4();
	private static Model buildingModel;
	private static Matrix4 buildingScaleMat = new Matrix4();
	
	public static void initResource() {
		ObjLoaderEx objLoader = new ObjLoaderEx();
		
		trackModel = objLoader.loadObj(Gdx.files.internal("data/track.obj"), true);
		Pixmap trackModelSkeletonMapping = new Pixmap(Gdx.files.internal("data/track.skeleton.png"));
		new ObjLoaderSkeletonPatcher().patch(trackModel, trackModelSkeletonMapping);
		trackModelSkeletonMapping.dispose();
		StaticModelTextureFilterConfigManager.add(trackModel);
		trackModelMeshContext = MeshOptimized.globalStaticMesh.add(trackModel, 16);
		trackSegHeight = 5;
		trackScaleFactor = 40;
		trackScaleMat.scale(trackScaleFactor, trackSegHeight, trackScaleFactor);
		
		lampModel = objLoader.loadObj(Gdx.files.internal("data/lamp.obj"), true);
		Pixmap lampModelSkeletonMapping = new Pixmap(Gdx.files.internal("data/lamp.skeleton.png"));
		new ObjLoaderSkeletonPatcher().patch(lampModel, lampModelSkeletonMapping);
		lampModelSkeletonMapping.dispose();
		StaticModelTextureFilterConfigManager.add(lampModel);
		lampScaleMat.scale(5, 5, 5);
		lampModelMeshContext = MeshOptimized.globalStaticMesh.add(lampModel, 8);
		
		buildingModel = objLoader.loadObj(Gdx.files.internal("data/building.obj"), true);
		buildingScaleMat.scale(trackScaleFactor - 1, trackScaleFactor - 1, trackScaleFactor - 1);
	}
	
	private List<TrackSegment> trackSegments;
	private List<TrackSegment> trackSegmentsOfStart;
	private List<TrackSegment> trackSegmentsOfEnd;
	private TrackSegment tsStart;
	private TrackSegment tsEnd;
	private Matrix4[] trackModelISkeMats;
	private FloatBuffer trackModelISkeMatsFBuf;
	private float trackTSLen;
	private Matrix4[] lampModelISkeMats;
	private FloatBuffer lampModelISkeMatsFBuf;
	private Texture trackMap;
	private float trackMapMinX;
	private float trackMapMinY;
	private float trackMapDim;
	private int decorationQuota;
	
	private Vector3 tmpVec6 = new Vector3();
	public Track(TrackData trackData, int decorationQuota) {
		this.trackSegments = trackData.trackSegments;
		this.decorationQuota = decorationQuota;
		
		trackModelISkeMats = new Matrix4[trackModelMeshContext.getNCopies() * 2];
		for (int i = 0; i < trackModelISkeMats.length; ++i) trackModelISkeMats[i] = new Matrix4();
		trackModelISkeMatsFBuf = ByteBuffer.allocateDirect(trackModelMeshContext.getNCopies() * 2 * 16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		trackTSLen = (float)Math.sqrt(
				(trackSegments.get(0).x2 - trackSegments.get(0).x1) * trackScaleFactor * (trackSegments.get(0).x2 - trackSegments.get(0).x1) * trackScaleFactor +
				(trackSegments.get(0).y2 - trackSegments.get(0).y1) * trackScaleFactor * (trackSegments.get(0).y2 - trackSegments.get(0).y1) * trackScaleFactor);
		
		lampModelISkeMats = new Matrix4[lampModelMeshContext.getNCopies()];
		for (int i = 0; i < lampModelISkeMats.length; ++i) lampModelISkeMats[i] = new Matrix4();
		lampModelISkeMatsFBuf = ByteBuffer.allocateDirect(lampModelMeshContext.getNCopies() * 16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		for (TrackSegment ts : trackSegments) {
			TrackSegLines tsl = (TrackSegLines)ts.attributes.get("lines");
			TrackSegLines tslS = new TrackSegLines();
			tmpVec6.set(tsl.h.x1, 0, tsl.h.y1).mul(trackScaleMat); tslS.h.x1 = tmpVec6.x; tslS.h.y1 = tmpVec6.z;
			tmpVec6.set(tsl.h.x2, 0, tsl.h.y2).mul(trackScaleMat); tslS.h.x2 = tmpVec6.x; tslS.h.y2 = tmpVec6.z;
			tmpVec6.set(tsl.t.x1, 0, tsl.t.y1).mul(trackScaleMat); tslS.t.x1 = tmpVec6.x; tslS.t.y1 = tmpVec6.z;
			tmpVec6.set(tsl.t.x2, 0, tsl.t.y2).mul(trackScaleMat); tslS.t.x2 = tmpVec6.x; tslS.t.y2 = tmpVec6.z;
			tmpVec6.set(tsl.l.x1, 0, tsl.l.y1).mul(trackScaleMat); tslS.l.x1 = tmpVec6.x; tslS.l.y1 = tmpVec6.z;
			tmpVec6.set(tsl.l.x2, 0, tsl.l.y2).mul(trackScaleMat); tslS.l.x2 = tmpVec6.x; tslS.l.y2 = tmpVec6.z;
			tmpVec6.set(tsl.r.x1, 0, tsl.r.y1).mul(trackScaleMat); tslS.r.x1 = tmpVec6.x; tslS.r.y1 = tmpVec6.z;
			tmpVec6.set(tsl.r.x2, 0, tsl.r.y2).mul(trackScaleMat); tslS.r.x2 = tmpVec6.x; tslS.r.y2 = tmpVec6.z;
			ts.attributes.put("linesS", tslS);
		}
		
		tsStart = trackSegments.get(0);
		tsEnd = trackSegments.get(trackSegments.size() - 1);
		
		trackSegmentsOfStart = new ArrayList<TrackSegment>();
		for (int i = 0; i < 5; ++i) {
			TrackSegment ts = new TrackSegment();
			float xOffset = tsStart.x1 - tsStart.x2;
			float yOffset = tsStart.y1 - tsStart.y2;
			ts.x1 = tsStart.x1 + xOffset * (i + 1);
			ts.y1 = tsStart.y1 + yOffset * (i + 1);
			ts.w1 = tsStart.w1;
			ts.l1 = tsStart.l1;
			ts.r1 = tsStart.r1;
			ts.x2 = tsStart.x1 + xOffset * i;
			ts.y2 = tsStart.y1 + yOffset * i;
			ts.w2 = tsStart.w1;
			ts.l2 = tsStart.l1;
			ts.r2 = tsStart.r1;
			trackSegmentsOfStart.add(ts);
		}
		trackSegmentsOfEnd = new ArrayList<TrackSegment>();
		for (int i = 0; i < 5; ++i) {
			TrackSegment ts = new TrackSegment();
			float xOffset = tsEnd.x2 - tsEnd.x1;
			float yOffset = tsEnd.y2 - tsEnd.y1;
			ts.x1 = tsEnd.x2 + xOffset * i;
			ts.y1 = tsEnd.y2 + yOffset * i;
			ts.w1 = tsEnd.w2;
			ts.l1 = tsEnd.l2;
			ts.r1 = tsEnd.r2;
			ts.x2 = tsEnd.x2 + xOffset * (i + 1);
			ts.y2 = tsEnd.y2 + yOffset * (i + 1);
			ts.w2 = tsEnd.w2;
			ts.l2 = tsEnd.l2;
			ts.r2 = tsEnd.r2;
			trackSegmentsOfEnd.add(ts);
		}
		
		constructTrackMap();
	}
	
	public void resume() {
		constructTrackMap();
	}
	
	private Vector3 tmpVec12 = new Vector3();
	private void constructTrackMap() {
		if (trackMap != null && !trackMap.isManaged()) trackMap.dispose();
		
		trackMap = new Texture(256, 256, Format.RGBA8888);
		Pixmap m = new Pixmap(trackMap.getWidth(), trackMap.getHeight(), Format.RGBA8888);
		m.setColor(1, 0.75f, 0, 0);
		m.fill();
		m.setColor(1, 0.75f, 0, 1);
		float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
		TrackSegLine[] lines = new TrackSegLine[4];
		for (TrackSegment ts : trackSegments) {
			TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
			lines[0] = tsl.h;
			lines[1] = tsl.t;
			lines[2] = tsl.l;
			lines[3] = tsl.r;
			for (int i = 0; i < 4; ++i) {
				if (lines[i].x1 < minX) minX = lines[i].x1;
				if (lines[i].x1 > maxX) maxX = lines[i].x1;
				if (lines[i].y1 < minY) minY = lines[i].y1;
				if (lines[i].y1 > maxY) maxY = lines[i].y1;
				if (lines[i].x2 < minX) minX = lines[i].x2;
				if (lines[i].x2 > maxX) maxX = lines[i].x2;
				if (lines[i].y2 < minY) minY = lines[i].x2;
				if (lines[i].y2 > maxY) maxY = lines[i].x2;
			}
		}
		float mD = Math.max(maxX - minX, maxY - minY) / trackMap.getWidth();
		Vector3 tmpVec = new Vector3();
		for (TrackSegment ts : trackSegments) {
			TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
			float lx1 = (tsl.l.x1 - minX) / mD;
			float ly1 = (tsl.l.y1 - minY) / mD;
			float lx2 = (tsl.l.x2 - minX) / mD;
			float ly2 = (tsl.l.y2 - minY) / mD;
			float rx1 = (tsl.r.x1 - minX) / mD;
			float ry1 = (tsl.r.y1 - minY) / mD;
			float rx2 = (tsl.r.x2 - minX) / mD;
			float ry2 = (tsl.r.y2 - minY) / mD;
			int nInterpolation = 1 + 10 * (int)Math.ceil(Math.max(
					tmpVec.set(lx1 - rx2, ly1 - ry2, 0).len(),
					tmpVec.set(lx2 - rx1, ly2 - ry1, 0).len()));
			for (int i = 0; i < nInterpolation; ++i) {
				float interpolation = i / (float)nInterpolation;
				m.drawLine(
					(int)Math.round(lx1 * interpolation + rx2 * (1 - interpolation)),
					(int)Math.round(ly1 * interpolation + ry2 * (1 - interpolation)),
					(int)Math.round(lx2 * interpolation + rx1 * (1 - interpolation)),
					(int)Math.round(ly2 * interpolation + ry1 * (1 - interpolation)));
			}
		}
		tmpVec12.set(tsStart.x1, 0, tsStart.y1).mul(trackScaleMat);
		m.setColor(1, 0.9f, 0, 1);
		m.fillCircle(
				(int)Math.round((tmpVec12.x - minX) / mD),
				(int)Math.round((tmpVec12.z - minY) / mD), 7);
		tmpVec12.set(tsEnd.x2, 0, tsEnd.y2).mul(trackScaleMat);
		m.setColor(1, 0.6f, 0, 1);
		m.fillCircle(
				(int)Math.round((tmpVec12.x - minX) / mD),
				(int)Math.round((tmpVec12.z - minY) / mD), 7);
		trackMap.draw(m, 0, 0);
		m.dispose();
		trackMap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		trackMap.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		trackMapMinX = minX;
		trackMapMinY = minY;
		trackMapDim = mD;
	}
	
	public Texture getTrackMap() {
		return trackMap;
	}
	
	private Vector3 tmpVec7 = new Vector3();
	public void getTrackeeTrackMapLoc(ITrackee trackee, Vector3 trackMapPos) {
		trackee.getTrackeePos(tmpVec7);
		float tX = (tmpVec7.x - trackMapMinX) / trackMapDim;
		float tY = (tmpVec7.z - trackMapMinY) / trackMapDim;
		trackMapPos.x = tX;
		trackMapPos.y = 0;
		trackMapPos.z = tY;
	}
	
	public float getTrackeeTrackPercentage(ITrackee trackee) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		return (Float)ts.attributes.get("percentage");
	}
	
	private Vector3 tmpVec = new Vector3();
	private Matrix4 tmpMat = new Matrix4();
	private Matrix4 tmpMat3 = new Matrix4();
	private void renderTS(ShaderProgram shader, Camera camera, TrackSegment ts, boolean decorate,
			int trackSkeMatsOffset,
			int lampSkeMatsOffset) {
		float ox = (ts.x1 + ts.x2) * 0.5f;
		float oy = (ts.y1 + ts.y2) * 0.5f;
		tmpVec.set(ts.y2 - ts.y1, ts.x2 - ts.x1, 0).nor();
		float or = (float)(Math.atan2(tmpVec.y, tmpVec.x) * (180 / Math.PI)) + 90 + trackModelRot;
		float p1Len = tmpVec.set(ox - ts.x1, oy - ts.y1, 0).len();
		float p2Len = tmpVec.set(ox - ts.x2, oy - ts.y2, 0).len();
		
		tmpMat.set(trackScaleMat);
		tmpMat.translate(ox, 0, oy);
		tmpMat.rotate(0, 1, 0, or);
		Matrix4 trackSkeMat0 = trackModelISkeMats[trackSkeMatsOffset];
		Matrix4 trackSkeMat1 = trackModelISkeMats[trackSkeMatsOffset + 1];
		trackSkeMat0.set(tmpMat);
		trackSkeMat0.translate(0, 0, -p1Len);
		trackSkeMat0.rotate(0, 1, 0, -(or + ts.w1));
		trackSkeMat0.scale((-ts.l1 + ts.r1) * 0.5f, 1, 1);
		trackSkeMat0.translate(0, 0, trackModelLen);
		trackSkeMat1.set(tmpMat);
		trackSkeMat1.translate(0, 0, p2Len);
		trackSkeMat1.rotate(0, 1, 0, -(or + ts.w2));
		trackSkeMat1.scale((-ts.l2 + ts.r2) * 0.5f, 1, 1);
		trackSkeMat1.translate(0, 0, -trackModelLen);
		
		if (decorate) {
			tmpVec.set(0, 0, 0);
			tmpMat.idt();
			tmpMat.mul(trackScaleMat);
			tmpMat.translate(ox, 0, oy);
			tmpMat.rotate(0, 1, 0, or);
			tmpMat.translate(1.2f, 0, 0);
			tmpVec.mul(tmpMat);
			Matrix4 lampSkeMat0 = lampModelISkeMats[lampSkeMatsOffset];
			lampSkeMat0.idt();
			lampSkeMat0.trn(tmpVec);
			lampSkeMat0.rotate(0, 1, 0, or + 180);
			lampSkeMat0.mul(lampScaleMat);
			
			tmpVec.set(0, 0, 0);
			tmpMat.idt();
			tmpMat.mul(trackScaleMat);
			tmpMat.translate(ox, 0, oy);
			tmpMat.rotate(0, 1, 0, or);
			tmpMat.translate(-1.2f, 0, 0);
			tmpVec.mul(tmpMat);
			Matrix4 lampSkeMat1 = lampModelISkeMats[lampSkeMatsOffset + 1];
			lampSkeMat1.idt();
			lampSkeMat1.trn(tmpVec);
			lampSkeMat1.rotate(0, 1, 0, or);
			lampSkeMat1.mul(lampScaleMat);
			
//			tmpVec.set(0, 0, 0);
//			tmpMat.idt();
//			tmpMat.mul(trackScaleMat);
//			tmpMat.translate(ox, 0, oy);
//			tmpMat.rotate(0, 1, 0, or);
//			tmpMat.translate(-1.2f, 0, 0);
//			tmpVec.mul(tmpMat);
//			tmpMat3.set(camera.combined);
//			tmpMat3.translate(tmpVec);
//			tmpMat3.rotate(0, 1, 0, or);
//			tmpMat3.mul(buildingScaleMat);
//			shader.setUniformMatrix("modelviewproj", tmpMat3);
//			tmpMat3.set(camera.view);
//			tmpMat3.translate(tmpVec);
//			tmpMat3.rotate(0, 1, 0, or) ;
//			tmpMat3.mul(buildingScaleMat);
//			shader.setUniformMatrix("modelview", tmpMat3);
//			buildingModel.render(shader);
		}
	}
	
	private Vector3 tmpVec4 = new Vector3();
	private float getTrackSegmentDistance2(TrackSegment ts, float px, float py, float pz) {
		float ox = (ts.x1 + ts.x2) * 0.5f;
		float oy = (ts.y1 + ts.y2) * 0.5f;
		float dis = tmpVec4.set(ox, 0, oy).mul(trackScaleMat).sub(px, py, pz).len2();
		return dis;
	}
	
	private TrackSegment getNearestTrackSegment(float px, float py, float pz) {
		TrackSegment nearestTS = null;
		float nearestTSDis = Float.POSITIVE_INFINITY;
		for (int i = 0; i < trackSegments.size(); ++i) {
			TrackSegment ts = trackSegments.get(i);
			float dis = getTrackSegmentDistance2(ts, px, py, pz);
			if (nearestTSDis >= dis) {
				nearestTS = ts;
				nearestTSDis = dis;
			}
		}
		return nearestTS;
	}
	
	private Vector3 tmpVec3 = new Vector3();
	private TrackSegment getTrackeeTrackSegment(ITrackee trackee) {
		trackee.getTrackeePos(tmpVec3);
		tmpVec3.y = 0;
		TrackSegment lastTS = trackee.getLastTrackSegment();
		if (lastTS == null) {
			return getNearestTrackSegment(tmpVec3.x, tmpVec3.y, tmpVec3.z);
		}
		
		TrackSegment nearestTS = lastTS;
		float nearestTSDis = getTrackSegmentDistance2(lastTS, tmpVec3.x, tmpVec3.y, tmpVec3.z);
		if (lastTS.next != null) {
			float dis = getTrackSegmentDistance2(lastTS.next, tmpVec3.x, tmpVec3.y, tmpVec3.z);
			if (dis < nearestTSDis) {
				nearestTSDis = dis;
				nearestTS = lastTS.next;
			}
		}
		if (lastTS.prev != null) {
			float dis = getTrackSegmentDistance2(lastTS.prev, tmpVec3.x, tmpVec3.y, tmpVec3.z);
			if (dis < nearestTSDis) {
				nearestTSDis = dis;
				nearestTS = lastTS.prev;
			}
		}
		
		if (nearestTSDis <= trackTSLen * trackTSLen)
			return nearestTS;
		else
			return getNearestTrackSegment(tmpVec3.x, tmpVec3.y, tmpVec3.z);
	}
	
	public void updateCurrentTrackSegment(ITrackee trackee) {
		trackee.setLastTrackSegment(getTrackeeTrackSegment(trackee));
	}
	
	public boolean isInTrackEnd(ITrackee trackee) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		return ts == tsEnd;
	}
	
	public Matrix4 getStartSpawnPosition() {
		return getSpawnPosition(tsStart);
	}
	
	public Matrix4 getSpawnPosition(ITrackee trackee) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		return getSpawnPosition(ts);
	}
	
	private Matrix4 getSpawnPosition(TrackSegment ts) {
		Vector3 tmpVec = new Vector3();
		
		tmpVec.set((ts.x1 + ts.x2) * 0.5f, 0, (ts.y1 + ts.y2) * 0.5f);
		Matrix4 pos = new Matrix4();
		pos.mul(trackScaleMat);
		tmpVec.mul(pos);
		pos.idt();
		pos.trn(tmpVec);
		
		tmpVec.set(ts.y2 - ts.y1, ts.x2 - ts.x1, 0).nor();
		float or = (float)(Math.atan2(tmpVec.y, tmpVec.x) * (180 / Math.PI));
		pos.rotate(0, 1, 0, or);
		
		return pos;
	}
	
	public TrackDirection getTrackeeDirectionNotice(ITrackee trackee) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		TrackDirection nextTrackDirection = (TrackDirection)ts.attributes.get("directionNotice");
		return nextTrackDirection;
	}
	
	public TrackDirection getTrackeeDirectionChange(ITrackee trackee, int forseen) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		for (int i = 0; i < forseen; ++i) if (ts.next != null) ts = ts.next;
		TrackDirection nextTrackDirection = (TrackDirection)ts.attributes.get("directionChange");
		return nextTrackDirection;
	}
	
	private BoundingBox tmpBB = new BoundingBox();
	private boolean isTrackSegmentInsideCamera(TrackSegment ts, Frustum f) {
		TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
		tmpBB.inf();
		tmpBB.ext(tsl.h.x1, 0, tsl.h.y1);
		tmpBB.ext(tsl.h.x2, 0, tsl.h.y2);
		tmpBB.ext(tsl.t.x1, 0, tsl.t.y1);
		tmpBB.ext(tsl.t.x2, 0, tsl.t.y2);
		tmpBB.ext(tsl.h.x1, trackSegHeight, tsl.h.y1);
		tmpBB.ext(tsl.h.x2, trackSegHeight, tsl.h.y2);
		tmpBB.ext(tsl.t.x1, trackSegHeight, tsl.t.y1);
		tmpBB.ext(tsl.t.x2, trackSegHeight, tsl.t.y2);
		return f.boundsInFrustum(tmpBB);
	}
	
	private float[] tmpPP = new float[4 * 3];
	private void constructPortalFrustum(Camera c, TrackPortalFrustum f, Frustum f2, TrackSegment ts, boolean isNextTS) {
		TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
		float x1, y1;
		float x2, y2;
		if (isNextTS) {
			x1 = tsl.t.x1;
			y1 = tsl.t.y1;
			x2 = tsl.t.x2;
			y2 = tsl.t.y2;
		} else {
			x1 = tsl.h.x1;
			y1 = tsl.h.y1;
			x2 = tsl.h.x2;
			y2 = tsl.h.y2;
		}
		tmpPP[0] = x1; tmpPP[1] = 2 * trackSegHeight; tmpPP[2] = y1;
		tmpPP[3] = x1; tmpPP[4] = -trackSegHeight; tmpPP[5] = y1;
		tmpPP[6] = x2; tmpPP[7] = 2 * trackSegHeight; tmpPP[8] = y2;
		tmpPP[9] = x2; tmpPP[10] = -trackSegHeight; tmpPP[11] = y2;
		
		f.update(c, f2, tmpPP);
	}
	
	private void renderTrackModelI(ShaderProgram shader, Camera camera, int nInst) {
		shader.setUniformMatrix("modelviewproj", camera.combined);
		shader.setUniformMatrix("modelview", camera.view);
		int nSkeMats = nInst * 2;
		for (int i = 0; i < nSkeMats; ++i) {
			trackModelISkeMatsFBuf.position(i * 16);
			trackModelISkeMatsFBuf.put(trackModelISkeMats[i].val);
		}
		shader.setUniformMatrix4fv("skeletonmat", trackModelISkeMatsFBuf, nSkeMats, false);
		trackModelMeshContext.render(shader, nInst);
	}
	
	private void renderLampModelI(ShaderProgram shader, Camera camera, int nInst) {
		shader.setUniformMatrix("modelviewproj", camera.combined);
		shader.setUniformMatrix("modelview", camera.view);
		int nSkeMats = nInst;
		for (int i = 0; i < nSkeMats; ++i) {
			lampModelISkeMatsFBuf.position(i * 16);
			lampModelISkeMatsFBuf.put(lampModelISkeMats[i].val);
		}
		shader.setUniformMatrix4fv("skeletonmat", lampModelISkeMatsFBuf, nSkeMats, false);
		lampModelMeshContext.render(shader, nInst);
	}
	
	private boolean wasUseTmpTPF1 = false;
	private TrackPortalFrustum tmpTPF1 = new TrackPortalFrustum();
	private TrackPortalFrustum tmpTPF2 = new TrackPortalFrustum();
	private TrackPortalFrustum getNextTmpTPF() {
		if (wasUseTmpTPF1) {
			wasUseTmpTPF1 = false;
			return tmpTPF2;
		} else {
			wasUseTmpTPF1 = true;
			return tmpTPF1;
		}
			
	}
	
	public void updateVanishingPoint(MotomanCamera camera) {
		TrackSegment nearestTS = getTrackeeTrackSegment((ITrackee)camera);
		
		TrackSegment vanishingTS = nearestTS;
		TrackSegment ts = nearestTS;
		Frustum f = camera.frustum;
		while ((ts = ts.next) != null) {
			if (isTrackSegmentInsideCamera(ts, f)) {
				vanishingTS = ts;
			} else {
				break;
			}
			TrackPortalFrustum tmpTPF = getNextTmpTPF();
			constructPortalFrustum(camera, tmpTPF, f, ts, true);
			f = tmpTPF;
		}
		
		if (vanishingTS == nearestTS) {
			tmpVec8.set(vanishingTS.x2, 0, vanishingTS.y2).mul(trackScaleMat);
			tmpMat2.idt().trn(tmpVec8.x, 0, tmpVec8.z).rotate(0, 1, 0, vanishingTS.w2);
			tmpVec8.set(0, 0, trackTSLen * 0.5f).mul(tmpMat2);
		} else if (nearestTS.next != null &&
				vanishingTS == nearestTS.next)
			tmpVec8.set(vanishingTS.x2, 0, vanishingTS.y2).mul(trackScaleMat);
		else
			tmpVec8.set(vanishingTS.x1, 0, vanishingTS.y1).mul(trackScaleMat);
		tmpVec8.y = trackSegHeight;
		camera.updateVanishingPoint(tmpVec8);
	}
	
	private Vector3 tmpVec8 = new Vector3();
	private Matrix4 tmpMat2 = new Matrix4();
	public void render(ShaderProgram shader, MotomanCamera camera) {
		int nTrackInst = 0;
		int nLampInst = 0;
		
		TrackSegment nearestTS = getTrackeeTrackSegment((ITrackee)camera);
		
		boolean decorateNearestTS = this.decorationQuota > 0;
		renderTS(shader, camera, nearestTS, decorateNearestTS, 0, 0);
		if (++nTrackInst >= trackModelMeshContext.getNCopies()) {
			renderTrackModelI(shader, camera, nTrackInst);
			nTrackInst = 0;
		}
		if (decorateNearestTS &&
			(nLampInst += 2) >= lampModelMeshContext.getNCopies()) {
			renderLampModelI(shader, camera, nLampInst);
			nLampInst = 0;
		}
		
		TrackSegment ts = nearestTS;
		Frustum f = camera.frustum;
		int decorationQuta = this.decorationQuota - 1;
		boolean isThisWay = false;
		boolean seeEnd = nearestTS == tsEnd;
		while ((ts = ts.next) != null) {
			if (isTrackSegmentInsideCamera(ts, f)) {
				if (ts == tsEnd) seeEnd = true;
			} else {
				if (!isThisWay) break;
				if (decorationQuta <= 0) break;
			}
			TrackPortalFrustum tmpTPF = getNextTmpTPF();
			constructPortalFrustum(camera, tmpTPF, f, ts, true);
			f = tmpTPF;
			isThisWay = true;
			boolean decorateThisTS = --decorationQuta >= 0;
			renderTS(shader, camera, ts, decorateThisTS, nTrackInst * 2, nLampInst);
			if (++nTrackInst >= trackModelMeshContext.getNCopies()) {
				renderTrackModelI(shader, camera, nTrackInst);
				nTrackInst = 0;
			}
			if (decorateThisTS)
				if ((nLampInst += 2) >= lampModelMeshContext.getNCopies()) {
					renderLampModelI(shader, camera, nLampInst);
					nLampInst = 0;
				}
		}
		if (seeEnd) {
			for (int i = 0; i < trackSegmentsOfEnd.size(); ++i) {
				renderTS(shader, camera, trackSegmentsOfEnd.get(i), false, nTrackInst * 2, nLampInst);
				if (++nTrackInst >= trackModelMeshContext.getNCopies()) {
					renderTrackModelI(shader, camera, nTrackInst);
					nTrackInst = 0;
				}
			}
		}
		
		ts = nearestTS;
		f = camera.frustum;
		decorationQuta = this.decorationQuota - 1;
		isThisWay = false;
		boolean seeStart = nearestTS == tsStart;
		while ((ts = ts.prev) != null) {
			if (isTrackSegmentInsideCamera(ts, f)) {
				if (ts == tsStart) seeStart = true;
			} else {
				if (!isThisWay) break;
				if (decorationQuta <= 0) break;
			}
			TrackPortalFrustum tmpTPF = getNextTmpTPF();
			constructPortalFrustum(camera, tmpTPF, f, ts, false);
			f = tmpTPF;
			isThisWay = true;
			boolean decorateThisTS = --decorationQuta >= 0;
			renderTS(shader, camera, ts, decorateThisTS, nTrackInst * 2, nLampInst);
			if (++nTrackInst >= trackModelMeshContext.getNCopies()) {
				renderTrackModelI(shader, camera, nTrackInst);
				nTrackInst = 0;
			}
			if (decorateThisTS)
				if ((nLampInst += 2) >= lampModelMeshContext.getNCopies()) {
					renderLampModelI(shader, camera, nLampInst);
					nLampInst = 0;
				}
		}
		if (seeStart) {
			for (int i = 0; i < trackSegmentsOfStart.size(); ++i) {
				renderTS(shader, camera, trackSegmentsOfStart.get(i), false, nTrackInst * 2, nLampInst);
				if (++nTrackInst >= trackModelMeshContext.getNCopies()) {
					renderTrackModelI(shader, camera, nTrackInst);
					nTrackInst = 0;
				}
			}
		}
		
		if (nTrackInst != 0)
			renderTrackModelI(shader, camera, nTrackInst);
		if (nLampInst != 0)
			renderLampModelI(shader, camera, nLampInst);
	}
	
	private Vector3 tmpVec5 = new Vector3();
	private TrackSegLine[] tmpTSL = new TrackSegLine[4];
	private boolean isInsideTrackInternal(ITrackee trackee, TrackSegment ts) {
		trackee.getTrackeePos(tmpVec5);
		TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
		tmpTSL[0] = tsl.h;
		tmpTSL[1] = tsl.l;
		tmpTSL[2] = tsl.r;
		tmpTSL[3] = tsl.t;
		for (int i = 0; i < 4; ++i) {
			boolean outside = Intersector.pointLineSide(
					tmpTSL[i].x1, tmpTSL[i].y1,
					tmpTSL[i].x2, tmpTSL[i].y2, tmpVec5.x, tmpVec5.z) < 0;
			if (outside) return false;
		}
		return true;
	}
	
	public boolean isInsideTrack(ITrackee trackee) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		return isInsideTrackInternal(trackee, ts) ||
				((ts.next != null) && isInsideTrackInternal(trackee, ts.next)) ||
				((ts.prev != null) && isInsideTrackInternal(trackee, ts.prev));
	}
	
	private Vector3 tmpVec10 = new Vector3();
	private boolean getTrackCollisionVectorInternal(ITrackee trackee, TrackSegment ts, Vector3 collisionPos, Vector3 collisionNor) {
		trackee.getTrackeePos(tmpVec10);
		TrackSegLines tsl = (TrackSegLines)ts.attributes.get("linesS");
		boolean notThisSegment =
				Intersector.pointLineSide(tsl.h.x1, tsl.h.y1, tsl.h.x2, tsl.h.y2, tmpVec10.x, tmpVec10.z) < 0 ||
				Intersector.pointLineSide(tsl.t.x1, tsl.t.y1, tsl.t.x2, tsl.t.y2, tmpVec10.x, tmpVec10.z) < 0;
		if (notThisSegment)
			return false;
		if (Intersector.pointLineSide(tsl.l.x1, tsl.l.y1, tsl.l.x2, tsl.l.y2, tmpVec10.x, tmpVec10.z) < 0) {
			float m = (tsl.l.y2 - tsl.l.y1) / (tsl.l.x2 - tsl.l.x1);
			float b = tsl.l.y1 - m * tsl.l.x1;
			collisionPos.x = (m * tmpVec10.z + tmpVec10.x - m * b) / (m * m + 1);
			collisionPos.y = tmpVec10.y;
			collisionPos.z = (m * m * tmpVec10.z + m * tmpVec10.x + b) / (m * m + 1);
			collisionNor.x = -(tsl.l.y2 - tsl.l.y1);
			collisionNor.y = 0;
			collisionNor.z = (tsl.l.x2 - tsl.l.x1);
			collisionNor.nor();
			return true;
		}
		if (Intersector.pointLineSide(tsl.r.x1, tsl.r.y1, tsl.r.x2, tsl.r.y2, tmpVec10.x, tmpVec10.z) < 0) {
			float m = (tsl.r.y2 - tsl.r.y1) / (tsl.r.x2 - tsl.r.x1);
			float b = tsl.r.y1 - m * tsl.r.x1;
			collisionPos.x = (m * tmpVec10.z + tmpVec10.x - m * b) / (m * m + 1);
			collisionPos.y = tmpVec10.y;
			collisionPos.z = (m * m * tmpVec10.z + m * tmpVec10.x + b) / (m * m + 1);
			collisionNor.x = -(tsl.r.y2 - tsl.r.y1);
			collisionNor.y = 0;
			collisionNor.z = (tsl.r.x2 - tsl.r.x1);
			collisionNor.nor();
			return true;
		}
		return false;
	}
	
	private Vector3 tmpVec9 = new Vector3();
	private Vector3 tmpVec11 = new Vector3();
	public boolean getTrackCollisionVector(ITrackee trackee, Vector3 collisionPos, Vector3 collisionNor) {
		TrackSegment ts = getTrackeeTrackSegment(trackee);
		collisionPos.set(0, 0, 0);
		collisionNor.set(0, 0, 0);
		int collisionCount = 0;
		if (getTrackCollisionVectorInternal(trackee, ts, tmpVec9, tmpVec11)) {
			++collisionCount;
			collisionPos.add(tmpVec9);
			collisionNor.add(tmpVec11);
		}
		if (ts.next != null && getTrackCollisionVectorInternal(trackee, ts.next, tmpVec9, tmpVec11)) {
			++collisionCount;
			collisionPos.add(tmpVec9);
			collisionNor.add(tmpVec11);
		}
		if (ts.prev != null && getTrackCollisionVectorInternal(trackee, ts.prev, tmpVec9, tmpVec11)) {
			++collisionCount;
			collisionPos.add(tmpVec9);
			collisionNor.add(tmpVec11);
		}
		if (collisionCount > 1) {
			collisionPos.div(collisionCount);
			collisionNor.nor();
			return true;
		} else if (collisionCount == 1)
			return true;
		else
			return false;
	}
	
	public void dispose() {
		if (trackMap != null && !trackMap.isManaged()) trackMap.dispose();
	}
}
