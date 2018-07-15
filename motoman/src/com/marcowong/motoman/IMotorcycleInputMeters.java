package com.marcowong.motoman;

public interface IMotorcycleInputMeters {

	public abstract float getEngineAndBrakeMeter();

	public abstract float getCounterSteeringMeter();

	public abstract float getLeanMeter();

	public abstract void setMotorcycle(Motorcycle motorcycle);
}