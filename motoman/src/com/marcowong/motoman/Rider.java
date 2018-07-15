package com.marcowong.motoman;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.marcowong.motoman.track.TrackDirection;
import com.marcowong.motoman.track.TrackSegment;

public class Rider implements ITrackee {
	private final static int skeHead = 0;
	private final static int skeChest = 1;
	private final static int skeWaist = 2;
	private final static int skeHip = 3;
	private final static int skeArmUpperL = 4;
	private final static int skeArmLowerL = 5;
	private final static int skeArmUpperR = 6;
	private final static int skeArmLowerR = 7;
	private final static int skeLegUpperL = 8;
	private final static int skeLegLowerL = 9;
	private final static int skeLegUpperR = 10;
	private final static int skeLegLowerR = 11;
	
	private final static Vector3 posHip = new Vector3(0, 0, 0);
	private final static Vector3 posWaist = new Vector3(0, 1.05f, 0.78f);
	private final static Vector3 posChest = new Vector3(0, 1.68f, 1.49f);
	private final static Vector3 posHead = new Vector3(0, 2.45f, 2.41f);
	private final static Vector3 posArmRoot = new Vector3(1, 2.1f, 2.03f);
	private final static Vector3 posArmUpper = new Vector3(1.19f, 0.68f, 3.24f);
	private final static Vector3 posLegRoot = new Vector3(0.5f, -0.1f, -0.14f);
	private final static Vector3 posLegUpper = new Vector3(0.75f, -1.63f, 2.27f);
	
	private final static Vector3 offHip = new Vector3(posHip);
	private final static Vector3 offWaist = new Vector3(posWaist).sub(posHip);
	private final static Vector3 offChest = new Vector3(posChest).sub(posWaist);
	private final static Vector3 offHead = new Vector3(posHead).sub(posChest);
	private final static Vector3 offArmUpperR = new Vector3(-posArmRoot.x, posArmRoot.y, posArmRoot.z).sub(posChest);
	private final static Vector3 offArmLowerR = new Vector3(-posArmUpper.x, posArmUpper.y, posArmUpper.z).sub(-posArmRoot.x, posArmRoot.y, posArmRoot.z);
	private final static Vector3 offArmUpperL = new Vector3(posArmRoot).sub(posChest);
	private final static Vector3 offArmLowerL = new Vector3(posArmUpper).sub(posArmRoot);
	private final static Vector3 offLegUpperR = new Vector3(-posLegRoot.x, posLegRoot.y, posLegRoot.z).sub(posHip);
	private final static Vector3 offLegLowerR = new Vector3(-posLegUpper.x, posLegUpper.y, posLegUpper.z).sub(-posLegRoot.x, posLegRoot.y, posLegRoot.z);
	private final static Vector3 offLegUpperL = new Vector3(posLegRoot).sub(posHip);
	private final static Vector3 offLegLowerL = new Vector3(posLegUpper).sub(posLegRoot);
	
	private Matrix4 dynHip = new Matrix4();
	private Matrix4 dynWaist = new Matrix4();
	private Matrix4 dynChest = new Matrix4();
	private Matrix4 dynHead = new Matrix4();
	private Matrix4 dynArmUpperL = new Matrix4();
	private Matrix4 dynArmLowerL = new Matrix4();
	private Matrix4 dynArmUpperR = new Matrix4();
	private Matrix4 dynArmLowerR = new Matrix4();
	private Matrix4 dynLegUpperL = new Matrix4();
	private Matrix4 dynLegLowerL = new Matrix4();
	private Matrix4 dynLegUpperR = new Matrix4();
	private Matrix4 dynLegLowerR = new Matrix4();
	
	public Motorcycle motorcycle;
	private Track track;
	private Pose poseStandBy = null;
	private Pose poseGo = null;
	
	private FloatBuffer modelSkeMatsFBuf;
	
	public class UpdateState {
		public TrackSegment lastTrackSegment;
		public boolean attached = true;
		public Vector3 detachedVelo = new Vector3();
		public Matrix4 detachedPos = new Matrix4();
		public float strength = 1;
		public Pose pose = new Pose();
		public Pose poseSrc = new Pose();
		public Pose poseDst = null;
		public Pose poseDst2 = null;
		public float poseTime = 0;
		public float poseTimeRemaining = 0;
		public int directionCurrent = 0;
		public int directionNoticed = 0;
		public float bodyShiftTarget = 0;
		public float bodyExposeTarget = 1;
		public float leanReadingSmoothed = 0;
		
		public void copyTo(UpdateState s) {
			s.lastTrackSegment = lastTrackSegment;
			s.attached = attached;
			s.detachedVelo.set(detachedVelo);
			s.detachedPos.set(detachedPos);
			s.strength = strength;
			s.pose.set(pose);
			s.poseSrc.set(poseSrc);
			s.poseDst = poseDst;
			s.poseDst2 = poseDst2;
			s.poseTime = poseTime;
			s.poseTimeRemaining = poseTimeRemaining;
			s.directionCurrent = directionCurrent;
			s.directionNoticed = directionNoticed;
			s.bodyShiftTarget = bodyShiftTarget;
			s.bodyExposeTarget = bodyExposeTarget;
			s.leanReadingSmoothed = leanReadingSmoothed;
		}
	}
	public UpdateState statePersist = new UpdateState();
	public UpdateState stateTmp = new UpdateState();
	public UpdateState state = statePersist;
	
	private static class Pose {
		public Matrix4 matHip = new Matrix4();
		public Matrix4 matWaist = new Matrix4();
		public Matrix4 matChest = new Matrix4();
		public Matrix4 matHead = new Matrix4();
		public Matrix4 matArmUpperL = new Matrix4();
		public Matrix4 matArmLowerL = new Matrix4();
		public Matrix4 matArmUpperR = new Matrix4();
		public Matrix4 matArmLowerR = new Matrix4();
		public Matrix4 matLegUpperL = new Matrix4();
		public Matrix4 matLegLowerL = new Matrix4();
		public Matrix4 matLegUpperR = new Matrix4();
		public Matrix4 matLegLowerR = new Matrix4();
		
		public void set(Pose p) {
			matHip.set(p.matHip);
			matWaist.set(p.matWaist);
			matChest.set(p.matChest);
			matHead.set(p.matHead);
			matArmUpperL.set(p.matArmUpperL);
			matArmLowerL.set(p.matArmLowerL);
			matArmUpperR.set(p.matArmUpperR);
			matArmLowerR.set(p.matArmLowerR);
			matLegUpperL.set(p.matLegUpperL);
			matLegLowerL.set(p.matLegLowerL);
			matLegUpperR.set(p.matLegUpperR);
			matLegLowerR.set(p.matLegLowerR);
		}
	}
	
	private static void rotMatrix(Matrix4 m, float x, float y, float z) {
		m.rotate(1, 0, 0, x);
		m.rotate(0, 1, 0, y);
		m.rotate(0, 0, 1, z);
	}
	
	private static void rotMatrix(Matrix4 l, Matrix4 r, float x, float y, float z) {
		l.rotate(1, 0, 0, x);
		l.rotate(0, 1, 0, y);
		l.rotate(0, 0, 1, z);
		r.rotate(1, 0, 0, x);
		r.rotate(0, 1, 0, -y);
		r.rotate(0, 0, 1, -z);
	}
	
	private static Quaternion tmpQua3 = new Quaternion();
	private static Vector3 tmpVec3 = new Vector3();
	private static void mirrorMatrix(Matrix4 m, Matrix4 m2) {
		m.idt();
		m2.getRotation(tmpQua3);
		m2.getTranslation(tmpVec3);
		tmpQua3.x = -tmpQua3.x;
		tmpQua3.w = -tmpQua3.w;
		tmpVec3.x = -tmpVec3.x;
		m.set(tmpQua3);
		m.trn(tmpVec3);
	}
	
	private static void mirrorPose(Pose p, Pose p2) {
		mirrorMatrix(p.matHead, p2.matHead);
		mirrorMatrix(p.matChest, p2.matChest);
		mirrorMatrix(p.matWaist, p2.matWaist);
		mirrorMatrix(p.matHip, p2.matHip);
		mirrorMatrix(p.matArmUpperL, p2.matArmUpperR);
		mirrorMatrix(p.matArmLowerL, p2.matArmLowerR);
		mirrorMatrix(p.matArmUpperR, p2.matArmUpperL);
		mirrorMatrix(p.matArmLowerR, p2.matArmLowerL);
		mirrorMatrix(p.matLegUpperL, p2.matLegUpperR);
		mirrorMatrix(p.matLegLowerL, p2.matLegLowerR);
		mirrorMatrix(p.matLegUpperR, p2.matLegUpperL);
		mirrorMatrix(p.matLegLowerR, p2.matLegLowerL);
	}
	
	private Quaternion tmpQua = new Quaternion();
	private Quaternion tmpQua2 = new Quaternion();
	private Vector3 tmpVec = new Vector3();
	private Vector3 tmpVec2 = new Vector3();
	private void lerpMatrix(Matrix4 m, Matrix4 a, Matrix4 b, float d) {
		m.idt();
		a.getRotation(tmpQua);
		b.getRotation(tmpQua2);
		a.getTranslation(tmpVec);
		b.getTranslation(tmpVec2);
		tmpQua.slerp(tmpQua2, d);
		tmpVec.lerp(tmpVec2, d);
		m.set(tmpQua);
		m.trn(tmpVec);
	}
	
	private void lerpPose(Pose p, Pose a, Pose b, float d) {
		lerpMatrix(p.matHead, a.matHead, b.matHead, d);
		lerpMatrix(p.matChest, a.matChest, b.matChest, d);
		lerpMatrix(p.matWaist, a.matWaist, b.matWaist, d);
		lerpMatrix(p.matHip, a.matHip, b.matHip, d);
		lerpMatrix(p.matArmUpperL, a.matArmUpperL, b.matArmUpperL, d);
		lerpMatrix(p.matArmLowerL, a.matArmLowerL, b.matArmLowerL, d);
		lerpMatrix(p.matArmUpperR, a.matArmUpperR, b.matArmUpperR, d);
		lerpMatrix(p.matArmLowerR, a.matArmLowerR, b.matArmLowerR, d);
		lerpMatrix(p.matLegUpperL, a.matLegUpperL, b.matLegUpperL, d);
		lerpMatrix(p.matLegLowerL, a.matLegLowerL, b.matLegLowerL, d);
		lerpMatrix(p.matLegUpperR, a.matLegUpperR, b.matLegUpperR, d);
		lerpMatrix(p.matLegLowerR, a.matLegLowerR, b.matLegLowerR, d);
	}
	
	private static Pose centerBrakingPose = new Pose();
	private static Pose leftBrakingPose = new Pose();
	private static Pose leftBrakingFootDraggingPose = new Pose();
	private static Pose leftBrakingKneeDraggingPose = new Pose();
	private static Pose rightBrakingPose = new Pose();
	private static Pose rightBrakingFootDraggingPose = new Pose();
	private static Pose rightBrakingKneeDraggingPose = new Pose();
	private static Pose leftBrakingLeanedPose = new Pose();
	private static Pose leftBrakingKneeDraggingLeanedPose = new Pose();
	private static Pose leftBrakingFootDraggingLeanedPose = new Pose();
	private static Pose rightBrakingLeanedPose = new Pose();
	private static Pose rightBrakingFootDraggingLeanedPose = new Pose();
	private static Pose rightBrakingKneeDraggingLeanedPose = new Pose();
	private static Pose centerThottlingPose = new Pose();
	private static Pose leftThottlingPose = new Pose();
	private static Pose leftThottlingKneeDraggingPose = new Pose();
	private static Pose rightThottlingPose = new Pose();
	private static Pose rightThottlingKneeDraggingPose = new Pose();
	private static Pose leftThottlingLeanedPose = new Pose();
	private static Pose leftThottlingKneeDraggingLeanedPose = new Pose();
	private static Pose rightThottlingLeanedPose = new Pose();
	private static Pose rightThottlingKneeDraggingLeanedPose = new Pose();
	private static Pose crashedPose = new Pose();
	private static Pose standByPose = new Pose();
	
	private static Model model;
	private static IMeshContext modelMeshContext;
	private static Matrix4 pos = new Matrix4();
	private static Matrix4 scaleMat = new Matrix4();
	private static float detachedHeight;
	
	public static void initResource() {
		model = new ObjLoaderEx().loadObj(Gdx.files.internal("data/rider.obj"), true);
		Pixmap riderSkeletonMapping = new Pixmap(Gdx.files.internal("data/rider.skeleton.png"));
		new ObjLoaderSkeletonPatcher().patch(model, riderSkeletonMapping);
		riderSkeletonMapping.dispose();
		StaticModelTextureFilterConfigManager.add(model);
		modelMeshContext = MeshOptimized.globalStaticMesh.add(model);
		
		pos.translate(0, 0.35f, -0.05f);
		scaleMat.scale(0.55f, 0.55f, 0.55f);
		
		detachedHeight = 0.25f;
		
		rotMatrix(centerBrakingPose.matHip, -5, 0, 0);
		rotMatrix(centerBrakingPose.matWaist, 25, 0, 0);
		rotMatrix(centerBrakingPose.matChest, 10, 0, 0);
		rotMatrix(centerBrakingPose.matHead, -55, 0, 0);
		rotMatrix(centerBrakingPose.matArmUpperL, centerBrakingPose.matArmUpperR, -15, 5, 0);
		rotMatrix(centerBrakingPose.matArmLowerL, centerBrakingPose.matArmLowerR, -15, 0, 0);
		rotMatrix(centerBrakingPose.matLegUpperL, centerBrakingPose.matLegUpperR, -10, 10, 10);
		
		rotMatrix(centerThottlingPose.matHip, 15, 0, 0);
		rotMatrix(centerThottlingPose.matWaist, 25, 0, 0);
		rotMatrix(centerThottlingPose.matChest, 10, 0, 0);
		rotMatrix(centerThottlingPose.matHead, -65, 0, 0);
		rotMatrix(centerThottlingPose.matArmUpperL, centerThottlingPose.matArmUpperR, -15, 5, -5);
		rotMatrix(centerThottlingPose.matArmLowerL, centerThottlingPose.matArmLowerR, -70, 0, 0);
		rotMatrix(centerThottlingPose.matLegUpperL, centerThottlingPose.matLegUpperR, -30, 10, 10);
		
		leftBrakingPose.matHip.translate(1, 0, 0.25f);
		rotMatrix(leftBrakingPose.matHip, -5, 0, 0);
		rotMatrix(leftBrakingPose.matWaist, 25, -35, 0);
		rotMatrix(leftBrakingPose.matChest, 10, 0, 10);
		rotMatrix(leftBrakingPose.matHead, -55, 25, 15);
		rotMatrix(leftBrakingPose.matArmUpperR, -32.5f, 30, 0);
		rotMatrix(leftBrakingPose.matArmLowerR, 20, 0, 0);
		rotMatrix(leftBrakingPose.matArmUpperL, 30, 30, 0);
		rotMatrix(leftBrakingPose.matArmLowerL, -50, 0, 0);
		rotMatrix(leftBrakingPose.matLegUpperR, -20, -45, -30);
		rotMatrix(leftBrakingPose.matLegLowerR, -10, 0, 0);
		rotMatrix(leftBrakingPose.matLegUpperL, 0, -5, -10);
		mirrorPose(rightBrakingPose, leftBrakingPose);
		
		leftBrakingLeanedPose.set(leftBrakingPose);
		rotMatrix(leftBrakingLeanedPose.matWaist, 0, 35, 0);
		rotMatrix(leftBrakingLeanedPose.matChest, 0, 0, -10);
		rotMatrix(leftBrakingLeanedPose.matHead, 0, -25, -15);
		rotMatrix(leftBrakingLeanedPose.matArmUpperR, 50, -10, -60);
		rotMatrix(leftBrakingLeanedPose.matArmLowerR, -30, 0, 0);
		rotMatrix(leftBrakingLeanedPose.matArmUpperL, -40, -40, 0);
		rotMatrix(leftBrakingLeanedPose.matArmLowerL, 15, 0, 0);
		mirrorPose(rightBrakingLeanedPose, leftBrakingLeanedPose);
		
		leftBrakingFootDraggingPose.set(leftBrakingPose);
		rotMatrix(leftBrakingFootDraggingPose.matLegUpperL, 20, 50, 0);
		rotMatrix(leftBrakingFootDraggingPose.matLegLowerL, -90, 0, 0);
		mirrorPose(rightBrakingFootDraggingPose, leftBrakingFootDraggingPose);
		
		leftBrakingFootDraggingLeanedPose.set(leftBrakingFootDraggingPose);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matWaist, 0, 35, 0);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matChest, 0, 0, -10);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matHead, 0, -25, -15);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matArmUpperR, 50, -10, -60);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matArmLowerR, -30, 0, 0);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matArmUpperL, -40, -40, 0);
		rotMatrix(leftBrakingFootDraggingLeanedPose.matArmLowerL, 15, 0, 0);
		mirrorPose(rightBrakingFootDraggingLeanedPose, leftBrakingFootDraggingLeanedPose);
		
		leftBrakingKneeDraggingPose.set(leftBrakingPose);
		rotMatrix(leftBrakingKneeDraggingPose.matLegUpperL, -10, 50, 0);
		rotMatrix(leftBrakingKneeDraggingPose.matLegLowerL, 10, 0, 0);
		mirrorPose(rightBrakingKneeDraggingPose, leftBrakingKneeDraggingPose);
		
		leftBrakingKneeDraggingLeanedPose.set(leftBrakingKneeDraggingPose);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matWaist, 0, 35, 0);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matChest, 0, 0, -10);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matHead, 0, -25, -15);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matArmUpperR, 50, -10, -60);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matArmLowerR, -30, 0, 0);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matArmUpperL, -40, -40, 0);
		rotMatrix(leftBrakingKneeDraggingLeanedPose.matArmLowerL, 15, 0, 0);
		mirrorPose(rightBrakingKneeDraggingLeanedPose, leftBrakingKneeDraggingLeanedPose);
		
		leftThottlingPose.matHip.translate(1, 0, 0.25f);
		rotMatrix(leftThottlingPose.matHip, 15, 0, 0);
		rotMatrix(leftThottlingPose.matWaist, 25, -35, 0);
		rotMatrix(leftThottlingPose.matChest, 10, 0, 10);
		rotMatrix(leftThottlingPose.matHead, -55, 25, 15);
		rotMatrix(leftThottlingPose.matArmUpperR, -30, 30, 0);
		rotMatrix(leftThottlingPose.matArmLowerR, -35, 0, 0);
		rotMatrix(leftThottlingPose.matArmUpperL, 20, 40, -20);
		rotMatrix(leftThottlingPose.matArmLowerL, -90, 0, 0);
		rotMatrix(leftThottlingPose.matLegUpperR, -30, -25, -30);
		rotMatrix(leftThottlingPose.matLegLowerR, -10, 0, 0);
		rotMatrix(leftThottlingPose.matLegUpperL, -20, -5, -10);
		mirrorPose(rightThottlingPose, leftThottlingPose);
		
		leftThottlingLeanedPose.set(leftThottlingPose);
		rotMatrix(leftThottlingLeanedPose.matWaist, 0, 35, 0);
		rotMatrix(leftThottlingLeanedPose.matChest, 0, 0, -10);
		rotMatrix(leftThottlingLeanedPose.matHead, 0, -25, -15);
		rotMatrix(leftThottlingLeanedPose.matArmUpperR, 30, -50, -40);
		rotMatrix(leftThottlingLeanedPose.matArmLowerR, -30, 0, 0);
		rotMatrix(leftThottlingLeanedPose.matArmUpperL, -20, -60, 20);
		rotMatrix(leftThottlingLeanedPose.matArmLowerL, 20, 0, 0);
		mirrorPose(rightThottlingLeanedPose, leftThottlingLeanedPose);
		
		leftThottlingKneeDraggingPose.set(leftThottlingPose);
		rotMatrix(leftThottlingKneeDraggingPose.matLegUpperL, -10, 50, 0);
		rotMatrix(leftThottlingKneeDraggingPose.matLegLowerL, 10, 0, 0);
		mirrorPose(rightThottlingKneeDraggingPose, leftThottlingKneeDraggingPose);
		
		leftThottlingKneeDraggingLeanedPose.set(leftThottlingKneeDraggingPose);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matWaist, 0, 35, 0);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matChest, 0, 0, -10);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matHead, 0, -25, -15);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matArmUpperR, 30, -50, -40);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matArmLowerR, -30, 0, 0);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matArmUpperL, -20, -60, 20);
		rotMatrix(leftThottlingKneeDraggingLeanedPose.matArmLowerL, 20, 0, 0);
		mirrorPose(rightThottlingKneeDraggingLeanedPose, leftThottlingKneeDraggingLeanedPose);
		
		rotMatrix(crashedPose.matHip, 40, 0, 0);
		rotMatrix(crashedPose.matArmUpperL, crashedPose.matArmUpperR, 70, 60, 0);
		rotMatrix(crashedPose.matArmLowerL, crashedPose.matArmLowerR, 15, 0, 0);
		rotMatrix(crashedPose.matLegUpperL, crashedPose.matLegUpperR, 105, 30, 0);
		rotMatrix(crashedPose.matLegLowerL, crashedPose.matLegLowerR, -120, 0, 0);
		
		standByPose.set(centerBrakingPose);
		rotMatrix(standByPose.matLegUpperL, standByPose.matLegUpperR, 45, 15, 0);
		rotMatrix(standByPose.matLegLowerL, standByPose.matLegLowerR, -90, 0, 0);
	}
	
	public Rider(Track track) {
		this.track = track;
		
		poseStandBy = standByPose;
		poseGo = centerBrakingPose;
		state.poseDst = poseStandBy;
		state.poseSrc.set(state.poseDst);
		state.pose.set(state.poseDst);
		state.poseTime = 1;
		state.poseTimeRemaining = 0;
		
		modelSkeMatsFBuf = ByteBuffer.allocateDirect(12 * 16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	private Matrix4 tmpMat = new Matrix4();
	private Matrix4 tmpMat2 = new Matrix4();
	public void render(ShaderProgram shader, Camera camera) {
		Pose pose = state.pose;
		
		dynHip.set(pose.matHip).trn(offHip);
		tmpMat.idt().trn(-posHip.x, -posHip.y, -posHip.z).mul(dynHip);
		modelSkeMatsFBuf.position(skeHip * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matWaist).trn(offWaist);
		dynWaist.set(dynHip).mul(tmpMat);
		tmpMat.set(dynWaist).translate(-posWaist.x, -posWaist.y, -posWaist.z);
		modelSkeMatsFBuf.position(skeWaist * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matChest).trn(offChest);
		dynChest.set(dynWaist).mul(tmpMat);
		tmpMat.set(dynChest).translate(-posChest.x, -posChest.y, -posChest.z);
		modelSkeMatsFBuf.position(skeChest * 16);
		modelSkeMatsFBuf.put(tmpMat.val);

		tmpMat.set(pose.matHead).trn(offHead);
		dynHead.set(dynChest).mul(tmpMat);
		tmpMat.set(dynHead).translate(-posHead.x, -posHead.y, -posHead.z);
		modelSkeMatsFBuf.position(skeHead * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matLegUpperR).trn(offLegUpperR);
		dynLegUpperR.set(dynHip).mul(tmpMat);
		tmpMat.set(dynLegUpperR).translate(posLegRoot.x, -posLegRoot.y, -posLegRoot.z);
		modelSkeMatsFBuf.position(skeLegUpperR * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matLegLowerR).trn(offLegLowerR);
		dynLegLowerR.set(dynLegUpperR).mul(tmpMat);
		tmpMat.set(dynLegLowerR).translate(posLegUpper.x, -posLegUpper.y, -posLegUpper.z);
		modelSkeMatsFBuf.position(skeLegLowerR * 16);
		modelSkeMatsFBuf.put(tmpMat.val);

		tmpMat.set(pose.matLegUpperL).trn(offLegUpperL);
		dynLegUpperL.set(dynHip).mul(tmpMat);
		tmpMat.set(dynLegUpperL).translate(-posLegRoot.x, -posLegRoot.y, -posLegRoot.z);
		modelSkeMatsFBuf.position(skeLegUpperL * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matLegLowerL).trn(offLegLowerL);
		dynLegLowerL.set(dynLegUpperL).mul(tmpMat);
		tmpMat.set(dynLegLowerL).translate(-posLegUpper.x, -posLegUpper.y, -posLegUpper.z);
		modelSkeMatsFBuf.position(skeLegLowerL * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matArmUpperR).trn(offArmUpperR);
		dynArmUpperR.set(dynChest).mul(tmpMat);
		tmpMat.set(dynArmUpperR).translate(posArmRoot.x, -posArmRoot.y, -posArmRoot.z);
		modelSkeMatsFBuf.position(skeArmUpperR * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matArmLowerR).trn(offArmLowerR);
		dynArmLowerR.set(dynArmUpperR).mul(tmpMat);
		tmpMat.set(dynArmLowerR).translate(posArmUpper.x, -posArmUpper.y, -posArmUpper.z);
		modelSkeMatsFBuf.position(skeArmLowerR * 16);
		modelSkeMatsFBuf.put(tmpMat.val);

		tmpMat.set(pose.matArmUpperL).trn(offArmUpperL);
		dynArmUpperL.set(dynChest).mul(tmpMat);
		tmpMat.set(dynArmUpperL).translate(-posArmRoot.x, -posArmRoot.y, -posArmRoot.z);
		modelSkeMatsFBuf.position(skeArmUpperL * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		tmpMat.set(pose.matArmLowerL).trn(offArmLowerL);
		dynArmLowerL.set(dynArmUpperL).mul(tmpMat);
		tmpMat.set(dynArmLowerL).translate(-posArmUpper.x, -posArmUpper.y, -posArmUpper.z);
		modelSkeMatsFBuf.position(skeArmLowerL * 16);
		modelSkeMatsFBuf.put(tmpMat.val);
		
		shader.setUniformMatrix4fv("skeletonmat", modelSkeMatsFBuf, 12, false);
		
		if (state.attached) {
			tmpMat2.set(motorcycle.state.pos);
			tmpMat2.translate(0, motorcycle.getLeanHeightShift(), 0);
			tmpMat2.mul(motorcycle.state.lean);
			tmpMat2.mul(motorcycle.ridePos);
			tmpMat2.mul(pos);
			tmpMat2.mul(scaleMat);
			
			tmpMat.set(camera.combined);
			tmpMat.mul(tmpMat2);
			shader.setUniformMatrix("modelviewproj", tmpMat);
			tmpMat.set(camera.view);
			tmpMat.mul(tmpMat2);
			shader.setUniformMatrix("modelview", tmpMat);
		} else {
			tmpMat.set(camera.combined);
			tmpMat.mul(state.detachedPos);
			tmpMat.mul(scaleMat);
			shader.setUniformMatrix("modelviewproj", tmpMat);
			tmpMat.set(camera.view);
			tmpMat.mul(state.detachedPos);
			tmpMat.mul(scaleMat);
			shader.setUniformMatrix("modelview", tmpMat);
		}
		//model.render(shader);
		modelMeshContext.render(shader);
	}
	
	private static int getDirectionNum(TrackDirection td) {
		switch (td) {
		case Straight:
		case LeftLow:
		case RightLow: return 0;
		case LeftMed:
		case LeftHigh:
		case LeftSharp: return 1;
		case RightMed:
		case RightHigh:
		case RightSharp: return -1;
		default: return 0;
		}
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
	
	private Vector3 tmpVec4 = new Vector3();
	private Vector3 tmpVec5 = new Vector3();
	private Vector3 tmpVec6 = new Vector3();
	private Quaternion tmpQua4 = new Quaternion();
	private Quaternion tmpQua5 = new Quaternion();
	private Pose leanedPose = new Pose();
	public void update(float delta) {
		float bikeThottleBrakeReading = motorcycle.getEngineAndBrakeMeter();
		float leanReading = motorcycle.getLeanMeter();
		
		if (state.attached) {
			final float strengthDecayFactor = 0.1f;
			final float strengthRecoverFactor = 0.04f;
			float strengthDecay = 0;
			if (bikeThottleBrakeReading < 0) strengthDecay = Math.max(strengthDecay, -bikeThottleBrakeReading * 0.5f);
			if (leanReading != 0) strengthDecay = Math.max(strengthDecay, Math.abs(leanReading));
			state.strength -= strengthDecay * strengthDecayFactor * delta;
			state.strength += strengthRecoverFactor * delta;
			if (state.strength > 1) state.strength = 1;
			if (state.strength < 0) state.strength = 0;
		}
		
		if (state.attached && !motorcycle.state.isStandBy) {	
			float bodyShiftTargetO = state.bodyShiftTarget;
			float bodyExposeTargetO = state.bodyExposeTarget;
			
			TrackDirection tdChange = track.getTrackeeDirectionChange(this, 1);
			if (tdChange != null) state.directionCurrent = getDirectionNum(tdChange);
			TrackDirection tdNotice = track.getTrackeeDirectionNotice(this);
			if (tdNotice != null) state.directionNoticed = getDirectionNum(tdNotice);
			if (state.directionCurrent == state.directionNoticed) {
				state.bodyShiftTarget = state.directionCurrent;
			} else if (state.directionCurrent == 0 && state.directionNoticed != 0) {
				state.bodyShiftTarget = state.directionNoticed;
			}
			
			if (bikeThottleBrakeReading > 0) {
				state.bodyExposeTarget = 0;
			} else if (bikeThottleBrakeReading < 0) {
				state.bodyExposeTarget = 1;
			} else {
				if (state.bodyShiftTarget == 0) {
					state.bodyExposeTarget = 0;
				} else {
					state.bodyExposeTarget = 1;
				}
			}
			
			if (bodyShiftTargetO != state.bodyShiftTarget ||
				bodyExposeTargetO != state.bodyExposeTarget) {
				if (state.bodyShiftTarget < 0) {
					if (state.poseDst != rightBrakingPose &&
						state.poseDst != rightBrakingKneeDraggingPose &&
						state.poseDst != rightThottlingPose &&
						state.poseDst != rightThottlingKneeDraggingPose)
						runToPose(state.bodyExposeTarget == 0 ? rightThottlingPose : rightBrakingPose, 0.5f);
					else if (state.bodyExposeTarget == 1) {
						if (state.poseDst == rightThottlingPose)
							runToPose(rightBrakingPose, 0.5f);
						if (state.poseDst == rightThottlingKneeDraggingPose)
							runToPose(rightBrakingKneeDraggingPose, 0.5f);
					}
					else if (state.bodyExposeTarget == 0) {
						if (state.poseDst == rightBrakingPose)
							runToPose(rightThottlingPose, 0.5f);
						if (state.poseDst == rightBrakingKneeDraggingPose)
							runToPose(rightThottlingKneeDraggingPose, 0.5f);
					}
				} else if (state.bodyShiftTarget > 0) {
					if (state.poseDst != leftBrakingPose &&
						state.poseDst != leftBrakingKneeDraggingPose &&
						state.poseDst != leftThottlingPose &&
						state.poseDst != leftThottlingKneeDraggingPose)
						runToPose(state.bodyExposeTarget == 0 ? leftThottlingPose : leftBrakingPose, 0.5f);
					else if (state.bodyExposeTarget == 1) {
						if (state.poseDst == leftThottlingPose)
							runToPose(leftBrakingPose, 0.5f);
						if (state.poseDst == leftThottlingKneeDraggingPose)
							runToPose(leftBrakingKneeDraggingPose, 0.5f);
					}
					else if (state.bodyExposeTarget == 0) {
						if (state.poseDst == leftBrakingPose)
							runToPose(leftThottlingPose, 0.5f);
						if (state.poseDst == leftBrakingKneeDraggingPose)
							runToPose(leftThottlingKneeDraggingPose, 0.5f);
					}
				} else {
					if (state.poseDst != centerBrakingPose &&
						state.poseDst != centerThottlingPose)
						runToPose(state.bodyExposeTarget == 0 ? centerThottlingPose : centerBrakingPose, 0.5f);
					else if (state.bodyExposeTarget == 0 &&
							state.poseDst != centerThottlingPose)
						runToPose(centerThottlingPose, 0.5f);
					else if (state.bodyExposeTarget == 1 &&
							state.poseDst != centerBrakingPose)
						runToPose(centerBrakingPose, 0.5f);
				}
			}
			
			final float footDraggingAngle = 15;
			if (state.poseTimeRemaining == 0) {
				if (state.poseDst == leftBrakingPose && motorcycle.state.leanAngle <= 0) {
					if (motorcycle.state.leanAngle > -footDraggingAngle && state.directionCurrent == 0)
						runToPose(leftBrakingFootDraggingPose, 0.5f);
					else
						runToPose(leftBrakingKneeDraggingPose, 0.25f);
				}
				if (state.poseDst == leftThottlingPose && motorcycle.state.leanAngle <= 0)
					runToPose(leftThottlingKneeDraggingPose, 0.25f);
				if (state.poseDst == rightBrakingPose && motorcycle.state.leanAngle >= 0) {
					if (motorcycle.state.leanAngle < footDraggingAngle && state.directionCurrent == 0)
						runToPose(rightBrakingFootDraggingPose, 0.5f);
					else
						runToPose(rightBrakingKneeDraggingPose, 0.25f);
				}
				if (state.poseDst == rightThottlingPose && motorcycle.state.leanAngle >= 0)
					runToPose(rightThottlingKneeDraggingPose, 0.25f);
			}
			if (state.poseDst == leftBrakingFootDraggingPose && !(motorcycle.state.leanAngle > -footDraggingAngle && state.directionCurrent == 0))
				runToPose(leftBrakingKneeDraggingPose, 0.25f);
			if (state.poseDst == rightBrakingFootDraggingPose && !(motorcycle.state.leanAngle < footDraggingAngle && state.directionCurrent == 0))
				runToPose(rightBrakingKneeDraggingPose, 0.25f);
			
			if (state.poseDst == leftBrakingPose ||
				state.poseDst == leftBrakingFootDraggingPose ||
				state.poseDst == leftBrakingKneeDraggingPose ||
				state.poseDst == leftThottlingPose ||
				state.poseDst == leftThottlingKneeDraggingPose ||
				state.poseDst == rightBrakingPose ||
				state.poseDst == rightBrakingFootDraggingPose ||
				state.poseDst == rightBrakingKneeDraggingPose ||
				state.poseDst == rightThottlingPose ||
				state.poseDst == rightThottlingKneeDraggingPose) {
				final float leanReadingSmoothFactor = 5;
				float leanReadingSmoothFactor1 = Math.min(1, leanReadingSmoothFactor * delta);
				state.leanReadingSmoothed = (1 - leanReadingSmoothFactor1) * state.leanReadingSmoothed + leanReadingSmoothFactor1 * leanReading;
				state.poseDst2 = null;
				if (state.leanReadingSmoothed <= 0) {
					if (state.poseDst == leftBrakingPose) { lerpPose(leanedPose, leftBrakingPose, leftBrakingLeanedPose, -state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == leftBrakingFootDraggingPose) { lerpPose(leanedPose, leftBrakingFootDraggingPose, leftBrakingFootDraggingLeanedPose, -state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == leftBrakingKneeDraggingPose) { lerpPose(leanedPose, leftBrakingKneeDraggingPose, leftBrakingKneeDraggingLeanedPose, -state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == leftThottlingPose) { lerpPose(leanedPose, leftThottlingPose, leftThottlingLeanedPose, -state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == leftThottlingKneeDraggingPose) { lerpPose(leanedPose, leftThottlingKneeDraggingPose, leftThottlingKneeDraggingLeanedPose, -state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
				}
				if (state.leanReadingSmoothed >= 0) {
					if (state.poseDst == rightBrakingPose) { lerpPose(leanedPose, rightBrakingPose, rightBrakingLeanedPose, state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == rightBrakingFootDraggingPose) { lerpPose(leanedPose, rightBrakingFootDraggingPose, rightBrakingFootDraggingLeanedPose, state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == rightBrakingKneeDraggingPose) { lerpPose(leanedPose, rightBrakingKneeDraggingPose, rightBrakingKneeDraggingLeanedPose, state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == rightThottlingPose) { lerpPose(leanedPose, rightThottlingPose, rightThottlingLeanedPose, state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
					if (state.poseDst == rightThottlingKneeDraggingPose) { lerpPose(leanedPose, rightThottlingKneeDraggingPose, rightThottlingKneeDraggingLeanedPose, state.leanReadingSmoothed); state.poseDst2 = leanedPose; }
				}
			} else {
				state.leanReadingSmoothed = 0;
				state.poseDst2 = null;
			}
			
			if (state.poseDst == poseStandBy) {
				state.poseDst2 = null;
				runToPose(poseGo, 0.25f);
			}
		} else if (state.attached && motorcycle.state.isStandBy) {
			if (state.poseDst != poseStandBy) {
				state.poseDst2 = null;
				runToPose(poseStandBy, 0.25f);
			}
		} else {
			final float detachedVeloDecay = 1f;
			state.detachedVelo.sub(
					state.detachedVelo.x * detachedVeloDecay * delta,
					state.detachedVelo.y * detachedVeloDecay * delta,
					state.detachedVelo.z * detachedVeloDecay * delta);
			state.detachedPos.trn(state.detachedVelo);
			if (!track.isInsideTrack(this) &&
				track.getTrackCollisionVector(this, tmpVec4, tmpVec5)) {
				state.detachedPos.getTranslation(tmpVec6);
				state.detachedPos.trn(tmpVec4.sub(tmpVec6));
				state.detachedVelo.sub(tmpVec5.mul(2 * tmpVec5.dot(state.detachedVelo))).mul(0.5f);
				motorcycle.sfx.playCrashSound();
			}
			state.detachedPos.getTranslation(tmpVec4);
			state.detachedPos.trn(0, detachedVeloDecay * delta * (detachedHeight - tmpVec4.y), 0);
			state.detachedPos.getRotation(tmpQua4);
			tmpQua5.setEulerAngles(0, 0, 0);
			tmpQua4.slerp(tmpQua5, 0.1f);
			state.detachedPos.getTranslation(tmpVec4);
			state.detachedPos.set(tmpQua4).trn(tmpVec4);
		}
		
		updatePose(delta);
	}
	
	private void runToPose(Pose targetPose, float t) {
		state.poseSrc.set(state.pose);
		state.poseDst = targetPose;
		state.poseTime = t;
		state.poseTimeRemaining = t;
	}
	
	private void updatePose(float delta) {
		if (state.poseTimeRemaining > 0) {
			state.poseTimeRemaining -= delta;
			if (state.poseTimeRemaining <= 0) {
				state.poseTimeRemaining = 0;
			}
		}
		
		if (state.poseDst2 == null)
			lerpPose(state.pose, state.poseDst, state.poseSrc, state.poseTimeRemaining / state.poseTime);
		else
			lerpPose(state.pose, state.poseDst2, state.poseSrc, state.poseTimeRemaining / state.poseTime);
	}
	
	public boolean isKneeDragging() {
		return
			Math.abs(motorcycle.state.leanAngle) >= 45 &&
			(state.poseDst == leftThottlingKneeDraggingPose ||
			state.poseDst == rightThottlingKneeDraggingPose ||
			state.poseDst == leftBrakingKneeDraggingPose ||
			state.poseDst == rightBrakingKneeDraggingPose) &&
			state.poseTimeRemaining <= 0;
	}
	
	@Override
	public void getTrackeePos(Vector3 vec) {
		if (state.attached) {
			state.detachedPos.set(motorcycle.state.pos);
			state.detachedPos.translate(0, motorcycle.getLeanHeightShift(), 0);
			state.detachedPos.mul(motorcycle.state.lean);
			state.detachedPos.mul(motorcycle.ridePos);
			state.detachedPos.mul(pos);
			state.detachedPos.getTranslation(vec);
		} else
			state.detachedPos.getTranslation(vec);
	}
	
	@Override
	public void setLastTrackSegment(TrackSegment ts) {
		state.lastTrackSegment = ts;
	}
	
	@Override
	public TrackSegment getLastTrackSegment() {
		return state.lastTrackSegment;
	}
	
	public void attach() {
		state.attached = true;
		state.poseDst2 = null;
		state.poseSrc.set(poseStandBy);
		state.pose.set(poseStandBy);
		runToPose(poseStandBy, Float.MIN_VALUE);
		
		state.directionCurrent = 0;
		state.directionNoticed = 0;
		state.bodyShiftTarget = 0;
		state.bodyExposeTarget = 1;
		state.leanReadingSmoothed = 0;
	}
	
	public void detach() {
		state.poseDst2 = null;
		runToPose(crashedPose, 0.5f);
		
		state.attached = false;
		state.detachedPos.set(motorcycle.state.pos);
		state.detachedPos.translate(0, motorcycle.getLeanHeightShift(), 0);
		state.detachedPos.mul(motorcycle.state.lean);
		state.detachedPos.mul(motorcycle.ridePos);
		state.detachedPos.mul(pos);
		state.detachedVelo.set(motorcycle.state.bikeVelo);
		state.detachedVelo.y = 0;
	}
	
	public float getStrength() {
		return state.strength;
	}
	
	public void setStrength(float strength) {
		this.state.strength = strength;
	}
	
	public void dispose() {
	}
}
