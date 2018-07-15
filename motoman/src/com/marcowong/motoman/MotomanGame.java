package com.marcowong.motoman;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.marcowong.motoman.track.BasicRandom;
import com.marcowong.motoman.track.TrackGenerator;

public class MotomanGame extends Game {
	private final static boolean isGL20Debug = false;
	
	private static boolean initedStaticResource = false;
	private static GL20Optimized gl20Optimized;
	private static GL20Debug gl20Debug;
	private static void initStaticResource() {
		if (isGL20Debug) {
			gl20Debug = new GL20Debug(Gdx.gl20, System.out);
			gl20Optimized = new GL20Optimized(gl20Debug);
		} else
			gl20Optimized = new GL20Optimized(Gdx.gl20);
		Gdx.gl20 = gl20Optimized;
		Gdx.gl = gl20Optimized;
		Gdx.graphics = new GraphicsOptimized(Gdx.graphics, gl20Optimized);
		MeshOptimized.initResource();
		Rider.initResource();
		Motorcycle.initResource();
		MainMotorcycle.initResource();
		MotorcycleFX.initResource();
		MotorcycleSFX.initResource();
		Track.initResource();
		BackgroundObjs.initResource();
		Tile.initResource();
		SkyBox.initResource();
		MotorcycleControlButtonUIFactory.initResource();
		MotomanGameScreen.initResource();
		MotomanLoadingScreen.initResource();
		MeshOptimized.globalStaticMesh.optimize();
	}
	
	@Override
	public void create() {
		if (!initedStaticResource) {
			initedStaticResource = true;
			initStaticResource();
		}
		
		setScreen(new MotomanLoadingScreen(createTrackGenerator()));
	}

	@Override
	public void render() {
		if (isGL20Debug)
			gl20Debug.renderBegin();
		
		super.render();
		
		Screen screen0 = getScreen(); 
		if (screen0 instanceof MotomanGameScreen) {
			MotomanGameScreen screen = (MotomanGameScreen)screen0;
			if (screen.isGameFinished()) {
				setScreen(null);
				screen.dispose();
				setScreen(new MotomanLoadingScreen(createTrackGenerator()));
			}
		} else if (screen0 instanceof MotomanLoadingScreen) {
			MotomanLoadingScreen screen = (MotomanLoadingScreen)screen0;
			if (screen.isLoadingDone()) {
				setScreen(null);
				MotomanGameScreen gameScreen = screen.getGameScreen();
				screen.dispose();
				setScreen(gameScreen);
			}
		}
		
		if (isGL20Debug)
			gl20Debug.renderEnd();
	}
	
	private int genTrackSegmentsSeed = 999939;
	private TrackGenerator createTrackGenerator() {
		int seed = genTrackSegmentsSeed++;
		TrackGenerator trackGenerator = new TrackGenerator();
		trackGenerator.random = new BasicRandom(seed);
		trackGenerator.trackDataRandomSeed = seed;
		trackGenerator.trackLen = 500;
		trackGenerator.turnAngleZeroFactor = 0.3f;
		trackGenerator.segLen = 2;
		trackGenerator.segWidth = 1;
		trackGenerator.segPad = 0.5f;
		return trackGenerator;
	}
	
	@Override
	public void pause() {
		super.pause();
		Gdx.app.exit();
		System.exit(0);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		Screen screen = getScreen();
		if (screen != null) {
			setScreen(null);
			screen.dispose();
		}
	}
}
