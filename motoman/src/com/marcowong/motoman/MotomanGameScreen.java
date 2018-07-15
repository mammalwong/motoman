package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.marcowong.motoman.track.*;

public class MotomanGameScreen implements Screen {
	private static BitmapFont bitmapFont;
	private static Texture texStrengthBarBg;
	private static Texture texStrengthBarFg;
	private static Texture texStrengthBarCur;
	private static Texture texStraight;
	private static Texture texLeftLow;
	private static Texture texLeftMed;
	private static Texture texLeftHigh;
	private static Texture texLeftSharp;
	private static Texture texRightLow;
	private static Texture texRightMed;
	private static Texture texRightHigh;
	private static Texture texRightSharp;
	private static Texture texTrackLoc;
	private static ShaderProgram standardShader;
	private static ShaderProgram maskShader;
	private static ShaderProgram ppFinalShader;
	private static ShaderProgram ppCopyShader;
	private static ShaderProgram ppMotionBlurShader;
	private static ShaderProgram ppBloom1Shader;
	private static ShaderProgram ppBloom2Shader;
	private static ShaderProgram ppAntiAliasingShader;
	private static ShaderProgram ppShader;
	private static Mesh frameBufferMesh;
	private static IMeshContext frameBufferMeshContext;
	private static float bloomBufferSize = 48;
	
	public static void initResource() {
		bitmapFont = new BitmapFont();
		texStrengthBarBg = new Texture(Gdx.files.internal("data/StrengthBarBg.png"), false);
		texStrengthBarFg = new Texture(Gdx.files.internal("data/StrengthBarFg.png"), false);
		texStrengthBarCur = new Texture(Gdx.files.internal("data/StrengthBarCur.png"), false);
		texStraight = new Texture(Gdx.files.internal("data/dir_straight2.png"), false);
        texStraight.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texLeftLow = new Texture(Gdx.files.internal("data/dir_left_low2.png"), false);
        texLeftLow.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texLeftMed = new Texture(Gdx.files.internal("data/dir_left_med2.png"), false);
        texLeftMed.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texLeftHigh = new Texture(Gdx.files.internal("data/dir_left_high2.png"), false);
        texLeftHigh.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texLeftSharp = new Texture(Gdx.files.internal("data/dir_left_sharp2.png"), false);
        texLeftSharp.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texRightLow = new Texture(Gdx.files.internal("data/dir_right_low2.png"), false);
        texRightLow.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texRightMed = new Texture(Gdx.files.internal("data/dir_right_med2.png"), false);
        texRightMed.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texRightHigh = new Texture(Gdx.files.internal("data/dir_right_high2.png"), false);
        texRightHigh.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texRightSharp = new Texture(Gdx.files.internal("data/dir_right_sharp2.png"), false);
        texRightSharp.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texTrackLoc = new Texture(Gdx.files.internal("data/whiteDot.png"), false);
        
        frameBufferMesh = new Mesh(true, 4, 4,
        	new VertexAttribute(Usage.Position, 3, "a_position"),
        	new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        frameBufferMesh.setVertices(new float[] { 1, 1, 0, 1, 1, -1, 1, 0, 0, 1, 1, -1, 0, 1, 0, -1, -1, 0, 0, 0});
        frameBufferMesh.setIndices(new short[] { 0, 1, 2, 3 });
        frameBufferMeshContext = MeshOptimized.globalStaticMesh.add(frameBufferMesh, GL20.GL_TRIANGLE_STRIP);
        
        standardShader = new ShaderProgram(
				Gdx.files.internal("data/shader.standard.vertex.txt"),
				Gdx.files.internal("data/shader.standard.fragment.txt"));
		if (!standardShader.isCompiled()) throw new IllegalStateException("standard shader failed to compile: " + standardShader.getLog());
		maskShader = new ShaderProgram(
				Gdx.files.internal("data/shader.standard.vertex.txt"),
				Gdx.files.internal("data/shader.mask.fragment.txt"));
		if (!maskShader.isCompiled()) throw new IllegalStateException("mask shader failed to compile: " + maskShader.getLog());
		ppFinalShader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.final.fragment.txt"));
		if (!ppFinalShader.isCompiled()) throw new IllegalStateException("final pp shader failed to compile: " + ppFinalShader.getLog());
		ppCopyShader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.final.fragment.txt"));
		if (!ppCopyShader.isCompiled()) throw new IllegalStateException("copy pp shader failed to compile: " + ppCopyShader.getLog());
		ppMotionBlurShader = new ShaderProgram(
				Gdx.files.internal("data/shader.motionblur.vertex.txt"),
				Gdx.files.internal("data/shader.motionblur.fragment.txt"));
		if (!ppMotionBlurShader.isCompiled()) throw new IllegalStateException("motion blur pp shader failed to compile: " + ppMotionBlurShader.getLog());
		ppBloom1Shader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.bloom1.fragment.txt"));
		if (!ppBloom1Shader.isCompiled()) throw new IllegalStateException("bloom1 pp shader failed to compile: " + ppBloom1Shader.getLog());
		ppBloom2Shader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.bloom2.fragment.txt"));
		if (!ppBloom2Shader.isCompiled()) throw new IllegalStateException("bloom2 pp shader failed to compile: " + ppBloom2Shader.getLog());
		ppAntiAliasingShader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.antialiasing.fragment.txt"));
		if (!ppAntiAliasingShader.isCompiled()) throw new IllegalStateException("anti aliasing pp shader failed to compile: " + ppAntiAliasingShader.getLog());
		ppShader = new ShaderProgram(
				Gdx.files.internal("data/shader.postprocess.vertex.txt"),
				Gdx.files.internal("data/shader.postprocess.fragment.txt"));
		if (!ppShader.isCompiled()) throw new IllegalStateException("pp shader failed to compile: " + ppShader.getLog());
	}
	
	private MotorcycleUIHelper uiHelper;
	private Stage ui;
	private IMotorcycleControlButtonUI controlButtonUI;
	private IMotorcycleInputMeters motorcycleInputMeters;
	private TextureRegionDrawable directionStraight;
	private TextureRegionDrawable directionLeftLow;
	private TextureRegionDrawable directionLeftMed;
	private TextureRegionDrawable directionLeftHigh;
	private TextureRegionDrawable directionLeftSharp;
	private TextureRegionDrawable directionRightLow;
	private TextureRegionDrawable directionRightMed;
	private TextureRegionDrawable directionRightHigh;
	private TextureRegionDrawable directionRightSharp;
	private Image trackMapImg;
	private Image trackLocImg;
	private Label trackPercentageLabel;
	private Label timeDisplayLabel;
	private Slider riderStrengthSlider;
	private Image directionImg;
	private FrameBuffer mainFrameBufferA;
	private FrameBuffer mainFrameBufferB;
	private FrameBuffer bloomFrameBufferA;
	private FrameBuffer bloomFrameBufferB;
	private MotomanCamera camera;
	private SkyBox skyBox;
	private Track track;
	private Motorcycle motorcycle;
	private Rider rider;
	private Tile tile;
	private BackgroundObjs backgroundObjs;
	private MotomanBGMusic bgMusic;
	private boolean wasResumed = true;
	private boolean gameUpdating = true;
	
	public MotomanGameScreen(TrackData trackData) {
		uiHelper = new MotorcycleUIHelper();
		
		ui = new Stage(0, 0, true);
		createMotorcycleControlUIAndMeter();
		controlButtonUI.addToUI(ui);
		trackPercentageLabel = new Label("", new Label.LabelStyle(bitmapFont, new Color(1, 0.75f, 0, 1)));
		trackPercentageLabel.setAlignment(Align.left);
		ui.addActor(trackPercentageLabel);
		timeDisplayLabel = new Label("", new Label.LabelStyle(bitmapFont, new Color(1, 0.75f, 0, 1)));
		timeDisplayLabel.setAlignment(Align.right);
		ui.addActor(timeDisplayLabel);
		Slider.SliderStyle riderStrengthSliderStyle = new Slider.SliderStyle();
		riderStrengthSliderStyle.background = new TextureRegionDrawable(new TextureRegion(texStrengthBarBg));
		riderStrengthSliderStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(texStrengthBarFg));
		riderStrengthSliderStyle.knob = new TextureRegionDrawable(new TextureRegion(texStrengthBarCur));
		riderStrengthSlider = new SliderEx(0, 1, 0.000001f, false, riderStrengthSliderStyle);
		riderStrengthSlider.setTouchable(Touchable.disabled);
		ui.addActor(riderStrengthSlider);
        directionImg = new Image();
        directionStraight = new TextureRegionDrawable(new TextureRegion(texStraight));
        directionLeftLow = new TextureRegionDrawable(new TextureRegion(texLeftLow));
        directionLeftMed = new TextureRegionDrawable(new TextureRegion(texLeftMed));
        directionLeftHigh = new TextureRegionDrawable(new TextureRegion(texLeftHigh));
        directionLeftSharp = new TextureRegionDrawable(new TextureRegion(texLeftSharp));
        directionRightLow = new TextureRegionDrawable(new TextureRegion(texRightLow));
        directionRightMed = new TextureRegionDrawable(new TextureRegion(texRightMed));
        directionRightHigh = new TextureRegionDrawable(new TextureRegion(texRightHigh));
        directionRightSharp = new TextureRegionDrawable(new TextureRegion(texRightSharp));
        ui.addActor(directionImg);
        Gdx.input.setInputProcessor(ui);
		
		TrackGuideGenerator trackGuideGenerator = new TrackGuideGenerator();
		trackGuideGenerator.earlyNoticeDistance = 8;
		trackGuideGenerator.generate(trackData.trackSegments);
		
		skyBox = new SkyBox();
		track = new Track(trackData, ConfigHelper.getTrackDecorationQuota());
		rider = new Rider(track);
		motorcycle = new MainMotorcycle(track, motorcycleInputMeters);
		motorcycle.rider = rider;
		rider.motorcycle = motorcycle;
		tile = new Tile();
		backgroundObjs = new BackgroundObjs();
		
		camera = new MotomanCamera(67, 1, 1, motorcycle, uiHelper);
		camera.far = 1500;
		motorcycle.fx.init(camera);
		
		motorcycle.state.pos.set(track.getStartSpawnPosition());
		motorcycle.rider.setStrength(1);
		
		trackMapImg = new Image(new TextureRegionDrawable(new TextureRegion(track.getTrackMap())));
		trackLocImg = new Image(new TextureRegionDrawable(new TextureRegion(texTrackLoc)));
		ui.addActor(trackMapImg);
		ui.addActor(trackLocImg);
		
		bgMusic = new MotomanBGMusic();
		bgMusic.play();
		
		resetMotionBlurData();
	}

	@Override
	public void dispose() {
		ui.dispose();
		mainFrameBufferA.dispose();
		mainFrameBufferB.dispose();
		bloomFrameBufferA.dispose();
		bloomFrameBufferB.dispose();
		skyBox.dispose();
		track.dispose();
		motorcycle.dispose();
		rider.dispose();
		tile.dispose();
		backgroundObjs.dispose();
		bgMusic.dispose();
	}
	
	private void createMotorcycleControlUIAndMeter() {
		switch (ConfigHelper.getMotorcycleControlMode()) {
		case CombinedButton: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createCounterSteeringControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMetersEasy(
					uiHelper,
					ConfigHelper.getCombinedLeanAndCounterSteeringBlendingFactor(), false,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		case CombinedDeviceRot: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createThottleBrakeOnlyControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMetersEasy(
					uiHelper,
					ConfigHelper.getCombinedLeanAndCounterSteeringBlendingFactor(), true,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		case CombinedJoystick: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createCounterSteeringJoystickControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMetersEasy(
					uiHelper,
					ConfigHelper.getCombinedLeanAndCounterSteeringBlendingFactor(), false,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		case CSButtonLeanButton: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createCounterSteeringLeaningControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMeters(
					uiHelper, false,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		case CSButtonLeanDeviceRot: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createCounterSteeringControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMeters(
					uiHelper, true,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		case CSJoystickLeanDeviceRot: {
			controlButtonUI = MotorcycleControlButtonUIFactory.createCounterSteeringJoystickControlUI(uiHelper);
			motorcycleInputMeters = new MotorcycleInputMeters(
					uiHelper, true,
					ConfigHelper.getLeanDeviceRotBegin(),
					ConfigHelper.getLeanDeviceRotEnd());
		}
		break;
		default:
			throw new IllegalStateException("unknown control mode");
		}
	}
	
	private Vector3 tmpVec = new Vector3();
	private Matrix4 lastCameraView = new Matrix4();
	private boolean lastCameraViewReset = false;
	private int motorcycleTrackFinishedPercentage = 0;
	private float deltaBudget = 0;
	@Override
	public void render(float oDelta) {
		oDelta = Gdx.graphics.getRawDeltaTime();
		if (wasResumed) {
			wasResumed = false;
			return;
		}

		if (gameUpdating) {
			deltaBudget += oDelta;
			final float deltaTarget = 1/60f;
			while (true) {
				float delta;
				boolean isPersistUpdateStep;
				if (deltaBudget >= deltaTarget) {
					deltaBudget -= deltaTarget;
					delta = deltaTarget;
					isPersistUpdateStep = true;
					backgroundObjs.setPersist(true);
					motorcycle.setPersist(true);
					rider.setPersist(true);
					camera.setPersist(true);
				} else {
					delta = deltaBudget;
					isPersistUpdateStep = false;
					backgroundObjs.setPersist(false);
					motorcycle.setPersist(false);
					rider.setPersist(false);
					camera.setPersist(false);
				}
				
				TrackSegment motorcycleTrackSegBeforeUpdate = motorcycle.getLastTrackSegment();
				
				backgroundObjs.update(delta);
				controlButtonUI.update();
				motorcycle.update(delta);
				rider.update(delta);
				track.updateCurrentTrackSegment(motorcycle);
				track.updateCurrentTrackSegment(rider);
				
				if (isPersistUpdateStep) {
					runGameRules(delta);
					
					TrackDirection trackDirectionNotice = null;
					if (!motorcycle.state.isCrashed &&
						!motorcycle.state.isStandBy &&
						motorcycle.getLastTrackSegment() != motorcycleTrackSegBeforeUpdate)
							trackDirectionNotice = track.getTrackeeDirectionNotice(motorcycle);
					updateTrackDirectionNoticeUI(trackDirectionNotice, delta);
					if (motorcycle.state.isCrashed)
						clearTrackDirectionNoticeUI();
					
					ui.act(delta);
				}
				
				camera.setVanishingPointLookingFactor(
						getTrackDirectionNoticeUIDirection(),
						ConfigHelper.getLookIntoCornerLevel());
				camera.followMotorcycle(delta);
				camera.update(false);
				track.updateCurrentTrackSegment(camera);
				track.updateVanishingPoint(camera);
				
		//		camera.position.set(0, 20, 0.0001f);
		//		motorcycle.pos.getTranslation(tmpVec);
		//		camera.lookAt(tmpVec.x, tmpVec.y, tmpVec.z);
		//		camera.update(false);
				
	//			camera.position.set(0, 3, -10);
	//			camera.lookAt(0, 0, 0);
	//			camera.up.set(0, 0, 1);
	//			camera.update(false);
				
				if (!isPersistUpdateStep)
					break;
			}
			
			if (ConfigHelper.turnOnCameraDeviceRot())
				camera.alignCameraWithDevice();
			camera.update(true);
		}
		
		FrameBuffer mainFrameBuffer = mainFrameBufferA;
		FrameBuffer mainFrameBufferSpare = mainFrameBufferB;
		
		mainFrameBuffer.begin();
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
		standardShader.begin();
		standardShader.setUniformi("isNoLightEffect", 1);
		skyBox.render(standardShader, camera, ConfigHelper.turnOnBloomEffect());
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		backgroundObjs.render(standardShader, camera);
		Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		tile.render(standardShader, camera);
		track.render(standardShader, camera);
		if (ConfigHelper.turnOnMotionBlurEffect()) {
			standardShader.end();
			mainFrameBuffer.end();
			mainFrameBufferSpare.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			ppCopyShader.begin();
			mainFrameBuffer.getColorBufferTexture().bind(0);
			frameBufferMeshContext.render(ppCopyShader);
			ppCopyShader.end();
			Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
			mainFrameBufferSpare.end();
			mainFrameBuffer.begin();
			Gdx.gl20.glDepthFunc(GL20.GL_EQUAL);
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				mainFrameBufferSpare.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			if (lastCameraViewReset) {
				lastCameraViewReset = false;
				lastCameraView.set(camera.combined);
			}
			ppMotionBlurShader.begin();
			mainFrameBufferSpare.getColorBufferTexture().bind(1);
			ppMotionBlurShader.setUniformi("pass1Texture", 1);
			ppMotionBlurShader.setUniformf("frameBufferPixelSize",
					1f / mainFrameBuffer.getWidth(),
					1f / mainFrameBuffer.getHeight());
			ppMotionBlurShader.setUniformMatrix("viewproj", camera.combined);
			ppMotionBlurShader.setUniformMatrix("viewprojinv", camera.invProjectionView);
			ppMotionBlurShader.setUniformMatrix("lastviewproj", lastCameraView);
			track.render(ppMotionBlurShader, camera);
			ppMotionBlurShader.end();
			lastCameraView.set(camera.combined);
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				mainFrameBufferSpare.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			Gdx.gl20.glDepthFunc(GL20.GL_LESS);
			standardShader.begin();
		}
		standardShader.setUniformi("isNoLightEffect", 0);
		rider.render(standardShader, camera);
		motorcycle.render(standardShader, camera);
		standardShader.setUniformi("isNoLightEffect", 1);
		Gdx.gl20.glDisable(GL20.GL_BLEND);
		standardShader.end();
		motorcycle.fx.render();
		mainFrameBuffer.end();
		
		if (ConfigHelper.turnOnBloomEffect()) {
			bloomFrameBufferA.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
			Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
			maskShader.begin();
			maskShader.setUniformi("isNoLightEffect", 1);
			Gdx.gl20.glClearColor(
					skyBox.getSkyBloomColor().r,
					skyBox.getSkyBloomColor().g,
					skyBox.getSkyBloomColor().b, 1);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			maskShader.setUniformf("maskColor", 0, 0, 0, 1);
			backgroundObjs.render(maskShader, camera);
			Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			tile.render(maskShader, camera);
			track.render(maskShader, camera);
			//motorcycle.render(maskShader, camera);
			Gdx.gl20.glDisable(GL20.GL_BLEND);
			maskShader.end();
			bloomFrameBufferA.end();
			
			bloomFrameBufferB.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			ppBloom1Shader.begin();
			ppBloom1Shader.setUniformf("blurSize", 1f / bloomFrameBufferA.getWidth());
			bloomFrameBufferA.getColorBufferTexture().bind(0);
			frameBufferMeshContext.render(ppBloom1Shader);
			ppBloom1Shader.end();
			bloomFrameBufferB.end();
			
			bloomFrameBufferA.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			ppBloom2Shader.begin();
			ppBloom2Shader.setUniformf("blurSize", 1f / bloomFrameBufferB.getHeight());
			bloomFrameBufferB.getColorBufferTexture().bind(0);
			frameBufferMeshContext.render(ppBloom2Shader);
			ppBloom2Shader.end();
			bloomFrameBufferA.end();
			
			// bloomFrameBufferA will be resized, so must set to linear sampling
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				bloomFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			
			mainFrameBufferSpare.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			ppShader.begin();
			mainFrameBuffer.getColorBufferTexture().bind(0);
			bloomFrameBufferA.getColorBufferTexture().bind(1);
			ppShader.setUniformi("mainFrameBuffer", 0);
			ppShader.setUniformi("bloomFrameBuffer", 1);
			frameBufferMeshContext.render(ppShader);
			ppShader.end();
			mainFrameBufferSpare.end();
			
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				bloomFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			
			FrameBuffer tmpFB = mainFrameBuffer;
			mainFrameBuffer = mainFrameBufferSpare;
			mainFrameBufferSpare = tmpFB;
		}
		
		if (ConfigHelper.turnOnAntiAliasing()) {
			// FXAA shader depends on linear sampling
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				mainFrameBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			
			mainFrameBufferSpare.begin();
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
			ppAntiAliasingShader.begin();
			ppAntiAliasingShader.setUniformf("frameBufferPixelSize",
					1f / mainFrameBuffer.getWidth(),
					1f / mainFrameBuffer.getHeight());
			mainFrameBuffer.getColorBufferTexture().bind(0);
			frameBufferMeshContext.render(ppAntiAliasingShader);
			ppAntiAliasingShader.end();
			mainFrameBufferSpare.end();
			
			if (!ConfigHelper.turnOnFrameBufferLinearFilter())
				mainFrameBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			
			FrameBuffer tmpFB = mainFrameBuffer;
			mainFrameBuffer = mainFrameBufferSpare;
			mainFrameBufferSpare = tmpFB;
		}
		
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
		ppFinalShader.begin();
		mainFrameBuffer.getColorBufferTexture().bind(0);
		//bloomFrameBufferA.getColorBufferTexture().bind(0);
		frameBufferMeshContext.render(ppFinalShader);
		ppFinalShader.end();

		directionImg.setRotation(uiHelper.getDeviceRotation());
		track.getTrackeeTrackMapLoc(motorcycle, tmpVec);
		trackLocImg.setBounds(
				(trackMapImg.getX() + (tmpVec.x * trackMapImg.getWidth() / track.getTrackMap().getWidth())) - (Gdx.graphics.getHeight() * 0.02f * 0.5f),
				(trackMapImg.getY() + trackMapImg.getHeight() - (tmpVec.z * trackMapImg.getHeight() / track.getTrackMap().getHeight())) - (Gdx.graphics.getHeight() * 0.02f * 0.5f),
				Gdx.graphics.getHeight() * 0.02f,
				Gdx.graphics.getHeight() * 0.02f);
		if (!motorcycle.state.isCrashed) motorcycleTrackFinishedPercentage = (int)Math.round(track.getTrackeeTrackPercentage(motorcycle) * 100);
		trackPercentageLabel.setText("Finished " + motorcycleTrackFinishedPercentage + "%");
		timeDisplayLabel.setText(uiHelper.formatDuration(gameTimeUsed));
		timeDisplayLabel.setText(timeDisplayLabel.getText() + " FPS:" + Gdx.graphics.getFramesPerSecond());
		riderStrengthSlider.setValue(motorcycle.rider.getStrength());
		ui.draw();
	}
	
	private void resetMotionBlurData() {
		if (ConfigHelper.turnOnMotionBlurEffect())
			lastCameraViewReset = true;
	}
	
	private void gamePause() {
		gameUpdating = false;
		motorcycle.sfx.gamePause();
		bgMusic.gamePause();
	}
	
	private void gameResume() {
		gameUpdating = true;
		deltaBudget = 0;
		motorcycle.sfx.gameResume();
		bgMusic.gameResume();
	}
	
	private boolean motorcycleCrashed = false;
	private boolean motorcycleStandBy = false;
	private boolean motorcycleFinished = false;
	private boolean gameFinished = false;
	private boolean gameWined = false;
	private float gameTimeUsed = 0;
	private float motorcycleCrashedTimeRemaining;
	private float motorcycleStandByTimeRemaining;
	private float motorcycleFinishedTimeRemaining;
	private Matrix4 motorcycleCrashedPos = new Matrix4();
	private void runGameRules(float delta) {
		if (motorcycleCrashedTimeRemaining > 0) motorcycleCrashedTimeRemaining -= delta;
		if (motorcycleStandByTimeRemaining > 0) motorcycleStandByTimeRemaining -= delta;
		if (motorcycleFinishedTimeRemaining > 0) motorcycleFinishedTimeRemaining -= delta;
		
		if (!motorcycleCrashed &&
			motorcycle.state.isCrashed) {
			motorcycleCrashed = true;
			motorcycleCrashedPos.set(motorcycle.state.pos);
			motorcycleCrashedTimeRemaining = 2;
		}
		if (motorcycleCrashed &&
			motorcycleCrashedTimeRemaining <= 0) {
			motorcycleCrashed = false;
			motorcycle.reset();
			motorcycle.state.pos.set(motorcycleCrashedPos);
			motorcycle.state.pos.set(track.getSpawnPosition(motorcycle));
			resetMotionBlurData();
		}
		if (!motorcycleCrashed &&
			!motorcycleStandBy &&
			!motorcycleFinished &&
			track.isInTrackEnd(motorcycle)) {
			motorcycleFinished = true;
			motorcycleFinishedTimeRemaining = 2;
			motorcycle.standBy();
		}
		if (motorcycleFinished &&
			motorcycleFinishedTimeRemaining <= 0) {
			motorcycleFinished = false;
//			motorcycle.reset();
//			motorcycle.pos.set(track.getStartSpawnPosition());
//			rider.setStrength(1);
			gameFinished = true;
			gameWined = true;
		}
		if (!motorcycleStandBy &&
			!motorcycleFinished &&
			motorcycle.state.isStandBy) {
			motorcycleStandBy = true;
			motorcycleStandByTimeRemaining = 2;
		}
		if (!motorcycleFinished &&
			motorcycleStandBy &&
			motorcycleStandByTimeRemaining <= 0) {
			motorcycleStandBy = false;
			motorcycle.go();
		}
		if (!motorcycleFinished &&
			!motorcycleStandBy &&
			!motorcycleCrashed) {
			gameTimeUsed += delta;
		}
	}

	@Override
	public void resize(int width, int height) {
		float aspectRatio = (float) width / (float) height;
		camera.viewportWidth = aspectRatio;
		camera.viewportHeight = 1;
		
		ui.setViewport(width, height, true);
		controlButtonUI.resize(width, height);
		trackMapImg.setBounds(
				0.05f * height,
				height - height * (0.05f + 0.3f),
				0.3f * height,
				0.3f * height);
		trackPercentageLabel.setFontScale(height * 0.003f);
		trackPercentageLabel.setBounds(
				height * 0.05f,
				height - height * (0.05f + 0.3f + 0.1f),
				width,
				0.1f * height);
		timeDisplayLabel.setFontScale(height * 0.003f);
		timeDisplayLabel.setBounds(
				-height * 0.05f,
				height - height * (0.05f + 0.05f),
				width,
				0.1f * height);
		riderStrengthSlider.setBounds(
				0,
				height - 0.015f * height,
				width,
				0.015f * height);
		directionImg.setOrigin(0.5f * 0.25f * height, 0.5f * 0.25f * height);
		directionImg.setBounds(
				width * 0.5f - height * 0.25f * 0.5f,
				height * 0.75f - height * 0.25f * 0.5f,
				0.25f * height,
				0.25f * height);
		
		float frameBufferSizeReduction = ConfigHelper.getResolutionReduction();
		if (mainFrameBufferA != null) mainFrameBufferA.dispose();
		mainFrameBufferA = new FrameBuffer(Format.RGBA8888, (int)(width * frameBufferSizeReduction), (int)(height * frameBufferSizeReduction), true);
		if (ConfigHelper.turnOnFrameBufferLinearFilter())
			mainFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			mainFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		if (mainFrameBufferB != null) mainFrameBufferB.dispose();
		mainFrameBufferB = new FrameBuffer(Format.RGBA8888, (int)(width * frameBufferSizeReduction), (int)(height * frameBufferSizeReduction), true);
		if (ConfigHelper.turnOnFrameBufferLinearFilter())
			mainFrameBufferB.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			mainFrameBufferB.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		if (bloomFrameBufferA != null) bloomFrameBufferA.dispose();
		bloomFrameBufferA = new FrameBuffer(Format.RGBA8888, (int)Math.ceil(aspectRatio * bloomBufferSize), (int)Math.ceil(bloomBufferSize), true);
		if (ConfigHelper.turnOnFrameBufferLinearFilter())
			bloomFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			bloomFrameBufferA.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		if (bloomFrameBufferB != null) bloomFrameBufferB.dispose();
		bloomFrameBufferB = new FrameBuffer(Format.RGBA8888, (int)Math.ceil(aspectRatio * bloomBufferSize), (int)Math.ceil(bloomBufferSize), true);
		if (ConfigHelper.turnOnFrameBufferLinearFilter())
			bloomFrameBufferB.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			bloomFrameBufferB.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		wasResumed = true;
		track.resume();
		((TextureRegionDrawable)trackMapImg.getDrawable())
			.getRegion().setTexture(track.getTrackMap());
		skyBox.resume();
	}
	
	private float lastTrackDirectionUpdateRemaining = 0;
	private TrackDirection lastShowingTrackDirectionUI = null;
	private void clearTrackDirectionNoticeUI() {
		lastTrackDirectionUpdateRemaining = 0;
		lastShowingTrackDirectionUI = null;
		directionImg.setDrawable(null);
	}
	
	private void updateTrackDirectionNoticeUI(TrackDirection td, float delta) {
		final float lastTrackDirectionUpdateSpan = 5;
		if (lastTrackDirectionUpdateRemaining > 0)
			lastTrackDirectionUpdateRemaining -= delta;
		if (td != null) lastTrackDirectionUpdateRemaining = lastTrackDirectionUpdateSpan;
		if (lastTrackDirectionUpdateRemaining <= 0)
			directionImg.setDrawable(null);
		else if (directionImg.getDrawable() != null) {
			float alpha = lastTrackDirectionUpdateRemaining / lastTrackDirectionUpdateSpan;
			alpha *= alpha * alpha;
			directionImg.setColor(1, 1, 1, alpha);
		}
		
		if (td == lastShowingTrackDirectionUI) return;
		if (td == null) return;
		else {
			switch (td) {
			case Straight: directionImg.setDrawable(directionStraight); break;
			case LeftLow: directionImg.setDrawable(directionLeftLow); break;
			case LeftMed: directionImg.setDrawable(directionLeftMed); break;
			case LeftHigh: directionImg.setDrawable(directionLeftHigh); break;
			case LeftSharp: directionImg.setDrawable(directionLeftSharp); break;
			case RightLow: directionImg.setDrawable(directionRightLow); break;
			case RightMed: directionImg.setDrawable(directionRightMed); break;
			case RightHigh: directionImg.setDrawable(directionRightHigh); break;
			case RightSharp: directionImg.setDrawable(directionRightSharp); break;
			default : directionImg.setDrawable(null); break;
			}
		}
		if (directionImg.getDrawable() != null)
			directionImg.setColor(1, 1, 1, 1);
		lastShowingTrackDirectionUI = td;
	}
	
	private TrackDirection getTrackDirectionNoticeUIDirection() {
		Drawable d = directionImg.getDrawable();
		if (d == null) return null;
		if (d == directionStraight) return TrackDirection.Straight;
		if (d == directionLeftLow) return TrackDirection.LeftLow;
		if (d == directionRightLow) return TrackDirection.RightLow;
		if (d == directionLeftMed) return TrackDirection.LeftMed;
		if (d == directionRightMed) return TrackDirection.RightMed;
		if (d == directionLeftHigh) return TrackDirection.LeftHigh;
		if (d == directionRightHigh) return TrackDirection.RightHigh;
		if (d == directionLeftSharp) return TrackDirection.LeftSharp;
		if (d == directionRightSharp) return TrackDirection.RightSharp;
		throw new IllegalStateException("unknown direction");
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}
	
	public boolean isGameFinished() {
		return gameFinished;
	}
	
	public boolean isGameWined() {
		return gameWined;
	}
	
	public float getGameTimeUsed() {
		return gameTimeUsed;
	}
}
