package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MotorcycleControlButtonUIFactory {
	private static Texture turnLeftBtnTexture;
	private static Texture turnRightBtnTexture;
	private static Texture thottleBtnTexture;
	private static Texture brakeBtnTexture;
	private static Texture joystickBgTexture;
	
	public static void initResource() {
		turnLeftBtnTexture = new Texture(Gdx.files.internal("data/turnL.png"), false);
		turnRightBtnTexture = new Texture(Gdx.files.internal("data/turnR.png"), false);
		thottleBtnTexture = new Texture(Gdx.files.internal("data/thottle.png"), false);
		brakeBtnTexture = new Texture(Gdx.files.internal("data/brake.png"), false);
		joystickBgTexture = new Texture(Gdx.files.internal("data/joystickBg.png"), false);
	}
	
	private static class MotorcycleControlButtonBase implements IMotorcycleControlButtonUI {
		private ImageButton thottleBtn;
		private ImageButton brakeBtn;
		
		public MotorcycleControlButtonBase(final MotorcycleUIHelper uiHelper) {
			TextureRegionDrawable thottleBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(thottleBtnTexture));
			thottleBtn = new ImageButton(thottleBtnTextureDrawable);
			thottleBtn.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setThottlePressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setThottlePressed(false);
				}
			});
			TextureRegionDrawable brakeBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(brakeBtnTexture));
			brakeBtn = new ImageButton(brakeBtnTextureDrawable);
			brakeBtn.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setBrakePressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setBrakePressed(false);
				}
			});
		}

		@Override
		public void addToUI(Stage ui) {
			ui.addActor(thottleBtn);
			ui.addActor(brakeBtn);
		}

		@Override
		public void resize(int width, int height) {
			thottleBtn.setBounds(
					width - (0.05f + 0.25f) * height,
					(0.05f + 0.25f) * height,
					0.25f * height,
					0.25f * height);
			brakeBtn.setBounds(
					width - (0.05f + 0.25f) * height,
					0.05f * height,
					0.25f * height,
					0.25f * height);
		}
		
		@Override
		public void update() {
		}
	}
	
	public static IMotorcycleControlButtonUI createThottleBrakeOnlyControlUI(MotorcycleUIHelper uiHelper) {
		return new MotorcycleControlButtonBase(uiHelper);
	}
	
	private static class MotorcycleSingleTurningControlButton extends MotorcycleControlButtonBase {
		private ImageButton turnL1;
		private ImageButton turnR1;
		
		public MotorcycleSingleTurningControlButton(final MotorcycleUIHelper uiHelper, final boolean controlCounterSteering) {
			super(uiHelper);
			
			TextureRegionDrawable counterSteeringLeftBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(turnLeftBtnTexture));
			turnL1 = new ImageButton(counterSteeringLeftBtnTextureDrawable);
			turnL1.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (controlCounterSteering)
						uiHelper.setCounterSteerLeftPressed(true);
					else
						uiHelper.setLeanLeftPressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					if (controlCounterSteering)
						uiHelper.setCounterSteerLeftPressed(false);
					else
						uiHelper.setLeanLeftPressed(false);
				}
			});
			TextureRegionDrawable counterSteeringRightBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(turnRightBtnTexture));
			turnR1 = new ImageButton(counterSteeringRightBtnTextureDrawable);
			turnR1.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (controlCounterSteering)
						uiHelper.setCounterSteerRightPressed(true);
					else
						uiHelper.setLeanRightPressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					if (controlCounterSteering)
						uiHelper.setCounterSteerRightPressed(false);
					else
						uiHelper.setLeanRightPressed(false);
				}
			});
		}
		
		@Override
		public void addToUI(Stage ui) {
			super.addToUI(ui);
			ui.addActor(turnL1);
			ui.addActor(turnR1);
		}
		
		@Override
		public void resize(int width, int height) {
			super.resize(width, height);
			turnL1.setBounds(
					0.05f * height,
					0.05f * height,
					0.25f * height,
					0.25f * height);
			turnR1.setBounds(
					(0.05f + 0.25f) * height,
					0.05f * height,
					0.25f * height,
					0.25f * height);
		}
	}
	
	public static IMotorcycleControlButtonUI createCounterSteeringControlUI(MotorcycleUIHelper uiHelper) {
		return new MotorcycleSingleTurningControlButton(uiHelper, true);
	}
	
	private static class MotorcycleMultiTurningControlButton extends MotorcycleSingleTurningControlButton {
		private ImageButton turnL2;
		private ImageButton turnR2;
		
		public MotorcycleMultiTurningControlButton(final MotorcycleUIHelper uiHelper) {
			super(uiHelper, false);
			
			TextureRegionDrawable leanLeftBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(turnLeftBtnTexture));
			turnL2 = new ImageButton(leanLeftBtnTextureDrawable);
			turnL2.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setCounterSteerLeftPressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setCounterSteerLeftPressed(false);
				}
			});
			TextureRegionDrawable leanRightBtnTextureDrawable = new TextureRegionDrawable(new TextureRegion(turnRightBtnTexture));
			turnR2 = new ImageButton(leanRightBtnTextureDrawable);
			turnR2.addListener(new InputListener() {
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setCounterSteerRightPressed(true);
					return true;
				}
				@Override
				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					uiHelper.setCounterSteerRightPressed(false);
				}
			});
		}
		
		@Override
		public void addToUI(Stage ui) {
			super.addToUI(ui);
			ui.addActor(turnL2);
			ui.addActor(turnR2);
		}
		
		@Override
		public void resize(int width, int height) {
			super.resize(width, height);
			turnL2.setBounds(
					0.05f * height,
					(0.05f + 0.25f) * height,
					0.25f * height,
					0.25f * height);
			turnR2.setBounds(
					(0.05f + 0.25f) * height,
					(0.05f + 0.25f) * height,
					0.25f * height,
					0.25f * height);
		}
	}
	
	public static IMotorcycleControlButtonUI createCounterSteeringLeaningControlUI(MotorcycleUIHelper uiHelper) {
		return new MotorcycleMultiTurningControlButton(uiHelper);
	}
	
	public static class MotorcycleJoystickControlButton extends MotorcycleControlButtonBase {
		private Touchpad joystick;
		private MotorcycleUIHelper uiHelper;
		
		public MotorcycleJoystickControlButton(MotorcycleUIHelper uiHelper) {
			super(uiHelper);
			this.uiHelper = uiHelper;
			
			joystick = new Touchpad(0, new Touchpad.TouchpadStyle(
					new TextureRegionDrawable(new TextureRegion(joystickBgTexture)), null));
		}
		
		@Override
		public void addToUI(Stage ui) {
			super.addToUI(ui);
			ui.addActor(joystick);
		}
		
		@Override
		public void resize(int width, int height) {
			super.resize(width, height);
			joystick.setBounds(
				0.075f * height,
				0.075f * height,
				0.4f * height,
				0.4f * height);
		}
		
		@Override
		public void update() {
			super.update();
			float reading = joystick.getKnobPercentX();
			if (reading < -1) reading = -1;
			if (reading > 1) reading = 1;
			uiHelper.setCounterSteeringJoystickReading(reading);
		}
	}
	
	public static IMotorcycleControlButtonUI createCounterSteeringJoystickControlUI(MotorcycleUIHelper uiHelper) {
		return new MotorcycleJoystickControlButton(uiHelper);
	}
}
