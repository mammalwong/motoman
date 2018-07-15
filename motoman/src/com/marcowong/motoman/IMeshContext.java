package com.marcowong.motoman;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface IMeshContext {
	public int getNCopies();
	public void render(ShaderProgram shader);
	public void render(ShaderProgram shader, int copies);
}
