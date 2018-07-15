package com.marcowong.motoman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MotomanBGMusic {
	private float volume = 0.25f;
	private Music m1;
	
	public MotomanBGMusic() {
		m1 = Gdx.audio.newMusic(Gdx.files.internal("data/bgm1.ogg"));
		m1.setVolume(volume);
		m1.setLooping(true);
	}
	
	public void play() {
		//m1.play();
	}
	
	public void stop() {
		//m1.stop();
	}
	
	public void gamePause() {
		//m1.stop();
	}
	
	public void gameResume() {
		//m1.play();
	}
	
	public void dispose() {
		stop();
		m1.stop();
		m1.dispose();
	}
}
