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

package com.ridiculousRPG.movement.auto;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ridiculousRPG.movement.CombinedMovesAdapter;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event around the given ellipse.
 * The move waits if a blocking event exists on it's way.<br>
 * After succeeding the switch finished is set to true.<br>
 * It also supports rotating the texture while moving the event around the
 * ellipse.<br>
 * It's only a convenience {@link MovementHandler} which chains a
 * {@link MoveSetXYAdapter} together with a {@link MoveArcAdapter}.
 * 
 * @see {@link CombinedMovesAdapter}, {@link MoveSetXYAdapter},
 *      {@link MoveArcAdapter}
 * @author Alexander Baumgartner
 */
public class MoveEllipseAdapter extends CombinedMovesAdapter {
	private static final long serialVersionUID = 1L;

	/**
	 * {@link StartPoint#BOTTOM} {@link StartPoint#TOP} {@link StartPoint#LEFT}
	 * {@link StartPoint#RIGHT}
	 */
	public enum StartPoint {
		BOTTOM, TOP, LEFT, RIGHT
	}

	/**
	 * This {@link MovementHandler} tries to move an event around the given
	 * ellipse. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param rect
	 *            The rectangle defines the outer bounds of the elipse.
	 * @return
	 */
	public MoveEllipseAdapter(Rectangle rect) {
		this(rect, -360, false);
	}

	/**
	 * This {@link MovementHandler} tries to move an event around the given
	 * ellipse. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param rect
	 *            The rectangle defines the outer bounds of the elipse.
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @return
	 */
	public MoveEllipseAdapter(Rectangle rect, float angle, boolean rotateTexture) {
		this(rect, StartPoint.BOTTOM, angle, rotateTexture);
	}

	/**
	 * This {@link MovementHandler} tries to move an event around the given
	 * ellipse. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param rect
	 *            The rectangle defines the outer bounds of the elipse.
	 * @param startAt
	 *            Use one of the defined starting points
	 *            {@link StartPoint#BOTTOM} {@link StartPoint#TOP}
	 *            {@link StartPoint#LEFT} {@link StartPoint#RIGHT}
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @return
	 */
	public MoveEllipseAdapter(Rectangle rect, StartPoint startAt, float angle,
			boolean rotateTexture) {
		this(rect, startAt, angle, rotateTexture, false, null);
	}

	/**
	 * This {@link MovementHandler} tries to move an event around the given
	 * ellipse. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param rect
	 *            The rectangle defines the outer bounds of the elipse.
	 * @param startAt
	 *            Use one of the defined starting points
	 *            {@link StartPoint#BOTTOM} {@link StartPoint#TOP}
	 *            {@link StartPoint#LEFT} {@link StartPoint#RIGHT}
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @param fixedSpeed
	 *            If this is set to true, the speed will be fixed. (Default
	 *            value = false = dynamic speed)
	 * @param drift
	 *            Specifies a drift into direction x,y (positive/negative
	 *            allowed).<br>
	 *            x-drift will produce a vertical helix. y-drift will produce a
	 *            horizontal helix. (Default value = 0f = no drift)
	 */
	public MoveEllipseAdapter(Rectangle rect, StartPoint startAt, float angle,
			boolean rotateTexture, boolean fixedSpeed, Vector2 drift) {
		super(false, false);
		// set start position
		switch (startAt) {
		case BOTTOM:
			addMoveToExecute(new MoveSetXYAdapter(rect.x + rect.width / 2,
					rect.y));
			break;
		case TOP:
			addMoveToExecute(new MoveSetXYAdapter(rect.x + rect.width / 2,
					rect.y + rect.height));
			break;
		case LEFT:
			addMoveToExecute(new MoveSetXYAdapter(rect.x, rect.y + rect.height
					/ 2));
			break;
		case RIGHT:
			addMoveToExecute(new MoveSetXYAdapter(rect.x + rect.width, rect.y
					+ rect.height / 2));
			break;
		}
		// add elliptic move
		addMoveToExecute(new MoveArcAdapter(new Vector2(
				rect.x + rect.width / 2, rect.y + rect.height / 2), angle,
				rotateTexture, fixedSpeed, new Vector2(
						rect.width / rect.height, 1f), drift));
	}
}
