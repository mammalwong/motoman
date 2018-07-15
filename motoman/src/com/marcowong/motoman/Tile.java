package com.marcowong.motoman;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Tile {
	private static Model model;
	private static InstancingModel modelI;
	private static float tileLen;
	private static float height;
	private static int duplicate;
	
	public static void initResource() {
		ObjLoaderEx objLoader = new ObjLoaderEx();
		model = objLoader.loadObj(Gdx.files.internal("data/tile.obj"), true);
		Pixmap modelSkeletonMapping = new Pixmap(Gdx.files.internal("data/tile.skeleton.png"));
		new ObjLoaderSkeletonPatcher().patch(model, modelSkeletonMapping);
		modelSkeletonMapping.dispose();
		StaticModelTextureFilterConfigManager.add(model);
		modelI = new InstancingModel(model, 12);
		SubMesh[] subMeshs = model.getSubMeshes();
		for (SubMesh subMesh : subMeshs) {
			int nAttributes = subMesh.material.getNumberOfAttributes();
			for (int i = 0; i < nAttributes; ++i) {
				MaterialAttribute att = subMesh.material.getAttribute(i);
				if (att instanceof TextureAttribute) {
					TextureAttribute ta = (TextureAttribute)att;
					ta.uWrap = TextureWrap.Repeat.getGLEnum();
					ta.vWrap = TextureWrap.Repeat.getGLEnum();
				}
			}
		}
		
		tileLen = 1000;
		height = -1;
		duplicate = 1;
	}
	
	private Matrix4[] modelISkeMats;
	private FloatBuffer modelISkeMatsFBuf;
	
	public Tile() {
		modelISkeMats = new Matrix4[modelI.copies];
		for (int i = 0; i < modelISkeMats.length; ++i) modelISkeMats[i] = new Matrix4();
		modelISkeMatsFBuf = ByteBuffer.allocateDirect(modelI.copies * 16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	private void renderModelI(ShaderProgram shader, Camera camera, int nInst) {
		shader.setUniformMatrix("modelviewproj", camera.combined);
		shader.setUniformMatrix("modelview", camera.view);
		int nSkeMats = nInst;
		for (int i = 0; i < nSkeMats; ++i) {
			modelISkeMatsFBuf.position(i * 16);
			modelISkeMatsFBuf.put(modelISkeMats[i].val);
		}
		shader.setUniformMatrix4fv("skeletonmat", modelISkeMatsFBuf, nSkeMats, false);
		modelI.render(shader, nInst);
	}
	
	public void render(ShaderProgram shader, Camera camera) {
		int startX = (int)Math.round(camera.position.x / tileLen);
		int startZ = (int)Math.round(camera.position.z / tileLen);
		
		int nInst = 0;
		for (int x = startX - duplicate; x <= startX + duplicate; ++x) {
			for (int z = startZ - duplicate; z <= startZ + duplicate; ++z) {
				Matrix4 skeMat = modelISkeMats[nInst];
				skeMat.idt();
				skeMat.trn(x * tileLen, height, z * tileLen);
				if (++nInst >= modelI.copies) {
					renderModelI(shader, camera, nInst);
					nInst = 0;
				}
			}
		}
		if (nInst != 0)
			renderModelI(shader, camera, nInst);
	}
	
	public void dispose() {
	}
}
