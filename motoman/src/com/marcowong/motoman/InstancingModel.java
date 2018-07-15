package com.marcowong.motoman;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class InstancingModel {
	private Model model;
	private Mesh[] meshes;
	private int[] nIndicesPerCopy;
	public int copies;
	
	public InstancingModel(Model model, int copies) {
		this.model = model;
		this.copies = copies;
		SubMesh[] subMeshes = model.getSubMeshes();
		meshes = new Mesh[subMeshes.length];
		nIndicesPerCopy = new int[subMeshes.length];
		for (int i = 0; i < meshes.length; ++i) {
			VertexAttributes vas = subMeshes[i].mesh.getVertexAttributes();
			VertexAttribute[] vaa = new VertexAttribute[vas.size()];
			for (int j = 0; j < vaa.length; ++j) vaa[j] = vas.get(j);
			meshes[i] = new Mesh(true,
					subMeshes[i].mesh.getNumVertices() * copies,
					subMeshes[i].mesh.getNumIndices() * copies, vaa);
			
			int nVertex = subMeshes[i].mesh.getNumVertices();
			int sizeVertex = subMeshes[i].mesh.getVertexSize() / 4;
			float[] vertices = new float[nVertex * sizeVertex];
			float[] vertices2 = new float[nVertex * sizeVertex * copies];
			subMeshes[i].mesh.getVertices(vertices);
			int nIndices = subMeshes[i].mesh.getNumIndices();
			short[] indices = new short [nIndices];
			short[] indices2 = new short [nIndices * copies];
			subMeshes[i].mesh.getIndices(indices);
			
			nIndicesPerCopy[i] = nIndices;
			
			VertexAttribute skeAttr = null;
			int skeIdOffset = 0;
			for (int k = 0; k < vaa.length; ++k) {
				if (vaa[k].alias.equals("a_skeleton")) {
					skeAttr = vaa[k];
					skeIdOffset = vaa[k].offset / 4;
					break;
				}
			}
			
			int maxSkeId = 0;
			if (skeAttr != null) {
				for (int k = 0; k < nVertex; ++k) {
					int skeId = (int)Math.round(vertices[k * sizeVertex + skeIdOffset]);
					if (skeId > maxSkeId) maxSkeId = skeId;
				}
			}
			
			for (int k = 0; k < copies; ++k) {
				System.arraycopy(vertices, 0, vertices2, k * vertices.length, vertices.length);
				System.arraycopy(indices, 0, indices2, k * nIndices, indices.length);
				for (int l = 0; l < nIndices; ++l)
					indices2[k * nIndices + l] += k * nVertex;
				if (skeAttr != null) {
					for (int l = 0; l < nVertex; ++l) {
						int skeId = (int)Math.round(vertices2[k * vertices.length + l * sizeVertex + skeIdOffset]);
						if (skeId != 0)
							vertices2[k * vertices.length + l * sizeVertex + skeIdOffset] = skeId + k * maxSkeId;
					}
				}
			}
			
			meshes[i].setVertices(vertices2);
			meshes[i].setIndices(indices2);
		}
	}
	
	public void render(ShaderProgram program, int nInst) {
		SubMesh[] subMeshes = model.getSubMeshes();
		for (int i = 0; i < meshes.length; i++) {
			SubMesh subMesh = subMeshes[i];
			if (i == 0) {
				subMesh.material.bind(program);
			} else if (!subMeshes[i - 1].material.equals(subMesh.material)) {
				subMesh.material.bind(program);
			}
			meshes[i].render(program, subMesh.primitiveType, 0, nInst * nIndicesPerCopy[i]);
		}
	}
	
	public void dispose() {
		for (int i = 0; i < meshes.length; ++i)
			meshes[i].dispose();
	}
}
