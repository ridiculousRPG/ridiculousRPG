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

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.madthrax.ridiculousRPG.movement.CombinedMovesAdapter;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

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
	/**
	 * {@link StartPoint#BOTTOM} {@link StartPoint#TOP} {@link StartPoint#LEFT}
	 * {@link StartPoint#RIGHT}
	 */
	public enum StartPoint {
		BOTTOM, TOP, LEFT, RIGHT
	}

	protected MoveEllipseAdapter(Rectangle rect, StartPoint startAt,
			float angle, boolean rotateTexture, boolean fixedSpeed,
			Vector2 drift) {
		super(false, false);
		// set start position
		switch (startAt) {
		case BOTTOM:
			addMoveToExecute(MoveSetXYAdapter
					.$(rect.x + rect.width / 2, rect.y));
			break;
		case TOP:
			addMoveToExecute(MoveSetXYAdapter.$(rect.x + rect.width / 2, rect.y
					+ rect.height));
			break;
		case LEFT:
			addMoveToExecute(MoveSetXYAdapter.$(rect.x, rect.y + rect.height
					/ 2));
			break;
		case RIGHT:
			addMoveToExecute(MoveSetXYAdapter.$(rect.x + rect.width, rect.y
					+ rect.height / 2));
			break;
		}
		// add elliptic move
		addMoveToExecute(MoveArcAdapter.$(new Vector2(rect.x + rect.width / 2,
				rect.y + rect.height / 2), angle, rotateTexture, fixedSpeed,
				new Vector2(rect.width / rect.height, 1f), drift));
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
	public static MovementHandler $(Rectangle rect) {
		return $(rect, false, -360);
	}

	/**
	 * This {@link MovementHandler} tries to move an event around the given
	 * ellipse. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 * 
	 * @param rect
	 *            The rectangle defines the outer bounds of the elipse.
	 * @param rotateTexture
	 *            Should the texture be rotated automatically?
	 * @param angle
	 *            The angle in degrees or +-0 if you want to loop forever. If
	 *            the angle is negative, the move will be clockwise.
	 * @return
	 */
	public static MovementHandler $(Rectangle rect, boolean rotateTexture,
			float angle) {
		return $(rect, StartPoint.BOTTOM, angle, rotateTexture, false,
				new Vector2(0f, 0f));
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
	 * @return
	 */
	public static MovementHandler $(Rectangle rect, StartPoint startAt,
			float angle, boolean rotateTexture, boolean fixedSpeed,
			Vector2 drift) {
		return new MoveEllipseAdapter(rect, startAt, angle, rotateTexture,
				fixedSpeed, drift);
	}
}
