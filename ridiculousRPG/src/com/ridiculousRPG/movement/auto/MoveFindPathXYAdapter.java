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

/**
 * STUB - NOT IMPLEMENTED YET!!!<br>
 * This {@link MovementHandler} tries to move an event to the given position.
 * Therefore a path will be computed where the event will move along.<br>
 * After the event has reached the given position, the switch finished is set to
 * true.
 * 
 * @author Alexander Baumgartner
 */
public class MoveFindPathXYAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	float x, y;

	/**
	 * STUB - NOT IMPLEMENTED YET!!!<br>
	 * This {@link MovementHandler} tries to move an event to the given
	 * position. Therefore a path will be computed where the event will move
	 * along.<br>
	 * After the event has reached the given position, the switch finished is
	 * set to true.
	 */
	public MoveFindPathXYAdapter(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * STUB - NOT IMPLEMENTED YET!!!<br>
	 * This MovementAdapter tries to move an event to a given other Movable. The
	 * move is blocked forever if two events block mutually.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given position, this
	 * movement will never finish.
	 */
	public MoveFindPathXYAdapter(Movable other) {
		this.x = other.getX();
		this.y = other.getY();
	}

	@Override
	/**
	 * STUB - NOT IMPLEMENTED YET!!!<br>
	 */
	public void tryMove(Movable event, float deltaTime,
			EventTrigger eventTrigger) {
		// TODO: implement it
		event.stop();
		finished = true;
	}

	@Override
	public void moveBlocked(Movable event) {
		// TODO: compute new path
	}

	@Override
	public void reset() {
		super.reset();
	}
}
