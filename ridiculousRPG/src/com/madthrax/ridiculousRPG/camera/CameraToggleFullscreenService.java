package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.GameService;

public class CameraToggleFullscreenService extends InputAdapter implements GameService {
	@Override
	public boolean keyUp(int keycode) {
		if (GameBase.isControlKeyPressed() || Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			if (keycode==Input.Keys.F || keycode==Input.Keys.ENTER) {
				return toggleFullscreen();
			}
		}
		return false;
	}
	public static boolean toggleFullscreen() {
		try {
			GameBase.fullscreen = !GameBase.fullscreen;
			// resize is called
			Gdx.graphics.setDisplayMode(GameBase.originalWidth, GameBase.originalHeight, GameBase.fullscreen);
			return true;
		} catch (Throwable notTooBad) {}
		return false;
	}
	@Override
	public void freeze() {}
	@Override
	public void unfreeze() {}
	@Override
	public void dispose() {}
}
