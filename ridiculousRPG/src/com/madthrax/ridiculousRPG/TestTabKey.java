package com.madthrax.ridiculousRPG;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class TestTabKey extends InputAdapter implements ApplicationListener {
	public static void main(String[] argv) {
		LwjglApplicationConfiguration conf = new LwjglApplicationConfiguration();
		conf.title = "TEST";
		conf.width = 640;
		conf.height = 480;
		new LwjglApplication(new TestTabKey(), conf);
	}

	@Override
	public boolean keyDown(int keycode) {
		System.out.println("KeyCode="+keycode);
		return false;
	}

	public void create() {
		Gdx.input.setInputProcessor(this);
	}

	public void dispose() {}
	public void pause() {}
	public void render() {}
	public void resize(int width, int height) {}
	public void resume() {}
}
