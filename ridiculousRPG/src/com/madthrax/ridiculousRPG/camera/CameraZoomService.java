package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.GameService;

public class CameraZoomService extends InputAdapter implements GameService {
	public float zoom = 1f;
	public float maxZoomOut = 5f;
	public float minZoomIn = .5f;
	public float zoomIntervall = 1.1f;

	@Override
	public boolean keyDown(int keycode) {
		if (GameBase.isControlKeyPressed()) {
			if (keycode==Input.Keys.PLUS) {
				zoomIn();
				return true;
			}
			if (keycode==Input.Keys.MINUS) {
				zoomOut();
				return true;
			}
			if (keycode==Input.Keys.NUM_0) {
				zoomNormal();
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		if (GameBase.isControlKeyPressed()) {
			if (amount>0) zoomIn();
			else zoomOut();
			return true;
		}
		return false;
	}
	public void zoomNormal() {
		zoom(0f);
	}
	public void zoomOut() {
		if (zoom < maxZoomOut) zoom(zoomIntervall);
	}
	public void zoomIn() {
		if (zoom > minZoomIn) zoom(1f/zoomIntervall);
	}
	private void zoom(float intervall) {
		Camera cam = GameBase.camera;
		float centerX = cam.viewportWidth*.5f;
		float centerY = cam.viewportHeight*.5f;
		if (intervall==0) {
			zoom = 1f;
			cam.viewportWidth = GameBase.screenWidth;
			cam.viewportHeight = GameBase.screenHeight;
		} else {
			zoom *= intervall;
			cam.viewportHeight *= intervall;
			cam.viewportWidth *= intervall;
		}
		centerX -= cam.viewportWidth*.5f;
		centerY -= cam.viewportHeight*.5f;
		cam.translate(centerX, centerY, 0);
		cam.update();
	}

	@Override
	public void freeze() {}
	@Override
	public void unfreeze() {}
	@Override
	public void dispose() {}
}
