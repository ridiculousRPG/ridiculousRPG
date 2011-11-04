/*
 * Copyright 2011 Alexander Baumgartner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.graphics.Camera;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * @author Alexander Baumgartner
 */
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
	
	public void compute(float deltaTime, boolean pushButtonPressed) {
		if (trackObj==null) return;
		float newX = trackObj.getX();
		float newY = trackObj.getY();
		if (oldX != newX || oldY != newY) {
			Camera cam = GameBase.$().getCamera();
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
		Camera cam = GameBase.$().getCamera();
		cam.lookAt(trackObj.getCenterX(), trackObj.getCenterY(), 0f);
		cam.update();
		oldX = trackObj.getX();
		oldY = trackObj.getY();
	}

	
	public void dispose() {}
}
