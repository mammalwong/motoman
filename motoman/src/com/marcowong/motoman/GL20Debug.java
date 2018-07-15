package com.marcowong.motoman;

import java.io.PrintStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL20;

public class GL20Debug implements GL20 {
	private GL20 gl20;
	private PrintStream out;
	
	public GL20Debug(GL20 gl20, PrintStream out) {
		this.gl20 = gl20;
		this.out = out;
	}
	
	public void renderBegin() {
		out.println("render begin ===========");
	}
	
	public void renderEnd() {
		out.println("render end =============");
	}

	@Override
	public void glActiveTexture(int texture) {
		out.println("glActiveTexture texture:" + texture);
		gl20.glActiveTexture(texture);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		out.println("glBindTexture target:" + target + " texture:" + texture);
		gl20.glBindTexture(target, texture);
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		out.println("glBlendFunc sfactor:" + sfactor + " dfactor:" + dfactor);
		gl20.glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glClear(int mask) {
		out.println("glClear mask:" + mask);
		gl20.glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		out.println("glClearColor red:" + red + " green:" + green + " blue:" + blue + " alpha:" + alpha);
		gl20.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf(float depth) {
		out.println("glClearDepthf depth:" + depth);
		gl20.glClearDepthf(depth);
	}

	@Override
	public void glClearStencil(int s) {
		out.println("glClearStencil s:" + s);
		gl20.glClearStencil(s);
	}

	@Override
	public void glColorMask(boolean red, boolean green, boolean blue,
			boolean alpha) {
		out.println("glColorMask red:" + red + " green:" + green + " blue:" + blue + " alpha:" + alpha);
		gl20.glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D(int target, int level,
			int internalformat, int width, int height, int border,
			int imageSize, Buffer data) {
		out.println("glCompressedTexImage2D target:" + target + " level:" + level + " internalformat:" + internalformat + " width:" + width + " height:" + height + " border:" + border + " imageSize:" + imageSize + " data:" + data);
		gl20.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override
	public void glCompressedTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int imageSize,
			Buffer data) {
		out.println("glCompressedTexImage2D target:" + target + " level:" + level + " xoffset:" + xoffset + " yoffset:" + yoffset + " width:" + width + " height:" + height + " imageSize:" + imageSize + " data:" + data);
		gl20.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override
	public void glCopyTexImage2D(int target, int level, int internalformat,
			int x, int y, int width, int height, int border) {
		out.println("glCopyTexImage2D target:" + target + " level:" + level + " internalformat:" + internalformat + " x:" + x + " y:" + y + " width:" + width + " height:" + height + " border:" + border);
		gl20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int x, int y, int width, int height) {
		out.println("glCopyTexSubImage2D target:" + target + " level:" + level + " xoffset:" + xoffset + " yoffset:" + yoffset + " x:" + x + " y:" + y + " width:" + width + " height:" + height);
		gl20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace(int mode) {
		out.println("glCullFace mode:" + mode);
		gl20.glCullFace(mode);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		out.println("glDeleteTextures n:" + n + " textures:" + textures);
		gl20.glDeleteTextures(n, textures);
	}

	@Override
	public void glDepthFunc(int func) {
		out.println("glDepthFunc func:" + func);
		gl20.glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		out.println("glDepthMask flag:" + flag);
		gl20.glDepthMask(flag);
	}

	@Override
	public void glDepthRangef(float zNear, float zFar) {
		out.println("glDepthRangef zNear:" + zNear + " zFar:" + zFar);
		gl20.glDepthRangef(zNear, zFar);
	}

	@Override
	public void glDisable(int cap) {
		out.println("glDisable cap:" + cap);
		gl20.glDisable(cap);
	}

	@Override
	public void glDrawArrays(int mode, int first, int count) {
		out.println("glDrawArrays mode:" + mode + " first:" + first + " count:" + count);
		gl20.glDrawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		out.println("glDrawElements mode:" + mode + " count:" + count + " type:" + type + " indices:" + indices);
		gl20.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnable(int cap) {
		out.println("glEnable cap:" + cap);
		gl20.glEnable(cap);
	}

	@Override
	public void glFinish() {
		out.println("glFinish");
		gl20.glFinish();
	}

	@Override
	public void glFlush() {
		out.println("glFlush");
		gl20.glFlush();
	}

	@Override
	public void glFrontFace(int mode) {
		out.println("glFrontFace mode:" + mode);
		gl20.glFrontFace(mode);
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		out.println("glGenTextures n:" + n + " textures:" + textures);
		gl20.glGenTextures(n, textures);
	}

	@Override
	public int glGetError() {
		out.println("glGetError");
		return gl20.glGetError();
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		out.println("glGetIntegerv pname:" + pname + " params:" + params);
		gl20.glGetIntegerv(pname, params);
	}

	@Override
	public String glGetString(int name) {
		out.println("glGetString name:" + name);
		return gl20.glGetString(name);
	}

	@Override
	public void glHint(int target, int mode) {
		out.println("glHint target:" + target + " mode:" + mode);
		gl20.glHint(target, mode);
	}

	@Override
	public void glLineWidth(float width) {
		out.println("glLineWidth width:" + width);
		gl20.glLineWidth(width);
	}

	@Override
	public void glPixelStorei(int pname, int param) {
		out.println("glPixelStorei pname:" + pname + " param:" + param);
		gl20.glPixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset(float factor, float units) {
		out.println("glPolygonOffset factor:" + factor + " units:" + units);
		gl20.glPolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels(int x, int y, int width, int height, int format,
			int type, Buffer pixels) {
		out.println("glReadPixels x:" + x + " y:" + y + " width:" + width + " height:" + height + " format:" + format + " type:" + type + " pixels:" + pixels); 
		gl20.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		out.println("glScissor x:" + x + " y:" + y + " width:" + width + " height:" + height);
		gl20.glScissor(x, y, width, height);
	}

	@Override
	public void glStencilFunc(int func, int ref, int mask) {
		out.println("glStencilFunc func:" + func + " ref:" + ref + " mask:" + mask);
		gl20.glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilMask(int mask) {
		out.println("glStencilMask mask:" + mask);
		gl20.glStencilMask(mask);
	}

	@Override
	public void glStencilOp(int fail, int zfail, int zpass) {
		out.println("glStencilOp fail:" + fail + " zfail:" + zfail + " zpass:" + zpass);
		gl20.glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat,
			int width, int height, int border, int format, int type,
			Buffer pixels) {
		out.println("glTexImage2D target:" + target + " level:" + level + " internalformat:" + internalformat + " width:" + width + " height:" + height + " border:" + border + " format:" + format + " type:" + type + " pixels:" + pixels);
		gl20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		out.println("glTexParameterf target:" + target + " pname:" + pname + " param:" + param);
		gl20.glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int type,
			Buffer pixels) {
		out.println("glTexSubImage2D target:" + target + " level:" + level + " xoffset:" + xoffset + " yoffset:" + yoffset + " width:" + width + " height:" + height + " format:" + format + " type:" + type + " pixels:" + pixels);
		gl20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		out.println("glViewport x:" + x + " y:" + y + " width:" + width + " height:" + height);
		gl20.glViewport(x, y, width, height);
	}

	@Override
	public void glAttachShader(int program, int shader) {
		out.println("glAttachShader program:" + program + " shader:" + shader);
		gl20.glAttachShader(program, shader);
	}

	@Override
	public void glBindAttribLocation(int program, int index, String name) {
		out.println("glBindAttribLocation program:" + program + " index:" + index + " name:" + name);
		gl20.glBindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		out.println("glBindBuffer target:" + target + " buffer:" + buffer);
		gl20.glBindBuffer(target, buffer);
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		out.println("glBindFramebuffer target:" + target + " framebuffer:" + framebuffer);
		gl20.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer(int target, int renderbuffer) {
		out.println("glBindRenderbuffer target:" + target + " renderbuffer:" + renderbuffer);
		gl20.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void glBlendColor(float red, float green, float blue, float alpha) {
		out.println("glBlendColor red:" + red + " green:" + green + " blue:" + blue + " alpha:" + alpha);
		gl20.glBlendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation(int mode) {
		out.println("glBlendEquation mode:" + mode);
		gl20.glBlendEquation(mode);
	}

	@Override
	public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
		out.println("glBlendEquationSeparate modeRGB:" + modeRGB + " modeAlpha");
		gl20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
			int dstAlpha) {
		out.println("glBlendFuncSeparate srcRGB:" + srcRGB + " dstRGB:" + dstRGB + " srcAlpha:" + srcAlpha + " dstAlpha:" + dstAlpha);
		gl20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage) {
		out.println("glBufferData target:" + target + " size:" + size + " data:" + data + " usage:" + usage);
		gl20.glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		out.println("glBufferSubData target:" + target + " offset:" + offset + " size:" + size + " data:" + data);
		gl20.glBufferSubData(target, offset, size, data);
	}

	@Override
	public int glCheckFramebufferStatus(int target) {
		out.println("glCheckFramebufferStatus target:" + target);
		return gl20.glCheckFramebufferStatus(target);
	}

	@Override
	public void glCompileShader(int shader) {
		out.println("glCompileShader shader:" + shader);
		gl20.glCompileShader(shader);
	}

	@Override
	public int glCreateProgram() {
		out.println("glCreateProgram");
		return gl20.glCreateProgram();
	}

	@Override
	public int glCreateShader(int type) {
		out.println("glCreateShader type:" + type);
		return gl20.glCreateShader(type);
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		out.println("glDeleteBuffers n:" + n + " buffers:" + buffers);
		gl20.glDeleteBuffers(n, buffers);
	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		out.println("glDeleteFramebuffers n:" + n + " framebuffers:" + framebuffers);
		gl20.glDeleteFramebuffers(n, framebuffers);
	}

	@Override
	public void glDeleteProgram(int program) {
		out.println("glDeleteProgram program:" + program);
		gl20.glDeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		out.println("glDeleteRenderbuffers n:" + n + " renderbuffers:" + renderbuffers);
		gl20.glDeleteRenderbuffers(n, renderbuffers);
	}

	@Override
	public void glDeleteShader(int shader) {
		out.println("glDeleteShader shader:" + shader);
		gl20.glDeleteShader(shader);
	}

	@Override
	public void glDetachShader(int program, int shader) {
		out.println("glDetachShader program:" + program + " shader:" + shader);
		gl20.glDetachShader(program, shader);
	}

	@Override
	public void glDisableVertexAttribArray(int index) {
		out.println("glDisableVertexAttribArray index:" + index);
		gl20.glDisableVertexAttribArray(index);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, int indices) {
		out.println("glDrawElements mode:" + mode + " count:" + count + " type:" + type + " indices:" + indices);
		gl20.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnableVertexAttribArray(int index) {
		out.println("glEnableVertexAttribArray index:" + index);
		gl20.glEnableVertexAttribArray(index);
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment,
			int renderbuffertarget, int renderbuffer) {
		out.println("glFramebufferRenderbuffer target:" + target + " attachment:" + attachment + " renderbuffertarget:" + renderbuffertarget + " renderbuffer:" + renderbuffer);
		gl20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment,
			int textarget, int texture, int level) {
		out.println("glFramebufferTexture2D target:" + target + " attachment:" + attachment + " textarget:" + textarget + " texture:" + texture + " level:" + level);
		gl20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		out.println("glGenBuffers n:" + n + " buffers:" + buffers);
		gl20.glGenBuffers(n, buffers);
	}

	@Override
	public void glGenerateMipmap(int target) {
		out.println("glGenerateMipmap target:" + target);
		gl20.glGenerateMipmap(target);
	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		out.println("glGenFramebuffers n:" + n + " framebuffers:" + framebuffers);
		gl20.glGenFramebuffers(n, framebuffers);
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		out.println("glGenRenderbuffers n:" + n + " renderbuffers:" + renderbuffers);
		gl20.glGenRenderbuffers(n, renderbuffers);
	}

	@Override
	public String glGetActiveAttrib(int program, int index, IntBuffer size,
			Buffer type) {
		out.println("glGetActiveAttrib program:" + program + " index:" + index + " size:" + size + " type:" + type);
		return gl20.glGetActiveAttrib(program, index, size, type);
	}

	@Override
	public String glGetActiveUniform(int program, int index, IntBuffer size,
			Buffer type) {
		out.println("glGetActiveUniform program:" + program + " index:" + index + " size:" + size + " type:" + type);
		return gl20.glGetActiveUniform(program, index, size, type);
	}

	@Override
	public void glGetAttachedShaders(int program, int maxcount, Buffer count,
			IntBuffer shaders) {
		out.println("glGetAttachedShaders program:" + program + " maxcount:" + maxcount + " count:" + count + " shaders:" + shaders);
		gl20.glGetAttachedShaders(program, maxcount, count, shaders);
	}

	@Override
	public int glGetAttribLocation(int program, String name) {
		out.println("glGetAttribLocation program:" + program + " name:" + name);
		return gl20.glGetAttribLocation(program, name);
	}

	@Override
	public void glGetBooleanv(int pname, Buffer params) {
		out.println("glGetBooleanv pname:" + pname + " params:" + params);
		gl20.glGetBooleanv(pname, params);
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		out.println("glGetBufferParameteriv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		out.println("glGetFloatv pname:" + pname + " params:" + params);
		gl20.glGetFloatv(pname, params);
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv(int target,
			int attachment, int pname, IntBuffer params) {
		out.println("glGetFramebufferAttachmentParameteriv target:" + target + " attachment:" + attachment + " pname:" + pname + " params:" + params);
		gl20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
	public void glGetProgramiv(int program, int pname, IntBuffer params) {
		out.println("glGetProgramiv program:" + program + " pname:" + pname + " params:" + params);
		gl20.glGetProgramiv(program, pname, params);
	}

	@Override
	public String glGetProgramInfoLog(int program) {
		out.println("glGetProgramInfoLog program:" + program);
		return gl20.glGetProgramInfoLog(program);
	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname,
			IntBuffer params) {
		out.println("glGetRenderbufferParameteriv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetShaderiv(int shader, int pname, IntBuffer params) {
		out.println("glGetShaderiv shader:" + shader + " pname:" + pname + " params:" + params);
		gl20.glGetShaderiv(shader, pname, params);
	}

	@Override
	public String glGetShaderInfoLog(int shader) {
		out.println("glGetShaderInfoLog shader:" + shader);
		return gl20.glGetShaderInfoLog(shader);
	}

	@Override
	public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
			IntBuffer range, IntBuffer precision) {
		out.println("glGetShaderPrecisionFormat shadertype:" + shadertype + " precisiontype:" + precisiontype + " range:" + range + " precision:" + precision);
		gl20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
	}

	@Override
	public void glGetShaderSource(int shader, int bufsize, Buffer length,
			String source) {
		out.println("glGetShaderSource shader:" + shader + " bufsize:" + bufsize + " length:" + length + " source:" + source);
		gl20.glGetShaderSource(shader, bufsize, length, source);
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
		out.println("glGetTexParamterfv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
		out.println("glGetTexParameteriv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glGetTexParameteriv(target, pname, params);
	}

	@Override
	public void glGetUniformfv(int program, int location, FloatBuffer params) {
		out.println("glGetUniformfv program:" + program + " location:" + location + " params:" + params);
		gl20.glGetUniformfv(program, location, params);
	}

	@Override
	public void glGetUniformiv(int program, int location, IntBuffer params) {
		out.println("glGetUniformiv program:" + program + " location:" + location + " params:" + params);
		gl20.glGetUniformiv(program, location, params);
	}

	@Override
	public int glGetUniformLocation(int program, String name) {
		out.println("glGetUniformLocation program:" + program + " name:" + name);
		return gl20.glGetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
		out.println("glGetVertexAttribfv index:" + index + " pname:" + pname + " params:" + params);
		gl20.glGetVertexAttribfv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
		out.println("glGetVertexAttribiv index:" + index + " pname:" + pname + " params:" + params);
		gl20.glGetVertexAttribiv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
		out.println("glGetVertexAttribPointerv index:" + index + " pname:" + pname + " pointer:" + pointer);
		gl20.glGetVertexAttribPointerv(index, pname, pointer);
	}

	@Override
	public boolean glIsBuffer(int buffer) {
		out.println("glIsBuffer buffer:" + buffer);
		return gl20.glIsBuffer(buffer);
	}

	@Override
	public boolean glIsEnabled(int cap) {
		out.println("glIsEnabled cap:" + cap);
		return gl20.glIsEnabled(cap);
	}

	@Override
	public boolean glIsFramebuffer(int framebuffer) {
		out.println("glIsFramebuffer framebuffer:" + framebuffer);
		return gl20.glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsProgram(int program) {
		out.println("glIsProgram program:" + program);
		return gl20.glIsProgram(program);
	}

	@Override
	public boolean glIsRenderbuffer(int renderbuffer) {
		out.println("glIsRenderbuffer renderbuffer:" + renderbuffer);
		return gl20.glIsRenderbuffer(renderbuffer);
	}

	@Override
	public boolean glIsShader(int shader) {
		out.println("glIsShader shader:" + shader);
		return gl20.glIsShader(shader);
	}

	@Override
	public boolean glIsTexture(int texture) {
		out.println("glIsTexture texture:" + texture);
		return gl20.glIsTexture(texture);
	}

	@Override
	public void glLinkProgram(int program) {
		out.println("glLinkProgram program:" + program);
		gl20.glLinkProgram(program);
	}

	@Override
	public void glReleaseShaderCompiler() {
		out.println("glReleaseShaderCompiler");
		gl20.glReleaseShaderCompiler();
	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat,
			int width, int height) {
		out.println("glRenderbufferStorage target:" + target + " internalformat:" + internalformat + " width:" + width + " height:" + height);
		gl20.glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage(float value, boolean invert) {
		out.println("glSampleCoverage value:" + value + " invert:" + invert);
		gl20.glSampleCoverage(value, invert);
	}

	@Override
	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat,
			Buffer binary, int length) {
		out.println("glShaderBinary n:" + n + " shaders:" + shaders + " binaryformat:" + binaryformat + " binary:" + binary + " length:" + length);
		gl20.glShaderBinary(n, shaders, binaryformat, binary, length);
	}

	@Override
	public void glShaderSource(int shader, String string) {
		out.println("glShaderSource shader:" + shader + " string:" + string);
		gl20.glShaderSource(shader, string);
	}

	@Override
	public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
		out.println("glStencilFuncSeperate face:" + face + " func:" + func + " ref:" + ref + " mask:" + mask);
		gl20.glStencilFuncSeparate(face, func, ref, mask);
	}

	@Override
	public void glStencilMaskSeparate(int face, int mask) {
		out.println("glStencilMaskSeparate face:" + face + " mask:" + mask);
		gl20.glStencilMaskSeparate(face, mask);
	}

	@Override
	public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
		out.println("glStencilOpSeparate face:" + face + " fail:" + fail + " zfail:" + zfail + " zpass:" + zpass);
		gl20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) {
		out.println("glTexParamterfv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glTexParameterfv(target, pname, params);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		out.println("glTexParamteri target:" + target + " pname:" + pname + " param:" + param);
		gl20.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) {
		out.println("glTexParameteriv target:" + target + " pname:" + pname + " params:" + params);
		gl20.glTexParameteriv(target, pname, params);
	}

	@Override
	public void glUniform1f(int location, float x) {
		out.println("glUniform1f location:" + location + " x:" + x);
		gl20.glUniform1f(location, x);
	}

	@Override
	public void glUniform1fv(int location, int count, FloatBuffer v) {
		out.println("glUniform1fv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform1fv(location, count, v);
	}

	@Override
	public void glUniform1i(int location, int x) {
		out.println("glUniform1i location:" + location + " x:" + x);
		gl20.glUniform1i(location, x);
	}

	@Override
	public void glUniform1iv(int location, int count, IntBuffer v) {
		out.println("glUniform1iv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform1iv(location, count, v);
	}

	@Override
	public void glUniform2f(int location, float x, float y) {
		out.println("glUniform2f location:" + location + " x:" + x + " y:" + y);
		gl20.glUniform2f(location, x, y);
	}

	@Override
	public void glUniform2fv(int location, int count, FloatBuffer v) {
		out.println("glUniform2fv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform2fv(location, count, v);
	}

	@Override
	public void glUniform2i(int location, int x, int y) {
		out.println("glUniform2i location:" + location + " x:" + x + " y:" + y);
		gl20.glUniform2i(location, x, y);
	}

	@Override
	public void glUniform2iv(int location, int count, IntBuffer v) {
		out.println("glUniform2iv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform2iv(location, count, v);
	}

	@Override
	public void glUniform3f(int location, float x, float y, float z) {
		out.println("glUniform3f location:" + location + " x:" + x + " y:" + y + " z:" + z);
		gl20.glUniform3f(location, x, y, z);
	}

	@Override
	public void glUniform3fv(int location, int count, FloatBuffer v) {
		out.println("glUniform3fv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform3fv(location, count, v);
	}

	@Override
	public void glUniform3i(int location, int x, int y, int z) {
		out.println("glUniform3i location:" + location + " x:" + x + " y:" + y + " z:" + z);
		gl20.glUniform3i(location, x, y, z);
	}

	@Override
	public void glUniform3iv(int location, int count, IntBuffer v) {
		out.println("glUniform3iv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform3iv(location, count, v);
	}

	@Override
	public void glUniform4f(int location, float x, float y, float z, float w) {
		out.println("glUniform4f location:" + location  + " x:" + x + " y:" + y + " z:" + z + " w:" + w);
		gl20.glUniform4f(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv(int location, int count, FloatBuffer v) {
		out.println("glUniform4fv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform4fv(location, count, v);
	}

	@Override
	public void glUniform4i(int location, int x, int y, int z, int w) {
		out.println("glUniform4i location:" + location + " x:" + x + " y:" + y + " z:" + z + " w:" + w);
		gl20.glUniform4i(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv(int location, int count, IntBuffer v) {
		out.println("glUniform4iv location:" + location + " count:" + count + " v:" + v);
		gl20.glUniform4iv(location, count, v);
	}

	@Override
	public void glUniformMatrix2fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		out.println("glUniformMatrix2fv location:" + location + " count:" + count + " transpose:" + transpose + " value:" + value);
		gl20.glUniformMatrix2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		out.println("glUniformMatrix4fv location:" + location + " count:" + count + " transpose:" + transpose + " value:" + value);
		gl20.glUniformMatrix3fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4fv(int location, int count, boolean transpose,
			FloatBuffer value) {
		out.println("glUniformMatrix4fv location:" + location + " count:" + count + " transpose:" + transpose + " value:" + value);
		gl20.glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override
	public void glUseProgram(int program) {
		out.println("glUseProgram program:" + program);
		gl20.glUseProgram(program);
	}

	@Override
	public void glValidateProgram(int program) {
		out.println("glValidateProgram program:" + program);
		gl20.glValidateProgram(program);
	}

	@Override
	public void glVertexAttrib1f(int indx, float x) {
		out.println("glVertexAttrib1f indx:" + indx + " x:" + x);
		gl20.glVertexAttrib1f(indx, x);
	}

	@Override
	public void glVertexAttrib1fv(int indx, FloatBuffer values) {
		out.println("glVertexAttrib1fv indx:" + indx + " values:" + values);
		gl20.glVertexAttrib1fv(indx, values);
	}

	@Override
	public void glVertexAttrib2f(int indx, float x, float y) {
		out.println("glVertexAttrib2f indx:" + indx + " x:" + x + " y:" + y);
		gl20.glVertexAttrib2f(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv(int indx, FloatBuffer values) {
		out.println("glVertexAttrib2fv indx:" + indx + " values:" + values);
		gl20.glVertexAttrib2fv(indx, values);
	}

	@Override
	public void glVertexAttrib3f(int indx, float x, float y, float z) {
		out.println("glVertexAttrib3f indx:" + indx + " x:" + x + " y:" + y + " z:" + z);
		gl20.glVertexAttrib3f(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv(int indx, FloatBuffer values) {
		out.println("glVertexAttrib3fv indx:" + indx +" values:" + values);
		gl20.glVertexAttrib3fv(indx, values);
	}

	@Override
	public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
		out.println("glVertexAttrib4f indx:" + indx + " x:" + x + " y:" + y + " z:" + z + " w:" + w);
		gl20.glVertexAttrib4f(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv(int indx, FloatBuffer values) {
		out.println("glVertexAttrib4fv indx:" + indx + " values:" + values);
		gl20.glVertexAttrib4fv(indx, values);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type,
			boolean normalized, int stride, Buffer ptr) {
		out.println("glVertexAttribPointer indx:" + indx + " size:" + size + " type:" + type + " normalized:" + normalized + " stride:" + stride + " ptr:" + ptr);
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override
	public void glVertexAttribPointer(int indx, int size, int type,
		boolean normalized, int stride, int ptr) {
		out.println("glVertexAttribPointer indx:" + indx + " size:" + size + " type:" + type + " normailized:" + normalized + " stride:" + stride + " ptr:" + ptr);
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

}
