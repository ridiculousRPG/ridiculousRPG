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

import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.Direction;

/**
 * This {@link MovementHandler} tries to move an event to the given position.
 * The move is blocked while a blocking event exists on the given position.<br>
 * After succeeding the switch finished is set to true.<br>
 * If there exists a none-moving blocking event at the given position, this
 * movement will never finish.
 * 
 * @author Alexander Baumgartner
 */
public class MoveSetXYAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	protected boolean checkPerformed;
	public Movable other;

	/**
	 * This MovementAdapter tries to move an event to the given position. The
	 * move is blocked while a blocking event exists on the given position.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given position, this
	 * movement will never finish.
	 */
	public MoveSetXYAdapter(float x, float y) {
		this(buildContainer(x, y));
	}

	private static Movable buildContainer(float x, float y) {
		Movable mv = new Movable() {
			private static final long serialVersionUID = 1L;

			@Override
			public void stop() {
			}

			@Override
			public void offerMove(float x, float y) {
			}

			@Override
			public float offerMove(Direction dir, float deltaTime) {
				return 0f;
			}

			@Override
			public boolean commitMove() {
				return false;
			}
		};
		mv.setX(x);
		mv.setY(y);
		return mv;
	}

	/**
	 * This MovementAdapter tries to move an event to a given other Movable. The
	 * move is blocked forever if two events block mutually.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given position, this
	 * movement will never finish.
	 */
	public MoveSetXYAdapter(Movable other) {
		this.other = other;
	}

	@Override
	public void tryMove(Movable event, float deltaTime,
			EventTrigger eventTrigger) {
		event.stop();
		// move could be blocked
		if (checkPerformed || finished) {
			finished = true;
		} else {
			event.offerMove(other.getX() - event.getX(), other.getY()
					- event.getY());
			checkPerformed = true;
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		checkPerformed = false;
	}

	@Override
	public void reset() {
		super.reset();
		checkPerformed = false;
	}
}
