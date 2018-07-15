package com.marcowong.motoman.track;

import java.util.List;

import com.badlogic.gdx.math.Vector3;

public class TrackGuideGenerator {
	public float earlyNoticeDistance = 20;
	
	Vector3 getSegAngleVec = new Vector3();
	private float getSegAngle(TrackSegment ts) {
		getSegAngleVec.set(ts.x2 - ts.x1, ts.y2 - ts.y1, 0).nor();
		float turnAngle = (float)(Math.atan2(getSegAngleVec.y, getSegAngleVec.x) * (180 / Math.PI)) + 90;
		if (turnAngle >= 360) {
			turnAngle -= 360;
		}
		if (turnAngle < 0) {
			turnAngle += 360;
		}
		return turnAngle;
	}
	
	private static TrackDirection getDirByAngle(float angle) {
		if (angle <= -75) {
			return TrackDirection.LeftSharp;
		}
		if (angle >= 75) {
			return TrackDirection.RightSharp;
		}
		if (angle <= -50) {
			return TrackDirection.LeftHigh;
		}
		if (angle >= 50) {
			return TrackDirection.RightHigh;
		}
		if (angle <= -30) {
			return TrackDirection.LeftMed;
		}
		if (angle >= 30) {
			return TrackDirection.RightMed;
		}
		if (angle <= -10) {
			return TrackDirection.LeftLow;
		}
		if (angle >= 10) {
			return TrackDirection.RightLow;
		}
		return TrackDirection.Straight;
	}
	
	private float getSegLen(TrackSegment ts) {
		return (float)Math.pow(
				Math.pow(ts.x2 - ts.x1, 2) +
				Math.pow(ts.y2 - ts.y1, 2), 0.5);
	}
	
	public void generate(List<TrackSegment> tss) {
		float tLen = 0;
		for (int i = 0; i < tss.size(); ++i) {
			tLen += getSegLen(tss.get(i));
		}
		
		float tLenCur = 0;
		float lastTurnAngle = getSegAngle(tss.get(0));
		TrackDirection lastDir = getDirByAngle(0);
		for (int i = 0; i < tss.size(); ++i) {
			TrackSegment ts = tss.get(i);
			
			float turnAngle = getSegAngle(ts);
			float angleDiff = turnAngle - lastTurnAngle;
			if (angleDiff > 180) {
				angleDiff = -360 + angleDiff;
			}
			if (angleDiff <= -180) {
				angleDiff = 360 + angleDiff;
			}
			
			TrackDirection dir = getDirByAngle(angleDiff);
			
			ts.attributes.put("length", tLenCur);
			ts.attributes.put("percentage", tLenCur / tLen);
			ts.attributes.put("angle", angleDiff);
			ts.attributes.put("direction", dir);
			if (dir != lastDir) {
				ts.attributes.put("directionChange", dir);
				TrackSegment earlyNoticeTS = ts;
				float segLenT = 0;
				for (int j = i - 1; j >= 0; --j) {
					TrackSegment candidate = tss.get(j);
					if (candidate.attributes.get("directionChange") != null) {
						if (candidate.attributes.get("directionNotice") == null) {
							earlyNoticeTS = candidate;
						}
						break;
					}
					earlyNoticeTS = candidate;
					float segLen = getSegLen(candidate);
					segLenT += segLen;
					if (segLenT > earlyNoticeDistance) {
						break;
					}
				}
				earlyNoticeTS.attributes.put("directionNotice", dir);
			}
			
			lastTurnAngle = turnAngle;
			lastDir = dir;
			tLenCur += getSegLen(ts);
		}
	}
}
