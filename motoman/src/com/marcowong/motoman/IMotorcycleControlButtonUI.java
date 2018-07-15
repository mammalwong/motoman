package com.marcowong.motoman;

import com.badlogic.gdx.scenes.scene2d.Stage;

public interface IMotorcycleControlButtonUI {
	public void addToUI(Stage stage);
	public void resize(int width, int height);
	public void update();
}
