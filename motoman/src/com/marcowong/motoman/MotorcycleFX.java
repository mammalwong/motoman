package com.marcowong.motoman;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

public class MotorcycleFX {
	private static TextureRegion smokeTex;
	private static TextureRegion sparkTex;
	private static TextureRegion backfireTex;
	
	public static void initResource() {
		smokeTex = new TextureRegion(new Texture(Gdx.files.internal("data/smoke.png"), false));
		if (ConfigHelper.turnOnModelTextureLinearFilter())
			smokeTex.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			smokeTex.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		StaticModelTextureFilterConfigManager.add(smokeTex.getTexture());
		sparkTex = new TextureRegion(new Texture(Gdx.files.internal("data/spark.png"), false));
		if (ConfigHelper.turnOnModelTextureLinearFilter())
			sparkTex.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			sparkTex.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		StaticModelTextureFilterConfigManager.add(sparkTex.getTexture());
		backfireTex = new TextureRegion(new Texture(Gdx.files.internal("data/backfire.png"), false));
		if (ConfigHelper.turnOnModelTextureLinearFilter())
			backfireTex.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		else
			backfireTex.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		StaticModelTextureFilterConfigManager.add(backfireTex.getTexture());
	}
	
	public interface DynamicFXPosition {
		public void getPosition(Vector3 vec);
	}
	
	private Camera camera;
	private DecalBatch decalBatch;
	private Set<DecalInst> decals = new LinkedHashSet<DecalInst>();
	
	private static class DecalInst {
		public Decal decal;
		public float duration;
		public float fadeOutTime;
		public boolean rendered = false;
		public Vector3 up;
		public DynamicFXPosition dFXPos;
	}
	
	public void init(Camera camera) {
		this.camera = camera;
		decalBatch = new DecalBatch(new CameraGroupStrategy(camera));
	}
	
	private Vector3 tmpVec = new Vector3();
	private void addDecal(TextureRegion tex, float size, float duration, float fadeOutTime, float px, float py, float pz, DynamicFXPosition dFXPos) {
		size *= 0.5f + (float)Math.random();
		DecalInst inst = new DecalInst();
		inst.decal = Decal.newDecal(size, size, tex, true);
		if (dFXPos == null)
			inst.decal.setPosition(px, py, pz);
		else {
			dFXPos.getPosition(tmpVec);
			inst.decal.setPosition(tmpVec.x, tmpVec.y, tmpVec.z);
		}
		inst.duration = duration;
		inst.fadeOutTime = fadeOutTime;
		inst.up = new Vector3();
		inst.up.set(0, 1, 0).rotate((float)Math.random() * 360, 0, 0, 1);
		inst.dFXPos = dFXPos;
		decals.add(inst);
	}
	
	public void addSmoke(float size, float px, float py, float pz) {
		addDecal(smokeTex, size, 1, 0.5f, px, py, pz, null);
	}
	
	public void addSpark(float size, float px, float py, float pz) {
		addDecal(sparkTex, size, 1, 1, px, py, pz, null);
	}
	
	public void addBackfire(float size, DynamicFXPosition dFXPos) {
		addDecal(backfireTex, size, -1, -1, 0, 0, 0, dFXPos);
	}
	
	private ArrayList<DecalInst> removingDecalInst = new ArrayList<DecalInst>();
	public void update(float delta) {
		if (decals.size() == 0) return;
		for (DecalInst inst : decals) {
			if (inst.duration >= 0) {
				inst.duration -= delta;
				if (inst.duration <= 0) removingDecalInst.add(inst);
				if (inst.fadeOutTime > 0 &&
					inst.duration < inst.fadeOutTime)
					inst.decal.setColor(1, 1, 1, inst.duration / inst.fadeOutTime);
			} else {
				if (inst.rendered) removingDecalInst.add(inst);
			}
		}
		decals.removeAll(removingDecalInst);
		removingDecalInst.clear();
	}
	
	private Vector3 tmpVec2 = new Vector3();
	public void render() {
		if (decals.size() == 0) return;
		for (DecalInst inst : decals) {
			inst.decal.lookAt(camera.position, inst.up);
			inst.rendered = true;
			if (inst.dFXPos != null) {
				inst.dFXPos.getPosition(tmpVec2);
				inst.decal.setPosition(tmpVec2.x, tmpVec2.y, tmpVec2.z);
			}
			decalBatch.add(inst.decal);
		}
		decalBatch.flush();
	}
	
	public void clear() {
		decals.clear();
	}
	
	public void dispose() {
		decalBatch.dispose();
	}
}
