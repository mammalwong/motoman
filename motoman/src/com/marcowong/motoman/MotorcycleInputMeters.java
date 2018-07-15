package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class MotorcycleInputMeters implements IMotorcycleInputMeters {
	private MotorcycleUIHelper uiHelper;
	private boolean useLeanDeviceRot;
	private float leanDeviceRotBegin;
	private float leanDeviceRotEnd;
	
	public MotorcycleInputMeters(MotorcycleUIHelper uiHelper,
			boolean useLeanDeviceRot, float leanDeviceRotBegin, float leanDeviceRotEnd) {
		this.uiHelper = uiHelper;
		this.useLeanDeviceRot = useLeanDeviceRot;
		this.leanDeviceRotBegin = leanDeviceRotBegin;
		this.leanDeviceRotEnd = leanDeviceRotEnd;
	}
	
	/* (non-Javadoc)
	 * @see com.marcowong.motoman.IMotorcycleInputMeters#getEngineAndBrakeMeter()
	 */
	@Override
	public float getEngineAndBrakeMeter() {
		if (Gdx.input.isKeyPressed(Input.Keys.S) ||
			uiHelper.isBrakePressed())
			return -1;
		if (Gdx.input.isKeyPressed(Input.Keys.W) ||
			uiHelper.isThottlePressed())
			return 1;
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.marcowong.motoman.IMotorcycleInputMeters#getCounterSteeringMeter()
	 */
	@Override
	public float getCounterSteeringMeter() {
		float counterSteeringMeter = 0;
		if (Gdx.input.isKeyPressed(Input.Keys.Q)) counterSteeringMeter = -1;
		if (Gdx.input.isKeyPressed(Input.Keys.E)) counterSteeringMeter = 1;
		if (!(uiHelper.isCounterSteerLeftPressed() &&
				uiHelper.isCounterSteerRightPressed())) {
			if (uiHelper.isCounterSteerLeftPressed()) counterSteeringMeter = -1;
			if (uiHelper.isCounterSteerRightPressed()) counterSteeringMeter = 1;
		}
		if (counterSteeringMeter == 0)
			counterSteeringMeter = uiHelper.getCounterSteeringJoystickReading();
		return counterSteeringMeter;
	}
	
	/* (non-Javadoc)
	 * @see com.marcowong.motoman.IMotorcycleInputMeters#getLeanMeter()
	 */
	@Override
	public float getLeanMeter() {
		float leanMeter = 0;
		if (Gdx.input.isKeyPressed(Input.Keys.A)) leanMeter = -1;
		if (Gdx.input.isKeyPressed(Input.Keys.D)) leanMeter = 1;
		if (!(uiHelper.isLeanLeftPressed() &&
				uiHelper.isLeanRightPressed())) {
			if (uiHelper.isLeanLeftPressed()) leanMeter = -1;
			if (uiHelper.isLeanRightPressed()) leanMeter = 1;
		}
		if (useLeanDeviceRot && leanMeter == 0) {
			float reading = uiHelper.getDeviceRotation();
			float sign = Math.signum(reading);
			float abs = (Math.abs(reading) - leanDeviceRotBegin) / (leanDeviceRotEnd - leanDeviceRotBegin);
			if (abs < 0) abs = 0;
			if (abs > 1) abs = 1;
			leanMeter = sign * abs;
		}
		return leanMeter;
	}
	
	@Override
	public void setMotorcycle(Motorcycle motorcycle) {
	}
}
