package com.marcowong.motoman;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;

public class ObjLoaderSkeletonPatcher {
	public final static Color[] skeletonIdColors = new Color[] {
		Color.WHITE, Color.BLACK, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN,
		Color.BLUE, Color.MAGENTA, Color.PINK, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY
	};
	
	public void patch(Model model, Pixmap skeletonMapping) {
		for (SubMesh subMesh : model.getSubMeshes()) {
			VertexAttributes vas = subMesh.mesh.getVertexAttributes();
			VertexAttribute[] vaa = new VertexAttribute[vas.size() + 1];
			for (int i = 0; i < vas.size(); ++i) vaa[i] = vas.get(i);
			
			VertexAttribute uvAttr = subMesh.mesh.getVertexAttribute(VertexAttributes.Usage.TextureCoordinates);
			int nVertex = subMesh.mesh.getNumVertices();
			int sizeVertex = subMesh.mesh.getVertexSize() / 4;
			float[] vertices = new float[nVertex * sizeVertex];
			float[] vertices2 = new float[nVertex * (sizeVertex + 1)];
			subMesh.mesh.getVertices(vertices);
			short[] indices = new short [subMesh.mesh.getNumIndices()];
			subMesh.mesh.getIndices(indices);
			for (int i = 0; i < nVertex; ++i) {
				int offset = i * sizeVertex;
				int offset2 = i * (sizeVertex + 1);
				int offsetUV = offset2 + (uvAttr.offset / 4);
				int offsetSke = offset2 + sizeVertex;
				for (int j = 0; j < sizeVertex; ++j) vertices2[offset2 + j] = vertices[offset + j];
				float u = vertices2[offsetUV];
				float v = vertices2[offsetUV + 1];
				vertices2[offsetSke] = getSkeletonId(skeletonMapping, u, v);
			}
			
			vaa[vaa.length - 1] = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_skeleton");
			Mesh newMesh = new Mesh(true, subMesh.mesh.getNumVertices(), subMesh.mesh.getNumIndices(), vaa);
			newMesh.setVertices(vertices2);
			newMesh.setIndices(indices);
			Mesh oldMesh = subMesh.getMesh();
			subMesh.setMesh(newMesh);
			oldMesh.dispose();
		}
	}
	
	private Color tmpColor = new Color();
	private int getSkeletonId(Pixmap m, float u, float v) {
		int x = (int)Math.round(u * m.getWidth());
		int y = (int)Math.round(v * m.getHeight());
		Color.rgba8888ToColor(tmpColor, m.getPixel(x, y));
		int id = 0;
		float lowestDiff = Float.POSITIVE_INFINITY;
		for (int i = 0; i < skeletonIdColors.length; ++i) {
			float diff = 
				Math.abs(skeletonIdColors[i].r - tmpColor.r) +
				Math.abs(skeletonIdColors[i].g - tmpColor.g) +
				Math.abs(skeletonIdColors[i].b - tmpColor.b);
			if (diff < lowestDiff) {
				id = i;
				lowestDiff = diff;
			}
		}
		return id;
	}
}
