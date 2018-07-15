package com.marcowong.motoman;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;

public class GraphicsOptimized implements Graphics {
	
	private Graphics g;
	private GL20 gl20;
	
	public GraphicsOptimized(Graphics g, GL20 gl20) {
		this.g = g;
		this.gl20 = gl20;
	}

	@Override
	public boolean isGL11Available() {
		return g.isGL11Available();
	}

	@Override
	public boolean isGL20Available() {
		return g.isGL20Available();
	}

	@Override
	public GLCommon getGLCommon() {
		return gl20;
	}

	@Override
	public GL10 getGL10() {
		return g.getGL10();
	}

	@Override
	public GL11 getGL11() {
		return g.getGL11();
	}

	@Override
	public GL20 getGL20() {
		return gl20;
	}

	@Override
	public GLU getGLU() {
		return g.getGLU();
	}

	@Override
	public int getWidth() {
		return g.getWidth();
	}

	@Override
	public int getHeight() {
		return g.getHeight();
	}

	@Override
	public float getDeltaTime() {
		return g.getDeltaTime();
	}

	@Override
	public float getRawDeltaTime() {
		return g.getRawDeltaTime();
	}

	@Override
	public int getFramesPerSecond() {
		return g.getFramesPerSecond();
	}

	@Override
	public GraphicsType getType() {
		return g.getType();
	}

	@Override
	public float getPpiX() {
		return g.getPpiX();
	}

	@Override
	public float getPpiY() {
		return g.getPpiY();
	}

	@Override
	public float getPpcX() {
		return g.getPpcX();
	}

	@Override
	public float getPpcY() {
		return g.getPpcY();
	}

	@Override
	public float getDensity() {
		return g.getDensity();
	}

	@Override
	public boolean supportsDisplayModeChange() {
		return g.supportsDisplayModeChange();
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return g.getDisplayModes();
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		return g.getDesktopDisplayMode();
	}

	@Override
	public boolean setDisplayMode(DisplayMode displayMode) {
		return g.setDisplayMode(displayMode);
	}

	@Override
	public boolean setDisplayMode(int width, int height, boolean fullscreen) {
		return g.setDisplayMode(width, height, fullscreen);
	}

	@Override
	public void setTitle(String title) {
		g.setTitle(title);
	}

	@Override
	public void setVSync(boolean vsync) {
		g.setVSync(vsync);
	}

	@Override
	public BufferFormat getBufferFormat() {
		return g.getBufferFormat();
	}

	@Override
	public boolean supportsExtension(String extension) {
		return g.supportsExtension(extension);
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
		g.setContinuousRendering(isContinuous);
	}

	@Override
	public boolean isContinuousRendering() {
		return g.isContinuousRendering();
	}

	@Override
	public void requestRendering() {
		g.requestRendering();
	}

	@Override
	public boolean isFullscreen() {
		return g.isFullscreen();
	}

}
