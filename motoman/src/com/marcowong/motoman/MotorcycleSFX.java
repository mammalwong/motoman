package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class MotorcycleSFX {
	public interface BackfireReporter {
		public void reportBackfire(float size);
	}
	
	private static Sound soundIdle;
	private static Sound soundExhaust;
	private static Sound soundCrankShaft;
	private static Sound soundBackfire;
	private static Sound soundKneeDragging;
	private static Sound soundKneeDragging2;
	private static Sound soundCrash;
	private static Sound soundDryClutch;
	private static Sound soundBrakeDisc;
	
	private static float gearBoxBase;
	private static float[] gearBoxRangesCombined;
	
	public static void initResource() {
		gearBoxBase = 3/4f;
		float[] gearBoxRanges = new float[] { 1, 1.4f, 1.8f, 2.2f, 2.6f, 3 };
		float gearBoxRangesT = 0;
		for (int i = 0; i < gearBoxRanges.length; ++i) gearBoxRangesT += gearBoxRanges[i];
		for (int i = 0; i < gearBoxRanges.length; ++i) gearBoxRanges[i] /= gearBoxRangesT;
		gearBoxRangesCombined = new float[gearBoxRanges.length];
		for (int i = 0; i < gearBoxRanges.length; ++i) gearBoxRangesCombined[i] = gearBoxRanges[i];
		for (int i = 1; i < gearBoxRanges.length; ++i) gearBoxRangesCombined[i] *= (1 - gearBoxBase);
		gearBoxRangesT = 0;
		for (int i = 0; i < gearBoxRanges.length; ++i) gearBoxRangesT += gearBoxRangesCombined[i];
		for (int i = 0; i < gearBoxRanges.length; ++i) gearBoxRangesCombined[i] /= gearBoxRangesT;
		gearBoxRangesCombined[gearBoxRangesCombined.length - 1] += 0.000001f;
		
		soundIdle = Gdx.audio.newSound(Gdx.files.internal("data/engineSoundIdle.wav"));
		soundDryClutch = Gdx.audio.newSound(Gdx.files.internal("data/dryClutchSound.wav"));
		soundExhaust = Gdx.audio.newSound(Gdx.files.internal("data/engineSoundC.wav"));
		soundCrankShaft = Gdx.audio.newSound(Gdx.files.internal("data/engineSound2B.wav"));
		soundBrakeDisc = Gdx.audio.newSound(Gdx.files.internal("data/brakeDiscSound.wav"));
		soundBackfire = Gdx.audio.newSound(Gdx.files.internal("data/backfireSound.wav"));
		soundKneeDragging = Gdx.audio.newSound(Gdx.files.internal("data/kneeDraggingSound.wav"));
		soundKneeDragging2 = Gdx.audio.newSound(Gdx.files.internal("data/kneeDraggingSound2.wav"));
		soundCrash = Gdx.audio.newSound(Gdx.files.internal("data/crashSound.wav"));
	}
	
	private Motorcycle motorcycle;
	private BackfireReporter backfireReporter;
	
	private long soundIdleInst;
	private long soundExhaustInst;
	private long soundCrankShaftInst;
	private long soundBackfireInst;
	private long soundKneeDraggingInst;
	private long soundKneeDragging2Inst;
	private long soundCrashInst;
	private long soundDryClutchInst;
	private long soundBrakeDiscInst;
	
	public MotorcycleSFX(Motorcycle motorcycle, BackfireReporter backfireReporter) {
		this.motorcycle = motorcycle;
		this.backfireReporter = backfireReporter;
		
		soundIdleInst = soundIdle.loop(0, 1, 0);
		soundDryClutchInst = soundDryClutch.loop(0, 1, 0);
		soundExhaustInst = soundExhaust.loop(0, 1, 0);
		soundCrankShaftInst = soundCrankShaft.loop(0, 0.5f, 0);
		soundBrakeDiscInst = soundBrakeDisc.loop(0, 1, 0);
		soundBackfireInst = -1;
		soundKneeDraggingInst = -1;
		soundKneeDragging2Inst = -1;
		soundCrashInst = -1;
	}
	
	private int getEngineGearOrder(float e) {
		int range = 0;
		float rangeStart = 0;
		float rangeEnd = 0;
		for (int i = 0; i < gearBoxRangesCombined.length; ++i) {
			rangeEnd += gearBoxRangesCombined[i];
			if (rangeStart <= e && e <= rangeEnd) {
				range = i;
			}
			rangeStart += gearBoxRangesCombined[i];
		}
		return range;
	}
	
	private float getEngineOutputGearboxed(float e) {
		int range = 0;
		float rangeStart = 0;
		float rangeEnd = 0;
		float rangedE = 0;
		for (int i = 0; i < gearBoxRangesCombined.length; ++i) {
			rangeEnd += gearBoxRangesCombined[i];
			if (rangeStart <= e && e <= rangeEnd) {
				range = i;
				rangedE = (e - rangeStart) / gearBoxRangesCombined[i];
			}
			rangeStart += gearBoxRangesCombined[i];
		}
		
		if (range == 0) {
			return rangedE;
		} else {
			return rangedE * (1 - gearBoxBase) + gearBoxBase;
		}
	}
	
	public void gamePause() {
		muteMainSFX();
	}
	
	public void gameResume() {
	}
	
	private void muteMainSFX() {
		soundIdle.setVolume(soundIdleInst, 0);
		soundExhaust.setVolume(soundExhaustInst, 0);
		soundCrankShaft.setVolume(soundCrankShaftInst, 0);
		soundDryClutch.setVolume(soundDryClutchInst, 0);
		soundBrakeDisc.setVolume(soundBrakeDiscInst, 0);
		if (soundKneeDraggingInst != -1) soundKneeDragging.stop(soundKneeDraggingInst);
		if (soundKneeDragging2Inst != -1) soundKneeDragging2.stop(soundKneeDragging2Inst);
		soundKneeDraggingInst = -1;
		soundKneeDragging2Inst = -1;
	}
	
	private float lastEngineOutputNoise = 0;
	private float lastEngineOutput = 0;
	private float lastEngineMeter = 0;
	private int lastEngineGearOrder = 0;
	private float engineAcceBackfireTime = -1;
	private float engineNoOutputTime = 0;
	private float backFireTime = 0;
	private float backFireSize = 0;
	private boolean lastKneeDragging = false;
	private float timeSinceStandBy = 0;
	private float halfClutchPitch = 0;
	private float lastExhaustPitch = 0;
	private boolean lastMotorcycleStandBy = false;
	public void update(float delta) {
		if (motorcycle.state.isCrashed) {
			muteMainSFX();
			timeSinceStandBy = 0;
		} else {
			float engineOutput = motorcycle.getEngineOutputPercentage();
			float engineOutputRaw = motorcycle.getRawEngineAndBrakeMeter();
			int engineGearOrder = getEngineGearOrder(engineOutput);
			
			if (motorcycle.state.isStandBy) {
				timeSinceStandBy = 0;
				engineNoOutputTime = 0;
			} else {
				timeSinceStandBy += delta;
				if (engineOutput == 0)
					engineNoOutputTime = 0;
				else
					engineNoOutputTime += delta;
			}
			
			final float halfClutchPitchMax = 1;
			final float halfClutchPitchIncreaseFactor = 10;
			final float halfClutchPitchDecreaseFactor = 2;
			if (engineNoOutputTime <= 0.5f && engineOutputRaw > 0)
				halfClutchPitch += halfClutchPitchIncreaseFactor * delta;
			else
				halfClutchPitch -= halfClutchPitchDecreaseFactor * delta;
			if (halfClutchPitch > 1) halfClutchPitch = 1;
			if (halfClutchPitch < 0) halfClutchPitch = 0;
			
			if (motorcycle.state.isStandBy) {
				if (!lastMotorcycleStandBy) {
					if (lastExhaustPitch > 1)
						halfClutchPitch = (lastExhaustPitch - 1) / halfClutchPitchMax;
					else
						halfClutchPitch = 0;
				}
				if (halfClutchPitch > 0) {
					soundIdle.setVolume(soundIdleInst, 0);
					soundExhaust.setVolume(soundExhaustInst, 1);
					soundExhaust.setPitch(soundExhaustInst, 1 + halfClutchPitch * halfClutchPitchMax);
					lastExhaustPitch = 1 + halfClutchPitch * halfClutchPitchMax;
					soundCrankShaft.setVolume(soundCrankShaftInst, 1);
					soundCrankShaft.setPitch(soundCrankShaftInst, 1 + halfClutchPitch * halfClutchPitchMax);
					soundDryClutch.setVolume(soundDryClutchInst, 0);
					soundBrakeDisc.setVolume(soundBrakeDiscInst, 0);
				} else {
					soundIdle.setVolume(soundIdleInst, 1);
					soundExhaust.setVolume(soundExhaustInst, 0);
					lastExhaustPitch = 0;
					soundCrankShaft.setVolume(soundCrankShaftInst, 0);
					soundDryClutch.setVolume(soundDryClutchInst, 1);
					soundDryClutch.setPitch(soundDryClutchInst, 1);
					soundBrakeDisc.setVolume(soundBrakeDiscInst, 0);
				}
			} else {
				boolean customCrankShaftVolume;
				if (engineOutput == 0 &&
					timeSinceStandBy >= 1) {
					soundIdle.setVolume(soundIdleInst, 1);
					soundExhaust.setVolume(soundExhaustInst, 0);
					soundDryClutch.setVolume(soundDryClutchInst, 1);
					soundDryClutch.setPitch(soundDryClutchInst, 1.5f);
					customCrankShaftVolume = false;
				} else {
					soundIdle.setVolume(soundIdleInst, 0);
					soundExhaust.setVolume(soundExhaustInst, 1);
					soundDryClutch.setVolume(soundDryClutchInst, 0);
					soundDryClutch.setPitch(soundDryClutchInst, 1);
					customCrankShaftVolume = true;
				}
				
				if (engineOutput == 0)
					engineNoOutputTime = 0;
				else
					engineNoOutputTime += delta;
				
				float engineOutputGeared = getEngineOutputGearboxed(engineOutput);
				final float engineOutputNoiseRange = 0.3f;
				float engineOutputNoise = (float)Math.random() * engineOutputNoiseRange - engineOutputNoiseRange * 0.5f;
				lastEngineOutputNoise = engineOutputNoise * 0.1f + lastEngineOutputNoise * 0.9f;
				engineOutputGeared += lastEngineOutputNoise;
				float leanFactor = (90 - Math.abs(motorcycle.state.leanAngle) * 0.333f) / 90;
				float engineSoundPitch = Math.max(
						0.5f + (engineOutputGeared * 1.5f * leanFactor),
						1 + halfClutchPitch * halfClutchPitchMax);
				if (engineSoundPitch < 0.5f) engineSoundPitch = 0.5f;
				if (engineSoundPitch > 2) engineSoundPitch = 2;
				soundExhaust.setPitch(soundExhaustInst, engineSoundPitch);
				lastExhaustPitch = engineSoundPitch;
				
				float soundCrankShaftPitch = engineSoundPitch;
				soundCrankShaft.setPitch(soundCrankShaftInst, soundCrankShaftPitch);
				if (!customCrankShaftVolume)
					soundCrankShaft.setVolume(soundCrankShaftInst, 0);
				else if (engineOutput > lastEngineOutput)
					soundCrankShaft.setVolume(soundCrankShaftInst, 1);
				else
					soundCrankShaft.setVolume(soundCrankShaftInst, 0.5f);
				if (lastEngineOutput > engineOutput &&
					Math.abs(motorcycle.state.leanAngle) <= 30)
					soundBrakeDisc.setVolume(soundBrakeDiscInst, engineOutput);
				else
					soundBrakeDisc.setVolume(soundBrakeDiscInst, 0);
				lastEngineOutput = engineOutput;
			}
			
			if (engineGearOrder < lastEngineGearOrder) {
				backFireTime = 0.5f;
				backFireSize = 0.25f;
				if (soundBackfireInst != -1) {
					soundBackfire.stop(soundBackfireInst);
					soundBackfireInst = -1;
				}
				soundBackfireInst = soundBackfire.play(1, 1, 0);
				backfireReporter.reportBackfire(0.5f);
				Gdx.input.vibrate(50);
			}
			lastEngineGearOrder = engineGearOrder;
			
			float engineMeter = motorcycle.getEngineAndBrakeMeter();
			if (engineMeter > 0 && lastEngineMeter <= 0) engineAcceBackfireTime = 0;
			if (engineMeter <= 0 && engineAcceBackfireTime >= 0) {
				if (engineAcceBackfireTime > 1) {
					backFireTime = 1;
					backFireSize = 0.5f;
					if (soundBackfireInst != -1) {
						soundBackfire.stop(soundBackfireInst);
						soundBackfireInst = -1;
					}
					soundBackfireInst = soundBackfire.play(2, 1, 0);
					backfireReporter.reportBackfire(1);
					Gdx.input.vibrate(100);
				}
				engineAcceBackfireTime = -1;
			}
			if (engineAcceBackfireTime >= 0) engineAcceBackfireTime += delta;
			lastEngineMeter = engineMeter;
			
			if (backFireTime > 0) {
				if (Math.random() < 0.025f) {
					if (soundBackfireInst != -1) {
						soundBackfire.stop(soundBackfireInst);
						soundBackfireInst = -1;
					}
					soundBackfireInst = soundBackfire.play(backFireSize * 2, 1, 0);
					backfireReporter.reportBackfire(backFireSize);
					Gdx.input.vibrate(50);
				}
				backFireTime -= delta;
				if (backFireTime < 0) backFireTime = 0;
			}
			
			boolean kneeDragging = motorcycle.rider.isKneeDragging();
			if (kneeDragging) {
				if (!lastKneeDragging) {
					soundKneeDraggingInst = soundKneeDragging.play(0.75f);
					soundKneeDragging2Inst = soundKneeDragging.loop(0.75f);
				}
			} else {
				if (soundKneeDraggingInst != -1) soundKneeDragging.stop(soundKneeDraggingInst);
				if (soundKneeDragging2Inst != -1) soundKneeDragging2.stop(soundKneeDragging2Inst);
				soundKneeDraggingInst = -1;
				soundKneeDragging2Inst = -1;
			}
			
			lastKneeDragging = kneeDragging;
			lastMotorcycleStandBy = motorcycle.state.isStandBy;
		}
	}
	
	public void playCrashSound() {
		if (soundCrashInst != -1) {
			soundCrash.stop(soundCrashInst);
			soundCrashInst = -1;
		}
		soundCrashInst = soundCrash.play();
		Gdx.input.vibrate(100);
	}
	
	public void clear() {
		lastEngineOutput = 0;
		lastEngineMeter = 0;
		lastEngineGearOrder = 0;
		engineAcceBackfireTime = -1;
		engineNoOutputTime = 0;
		backFireTime = 0;
		backFireSize = 0;
		lastKneeDragging = false;
		timeSinceStandBy = 0;
		halfClutchPitch = 0;
		lastExhaustPitch = 0;
		lastMotorcycleStandBy = false;
		
		if (soundKneeDraggingInst != -1) soundKneeDragging.stop(soundKneeDraggingInst);
		if (soundKneeDragging2Inst != -1) soundKneeDragging2.stop(soundKneeDragging2Inst);
		soundKneeDraggingInst = -1;
		soundKneeDragging2Inst = -1;
		
		if (soundBackfireInst != -1) soundBackfire.stop(soundBackfireInst);
		soundBackfireInst = -1;
		
		if (soundCrashInst != -1) soundCrash.stop(soundCrashInst);
		soundCrashInst = -1;
	}
	
	public void dispose() {
		clear();
		
		soundIdle.stop(soundIdleInst);
		soundExhaust.stop(soundExhaustInst);
		soundCrankShaft.stop(soundCrankShaftInst);
		soundDryClutch.stop(soundDryClutchInst);
		soundBrakeDisc.stop(soundBrakeDiscInst);
	}
}
