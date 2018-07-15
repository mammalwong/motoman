package com.marcowong.motoman;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;

public class StaticModelTextureFilterConfigManager {
	private static ArrayList<Texture> textures = new ArrayList<Texture>();
	
	public static void add(Texture t) {
		textures.add(t);
	}
	
	public static void remove(Texture t) {
		textures.remove(t);
	}
	
	public static void add(Model m) {
		SubMesh[] meshes = m.getSubMeshes();
		for (SubMesh mesh : meshes) {
			int na = mesh.material.getNumberOfAttributes();
			for (int i = 0; i < na; ++i) {
				MaterialAttribute attr = mesh.material.getAttribute(i);
				if (attr instanceof TextureAttribute) {
					TextureAttribute tattr = (TextureAttribute)attr;
					add(tattr.texture);
				}
			}
		}
	}
	
	public static void remove(Model m) {
		SubMesh[] meshes = m.getSubMeshes();
		for (SubMesh mesh : meshes) {
			int na = mesh.material.getNumberOfAttributes();
			for (int i = 0; i < na; ++i) {
				MaterialAttribute attr = mesh.material.getAttribute(i);
				if (attr instanceof TextureAttribute) {
					TextureAttribute tattr = (TextureAttribute)attr;
					remove(tattr.texture);
				}
			}
		}
	}
	
	public static void updateFilter() {
		for (Texture t : textures) {
			if (ConfigHelper.turnOnModelTextureLinearFilter())
				t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			else
				t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		}
	}
}
