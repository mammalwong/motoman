package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Model;

public class MainMotorcycle extends Motorcycle {
	private static Model mainBodyModel;
	private static IMeshContext mainBodyModelMeshContext;
	private static Model mainFrontWheelModel;
	private static IMeshContext mainFrontWheelModelMeshContext;
	private static Model mainRearWheelModel;
	private static IMeshContext mainRearWheelModelMeshContext;
	
	public static void initResource() {
		ObjLoaderEx objLoader = new ObjLoaderEx();
		mainBodyModel = objLoader.loadObj(Gdx.files.internal("data/bikeBody.obj"), true);
		StaticModelTextureFilterConfigManager.add(mainBodyModel);
		mainBodyModelMeshContext = MeshOptimized.globalStaticMesh.add(mainBodyModel);
		mainFrontWheelModel = objLoader.loadObj(Gdx.files.internal("data/bikeFrontWheel.obj"), true);
		StaticModelTextureFilterConfigManager.add(mainFrontWheelModel);
		mainFrontWheelModelMeshContext = MeshOptimized.globalStaticMesh.add(mainFrontWheelModel);
		mainRearWheelModel = objLoader.loadObj(Gdx.files.internal("data/bikeRearWheel.obj"), true);
		StaticModelTextureFilterConfigManager.add(mainRearWheelModel);
		mainRearWheelModelMeshContext = MeshOptimized.globalStaticMesh.add(mainRearWheelModel);
	}
	
	public MainMotorcycle(Track track, IMotorcycleInputMeters inputMeters) {
		super(track, inputMeters);
		
		this.bodyModelMeshContext = mainBodyModelMeshContext;
		this.frontWheelModelMeshContext = mainFrontWheelModelMeshContext;
		this.rearWheelModelMeshContext = mainRearWheelModelMeshContext;
		
		final float zAdjust = 0.95f;
		this.bodyPos.translate(0, zAdjust, 0);
		this.frontWheelPos.translate(0, zAdjust, 2.35f);
		this.rearWheelPos.translate(0, zAdjust + 0.05f, -2.4f);
		this.ridePos.translate(0, zAdjust + 1.6f, -1);
		this.backfirePos.set(0, zAdjust + 2.14038f, -2.74571f);
		this.massCenterHeight = zAdjust + 1.75f; // the height of oil tank
		this.leanAngleMaxWhenRunning = 55;
		this.leanAngleMaxWhenRunningRenderHeightShift = 0.07f;
		this.leanAngleMaxWhenCrashedRenderHeightShift = 0.7f;
		this.leanAngleSafe = 30;
	}
}
