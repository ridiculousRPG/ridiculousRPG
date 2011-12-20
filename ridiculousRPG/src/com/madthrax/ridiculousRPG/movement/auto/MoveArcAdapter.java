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

package com.madthrax.ridiculousRPG.movement.auto;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event by the given origin and
 * angle. The move waits if a blocking event exists on it's way.<br>
 * It also supports rotating the texture of the event automatically.<br>
 * Furthermore it allows stretching the arc either in x or in y direction. This
 * allows you to move elliptic curves.<br>
 * After succeeding the switch finished is set to true.
 * 
 * @author Alexander Baumgartner
 */
public class MoveArcAdapter extends MovementHandler {
	private Vector2 originReset;
	private Vector2 origin;
	private float angle;
	private boolean rotateTexture;
	private Vector2 stretch;
	private boolean fixedSpeed;
	private Vector2 drift;
	// initialized by the first call of tryMove(...)
	private float radius = Float.NaN;
	private float arcCorrection = Float.NaN;
	private float startArc = Float.NaN;
	private float correctX, correctY;

	private float lastMoveArc = 0f;
	private float entireMoveArc = 0f;

	private static final float PI = MathUtils.PI;
	private static final float PI2 = PI * 2;

	protected MoveArcAdapter(Vector2 origin, float angle,
			boolean rotateTexture, boolean variableSpeed, Vector2 stretch,
			Vector2 drift) {
		this.originReset = origin;
		this.origin = origin.cpy();
		this.angle = angle * MathUtils.degreesToRadians;
		this.rotateTexture = rotateTexture;
		this.stretch = stretch;
		this.fixedSpeed = variableSpeed;
		this.drift = drift;
	}

	/**
	 * This {@link MovementHandler} tries to move an event by the given origin
	 * and angle. The move waits if a blocking event exists on it's way.<br>
	 * It also supports rotating the texture of the event automatically.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param origin
	 *            The origin of the cycle
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @return
	 */
	public static MoveArcAdapter $(Vector2 origin, float angle) {
		return $(origin, angle, false, false);
	}

	/**
	 * This {@link MovementHandler} tries to move an event by the given origin
	 * and angle. The move waits if a blocking event exists on it's way.<br>
	 * It also supports rotating the texture of the event automatically.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param origin
	 *            The origin of the cycle
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @param fixedSpeed
	 *            If this is set to true, the speed will be fixed. (Default
	 *            value = false = dynamic speed)
	 * @return
	 */
	public static MoveArcAdapter $(Vector2 origin, float angle,
			boolean rotateTexture, boolean variableSpeed) {
		return $(origin, angle, rotateTexture, variableSpeed, new Vector2(1f,
				1f), new Vector2());
	}

	/**
	 * This {@link MovementHandler} tries to move an event by the given origin
	 * and angle. The move waits if a blocking event exists on it's way.<br>
	 * It also supports rotating the texture of the event automatically.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param origin
	 *            The origin of the cycle
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @param fixedSpeed
	 *            If this is set to true, the speed will be fixed. (Default
	 *            value = false = dynamic speed)
	 * @param stretch
	 *            Stretch the curve in x,y direction by the given relative
	 *            amount. (Default value = 1f = no stretching)
	 * @param drift
	 *            Specifies a drift into direction x,y (positive/negative
	 *            allowed).<br>
	 *            x-drift will produce a vertical helix. y-drift will produce a
	 *            horizontal helix. (Default value = 0f = no drift)
	 * @return
	 */
	public static MoveArcAdapter $(Vector2 origin, float angle,
			boolean rotateTexture, boolean variableSpeed, Vector2 stretch,
			Vector2 drift) {
		return new MoveArcAdapter(origin, angle, rotateTexture, variableSpeed,
				stretch, drift);
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		float absAngle = Math.abs(angle);
		if ((absAngle > 0f && entireMoveArc >= absAngle) || finished) {
			if (entireMoveArc == 0f)
				event.stop();
			finished = true;
		} else {
			float radius = this.radius;
			// computes distance between origin and event only once.
			if (Float.isNaN(radius)) {
				float evX = event.getX() - origin.x;
				float evY = event.getY() - origin.y;
				if ((radius = (float) Math.sqrt(Math.pow(evX / stretch.x, 2)
						+ Math.pow(evY / stretch.y, 2))) == 0f)
					return;
				this.radius = radius;
				double stretchCorrection = Math.sqrt(stretch.x * stretch.x
						+ stretch.y * stretch.y);
				this.arcCorrection = (float) (1. / (radius * stretchCorrection));
				this.correctX = (float) (stretch.x / stretchCorrection);
				this.correctY = (float) (stretch.y / stretchCorrection);
				this.startArc = (float) Math.asin(evY / radius);
				if (evX < 0f)
					startArc = PI - startArc;
				else if (startArc < 0f)
					startArc = PI2 + startArc;
			}
			float arc = event.moveSpeed.computeStretch(deltaTime)
					* arcCorrection;
			lastMoveArc = arc;
			// greater zero or positive zero
			if (angle > 0f || Float.floatToRawIntBits(angle) == 0f) {
				arc += startArc + entireMoveArc;
				arc %= PI2;
			} else {
				arc = startArc - entireMoveArc - arc;
				arc %= PI2;
				if (arc < 0f)
					arc += PI2;
			}
			float x = MathUtils.cos(arc);
			float y = MathUtils.sin(arc);
			event.offerMoveTo(origin.x + x * radius * stretch.x, origin.y + y
					* radius * stretch.y);
			if (fixedSpeed) {
				// it's only an approximation but it should be good enough
				// for most cases
				lastMoveArc *= Math.cbrt(x * x * correctX + y * y * correctY);
			}
			entireMoveArc += lastMoveArc;
			if (angle == 0f)
				entireMoveArc %= PI2;
			origin.x += drift.x * deltaTime;
			origin.y += drift.y * deltaTime;
			if (rotateTexture && event instanceof EventObject) {
				EventObject ev = (EventObject) event;
				ev.rotation = MathUtils.atan2(y * stretch.x, x * stretch.y)
						* MathUtils.radiansToDegrees;
				// lower zero or negative zero
				if (angle < 0f
						|| (angle == 0f && Float.floatToRawIntBits(angle) != 0f)) {
					ev.rotation = ev.rotation - 180f;
				}
			}
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		entireMoveArc -= lastMoveArc;
		event.stop();
	}

	@Override
	public void reset() {
		super.reset();
		origin.set(originReset);
		radius = Float.NaN;
		startArc = Float.NaN;
		entireMoveArc = 0f;
		lastMoveArc = 0f;
	}
}
