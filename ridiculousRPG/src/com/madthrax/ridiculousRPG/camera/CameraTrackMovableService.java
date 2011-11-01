package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.graphics.Camera;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

public class CameraTrackMovableService extends GameServiceDefaultImpl implements Computable {
	private Movable trackObj;
	private float oldX, oldY;

	public CameraTrackMovableService() {}
	/**
	 * Defines the object (event) which should be tracked by the camera
	 * @param trackObj
	 * @param centerIt
	 */
	public CameraTrackMovableService(Movable trackObj, boolean centerIt) {
		setTrackObj(trackObj, centerIt);
	}
	/**
	 * Defines the object (event) which should be tracked by the camera
	 * @param trackObj
	 * @param centerIt
	 */
	public void setTrackObj(Movable trackObj, boolean centerIt) {
		this.trackObj = trackObj;
		if (centerIt) centerTrackObj();
	}
	@Override
	public void compute(float deltaTime, boolean pushButtonPressed) {
		if (trackObj==null) return;
		float newX = trackObj.getX();
		float newY = trackObj.getY();
		if (oldX != newX || oldY != newY) {
			Camera cam = GameBase.camera;
			cam.translate(newX -oldX ,newY - oldY, 0f);
			cam.update();
			oldX = newX;
			oldY = newY;
		}
	}
	/**
	 * Centers the camera to the tracked object (event)
	 */
	public void centerTrackObj() {
		if (trackObj==null) return;
		Camera cam = GameBase.camera;
		cam.lookAt(trackObj.getCenterX(), trackObj.getCenterY(), 0f);
		cam.update();
		oldX = trackObj.getX();
		oldY = trackObj.getY();
	}

	@Override
	public void dispose() {}
}
