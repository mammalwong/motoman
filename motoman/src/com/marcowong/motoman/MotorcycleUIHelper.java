package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.math.Vector3;

public class MotorcycleUIHelper {
	private int counterSteerLeftPressed = 0;
	private int counterSteerRightPressed = 0;
	private int leanLeftPressed = 0;
	private int leanRightPressed = 0;
	private int thottlePressed = 0;
	private int brakePressed = 0;
	private float counterSteerLeftJoystickReading = 0;
	
	public boolean isCounterSteerLeftPressed() {
		return counterSteerLeftPressed > 0;
	}
	
	public void setCounterSteerLeftPressed(boolean counterSteerLeftPressed) {
		if (counterSteerLeftPressed)
			++this.counterSteerLeftPressed;
		else
			--this.counterSteerLeftPressed;
	}
	
	public boolean isCounterSteerRightPressed() {
		return counterSteerRightPressed > 0;
	}
	
	public void setCounterSteerRightPressed(boolean counterSteerRightPressed) {
		if (counterSteerRightPressed)
			++this.counterSteerRightPressed;
		else
			--this.counterSteerRightPressed;
	}
	
	public boolean isLeanLeftPressed() {
		return leanLeftPressed > 0;
	}
	
	public void setLeanLeftPressed(boolean leanLeftPressed) {
		if (leanLeftPressed)
			++this.leanLeftPressed;
		else
			--this.leanLeftPressed;
	}
	
	public boolean isLeanRightPressed() {
		return leanRightPressed > 0;
	}
	
	public void setLeanRightPressed(boolean leanRightPressed) {
		if (leanRightPressed)
			++this.leanRightPressed;
		else
			--this.leanRightPressed;
	}
	
	public boolean isThottlePressed() {
		return thottlePressed > 0;
	}
	
	public void setThottlePressed(boolean thottlePressed) {
		if (thottlePressed)
			++this.thottlePressed;
		else
			--this.thottlePressed;
	}
	
	public boolean isBrakePressed() {
		return brakePressed > 0;
	}
	
	public void setBrakePressed(boolean brakePressed) {
		if (brakePressed)
			++this.brakePressed;
		else
			--this.brakePressed;
	}
	
	public float getCounterSteeringJoystickReading() {
		return this.counterSteerLeftJoystickReading;
	}
	
	public void setCounterSteeringJoystickReading(float reading) {
		this.counterSteerLeftJoystickReading = reading;
	}
	
	private Vector3 tmpVec = new Vector3();
	private float getDeviceRotationInternal() {
		float gLen = tmpVec.set(Gdx.input.getAccelerometerX(), Gdx.input.getAccelerometerY(), Gdx.input.getAccelerometerZ()).len();
		float normalizedY = Gdx.input.getAccelerometerY() / gLen;
		float angleY = (float)(Math.asin(normalizedY) * (180 / Math.PI));
		return angleY;
	}
	
	private float lastDeviceRotation = getDeviceRotationInternal();
	public float getDeviceRotation() {
		if (!Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)) return 0;
		float rCurrent = getDeviceRotationInternal() * 0.1f + 0.9f * lastDeviceRotation;
		lastDeviceRotation = rCurrent;
		return rCurrent;
	}
	
	public CharSequence formatDuration(float d) {
		StringBuilder sb = new StringBuilder();
		String min = Integer.toString((int)Math.floor(d / 60));
		d = d % 60;
		for (int i = 0; i < 2 - min.length(); ++i) sb.append('0');
		sb.append(min);
		sb.append(':');
		String sec = Integer.toString((int)Math.floor(d));
		d = d % 1;
		for (int i = 0; i < 2 - sec.length(); ++i) sb.append('0');
		sb.append(sec);
		sb.append(':');
		String mill = Integer.toString((int)Math.floor(d * 1000));
		for (int i = 0; i < 3 - mill.length(); ++i) sb.append('0');
		sb.append(mill);
		return sb;
	}
}
