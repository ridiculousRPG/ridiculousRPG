package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;

public class MainMenuService extends InputAdapter implements GameService, Drawable {
	boolean paused;

	public void exit() {
		Gdx.app.exit();
	}
	@Override
	public boolean keyUp(int keycode) {
		if (keycode==Input.Keys.P) {
			if (paused) {
				return !(paused = !GameServiceProvider.releaseAttention(this));
			}
			return (paused = GameServiceProvider.requestAttention(this, true, false));
		}
		if (keycode==Input.Keys.ESCAPE) {
			exit();
			return true;
		}
		// Ctrl+Alt+9 easter egg function ;)
		if (GameBase.isControlKeyPressed()) {
			if (keycode==Input.Keys.ALT_LEFT || keycode==Input.Keys.NUM_9) {
				try {
					Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
					return true;
				} catch (Throwable notTooBad) {}
			}
		}
		return false;
	}
	@Override
	public void freeze() {}
	@Override
	public void unfreeze() {}
	@Override
	public void dispose() {}
	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		MessageBox.textBoxCentered(spriteBatch, 0);
	}
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
