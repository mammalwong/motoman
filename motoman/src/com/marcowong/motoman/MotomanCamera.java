package com.marcowong.motoman;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.marcowong.motoman.track.TrackDirection;
import com.marcowong.motoman.track.TrackSegment;

public class MotomanCamera extends PerspectiveCamera implements ITrackee {
	private Motorcycle motorcycle;
	private MotorcycleUIHelper uiHelper;
	
	public class UpdateState {
		public TrackSegment lastTrackSegment;
		public Vector3 vanishingPoint = new Vector3();
		public Vector3 vanishingPointSmoothed = new Vector3();
		public float vanishingPointLookingFactor = 0;
		public float vanishingPointLookingFactorTarget = 0;
		public float shakeRot = 0;
		public float shakeness = 0;
		public float distance = 0;
		public float motorcycleCounterSteeringShift = 0;
		public float motorcycleEngineOutput = 0;
		public boolean vanishingPointNeedReset = true;
		public Vector3 shake = new Vector3();
		
		public void copyTo(UpdateState s) {
			s.lastTrackSegment = lastTrackSegment;
			s.vanishingPoint.set(vanishingPoint);
			s.vanishingPointSmoothed.set(vanishingPointSmoothed);
			s.vanishingPointLookingFactor = vanishingPointLookingFactor;
			s.vanishingPointLookingFactorTarget = vanishingPointLookingFactorTarget;
			s.shakeRot = shakeRot;
			s.shakeness = shakeness;
			s.distance = distance;
			s.motorcycleCounterSteeringShift = motorcycleCounterSteeringShift;
			s.motorcycleEngineOutput = motorcycleEngineOutput;
			s.vanishingPointNeedReset = vanishingPointNeedReset;
			s.shake.set(shake);
		}
	}
	public UpdateState statePersist = new UpdateState();
	public UpdateState stateTmp = new UpdateState();
	public UpdateState state = statePersist;
	
	public MotomanCamera(float fieldOfView, float viewportW, float viewportHeight, Motorcycle motorcycle, MotorcycleUIHelper uiHelper) {
		super(fieldOfView, viewportW, viewportHeight);
		this.motorcycle = motorcycle;
		this.uiHelper = uiHelper;
	}

	@Override
	public void getTrackeePos(Vector3 vec) {
		vec.set(position);
	}

	@Override
	public void setLastTrackSegment(TrackSegment ts) {
		this.state.lastTrackSegment = ts;
	}

	@Override
	public TrackSegment getLastTrackSegment() {
		return this.state.lastTrackSegment;
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
	
	private Matrix4 tmpMat2 = new Matrix4();
	private Quaternion tmpQua = new Quaternion();
	private Quaternion tmpQua2 = new Quaternion();
	private Vector3 tmpVec = new Vector3();
	private Vector3 tmpVec2 = new Vector3();
	private Vector3 tmpVec5 = new Vector3();
	private Vector3 tmpVec6 = new Vector3();
	private final float shakeRotFactor = 5;
	public void followMotorcycle(float delta) {
		if (!motorcycle.state.isCrashed) {
			float shakeVectorChangeRate = 0.2f;
			state.shake.x = (1 - shakeVectorChangeRate) * state.shake.x + shakeVectorChangeRate * ((float)Math.random() - 0.5f);
			state.shake.y = (1 - shakeVectorChangeRate) * state.shake.y + shakeVectorChangeRate * ((float)Math.random() - 0.5f);
			state.shake.z = (1 - shakeVectorChangeRate) * state.shake.z + shakeVectorChangeRate * ((float)Math.random() - 0.5f);
			state.shake.nor();
			state.shakeRot = (1 - shakeVectorChangeRate) * state.shakeRot + shakeVectorChangeRate * ((float)Math.random() - 0.5f);
			
			float s = 0;
			float engineOutput = motorcycle.getEngineOutputPercentage();
			if (motorcycle.state.isStandBy) s = 0.05f;
			else if (engineOutput > state.motorcycleEngineOutput) s = 1 - engineOutput;
			else if (engineOutput < state.motorcycleEngineOutput) s = engineOutput;
			
			final float shakenessChangeRate = 0.05f;
			final float shakeFactor = 1;
			state.shakeness = state.shakeness * (1 - shakenessChangeRate) + s * shakenessChangeRate;
			tmpVec5.set(state.shake).mul(state.shakeness * shakeFactor);
			
			final float distanceChangeRate = 0.05f;
			float distanceTarget = 0;
			if (engineOutput > state.motorcycleEngineOutput) distanceTarget = 1;
			else if (engineOutput < state.motorcycleEngineOutput) distanceTarget = -1;
			state.distance = state.distance * (1 - distanceChangeRate) + distanceTarget * distanceChangeRate;
			
			if (state.vanishingPointNeedReset) {
				state.vanishingPointNeedReset = false;
				state.vanishingPoint.set(0, 0, 10).mul(motorcycle.state.pos);
				state.vanishingPoint.y = 0;
				state.vanishingPointSmoothed.set(state.vanishingPoint);
			} else {
				tmpVec.set(state.vanishingPoint).sub(state.vanishingPointSmoothed);
				tmpVec.y = 0;
				float stepFullLen = tmpVec.len();
				if (stepFullLen > 0) {
					float step1 = motorcycle.state.bikeVelo.len() * 2;
					float step2 = delta * tmpVec2.set(state.vanishingPoint).sub(state.vanishingPointSmoothed).len();
					float step = Math.max(step1, step2);
					if (step < stepFullLen) {
						tmpVec.div(stepFullLen).mul(step);
						state.vanishingPointSmoothed.x += tmpVec.x;
						state.vanishingPointSmoothed.z += tmpVec.z;
					} else {
						state.vanishingPointSmoothed.x = state.vanishingPoint.x;
						state.vanishingPointSmoothed.z = state.vanishingPoint.z;
					}
				}
			}
			
			final float motorcycleCounterSteeringShiftDecayFactor = 1.5f;
			state.motorcycleCounterSteeringShift += motorcycle.state.latestCounterSteeringPositionShift;
			state.motorcycleCounterSteeringShift -=
					state.motorcycleCounterSteeringShift *
					motorcycleCounterSteeringShiftDecayFactor * delta;
			
			final float vanishingPointLookingChangeRate = 0.01f;
			state.vanishingPointLookingFactor = (1 - vanishingPointLookingChangeRate) * state.vanishingPointLookingFactor + vanishingPointLookingChangeRate * state.vanishingPointLookingFactorTarget;
			
			motorcycle.state.pos.getTranslation(tmpVec);
			tmpVec.y = 0;
			tmpVec2.set(0, 1, 0);
			tmpVec6.set(state.vanishingPointSmoothed).sub(tmpVec).mul(-1).add(tmpVec);
			tmpMat2.setToLookAt(tmpVec, tmpVec6, tmpVec2).inv();
			tmpMat2.getRotation(tmpQua2);
			motorcycle.state.pos.getRotation(tmpQua);
			tmpQua.slerp(tmpQua2, state.vanishingPointLookingFactor).nor();
			tmpVec.set(state.motorcycleCounterSteeringShift, 5, 0).mul(motorcycle.state.pos);
			tmpMat2.idt().set(tmpQua).trn(tmpVec);
			tmpVec.set(0, 0, -(12  + state.distance)).add(tmpVec5).mul(tmpMat2);
			tmpVec2.set(state.motorcycleCounterSteeringShift, 5, 0).mul(motorcycle.state.pos);
			
			position.set(tmpVec);
			tmpMat2.idt().rotate(0, 0, 1, state.shakeRot * state.shakeness * shakeRotFactor);
			up.set(0, 1, 0).mul(tmpMat2);
			lookAt(tmpVec2.x, tmpVec2.y, tmpVec2.z);
			
			state.motorcycleEngineOutput = engineOutput;
		} else {
			motorcycle.getTrackeePos(tmpVec);
			motorcycle.rider.getTrackeePos(tmpVec2);
			tmpVec.add(tmpVec2).mul(0.5f);
			this.lookAt(tmpVec.x, 5, tmpVec.z);
			
			state.vanishingPointNeedReset = true;
			state.motorcycleCounterSteeringShift = 0;
			state.vanishingPointLookingFactor = state.vanishingPointLookingFactorTarget;
			state.shakeRot = 0;
			state.shakeness = 0;
			state.distance = 0;
			state.motorcycleEngineOutput = 0;
		}
	}
	
	private Matrix4 tmpMat = new Matrix4();
	private Vector3 tmpVec3 = new Vector3();
	private Vector3 tmpVec4 = new Vector3();
	public void alignCameraWithDevice() {
		float deviceRot = uiHelper.getDeviceRotation();
		tmpVec3.set(0, 0, 0);
		tmpVec4.set(0, 1, 0);
		tmpMat.setToLookAt(tmpVec3, direction, tmpVec4).inv();
		tmpMat.rotate(0, 0, 1, -deviceRot +
				(motorcycle.state.isCrashed ? 0 : state.shakeRot * state.shakeness * shakeRotFactor));
		up.set(0, 1, 0).mul(tmpMat);
	}

	public void updateVanishingPoint(Vector3 vec) {
		if (vec != null) state.vanishingPoint.set(vec);
	}
	
	public void setVanishingPointLookingFactor(float f) {
		state.vanishingPointLookingFactorTarget = f;
	}
	
	private float getVanishingPointLookingFactorForDirection(TrackDirection td) {
		if (td == null) return 0;
		switch (td) {
		case LeftMed:
		case RightMed:
			return 1/6f;
		case LeftHigh:
		case RightHigh:
			return 1/4f;
		case LeftSharp:
		case RightSharp:
			return 1/3f;
		default:
			return 0;
		}
	}
	
	public void setVanishingPointLookingFactor(TrackDirection td, float scale) {
		float f = getVanishingPointLookingFactorForDirection(td) * scale;
		setVanishingPointLookingFactor(f);
	}
}
