package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class BackgroundObjs {
	private static Model hillsModel;
	private static IMeshContext hillsModelMeshContext;
	private static Model buildingsModel;
	private static IMeshContext buildingsModelMeshContext;
	private static Model airplaneModel;
	private static IMeshContext airplaneModelMeshContext;
	private static Matrix4 hillsScale = new Matrix4();
	private static Matrix4 buildingsScale = new Matrix4();
	private static Matrix4 airplaneScale = new Matrix4();
	
	public static void initResource() {
		ObjLoaderEx objLoader = new ObjLoaderEx();
		hillsModel = objLoader.loadObj(Gdx.files.internal("data/hills.obj"), true);
		StaticModelTextureFilterConfigManager.add(hillsModel);
		hillsModelMeshContext = MeshOptimized.globalStaticMesh.add(hillsModel);
		hillsScale.scale(5, 2, 5);
		buildingsModel = objLoader.loadObj(Gdx.files.internal("data/buildings.obj"), true);
		StaticModelTextureFilterConfigManager.add(buildingsModel);
		buildingsModelMeshContext = MeshOptimized.globalStaticMesh.add(buildingsModel);
		buildingsScale.scale(3.5f, 1, 3.5f);
		airplaneModel = objLoader.loadObj(Gdx.files.internal("data/airplane.obj"), true);
		StaticModelTextureFilterConfigManager.add(airplaneModel);
		airplaneModelMeshContext = MeshOptimized.globalStaticMesh.add(airplaneModel);
		airplaneScale.scale(0.4f, 0.4f, 0.4f);
	}
	
	public class UpdateState {
		public Matrix4 airplanePos = new Matrix4();
		public boolean airplaneShowing = false;
		public float airplaneTotalTrans = 0;
		public float airplaneRestRemains = 0;
		
		public void copyTo(UpdateState s) {
			s.airplanePos.set(airplanePos);
			s.airplaneShowing = airplaneShowing;
			s.airplaneTotalTrans = airplaneTotalTrans;
			s.airplaneRestRemains = airplaneRestRemains;
		}
	}
	public UpdateState statePersist = new UpdateState();
	public UpdateState stateTmp = new UpdateState();
	public UpdateState state = statePersist;
	
	public BackgroundObjs() {
		state.airplaneShowing = false;
		state.airplaneRestRemains = getAirplaneRestSpan();
	}
	
	private Matrix4 tmpMat = new Matrix4();
	public void render(ShaderProgram shader, Camera camera) {
		tmpMat.set(camera.combined);
		tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
		tmpMat.mul(hillsScale);
		shader.setUniformMatrix("modelviewproj", tmpMat);
		tmpMat.set(camera.view);
		tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
		tmpMat.mul(hillsScale);
		shader.setUniformMatrix("modelview", tmpMat);
		hillsModelMeshContext.render(shader);
		
		tmpMat.set(camera.combined);
		tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
		tmpMat.mul(buildingsScale);
		shader.setUniformMatrix("modelviewproj", tmpMat);
		tmpMat.set(camera.view);
		tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
		tmpMat.mul(buildingsScale);
		shader.setUniformMatrix("modelview", tmpMat);
		buildingsModelMeshContext.render(shader);
		
		if (state.airplaneShowing) {
			tmpMat.set(camera.combined);
			tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
			tmpMat.mul(state.airplanePos);
			tmpMat.mul(airplaneScale);
			shader.setUniformMatrix("modelviewproj", tmpMat);
			tmpMat.set(camera.view);
			tmpMat.translate(camera.position.x, camera.position.y, camera.position.z);
			tmpMat.mul(state.airplanePos);
			tmpMat.mul(airplaneScale);
			shader.setUniformMatrix("modelview", tmpMat);
			airplaneModelMeshContext.render(shader);
		}
	}
	
	private float getAirplaneRestSpan() {
		return (float)(15 + Math.random() * 15);
	}
	
	public void setPersist(boolean b) {
		if (b) {
			if (state != statePersist) {
				state = statePersist;
			}
		} else {
			if (state != stateTmp) {
				statePersist.copyTo(stateTmp);
				state = stateTmp;
			}
		}
	}
	
	public void update(float delta) {
		if (state.airplaneShowing) {
			float trans = delta * 3;
			state.airplanePos.translate(0, 0, trans);
			state.airplaneTotalTrans += trans;
			if (state == statePersist &&
				state.airplaneTotalTrans > 100) {
				state.airplaneShowing = false;
				state.airplaneRestRemains = getAirplaneRestSpan();
			}
		} else {
			if (state != statePersist ||
				state.airplaneRestRemains > 0) {
				state.airplaneRestRemains -= delta;
			} else {
				state.airplaneShowing = true;
				state.airplaneTotalTrans = 0;
				state.airplanePos.idt();
				state.airplanePos.rotate(0, 1, 0, (float)(360 * Math.random()));
				state.airplanePos.translate(
						(float)(30 + Math.random() * 20) * (Math.random() > 0.5 ? 1 : -1),
						(float)(10 + Math.random() * 4),
						-50);
			}
		}
	}
	
	public void dispose() {
	}
}
