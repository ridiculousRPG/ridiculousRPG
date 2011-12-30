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

import com.madthrax.ridiculousRPG.event.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event by the given distance and
 * direction. The move waits if a blocking event exists on it's way.<br>
 * After succeeding the switch finished is set to true.
 * 
 * @author Alexander Baumgartner
 */
public class MoveDistanceAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private float distance;
	private float lastDistance;
	private float distanceCount;
	private Direction dir;

	protected MoveDistanceAdapter(float distance, Direction dir) {
		this.distance = distance;
		this.dir = dir;
	}

	/**
	 * This MovementAdapter tries to move an event by the given distance and
	 * direction. The move waits if a blocking event exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 */
	public static MovementHandler $(float distance, Direction dir) {
		return new MoveDistanceAdapter(distance, dir);
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		// move could be blocked
		if (distanceCount >= distance || finished) {
			if (distanceCount == 0f)
				event.stop();
			finished = true;
		} else {
			lastDistance = event.offerMove(dir, deltaTime);
			distanceCount += lastDistance;
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		distanceCount -= lastDistance;
		event.stop();
	}

	@Override
	public void reset() {
		super.reset();
		distanceCount = 0f;
		lastDistance = 0f;
	}
}
