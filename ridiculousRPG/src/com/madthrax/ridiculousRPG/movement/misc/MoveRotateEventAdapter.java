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

package com.madthrax.ridiculousRPG.movement.misc;

import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.events.Speed;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter doesn't move the event but rotates it by the given speed.<br>
 * It will finish after rotating the specified angle or loop forever if the
 * given angle to rotate is 0.<br>
 * A negative angle will rotate clockwise.
 * @author Alexander Baumgartner
 */
public class MoveRotateEventAdapter extends MovementHandler {
	private Speed rotationSpeed;
	private float angleInDeg;
	private float rotatedAngle = 0f;

	protected MoveRotateEventAdapter(Speed rotationSpeed, float angleInDeg) {
		this.rotationSpeed = rotationSpeed;
		this.angleInDeg = angleInDeg;
	}
	/**
	 * This MovementAdapter doesn't move the event but rotates it by the given speed.<br>
	 * It will finish after rotating the specified angle or loop forever if the
	 * given angle to rotate is 0.<br>
	 * A negative angle will rotate clockwise.
	 * @param rotationSpeed
	 * The speed for rotating this event or null if it should jump
	 * immediately to the given rotation position.
	 * @param angleInDeg
	 * The angle in degrees or +-0 if you want to loop forever.
	 * @return
	 */
	public static MovementHandler $(Speed rotationSpeed, float angleInDeg) {
		return new MoveRotateEventAdapter(rotationSpeed, angleInDeg);
	}
	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (!finished && movable instanceof EventObject) {
			EventObject ev = (EventObject) movable;
			float angle = rotationSpeed==null?360:rotationSpeed.computeStretch(deltaTime);
			if (angleInDeg<0f) {
				ev.rotation -= angle;
				rotatedAngle -= angle;
				if (rotatedAngle <= angleInDeg) {
					finished = true;
					// correction to the exact given angle
					ev.rotation -= rotatedAngle-angleInDeg;
				}
				ev.rotation %= 360;
			} else if (angleInDeg>0f) {
				ev.rotation += angle;
				rotatedAngle += angle;
				if (rotatedAngle >= angleInDeg) {
					finished = true;
					// correction to the exact given angle
					ev.rotation -= rotatedAngle-angleInDeg;
				}
				ev.rotation %= 360;
			} else {
				// positive zero
				if (Float.floatToRawIntBits(angleInDeg)==0) {
					ev.rotation = (ev.rotation+angle)%360;
				// negative zero
				} else {
					ev.rotation = (ev.rotation-angle)%360;
				}
			}
		} else {
			finished = true;
		}
	}
	@Override
	public void reset() {
		super.reset();
		rotatedAngle = 0f;
	}
}
