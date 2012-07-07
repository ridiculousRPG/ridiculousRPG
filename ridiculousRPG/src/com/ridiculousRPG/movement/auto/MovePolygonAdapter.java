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

import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event along the given polygon.
 * The move waits if a blocking event exists on it's way.<br>
 * After succeeding the switch finished is set to true.
 * 
 * @author Alexander Baumgartner
 */
// TODO: Implement it. hehehe;)
public class MovePolygonAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private float[] verticesX;
	private float[] verticesY;
	private String[] execAtNode;

	public MovePolygonAdapter(float[] verticesX, float[] verticesY) {
		this.verticesX = verticesX;
		this.verticesY = verticesY;
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		// move could be blocked
		/*
		 * if (distanceCount >= distance || finished) { if (finished)
		 * event.stop(); finished = true; } else { lastDistance =
		 * event.offerMove(dir, deltaTime); distanceCount += lastDistance; }
		 */
	}

	@Override
	public void moveBlocked(Movable event) {
		// distanceCount -= lastDistance;
		event.stop();
	}

	@Override
	public void reset() {
		super.reset();
		// distanceCount = 0f;
		// lastDistance = 0f;
	}
}
