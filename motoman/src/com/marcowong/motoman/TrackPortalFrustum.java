package com.marcowong.motoman;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;

public class TrackPortalFrustum extends Frustum {
//	new Vector3(-1, -1, -1),
//	new Vector3(1, -1, -1),
//	new Vector3(1, 1, -1),
//	new Vector3(-1, 1, -1), // near clip
//	new Vector3(-1, -1, 1),
//	new Vector3(1, -1, 1),
//	new Vector3(1, 1, 1),
//	new Vector3(-1, 1, 1)
	
	private float[] tmpF = new float[24];
	public void update(Camera c, Frustum f, float[] points) {
		Matrix4.prj(c.combined.val, points, 0, points.length / 3, 3);
		float minX = 1, minY = 1;
		float maxX = -1, maxY = -1;
		for (int i = 0; i < points.length; i += 3) {
			float x = points[i];
			float y = points[i + 1];
			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
		}
		
		for (int i = 0, j = 0; i < 8; ++i, j += 3) {
			tmpF[j] = f.planePoints[i].x;
			tmpF[j + 1] = f.planePoints[i].y;
			tmpF[j + 2] = f.planePoints[i].z;
		}
		Matrix4.prj(c.combined.val, tmpF, 0, 8, 3);
		float minX2 = 1, minY2 = 1;
		float maxX2 = -1, maxY2 = -1;
		for (int i = 0; i < 24; i += 3) {
			float x = tmpF[i];
			float y = tmpF[i + 1];
			if (x < minX2) minX2 = x;
			if (y < minY2) minY2 = y;
			if (x > maxX2) maxX2 = x;
			if (y > maxY2) maxY2 = y;
		}
		if (minX2 > minX) minX = minX2;
		if (maxX2 < maxX) maxX = maxX2;
		if (minY2 > minY) minY = minY2;
		if (maxY2 < maxY) maxY = maxY2;
		
		if (minX < -1) minX = -1;
		if (minY < -1) minY = -1;
		if (maxX > 1) maxX = 1;
		if (maxY > 1) maxY = 1;
		if (minX > maxX) minX = maxX = (minX + maxX) * 0.5f;
		if (minY > maxY) minY = maxY = (minY + maxY) * 0.5f;
		
		planePointsArray[0] = minX;planePointsArray[1] = minY;planePointsArray[2] = -1;
		planePointsArray[3] = maxX;planePointsArray[4] = minY;planePointsArray[5] = -1;
		planePointsArray[6] = maxX;planePointsArray[7] = maxY;planePointsArray[8] = -1;
		planePointsArray[9] = minX;planePointsArray[10] = maxY;planePointsArray[11] = -1;
		planePointsArray[12] = minX;planePointsArray[13] = minY;planePointsArray[14] = 1;
		planePointsArray[15] = maxX;planePointsArray[16] = minY;planePointsArray[17] = 1;
		planePointsArray[18] = maxX;planePointsArray[19] = maxY;planePointsArray[20] = 1;
		planePointsArray[21] = minX;planePointsArray[22] = maxY;planePointsArray[23] = 1;
		
		Matrix4.prj(c.invProjectionView.val, planePointsArray, 0, 8, 3);
		for (int i = 0, j = 0; i < 8; ++i, j += 3) {
			planePoints[i].x = planePointsArray[j];
			planePoints[i].y = planePointsArray[j + 1];
			planePoints[i].z = planePointsArray[j + 2];
		}
		planes[0].set(planePoints[1], planePoints[0], planePoints[2]);
		planes[1].set(planePoints[4], planePoints[5], planePoints[7]);
		planes[2].set(planePoints[0], planePoints[4], planePoints[3]);
		planes[3].set(planePoints[5], planePoints[1], planePoints[6]);
		planes[4].set(planePoints[2], planePoints[3], planePoints[6]);
		planes[5].set(planePoints[4], planePoints[0], planePoints[1]);
	}
}
