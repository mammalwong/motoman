package com.marcowong.motoman;

public class MotorcycleInputMetersEasy extends MotorcycleInputMeters implements IMotorcycleInputMeters {
	private Motorcycle motorcycle;
	private float useMoreCounterSteeringRatio;
	
	public MotorcycleInputMetersEasy(MotorcycleUIHelper uiHelper, float useMoreCounterSteeringRatio,
			boolean useLeanDeviceRot, float leanDeviceRotBegin, float leanDeviceRotEnd) {
		super(uiHelper, useLeanDeviceRot, leanDeviceRotBegin, leanDeviceRotEnd);
		this.useMoreCounterSteeringRatio = useMoreCounterSteeringRatio;
	}
	
	private float getCombinedSteeringMeter() {
		float counterSteeringMeter = super.getCounterSteeringMeter();
		float leanMeter = super.getLeanMeter();
		float counterSteeringMeterAbs = Math.abs(counterSteeringMeter);
		float leanMeterAbs = Math.abs(leanMeter);
		if (counterSteeringMeterAbs > leanMeterAbs)
			return counterSteeringMeter;
		else
			return leanMeter;
	}
	
	private boolean shouldUseCounterSteering(float meter) {
		if (meter < 0 && motorcycle.state.leanAngle > 0)
			return true;
		if (meter > 0 && motorcycle.state.leanAngle < 0)
			return true;
		if (Math.abs(motorcycle.state.leanAngle) <= motorcycle.leanAngleSafe * useMoreCounterSteeringRatio)
			return true;
		return false;
	}

	@Override
	public float getCounterSteeringMeter() {
		float meter = getCombinedSteeringMeter();
		if (shouldUseCounterSteering(meter))
			return meter;
		else
			return 0;
	}

	@Override
	public float getLeanMeter() {
		float meter = getCombinedSteeringMeter();
		if (shouldUseCounterSteering(meter))
			return 0;
		else
			return meter;
	}
	
	@Override
	public void setMotorcycle(Motorcycle motorcycle) {
		this.motorcycle = motorcycle;
	}
}
