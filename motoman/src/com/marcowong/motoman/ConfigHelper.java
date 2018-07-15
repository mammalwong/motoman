package com.marcowong.motoman;

public class ConfigHelper {
	public static float getResolutionReduction() {
		return 1/2f;
	}
	
	public static float getLookIntoCornerLevel() {
		return 0.8f;
	}
	
	public static boolean turnOnBloomEffect() {
		return true;
	}
	
	public static boolean turnOnMotionBlurEffect() {
		return true;
	}
	
	public static boolean turnOnFrameBufferLinearFilter() {
		return false;
	}
	
	public static boolean turnOnModelTextureLinearFilter() {
		return false;
	}
	
	public static void setTurnOnModelTextureLinearFilter(boolean b) {
		StaticModelTextureFilterConfigManager.updateFilter();
	}
	
	public static boolean turnOnAntiAliasing() {
		return true;
	}
	
	public static boolean turnOnCameraDeviceRot() {
		return
				getMotorcycleControlMode() == MotorcycleControlMode.CombinedDeviceRot ||
				getMotorcycleControlMode() == MotorcycleControlMode.CSButtonLeanDeviceRot ||
				getMotorcycleControlMode() == MotorcycleControlMode.CSJoystickLeanDeviceRot;
	}
	
	public static float getLeanDeviceRotBegin() {
		return 0;
	}
	
	public static float getLeanDeviceRotEnd() {
		return 22.5f;
	}
	
	public static float getCombinedLeanAndCounterSteeringBlendingFactor() {
		return 0.75f;
	}
	
	public static enum MotorcycleControlMode {
		CombinedButton,
		CombinedDeviceRot,
		CombinedJoystick,
		CSButtonLeanButton,
		CSButtonLeanDeviceRot,
		CSJoystickLeanDeviceRot
	}
	
	public static MotorcycleControlMode getMotorcycleControlMode() {
		return MotorcycleControlMode.CombinedButton;
	}
	
	public static int getTrackDecorationQuota() {
		return 5;
	}
}
