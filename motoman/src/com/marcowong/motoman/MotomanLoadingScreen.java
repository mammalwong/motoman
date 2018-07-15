package com.marcowong.motoman;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.marcowong.motoman.track.TrackData;
import com.marcowong.motoman.track.TrackGenerator;

public class MotomanLoadingScreen implements Screen {
	private static BitmapFont bitmapFont;
	private static ExecutorService genTrackSegmentsES;
	
	public static void initResource() {
		bitmapFont = new BitmapFont();
		genTrackSegmentsES = Executors.newSingleThreadExecutor();
	}
	
	private Stage ui;
	private Label loadingLabel;
	private TrackGenerator trackGenerator;
	private Future<TrackData> trackGenerationResult;
	private MotomanGameScreen gameScreen = null;
	private boolean gameScreenCreating = false;
	
	public MotomanLoadingScreen(TrackGenerator trackGenerator) {
		ui = new Stage();
		loadingLabel = new Label("", new Label.LabelStyle(bitmapFont, new Color(1, 0.75f, 0, 1)));
		loadingLabel.setAlignment(Align.center);
		ui.addActor(loadingLabel);
		
		this.trackGenerator = trackGenerator;
		trackGenerationResult = genTrackSegmentsES.submit(new Callable<TrackData>() {
			@Override
			public TrackData call() throws Exception {
				TrackData td = MotomanLoadingScreen.this.trackGenerator.generate();
				System.gc();
				return td;
			}
		});
	}
	
	public boolean isLoadingDone() {
		return gameScreen != null;
	}
	
	public MotomanGameScreen getGameScreen() {
		return gameScreen;
	}

	@Override
	public void render(float delta) {
		if (trackGenerationResult.isDone() &&
			!gameScreenCreating) {
			gameScreenCreating = true;
			final TrackData td;
			try {
				td = trackGenerationResult.get();
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
			
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					MotomanGameScreen gameScreen = new MotomanGameScreen(td);
					System.gc();
					MotomanLoadingScreen.this.gameScreen = gameScreen;
				}
			});
		}
		
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		int loadingProgress = (int)Math.round(trackGenerator.getGenerationProgress() * 100);
		loadingLabel.setText("Loading... " + loadingProgress + "%");
		ui.draw();
	}

	@Override
	public void resize(int width, int height) {
		ui.setViewport(width, height, true);
		loadingLabel.setFontScale(height * 0.003f);
		loadingLabel.setBounds(0, 0, width, height);
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		ui.dispose();
	}
}
