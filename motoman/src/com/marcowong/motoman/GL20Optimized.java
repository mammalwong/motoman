package com.marcowong.motoman;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.graphics.GL20;

public class GL20Optimized implements GL20 {
	
	private GL20 gl20;
	private Map<Integer, Boolean> capEnabled = new Hashtable<Integer, Boolean>();
	private int textureActive = GL20.GL_TEXTURE0;
	private Map<Integer, Map<Integer, Integer>> textureTarget = new Hashtable<Integer, Map<Integer, Integer>>();
	private Map<Integer, Map<Integer, Float>> texturePara = new Hashtable<Integer, Map<Integer, Float>>();
	private int bindedVBO = 0;
	private int bindedIndice = 0;
	public Set<Integer> enabledVertexAttribArray = new HashSet<Integer>();
	public Set<Integer> usedVertexAttribArray = new HashSet<Integer>();
	public Set<Integer> disablingVertexAttribArray = new HashSet<Integer>();
	private int shaderActive = 0;
	private Map<Integer, Map<Integer, float[]>> shaderUniform4f = new Hashtable<Integer, Map<Integer, float[]>>();
	private Map<Integer, Map<Integer, Integer>> shaderUniform1i = new Hashtable<Integer, Map<Integer, Integer>>();
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportW = 0;
	private int viewportH = 0;
	private boolean requestedUnbindFrameBuffer = false;
	private Integer blendFuncSFactor = null;
	private Integer blendFuncDFactor = null;
	private float[] clearColor = null;
	
	public GL20Optimized(GL20 gl20) {
		this.gl20 = gl20;
		textureActive = GL20.GL_TEXTURE0;
		gl20.glActiveTexture(GL_TEXTURE0);
		bindedVBO = 0;
		gl20.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl20.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void glActiveTexture(int texture) {
		if (texture != textureActive) {
			textureActive = texture;
			textureTarget.clear();
			gl20.glActiveTexture(texture);
		}
	}

	@Override
	public void glBindTexture(int target, int texture) {
		Map<Integer, Integer> m = textureTarget.get(textureActive);
		if (m == null) {
			m = new Hashtable<Integer, Integer>();
			m.put(target, texture);
			textureTarget.put(textureActive, m);
			gl20.glBindTexture(target, texture);
		} else {
			Integer t = m.get(target);
			if (t == null ||
				t.intValue() != texture) {
				m.put(target, texture);
				gl20.glBindTexture(target, texture);
			}
		}
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		if (blendFuncSFactor == null ||
			blendFuncDFactor == null ||
			sfactor != blendFuncSFactor.intValue() ||
			dfactor != blendFuncDFactor.intValue()) {
			blendFuncSFactor = sfactor;
			blendFuncDFactor = dfactor;
			gl20.glBlendFunc(sfactor, dfactor);
		}
	}

	@Override
	public void glClear(int mask) {
		gl20.glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		if (clearColor == null ||
			clearColor[0] != red ||
			clearColor[1] != green ||
			clearColor[2] != blue ||
			clearColor[3] != alpha) {
			clearColor = new float[] { red, green, blue, alpha };
			gl20.glClearColor(red, green, blue, alpha);
		}
	}

	@Override
	public void glClearDepthf(float depth) {
		gl20.glClearDepthf(depth);
	}

	@Override
	public void glClearStencil(int s) {
		gl20.glClearStencil(s);
	}

	@Override
	public void glColorMask(boolean red, boolean green, boolean blue,
			boolean alpha) {
		gl20.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D(int target, int level,
			int internalformat, int width, int height, int border,
			int imageSize, Buffer data) {
		gl20.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override
	public void glCompressedTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int imageSize,
			Buffer data) {
		gl20.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override
	public void glCopyTexImage2D(int target, int level, int internalformat,
			int x, int y, int width, int height, int border) {
		gl20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int x, int y, int width, int height) {
		gl20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace(int mode) {
		gl20.glCullFace(mode);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		for (int i = 0; i < n; ++i) {
			for (Map<Integer, Integer> tt : textureTarget.values()) {
				for (Entry<Integer, Integer> tte : tt.entrySet()) {
					if (tte.getValue().intValue() == textures.get(i))
						tt.remove(tte.getKey());
				}
			}
			texturePara.remove(textures.get(i));
		}
		gl20.glDeleteTextures(n, textures);
	}

	@Override
	public void glDepthFunc(int func) {
		gl20.glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		gl20.glDepthMask(flag);
	}

	@Override
	public void glDepthRangef(float zNear, float zFar) {
		gl20.glDepthRangef(zNear, zFar);
	}

	@Override
	public void glDisable(int cap) {
		Boolean b = capEnabled.get(cap);
		if (b == null ||
			b.booleanValue() == true) {
			capEnabled.put(cap, false);
			gl20.glDisable(cap);
		}
	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		disableUnusedVertexAttribArray();
		unbindFrameBufferIfNeeded();
		gl20.glDrawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		disableUnusedVertexAttribArray();
		unbindFrameBufferIfNeeded();
		gl20.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnable(int cap) {
		Boolean b = capEnabled.get(cap);
		if (b == null ||
			b.booleanValue() == false) {
			capEnabled.put(cap, true);
			gl20.glEnable(cap);
		}
	}

	@Override
	public void glFinish() {
		gl20.glFinish();
	}

	@Override
	public void glFlush() {
		gl20.glFlush();
	}

	@Override
	public void glFrontFace(int mode) {
		gl20.glFrontFace(mode);
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		gl20.glGenTextures(n, textures);
	}

	@Override
	public int glGetError() {
		return gl20.glGetError();
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		gl20.glGetIntegerv(pname, params);
	}

	@Override
	public String glGetString(int name) {
		return gl20.glGetString(name);
	}

	@Override
	public void glHint(int target, int mode) {
		gl20.glHint(target, mode);
	}

	@Override
	public void glLineWidth(float width) {
		gl20.glLineWidth(width);
	}

	@Override
	public void glPixelStorei(int pname, int param) {
		gl20.glPixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset(float factor, float units) {
		gl20.glPolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format,
			int type, Buffer pixels) {
		gl20.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		gl20.glScissor(x, y, width, height);
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		gl20.glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilMask(int mask) {
		gl20.glStencilMask(mask);
	}

	@Override
	public void glStencilOp(int fail, int zfail, int zpass) {
		gl20.glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat,
			int width, int height, int border, int format, int type,
			Buffer pixels) {
		gl20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	private boolean isRunGLTexParameter(int target, int pname, float param) {
		Map<Integer, Integer> mt = textureTarget.get(textureActive);
		if (mt == null)
			return true;
		else {
			Integer t = mt.get(target);
			if (t == null)
				return true;
			Map<Integer, Float> m = texturePara.get(t.intValue());
			if (m == null) {
				m = new Hashtable<Integer, Float>();
				m.put(pname, param);
				texturePara.put(t, m);
				return true;
			} else {
				Float f = m.get(pname);
				if (f == null ||
					f != param) {
					m.put(pname, param);
					return true;
				} else
					return false;
			}
		}
	}
	
	private void invalidateIsRunGLTexParameter(int target, int pname) {
		Map<Integer, Integer> mt = textureTarget.get(textureActive);
		if (mt == null)
			return;
		Integer t = mt.get(target);
		if (t == null)
			return;
		
		Map<Integer, Float> m = texturePara.get(t);
		if (m == null)
			return;
		m.remove(pname);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		if (isRunGLTexParameter(target, pname, param))
			gl20.glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int type,
			Buffer pixels) {
		gl20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		viewportX = x;
		viewportY = y;
		viewportW = width;
		viewportH = height;
		//gl20.glViewport(x, y, width, height);
	}

	@Override
	public void glAttachShader(int program, int shader) {
		gl20.glAttachShader(program, shader);
	}

	@Override
	public void glBindAttribLocation(int program, int index, String name) {
		gl20.glBindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		if (buffer != 0) {
			if (target == GL_ARRAY_BUFFER) bindedVBO = buffer;
			if (target == GL_ELEMENT_ARRAY_BUFFER) bindedIndice = buffer;
			gl20.glBindBuffer(target, buffer);
		}
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		if (framebuffer != 0) {
			requestedUnbindFrameBuffer = false;
			gl20.glViewport(viewportX, viewportY, viewportW, viewportH);
			gl20.glBindFramebuffer(target, framebuffer);
		} else
			requestedUnbindFrameBuffer = true;
	}
	
	private void unbindFrameBufferIfNeeded() {
		if (requestedUnbindFrameBuffer) {
			gl20.glViewport(viewportX, viewportY, viewportW, viewportH);
			gl20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		}
	}

	@Override
	public void glBindRenderbuffer(int target, int renderbuffer) {
		gl20.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void glBlendColor(float red, float green, float blue, float alpha) {
		gl20.glBlendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation(int mode) {
		gl20.glBlendEquation(mode);
	}

	@Override
	public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
		gl20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
			int dstAlpha) {
		blendFuncSFactor = null;
		blendFuncDFactor = null;
		gl20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage) {
		gl20.glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		gl20.glBufferSubData(target, offset, size, data);
	}

	@Override
	public int glCheckFramebufferStatus(int target) {
		return gl20.glCheckFramebufferStatus(target);
	}

	@Override
	public void glCompileShader(int shader) {
		gl20.glCompileShader(shader);
	}

	@Override
	public int glCreateProgram() {
		return gl20.glCreateProgram();
	}

	@Override
	public int glCreateShader(int type) {
		return gl20.glCreateShader(type);
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		for (int i = 0; i < n; ++i) {
			if (buffers.get(i) == bindedVBO) {
				bindedVBO = 0;
				gl20.glBindBuffer(GL_ARRAY_BUFFER, 0);
			}
			if (buffers.get(i) == bindedIndice) {
				bindedIndice = 0;
				gl20.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			}
		}
		gl20.glDeleteBuffers(n, buffers);
	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		gl20.glDeleteFramebuffers(n, framebuffers);
	}

	@Override
	public void glDeleteProgram(int program) {
		if (program == shaderActive) {
			shaderUniform4f.remove(shaderActive);
			shaderUniform1i.remove(shaderActive);
			shaderActive = 0;
			gl20.glUseProgram(0);
		}
		gl20.glDeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		gl20.glDeleteRenderbuffers(n, renderbuffers);
	}

	@Override
	public void glDeleteShader(int shader) {
		gl20.glDeleteShader(shader);
	}

	@Override
	public void glDetachShader(int program, int shader) {
		if (program == shaderActive) {
			shaderUniform4f.remove(shaderActive);
			shaderUniform1i.remove(shaderActive);
			shaderActive = 0;
			gl20.glUseProgram(0);
		}
		gl20.glDetachShader(program, shader);
	}

	@Override
	public void glDisableVertexAttribArray(int index) {
		//gl20.glDisableVertexAttribArray(index);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, int indices) {
		disableUnusedVertexAttribArray();
		unbindFrameBufferIfNeeded();
		gl20.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnableVertexAttribArray(int index) {
		//gl20.glEnableVertexAttribArray(index);
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment,
			int renderbuffertarget, int renderbuffer) {
		gl20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment,
			int textarget, int texture, int level) {
		gl20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		gl20.glGenBuffers(n, buffers);
	}

	@Override
	public void glGenerateMipmap(int target) {
		gl20.glGenerateMipmap(target);
	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		gl20.glGenFramebuffers(n, framebuffers);
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		gl20.glGenRenderbuffers(n, renderbuffers);
	}

	@Override
	public String glGetActiveAttrib(int program, int index, IntBuffer size,
			Buffer type) {
		return gl20.glGetActiveAttrib(program, index, size, type);
	}

	@Override
	public String glGetActiveUniform(int program, int index, IntBuffer size,
			Buffer type) {
		return gl20.glGetActiveUniform(program, index, size, type);
	}

	@Override
	public void glGetAttachedShaders(int program, int maxcount, Buffer count,
			IntBuffer shaders) {
		gl20.glGetAttachedShaders(program, maxcount, count, shaders);
	}

	@Override
	public int glGetAttribLocation(int program, String name) {
		return gl20.glGetAttribLocation(program, name);
	}

	@Override
	public void glGetBooleanv(int pname, Buffer params) {
		gl20.glGetBooleanv(pname, params);
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		gl20.glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		gl20.glGetFloatv(pname, params);
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int target,
			int attachment, int pname, IntBuffer params) {
		gl20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
	public void glGetProgramiv(int program, int pname, IntBuffer params) {
		gl20.glGetProgramiv(program, pname, params);
	}

	@Override
	public String glGetProgramInfoLog(int program) {
		return gl20.glGetProgramInfoLog(program);
	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname,
			IntBuffer params) {
		gl20.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetShaderiv(int shader, int pname, IntBuffer params) {
		gl20.glGetShaderiv(shader, pname, params);
	}

	@Override
	public String glGetShaderInfoLog(int shader) {
		return gl20.glGetShaderInfoLog(shader);
	}

	@Override
	public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
			IntBuffer range, IntBuffer precision) {
		gl20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
	}

	@Override
	public void glGetShaderSource(int shader, int bufsize, Buffer length,
			String source) {
		gl20.glGetShaderSource(shader, bufsize, length, source);
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
		gl20.glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
		gl20.glGetTexParameteriv(target, pname, params);
	}

	@Override
	public void glGetUniformfv(int program, int location, FloatBuffer params) {
		gl20.glGetUniformfv(program, location, params);
	}

	@Override
	public void glGetUniformiv(int program, int location, IntBuffer params) {
		gl20.glGetUniformiv(program, location, params);
	}

	@Override
	public int glGetUniformLocation(int program, String name) {
		return gl20.glGetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
		gl20.glGetVertexAttribfv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
		gl20.glGetVertexAttribiv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
		gl20.glGetVertexAttribPointerv(index, pname, pointer);
	}

	@Override
	public boolean glIsBuffer(int buffer) {
		return gl20.glIsBuffer(buffer);
	}

	@Override
	public boolean glIsEnabled(int cap) {
		return gl20.glIsEnabled(cap);
	}

	@Override
	public boolean glIsFramebuffer(int framebuffer) {
		return gl20.glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsProgram(int program) {
		return gl20.glIsProgram(program);
	}

	@Override
	public boolean glIsRenderbuffer(int renderbuffer) {
		return gl20.glIsRenderbuffer(renderbuffer);
	}

	@Override
	public boolean glIsShader(int shader) {
		return gl20.glIsShader(shader);
	}

	@Override
	public boolean glIsTexture(int texture) {
		return gl20.glIsTexture(texture);
	}

	@Override
	public void glLinkProgram(int program) {
		gl20.glLinkProgram(program);
	}

	@Override
	public void glReleaseShaderCompiler() {
		gl20.glReleaseShaderCompiler();
	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat,
			int width, int height) {
		gl20.glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage(float value, boolean invert) {
		gl20.glSampleCoverage(value, invert);
	}

	@Override
	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat,
			Buffer binary, int length) {
		gl20.glShaderBinary(n, shaders, binaryformat, binary, length);
	}

	@Override
	public void glShaderSource(int shader, String string) {
		gl20.glShaderSource(shader, string);
	}

	@Override
	public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
		gl20.glStencilFuncSeparate(face, func, ref, mask);
	}

	@Override
	public void glStencilMaskSeparate(int face, int mask) {
		gl20.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
		gl20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) {
		invalidateIsRunGLTexParameter(target, pname);
		gl20.glTexParameterfv(target, pname, params);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		if (isRunGLTexParameter(target, pname, param))
			gl20.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) {
		invalidateIsRunGLTexParameter(target, pname);
		gl20.glTexParameteriv(target, pname, params);
	}

	@Override
	public void glUniform1f(int location, float x) {
		gl20.glUniform1f(location, x);
	}

	@Override
	public void glUniform1fv(int location, int count, FloatBuffer v) {
		gl20.glUniform1fv(location, count, v);
	}

	@Override
	public void glUniform1i(int location, int x) {
		Map<Integer, Integer> m = shaderUniform1i.get(shaderActive);
		if (m == null) {
			m = new Hashtable<Integer, Integer>();
			shaderUniform1i.put(shaderActive, m);
		}
		Integer i = m.get(location);
		if (i == null ||
			i.intValue() != x) {
			m.put(location, x);
			gl20.glUniform1i(location, x);
		}
	}

	@Override
	public void glUniform1iv(int location, int count, IntBuffer v) {
		gl20.glUniform1iv(location, count, v);
	}

	@Override
	public void glUniform2f(int location, float x, float y) {
		gl20.glUniform2f(location, x, y);
	}

	@Override
	public void glUniform2fv(int location, int count, FloatBuffer v) {
		gl20.glUniform2fv(location, count, v);
	}

	@Override
	public void glUniform2i(int location, int x, int y) {
		gl20.glUniform2i(location, x, y);
	}

	@Override
	public void glUniform2iv(int location, int count, IntBuffer v) {
		gl20.glUniform2iv(location, count, v);
	}

	@Override
	public void glUniform3f(int location, float x, float y, float z) {
		gl20.glUniform3f(location, x, y, z);
	}

	@Override
	public void glUniform3fv(int location, int count, FloatBuffer v) {
		gl20.glUniform3fv(location, count, v);
	}

	@Override
	public void glUniform3i(int location, int x, int y, int z) {
		gl20.glUniform3i(location, x, y, z);
	}

	@Override
	public void glUniform3iv(int location, int count, IntBuffer v) {
		gl20.glUniform3iv(location, count, v);
	}

	@Override
	public void glUniform4f(int location, float x, float y, float z, float w) {
		Map<Integer, float[]> m = shaderUniform4f.get(shaderActive);
		if (m == null) {
			m = new Hashtable<Integer, float[]>();
			shaderUniform4f.put(shaderActive, m);
		}
		float[] f = m.get(location);
		if (f == null) {
			m.put(location, new float[] { x, y, z, w });
			gl20.glUniform4f(location, x, y, z, w);
		} else {
			if (f[0] != x ||
				f[1] != y ||
				f[2] != z ||
				f[3] != w) {
				f[0] = x;
				f[1] = y;
				f[2] = z;
				f[3] = w;
				gl20.glUniform4f(location, x, y, z, w);
			}
		}
	}

	@Override
	public void glUniform4fv(int location, int count, FloatBuffer v) {
		gl20.glUniform4fv(location, count, v);
	}

	@Override
	public void glUniform4i(int location, int x, int y, int z, int w) {
		gl20.glUniform4i(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv(int location, int count, IntBuffer v) {
		gl20.glUniform4iv(location, count, v);
	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		gl20.glUniformMatrix2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		gl20.glUniformMatrix3fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		gl20.glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override
	public void glUseProgram(int program) {
		if (program != 0 &&
			program != shaderActive) {
			shaderActive = program;
			gl20.glUseProgram(program);
		}
	}

	@Override
	public void glValidateProgram(int program) {
		gl20.glValidateProgram(program);
	}

	@Override
	public void glVertexAttrib1f(int indx, float x) {
		gl20.glVertexAttrib1f(indx, x);
	}

	@Override
	public void glVertexAttrib1fv(int indx, FloatBuffer values) {
		gl20.glVertexAttrib1fv(indx, values);
	}

	@Override
	public void glVertexAttrib2f(int indx, float x, float y) {
		gl20.glVertexAttrib2f(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv(int indx, FloatBuffer values) {
		gl20.glVertexAttrib2fv(indx, values);
	}

	@Override
	public void glVertexAttrib3f(int indx, float x, float y, float z) {
		gl20.glVertexAttrib3f(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv(int indx, FloatBuffer values) {
		gl20.glVertexAttrib3fv(indx, values);
	}

	@Override
	public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
		gl20.glVertexAttrib4f(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv(int indx, FloatBuffer values) {
		gl20.glVertexAttrib4fv(indx, values);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type,
			boolean normalized, int stride, Buffer ptr) {
		if (bindedVBO != 0) {
			bindedVBO = 0;
			gl20.glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
		if (bindedIndice != 0) {
			bindedIndice = 0;
			gl20.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		enableVertexAttribArray(indx);
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type,
			boolean normalized, int stride, int ptr) {
		enableVertexAttribArray(indx);
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
	
	private void enableVertexAttribArray(int indx) {
		if (!this.enabledVertexAttribArray.contains(indx)) {
			this.enabledVertexAttribArray.add(indx);
			gl20.glEnableVertexAttribArray(indx);
		}
		this.usedVertexAttribArray.add(indx);
	}
	
	private void disableUnusedVertexAttribArray() {
		for (Integer indx : this.enabledVertexAttribArray)
			if (!this.usedVertexAttribArray.contains(indx)) {
				this.disablingVertexAttribArray.add(indx);
			}
		this.usedVertexAttribArray.clear();
		for (Integer indx : this.disablingVertexAttribArray) {
			this.enabledVertexAttribArray.remove(indx);
			gl20.glDisableVertexAttribArray(indx);
		}
		this.disablingVertexAttribArray.clear();
	}

}
