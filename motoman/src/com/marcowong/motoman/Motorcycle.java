package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.marcowong.motoman.track.*;

public class Motorcycle implements ITrackee {
	private IMotorcycleInputMeters inputMeters;
	
	public Track track;
	
	public MotorcycleFX fx = new MotorcycleFX();
	public MotorcycleSFX sfx = new MotorcycleSFX(this, new SFXBackfireReporter());
	
	public Rider rider;
	public Matrix4 ridePos = new Matrix4();
	
	public Matrix4 bodyPos = new Matrix4();
	public Matrix4 frontWheelPos = new Matrix4();
	public Matrix4 rearWheelPos = new Matrix4();
	public Vector3 backfirePos = new Vector3();
	
	public float massCenterHeight = 0;
	public float leanAngleMaxWhenRunning;
	public float leanAngleMaxWhenRunningRenderHeightShift;
	public float leanAngleMaxWhenCrashedRenderHeightShift;
	public float leanAngleSafe;
	
	public IMeshContext bodyModelMeshContext;
	public IMeshContext frontWheelModelMeshContext;
	public IMeshContext rearWheelModelMeshContext;
	
	private float engineOutputMin = 20;
	private float engineOutputMax = 200;
	
	public class UpdateState {
		public float leanAngle = 0;
		public boolean isCrashed = false;
		public boolean isStandBy = true;
		public float engineOutput = engineOutputMin;
		public float frontTraction = 1;
		public float backTraction = 1;
		public float slideDuration = 0;
		public boolean isTouchingGround = false;
		public Vector3 bikeVelo = new Vector3();
		public Matrix4 pos = new Matrix4();
		public Matrix4 lean = new Matrix4();
		public Matrix4 frontWheelRot = new Matrix4();
		public Matrix4 rearWheelRot = new Matrix4();
		public float latestCounterSteeringPositionShift = 0;
		public TrackSegment lastTrackSegment;
		
		public void copyTo(UpdateState s) {
			s.leanAngle = leanAngle;
			s.isCrashed = isCrashed;
			s.isStandBy = isStandBy;
			s.engineOutput = engineOutput;
			s.frontTraction = frontTraction;
			s.backTraction = backTraction;
			s.slideDuration = slideDuration;
			s.isTouchingGround = isTouchingGround;
			s.bikeVelo.set(bikeVelo);
			s.pos.set(pos);
			s.lean.set(lean);
			s.frontWheelRot.set(frontWheelRot);
			s.rearWheelRot.set(rearWheelRot);
			s.latestCounterSteeringPositionShift = latestCounterSteeringPositionShift;
			s.lastTrackSegment = lastTrackSegment;
		}
	}
	public UpdateState stateTmp = new UpdateState();
	public UpdateState statePersist = new UpdateState();
	public UpdateState state = statePersist;
	
	private static Model shadowModel;
	private static IMeshContext shadowModelMeshContext;
	
	public static void initResource() {
		shadowModel = new ObjLoaderEx().loadObj(Gdx.files.internal("data/bikeShadow.obj"), true);
		StaticModelTextureFilterConfigManager.add(shadowModel);
		shadowModelMeshContext = MeshOptimized.globalStaticMesh.add(shadowModel);
	}
	
	public Motorcycle(Track track, IMotorcycleInputMeters inputMeters) {
		this.track = track;
		this.inputMeters = inputMeters;
		this.inputMeters.setMotorcycle(this);
	}
	
	private Matrix4 tmpMat2 = new Matrix4();
	private void renderModel(ShaderProgram shader, Camera camera, IMeshContext meshContext, Matrix4 modelPos) {
		tmpMat2.set(camera.combined);
		tmpMat2.mul(modelPos);
		shader.setUniformMatrix("modelviewproj", tmpMat2);
		tmpMat2.set(camera.view);
		tmpMat2.mul(modelPos);
		shader.setUniformMatrix("modelview", tmpMat2);
		meshContext.render(shader);
	}
	
	public float getLeanHeightShift() {
		float leanAngleAbs = Math.abs(state.leanAngle);
		float leanAngleHeightShift;
		if (leanAngleAbs <= leanAngleMaxWhenRunning) {
			leanAngleHeightShift = (leanAngleAbs / leanAngleMaxWhenRunning) * leanAngleMaxWhenRunningRenderHeightShift;
		} else {
			float crashHeightRatio = (leanAngleAbs - leanAngleMaxWhenRunning) / (90 - leanAngleMaxWhenRunning);
			leanAngleHeightShift =
					leanAngleMaxWhenRunningRenderHeightShift * (1 - crashHeightRatio) +
					leanAngleMaxWhenCrashedRenderHeightShift * crashHeightRatio;
		}
		return leanAngleHeightShift;
	}
	
	private Vector3 tmpVec4 = new Vector3();
	private Matrix4 tmpMat = new Matrix4();
	private Matrix4 tmpMat3 = new Matrix4();
	public void render(ShaderProgram shader, Camera camera) {
		tmpMat3.set(state.pos);
		tmpMat3.translate(0, getLeanHeightShift(), 0);
		tmpMat3.mul(state.lean);
		
		tmpMat.set(tmpMat3);
		tmpMat.mul(bodyPos);
		renderModel(shader, camera, bodyModelMeshContext, tmpMat);
		tmpMat.set(tmpMat3);
		tmpMat.mul(frontWheelPos);
		tmpMat.mul(state.frontWheelRot);
		renderModel(shader, camera, frontWheelModelMeshContext, tmpMat);
		tmpMat.set(tmpMat3);
		tmpMat.mul(rearWheelPos);
		tmpMat.mul(state.rearWheelRot);
		renderModel(shader, camera, rearWheelModelMeshContext, tmpMat);
		
		tmpVec4.set(0, massCenterHeight, 0);
		tmpVec4.mul(state.lean);
		tmpMat.set(state.pos);
		tmpMat.translate(tmpVec4.x, 0.01f, 0);
		tmpMat.scale(1 + 0.5f * Math.abs(tmpVec4.x), 1, 1);
		renderModel(shader, camera, shadowModelMeshContext, tmpMat);
	}
	
	public float getRawEngineAndBrakeMeter() {
		return inputMeters.getEngineAndBrakeMeter();
	}
	
	public float getEngineAndBrakeMeter() {
		if (state.isCrashed ||
			state.isStandBy)
			return 0;
		return getRawEngineAndBrakeMeter();
	}
	
	private float getCounterSteeringMeter() {
		if (state.isCrashed ||
			state.isStandBy)
			return 0;
		return inputMeters.getCounterSteeringMeter();
	}
	
	public float getLeanMeter() {
		if (state.isCrashed ||
			state.isStandBy)
			return 0;
		return inputMeters.getLeanMeter();
	}
	
	public void setPersist(boolean b) {
		if (b) {
			if (state != statePersist) {
				state = statePersist;
			}
		} else {
			if (state != stateTmp) {
				statePersist.copyTo(stateTmp);
				state = stateTmp;
			}
		}
	}
	
	public void update(float delta) {
		updateBikePhysics(delta);
		if (state == statePersist) {
			updateFX(delta);
			sfx.update(delta);
		}
	}
	
	private Vector3 tmpVec = new Vector3();
	private Vector3 tmpVec2 = new Vector3();
	private Vector3 tmpVec5 = new Vector3();
	private Matrix4 tmpMat4 = new Matrix4();
	private Quaternion tmpQua = new Quaternion();
	private Vector3 tmpBikeVeloNew = new Vector3();
	private void updateBikePhysics(float delta) {
		boolean isOverLean = false;
		float leanAngleMax = state.isCrashed ? 90 : leanAngleMaxWhenRunning;
		float rotAngle = 0;
		final float leanToRotFactor = 2f;
		
		final float counterSteeringLeanInc = 90;
		final float counterSteeringAffectRotAngleFactor = 1/5f;
		float counterSteeringMeter = getCounterSteeringMeter();
		float counterSteeringTractionFactor = (state.frontTraction <= 0 && state.backTraction <= 0) ? 0 : 1;
		float counterSteeringRiderStrengthFactor = 3/4f + rider.getStrength() * 1/4f;
		float counterSteeringLean = delta * counterSteeringMeter * counterSteeringLeanInc * counterSteeringTractionFactor * counterSteeringRiderStrengthFactor;
		float counterSteeredLeanAngle = state.leanAngle + counterSteeringLean;
		if (counterSteeredLeanAngle < -leanAngleMax) counterSteeredLeanAngle = -leanAngleMax;
		if (counterSteeredLeanAngle > leanAngleMax) counterSteeredLeanAngle = leanAngleMax;
		counterSteeringLean = counterSteeredLeanAngle - state.leanAngle;
		tmpMat4.idt();
		tmpMat4.rotate(0, 0, 1, state.leanAngle);
		tmpVec.set(0, massCenterHeight, 0);
		tmpVec.mul(tmpMat4);
		tmpMat4.idt();
		tmpMat4.rotate(0, 0, 1, counterSteeredLeanAngle);
		tmpVec2.set(0, massCenterHeight, 0);
		tmpVec2.mul(tmpMat4);
		float massCenterShift = tmpVec2.x - tmpVec.x;
		state.leanAngle = counterSteeredLeanAngle;
		rotAngle += counterSteeringLean * counterSteeringAffectRotAngleFactor * leanToRotFactor;
		state.pos.translate(massCenterShift * delta, 0, 0); // counter steering shift, regardless of traction;
		
		float leanPressureForceRatio = 0.8f + 0.2f * (Math.abs(state.leanAngle) / 90);
		final float leanAnglePressure = 90;
		final float leanAnglePressEpsilon = 10;
		float leanMeter = getLeanMeter();
		float leanRiderStrengthFactor = 2/3f + rider.getStrength() * 1/3f;
		if (state.leanAngle >= -leanAnglePressEpsilon && state.leanAngle <= leanAnglePressEpsilon ||
			state.leanAngle >= 0 && leanMeter > 0 ||
			state.leanAngle <= 0 && leanMeter < 0)
			state.leanAngle += delta * leanPressureForceRatio * leanAnglePressure * leanMeter * leanRiderStrengthFactor;
		if (state.leanAngle < -leanAngleMax) { state.leanAngle = -leanAngleMax; isOverLean = true; }
		if (state.leanAngle > leanAngleMax) { state.leanAngle = leanAngleMax; isOverLean = true; }
		
		float gravityForceRatio = Math.abs(state.leanAngle) / 90;
		final float gravityForceWhenRunning = 25;
		final float gravityForceWhenCrashed = gravityForceWhenRunning * 10;
		float gravityForce = state.isCrashed ? gravityForceWhenCrashed : gravityForceWhenRunning;
		if (state.leanAngle > 0) state.leanAngle += delta * gravityForceRatio * gravityForce;
		if (state.leanAngle < 0) state.leanAngle -= delta * gravityForceRatio * gravityForce;
		if (state.leanAngle < -leanAngleMax) { state.leanAngle = -leanAngleMax; isOverLean = true; }
		if (state.leanAngle > leanAngleMax) { state.leanAngle = leanAngleMax; isOverLean = true; }
		
		final float engineOutputAdjust = 50;
		float engineAndBrakeMeter = getEngineAndBrakeMeter();
		if (!state.isStandBy) {
			state.engineOutput += engineOutputAdjust * delta * engineAndBrakeMeter;
			if (state.engineOutput > engineOutputMax) state.engineOutput = engineOutputMax;
			if (state.engineOutput < engineOutputMin) state.engineOutput = engineOutputMin;
		} else
			state.engineOutput = 0;
		boolean isTheWheelIncreasingSpeed = engineAndBrakeMeter > 0 && state.engineOutput < engineOutputMax;
		boolean isTheWheelDecreasingSpeed = engineAndBrakeMeter < 0 && state.engineOutput > engineOutputMin;
		
		rotAngle += -state.leanAngle * leanToRotFactor * delta;
		
		float traction = state.isCrashed ? 0 : Math.min((state.frontTraction + state.backTraction) * 0.5f, state.backTraction);
		state.pos.rotate(0, 1, 0, rotAngle * traction);
		state.pos.getRotation(tmpQua);
		tmpQua.toMatrix(tmpMat4.val);
		tmpVec.set(0, 0, delta * state.engineOutput);
		tmpVec.mul(tmpMat4);
		tmpBikeVeloNew.set(
				traction * tmpVec.x + (1 - traction) * state.bikeVelo.x,
				traction * tmpVec.y + (1 - traction) * state.bikeVelo.y,
				traction * tmpVec.z + (1 - traction) * state.bikeVelo.z);
		state.pos.trn(tmpBikeVeloNew);
		float bikeVeloScalar = tmpBikeVeloNew.len() / delta;
		
		float directionFalse = 0;
		if (state.bikeVelo.len2() > 0 && tmpBikeVeloNew.len2() > 0) {
			tmpVec.set(state.bikeVelo);
			tmpVec.crs(0, 1, 0);
			tmpVec.nor();
			directionFalse = tmpVec.dot(tmpBikeVeloNew);
		}
		
		final float cornerForceFactor = 30;
		float cornerForceRatio = 1 - Math.abs(state.leanAngle) / 90f;
		//delta and velo is not required as cornerForce already factored delta and velo
		state.leanAngle -= cornerForceRatio * directionFalse * cornerForceFactor;
		if (state.leanAngle < -leanAngleMax) { state.leanAngle = -leanAngleMax; isOverLean = true; }
		if (state.leanAngle > leanAngleMax) { state.leanAngle = leanAngleMax; isOverLean = true; }
		
		if (state.isStandBy) {
			final float standByStandUpFactor = 90;
			if (state.leanAngle < 0) {
				state.leanAngle += standByStandUpFactor * delta;
				if (state.leanAngle > 0) state.leanAngle = 0;
			}
			if (state.leanAngle > 0) {
				state.leanAngle -= standByStandUpFactor * delta;
				if (state.leanAngle < 0) state.leanAngle = 0;
			}
		}
		
		boolean isSlided = false;
		if (state.backTraction < state.frontTraction || state.isCrashed) {
			final float bikeSlideRotFactor = 30;
			final float bikeSlideIncreaseDuration = 1;
			float slideDirection = state.leanAngle > 0 ? 1 : state.leanAngle < 0 ? -1 : 0;
			if (slideDirection != 0) {
				float bikeSlideDegree = Math.min(state.slideDuration / bikeSlideIncreaseDuration, 1) * Math.min(bikeVeloScalar, 1) * delta;
				float bikeSlideRotAngle = -slideDirection * bikeSlideRotFactor * bikeSlideDegree;
				state.pos.rotate(0, 1, 0, bikeSlideRotAngle);
				isSlided = true;
			}
		}
		if (isSlided) state.slideDuration += delta;
		else state.slideDuration = 0;
		
		state.lean.idt();
		state.lean.rotate(0, 0, 1, state.leanAngle);
		
		final float bikeVeloDecay = 0.5f;
		state.bikeVelo.set(tmpBikeVeloNew);
		state.bikeVelo.sub(
				state.bikeVelo.x * bikeVeloDecay * delta,
				state.bikeVelo.y * bikeVeloDecay * delta,
				state.bikeVelo.z * bikeVeloDecay * delta);
		
		final float leanAngleOverTractionLimitBase = 75;
		final float leanAngleOverTractionLimitFactor = 0.2f;
		float leanAngleOverTractionLimit = leanAngleOverTractionLimitBase - bikeVeloScalar * leanAngleOverTractionLimitFactor;
		if (leanAngleOverTractionLimit < 0) leanAngleOverTractionLimit = 0;
		if (leanAngleOverTractionLimit > leanAngleMax) leanAngleOverTractionLimit = leanAngleMax;
		
		if (state.isCrashed) {
			state.frontTraction = 0;
			state.backTraction = 0;
		} else {
			float leanAngleAbs = Math.abs(state.leanAngle);
			final float overLeanTractionDecay = 1/2f;
			final float overTractionLimitTractionDecay = 1/5f;
			final float overTractionLimitTractionFrontWheelDecay = 1/5f;
			final float overTractionLimitTractionRearWheelDecay = 1/5f;
			final float tractionRecoverRate = 1/3f;
			if (leanAngleAbs >= leanAngleMax || isOverLean) {
				state.frontTraction -= delta * overLeanTractionDecay;
				state.backTraction -= delta * overLeanTractionDecay;
				if (state.frontTraction < 0) state.frontTraction = 0;
				if (state.backTraction < 0) state.backTraction = 0;
			} else if (leanAngleAbs <= leanAngleSafe) {
				state.frontTraction = 1;
				state.backTraction = 1;
			} else if (leanAngleAbs >= leanAngleOverTractionLimit) {
				state.frontTraction -= delta * overTractionLimitTractionDecay;
				state.backTraction -= delta * overTractionLimitTractionDecay;
				if (isTheWheelDecreasingSpeed) state.frontTraction -= delta * overTractionLimitTractionFrontWheelDecay;
				if (isTheWheelIncreasingSpeed) state.backTraction -= delta * overTractionLimitTractionRearWheelDecay;
				if (state.frontTraction < 0) state.frontTraction = 0;
				if (state.backTraction < 0) state.backTraction = 0;
			} else {
				state.frontTraction += delta * tractionRecoverRate;
				state.backTraction += delta * tractionRecoverRate;
				if (state.frontTraction > 1) state.frontTraction = 1;
				if (state.backTraction > 1) state.backTraction = 1;
			}
			final float counterSteeringTractionDecay = 1.5f;
			if (leanAngleAbs > leanAngleSafe &&
				counterSteeringLean != 0 &&
				Math.signum(state.leanAngle) == Math.signum(counterSteeringLean)) {
				state.frontTraction -= (Math.abs(counterSteeringLean) / 90) * counterSteeringTractionDecay;
				if (state.frontTraction < 0) state.frontTraction = 0;
			}
			if (state.frontTraction <= 0 && state.backTraction > 0) state.backTraction -= delta * overTractionLimitTractionRearWheelDecay;
			if (state.backTraction <= 0 && state.frontTraction > 0) state.frontTraction -= delta * overTractionLimitTractionFrontWheelDecay;
		}
		
		if (!state.isStandBy) {
			boolean isInsideTrack = track.isInsideTrack(this);
			boolean isInsideTrackRider = track.isInsideTrack(rider);
			if (!isInsideTrack ||
				!isInsideTrackRider ||
				(state.frontTraction == 0 && state.backTraction == 0)) {
				if (!state.isCrashed) {
					state.isCrashed = true;
					if (!isInsideTrack ||
						!isInsideTrackRider)
						sfx.playCrashSound();
				}
				if (rider.state.attached)
					rider.detach();
			}
			
			if (!isInsideTrack &&
				track.getTrackCollisionVector(this, tmpVec, tmpVec2)) {
				state.pos.getTranslation(tmpVec5);
				state.pos.trn(tmpVec.sub(tmpVec5));
				state.bikeVelo.sub(tmpVec2.mul(2 * tmpVec2.dot(state.bikeVelo))).mul(0.5f);
				sfx.playCrashSound();
			}
		}
		
		float rotFactor = 720 * (state.engineOutput / 20);
		state.frontWheelRot.rotate(1, 0, 0, rotFactor * delta);
		state.rearWheelRot.rotate(1, 0, 0, rotFactor * delta);
		
		state.latestCounterSteeringPositionShift = massCenterShift;
		
		state.isTouchingGround = isOverLean || state.leanAngle >= leanAngleMaxWhenRunning;
	}
	
	public void reset() {
		state.isCrashed = false;
		state.isStandBy = true;
		state.frontTraction = 1;
		state.backTraction = 1;
		state.leanAngle = 0;
		state.lean.idt();
		state.engineOutput = engineOutputMin;
		state.bikeVelo.set(0, 0, 0);
		fx.clear();
		sfx.clear();
		if (!rider.state.attached)
			rider.attach();
	}
	
	public void go() {
		if (state.isCrashed ||
			!state.isStandBy)
			return;
		state.isStandBy = false;
	}
	
	public void standBy() {
		if (state.isCrashed ||
			state.isStandBy)
			return;
		state.isStandBy = true;
		state.frontTraction = 1;
		state.backTraction = 1;
		state.engineOutput = engineOutputMin;
		state.bikeVelo.set(0, 0, 0);
	}
	
	public float getEngineOutputPercentage() {
		float p = (state.engineOutput - engineOutputMin) / (engineOutputMax - engineOutputMin);
		if (p < 0)
			p = 0;
		return p;
	}
	
	@Override
	public void getTrackeePos(Vector3 vec) {
		state.pos.getTranslation(vec);
	}

	@Override
	public void setLastTrackSegment(TrackSegment ts) {
		state.lastTrackSegment = ts;
	}

	@Override
	public TrackSegment getLastTrackSegment() {
		return state.lastTrackSegment;
	}
	
	private float lastBackfireSize = 0;
	private class SFXBackfireReporter implements MotorcycleSFX.BackfireReporter {
		@Override
		public void reportBackfire(float size) {
			lastBackfireSize = size;
		}
	}
	
	private BackfireFXPosition backfireFXPosition = new BackfireFXPosition();
	private class BackfireFXPosition implements MotorcycleFX.DynamicFXPosition {
		@Override
		public void getPosition(Vector3 vec) {
			vec.set(backfirePos);
			vec.mul(state.lean);
			vec.mul(state.pos);
		}
	}
	
	private Matrix4 tmpMat5 = new Matrix4();
	private Vector3 tmpVec3 = new Vector3();
	private void updateFX(float delta) {
		float speedRatio = Math.min(1, state.bikeVelo.len2());
		if (state.frontTraction < 1) {
			tmpMat5.set(state.pos);
			tmpVec3.set(0, 0, 0);
			tmpVec3.mul(frontWheelPos);
			tmpMat5.translate(tmpVec3.x, 0, tmpVec3.z);
			tmpMat5.getTranslation(tmpVec3);
			fx.addSmoke(((1 - state.frontTraction) * 4 + ((float)Math.random() - 0.5f)) * speedRatio,
					tmpVec3.x + 2 * ((float)Math.random() - 0.5f) * speedRatio,
					tmpVec3.y + (float)Math.random() * speedRatio,
					tmpVec3.z + 2 * ((float)Math.random() - 0.5f) * speedRatio);
		}
		if (state.backTraction < 1) {
			tmpMat5.set(state.pos);
			tmpVec3.set(0, 0, 0);
			tmpVec3.mul(rearWheelPos);
			tmpMat5.translate(tmpVec3.x, 0, tmpVec3.z);
			tmpMat5.getTranslation(tmpVec3);
			fx.addSmoke(((1 - state.backTraction) * 4 + ((float)Math.random() - 0.5f)) * speedRatio,
					tmpVec3.x + 2 * ((float)Math.random() - 0.5f) * speedRatio,
					tmpVec3.y + (float)Math.random() * speedRatio,
					tmpVec3.z + 2 * ((float)Math.random() - 0.5f) * speedRatio);
		}
		if (state.isTouchingGround) {
			tmpVec3.set(0, massCenterHeight, 0);
			tmpVec3.mul(state.lean);
			tmpVec3.y = 0;
			tmpVec3.mul(state.pos);
			fx.addSpark(((float)Math.random() + 0.5f) * speedRatio,
					tmpVec3.x + 2 * ((float)Math.random() - 0.5f) * speedRatio,
					tmpVec3.y + (float)Math.random() * speedRatio,
					tmpVec3.z + 2 * ((float)Math.random() - 0.5f) * speedRatio);
		}
		if (lastBackfireSize > 0) {
			fx.addBackfire(2 * lastBackfireSize, backfireFXPosition);
			lastBackfireSize = 0;
		}
		fx.update(delta);
	}
	
	public void dispose() {
		fx.dispose();
		sfx.dispose();
	}
}
