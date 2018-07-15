package com.marcowong.motoman;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

public class MeshOptimized {
	private final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);
	public static MeshOptimized globalStaticMesh;
	
	public static void initResource() {
		globalStaticMesh = new MeshOptimized();
	}
	
	private ArrayList<float[]> vertices = new ArrayList<float[]>();
	private ArrayList<short[]> indices = new ArrayList<short[]>();
	
	private VertexAttributes attributes;
	private FloatBuffer bufferV;
	private ByteBuffer byteBufferV;
	private int bufferHandleV;
	private ShortBuffer bufferI;
	private ByteBuffer byteBufferI;
	private int bufferHandleI;
	
	private static class RenderContext {
		public Material mat;
		public int count;
		public int offset;
	}
	
	private class MeshContext implements IMeshContext {
		public int copies;
		public int primitiveType;
		public ArrayList<RenderContext> renderContexts;
		
		public int getNCopies() {
			return copies;
		}
		
		public void render(ShaderProgram shader) {
			render(shader, 1);
		}
		
		public void render(ShaderProgram shader, int copies) {
			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandleV);
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandleI);
			int nRenderContext = renderContexts.size();
			for (int i = 0; i < nRenderContext; ++i) {
				RenderContext rc = renderContexts.get(i);
				if (rc.mat != null)
					rc.mat.bind(shader);
				int nAttr = attributes.size();
				for (int j = 0; j < nAttr; ++j) {
					VertexAttribute attribute = attributes.get(j);
					shader.enableVertexAttribute(attribute.alias);
					shader.setVertexAttribute(attribute.alias, attribute.numComponents, GL20.GL_FLOAT, false, attributes.vertexSize, attribute.offset);
				}
				Gdx.gl20.glDrawElements(primitiveType, rc.count * copies, GL10.GL_UNSIGNED_SHORT, rc.offset * 2);
			}
		}
	}
	
	private class CreateOptimizedMeshResult {
		public float[] vertice;
		public short[] indice;
	}
	
	private CreateOptimizedMeshResult createOptimizedMesh(Set<Mesh> meshes, int copies) {
		List<float[]> verticeA = new ArrayList<float[]>();
		List<short[]> indiceA = new ArrayList<short[]>();
		short nVerticeT = 0;
		int nVerticeA = 0;
		int nIndiceA = 0;
		int maxSkeId = 0;
		for (Mesh mesh : meshes) {
			VertexAttribute attrPos = null;
			VertexAttribute attrNor = null;
			VertexAttribute attrTex = null;
			VertexAttribute attrSke = null;
			VertexAttributes attrs = mesh.getVertexAttributes();
			for (int i = 0; i < attrs.size(); ++i) {
				VertexAttribute va = attrs.get(i);
				if (va.alias.equals("a_position"))
					attrPos = va;
				else if (va.alias.equals("a_normal"))
					attrNor = va;
				else if (va.alias.equals("a_texCoord0"))
					attrTex = va;
				else if (va.alias.equals("a_skeleton"))
					attrSke = va;
				else
					throw new IllegalStateException("unknown vertex attribute");
			}
			int vertice0VSize = mesh.getVertexSize() / 4;
			float[] vertice0 = new float[mesh.getNumVertices() * vertice0VSize];
			float[] vertice = new float[mesh.getNumVertices() * 9];
			mesh.getVertices(vertice0);
			for (int i = 0; i < mesh.getNumVertices(); ++i) {
				if (attrPos != null) {
					int offset = attrPos.offset / 4;
					vertice[i * 9 + 0] = vertice0[i * vertice0VSize + offset + 0];
					vertice[i * 9 + 1] = vertice0[i * vertice0VSize + offset + 1];
					vertice[i * 9 + 2] = vertice0[i * vertice0VSize + offset + 2];
				}
				if (attrNor != null) {
					int offset = attrNor.offset / 4;
					vertice[i * 9 + 3] = vertice0[i * vertice0VSize + offset + 0];
					vertice[i * 9 + 4] = vertice0[i * vertice0VSize + offset + 1];
					vertice[i * 9 + 5] = vertice0[i * vertice0VSize + offset + 2];
				}
				if (attrTex != null) {
					int offset = attrTex.offset / 4;
					vertice[i * 9 + 6] = vertice0[i * vertice0VSize + offset + 0];
					vertice[i * 9 + 7] = vertice0[i * vertice0VSize + offset + 1];
				}
				if (attrSke != null) {
					int offset = attrSke.offset / 4;
					vertice[i * 9 + 8] = vertice0[i * vertice0VSize + offset + 0];
					if (vertice[i * 9 + 8] > maxSkeId)
						maxSkeId = (int)vertice[i * 9 + 8];
				}
			}
			short[] indice = new short[mesh.getNumIndices()];
			mesh.getIndices(indice);
			for (int i = 0; i < indice.length; ++i)
				indice[i] += nVerticeT;
			nVerticeT += mesh.getNumVertices();
			nVerticeA += vertice.length;
			nIndiceA += indice.length;
			verticeA.add(vertice);
			indiceA.add(indice);
		}
		float[] vertice = new float[nVerticeA];
		short[] indice = new short[nIndiceA];
		nVerticeA = 0;
		for (float[] v : verticeA) {
			System.arraycopy(v, 0, vertice, nVerticeA, v.length);
			nVerticeA += v.length;
		}
		nIndiceA = 0;
		for (short[] i : indiceA) {
			System.arraycopy(i, 0, indice, nIndiceA, i.length);
			nIndiceA += i.length;
		}
		
		short[] indiceMapping = new short[vertice.length / 9];
		short[] indiceReduced = new short[vertice.length / 9];
		int reducedVerticeCount = 0;
		for (int i = 0; i < indiceMapping.length; ++i)
			indiceMapping[i] = (short)i;
		for (int i = 0, nextNewIndex = 0; i < indiceMapping.length; ++i) {
			if (indiceMapping[i] == i) {
				++reducedVerticeCount;
				indiceReduced[i] = (short)(nextNewIndex++);
				for (int j = i + 1; j < indiceMapping.length; ++j)
					if (indiceMapping[j] == j &&
						vertice[i * 9 + 0] == vertice[j * 9 + 0] &&
						vertice[i * 9 + 1] == vertice[j * 9 + 1] &&
						vertice[i * 9 + 2] == vertice[j * 9 + 2] &&
						vertice[i * 9 + 3] == vertice[j * 9 + 3] &&
						vertice[i * 9 + 4] == vertice[j * 9 + 4] &&
						vertice[i * 9 + 5] == vertice[j * 9 + 5] &&
						vertice[i * 9 + 6] == vertice[j * 9 + 6] &&
						vertice[i * 9 + 7] == vertice[j * 9 + 7] &&
						vertice[i * 9 + 8] == vertice[j * 9 + 8]) {
						indiceMapping[j] = (short)i;
						indiceReduced[j] = indiceReduced[i];
					}
			}
		}
		float[] verticeReduced = new float[reducedVerticeCount * 9];
		for (int i = 0; i < indiceMapping.length; ++i) {
			verticeReduced[indiceReduced[i] * 9 + 0] = vertice[i * 9 + 0];
			verticeReduced[indiceReduced[i] * 9 + 1] = vertice[i * 9 + 1];
			verticeReduced[indiceReduced[i] * 9 + 2] = vertice[i * 9 + 2];
			verticeReduced[indiceReduced[i] * 9 + 3] = vertice[i * 9 + 3];
			verticeReduced[indiceReduced[i] * 9 + 4] = vertice[i * 9 + 4];
			verticeReduced[indiceReduced[i] * 9 + 5] = vertice[i * 9 + 5];
			verticeReduced[indiceReduced[i] * 9 + 6] = vertice[i * 9 + 6];
			verticeReduced[indiceReduced[i] * 9 + 7] = vertice[i * 9 + 7];
			verticeReduced[indiceReduced[i] * 9 + 8] = vertice[i * 9 + 8];
		}
		vertice = verticeReduced;
		for (int i = 0; i < indice.length; ++i) {
			indice[i] = indiceReduced[indice[i]];
		}
		
		if (copies > 1) {
			float[] vertice0 = vertice;
			short[] indice0 = indice;
			vertice = new float[vertice0.length * copies];
			indice = new short[indice0.length * copies];
			for (int j = 0; j < copies; ++j) {
				System.arraycopy(vertice0, 0, vertice, j * vertice0.length, vertice0.length);
				for (int i = 0; i < vertice0.length / 9; ++i) {
					vertice[j * vertice0.length + i * 9 + 8] += j * maxSkeId;
				}
				System.arraycopy(indice0, 0, indice, j * indice0.length, indice0.length);
				for (int i = 0; i < indice0.length; ++i) {
					indice[j * indice0.length + i] += j * (vertice0.length / 9);
				}
			}
		}
		
		CreateOptimizedMeshResult result = new CreateOptimizedMeshResult();
		result.vertice = vertice;
		result.indice = indice;
		return result;
	}
	
	private void shaftIndice(short[] indice) {
		int numVertice = 0;
		for (float[] vertice : vertices)
			numVertice += vertice.length / 9;
		for (int i = 0; i < indice.length; ++i)
			indice[i] += numVertice;
	}
	
	private int getIndiceOffset() {
		int numIndice = 0;
		for (short[] indice : indices)
			numIndice += indice.length;
		return numIndice;
	}
	
	public IMeshContext add(Model model) {
		return add(model, 1);
	}
	
	public IMeshContext add(Model model, int copies) {
		SubMesh[] meshes = model.getSubMeshes();
		
		Map<Material, Set<Mesh>> matMap = new Hashtable<Material, Set<Mesh>>();
		for (SubMesh mesh : meshes) {
			Set<Mesh> meshSet = matMap.get(mesh.material);
			if (meshSet == null) {
				meshSet = new HashSet<Mesh>();
				matMap.put(mesh.material, meshSet);
			}
			meshSet.add(mesh.mesh);
		}
		
		MeshContext meshContext = new MeshContext();
		meshContext.renderContexts = new ArrayList<RenderContext>();
		meshContext.primitiveType = GL20.GL_TRIANGLES;
		meshContext.copies = copies;
		for (Entry<Material, Set<Mesh>> kvp : matMap.entrySet()) {
			CreateOptimizedMeshResult result = createOptimizedMesh(kvp.getValue(), copies);
			int indiceOffset = getIndiceOffset();
			shaftIndice(result.indice);
			vertices.add(result.vertice);
			indices.add(result.indice);
			RenderContext rc = new RenderContext();
			rc.offset = indiceOffset;
			rc.count = result.indice.length / copies;
			rc.mat = kvp.getKey();
			meshContext.renderContexts.add(rc);
		}
		return meshContext;
	}
	
	public IMeshContext add(Mesh mesh, int primitiveType) {
		MeshContext meshContext = new MeshContext();
		meshContext.renderContexts = new ArrayList<RenderContext>();
		meshContext.primitiveType = primitiveType;
		meshContext.copies = 1;
		Set<Mesh> meshes = new HashSet<Mesh>();
		meshes.add(mesh);
		CreateOptimizedMeshResult result = createOptimizedMesh(meshes, 1);
		int indiceOffset = getIndiceOffset();
		shaftIndice(result.indice);
		vertices.add(result.vertice);
		indices.add(result.indice);
		RenderContext rc = new RenderContext();
		rc.offset = indiceOffset;
		rc.count = result.indice.length;
		rc.mat = null;
		meshContext.renderContexts.add(rc);
		return meshContext;
	}
	
	public void optimize() {
		int numVertice = 0;
		for (float[] vertice : this.vertices)
			numVertice += vertice.length / 9;
		float[] vertices = new float[numVertice * 9];
		int verticeOffset = 0;
		for (float[] vertice : this.vertices) {
			System.arraycopy(vertice, 0, vertices, verticeOffset, vertice.length);
			verticeOffset += vertice.length;
		}
		
		byteBufferV = BufferUtils.newUnsafeByteBuffer(numVertice * 9 * 4);
		bufferV = byteBufferV.asFloatBuffer();
		bufferV.flip();
		byteBufferV.flip();
		Gdx.gl20.glGenBuffers(1, tmpHandle);
		bufferHandleV = tmpHandle.get(0);
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandleV);
		
		BufferUtils.copy(vertices, byteBufferV, vertices.length, 0);
		bufferV.position(0);
		bufferV.limit(vertices.length);
		Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBufferV.limit(), byteBufferV, GL20.GL_STATIC_DRAW);
		
		int numIndex = 0;
		for (short[] indice : this.indices)
			numIndex += indice.length;
		short[] indices = new short[numIndex];
		int indiceOffset = 0;
		for (short[] indice : this.indices) {
			System.arraycopy(indice, 0, indices, indiceOffset, indice.length);
			indiceOffset += indice.length;
		}
		
		byteBufferI = BufferUtils.newUnsafeByteBuffer(numIndex * 2);
		bufferI = byteBufferI.asShortBuffer();
		bufferI.flip();
		byteBufferI.flip();
		Gdx.gl20.glGenBuffers(1, tmpHandle);
		bufferHandleI = tmpHandle.get(0);
		
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandleI);
		
		bufferI.clear();
		bufferI.put(indices, 0, indices.length);
		bufferI.flip();
		byteBufferI.position(0);
		byteBufferI.limit(indices.length << 1);
		Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBufferI.limit(), byteBufferI, GL20.GL_STATIC_DRAW);
		
		attributes = new VertexAttributes(
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
				new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_skeleton"));
	}
}
