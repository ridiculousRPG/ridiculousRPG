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

package com.ridiculousRPG.camera;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.map.MapRenderService;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.service.Computable;
import com.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * This service may be used to track an moving event with the camera
 * 
 * @author Alexander Baumgartner
 */
public class CameraTrackMovableService extends GameServiceDefaultImpl implements
		Computable, Serializable {
	private static final long serialVersionUID = 1L;

	private transient Movable trackObj;

	private float oldX, oldY;

	public CameraTrackMovableService() {
	}

	/**
	 * Defines the object (event) which should be tracked by the camera
	 * 
	 * @param trackObj
	 * @param centerIt
	 */
	public CameraTrackMovableService(Movable trackObj, boolean centerIt) {
		setTrackObj(trackObj, centerIt);
	}

	/**
	 * Defines the object (event) which should be tracked by the camera
	 * 
	 * @param trackObj
	 * @param centerIt
	 */
	public void setTrackObj(Movable trackObj, boolean centerIt) {
		this.trackObj = trackObj;
		if (centerIt)
			centerTrackObj();
	}

	public Movable getTrackObj() {
		return trackObj;
	}

	public void compute(float deltaTime, boolean pushButtonPressed) {
		if (trackObj == null)
			return;
		float newX = trackObj.getX();
		float newY = trackObj.getY();
		if (oldX != newX || oldY != newY) {
			Camera cam = GameBase.$().getCamera();
			cam.translate(newX - oldX, newY - oldY, 0f);
			cam.update();
			oldX = newX;
			oldY = newY;
		}
	}

	/**
	 * Centers the camera to the tracked object (event)
	 */
	public void centerTrackObj() {
		if (trackObj == null)
			return;
		Camera cam = GameBase.$().getCamera();
		cam.lookAt(trackObj.getCenterX(), trackObj.getCenterY(), 0f);
		cam.update();
		oldX = trackObj.getX();
		oldY = trackObj.getY();
	}

	public void dispose() {
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (trackObj instanceof EventObject) {
			EventObject evObj = (EventObject) trackObj;
			if (evObj.name != null) {
				out.writeBoolean(true);
				out.writeObject(evObj.name);
				return;
			}
		}
		out.writeBoolean(false);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		if (in.readBoolean()) {
			String objName = (String) in.readObject();
			Movable obj = GameBase.$().getGlobalEvents().get(objName);
			if (obj == null) {
				Array<MapRenderService> serviceIter = GameBase
						.$serviceProvider().getServices(MapRenderService.class);
				for (int i = 0; obj == null && i < serviceIter.size; i++) {
					obj = serviceIter.get(i).getMap().get(objName);
				}
			}
			this.trackObj = obj;
		}
	}
}
