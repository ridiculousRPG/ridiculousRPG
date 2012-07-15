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

/**
 * This MovementAdapter tries to move an event to a given position. If there
 * exists an blocking event at the given position, this movement will wait.<br>
 * After succeeding the switch finished is set to true.
 * 
 * @author Alexander Baumgartner
 */
public class MoveJumpAdapter extends MoveSetXYAdapter {
	private static final long serialVersionUID = 1L;

	private boolean jumpSuccess;
	private float distanceX, distanceY;
	private float offsetOldX, offsetOldY;
	private float absX, absY;

	private float centerXY;

	public MoveJumpAdapter(float x, float y) {
		super(x, y);
	}

	public MoveJumpAdapter(Movable other) {
		super(other);
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (jumpSuccess) {
			if (finished)
				return;
			if (distanceX == 0 && distanceY == 0) {
				event.offsetX = offsetOldX;
				event.offsetY = offsetOldY;
				finished = true;
				return;
			}
			float stretchX;
			float stretchY;
			if (absX > absY) {
				stretchX = event.getMoveSpeed().computeStretchJump(deltaTime);
				stretchY = stretchX * absY / absX;
			} else {
				stretchY = event.getMoveSpeed().computeStretchJump(deltaTime);
				stretchX = stretchY * absX / absY;
			}
			float yJump = centerXY;
			if (distanceX > 0f) {
				yJump -= distanceX;
				distanceX -= stretchX;
				event.offsetX += stretchX;
				if (distanceX < 0f) {
					event.offsetX += distanceX;
					distanceX = 0f;
				}
			} else if (distanceX < 0f) {
				yJump += distanceX;
				distanceX += stretchX;
				event.offsetX -= stretchX;
				if (distanceX > 0f) {
					event.offsetX += distanceX;
					distanceX = 0f;
				}
			}
			if (distanceY > 0f) {
				yJump -= distanceY;
				distanceY -= stretchY;
				event.offsetY += stretchY;
				if (distanceY < 0f) {
					event.offsetY += distanceY;
					distanceY = 0f;
				}
			} else if (distanceY < 0f) {
				yJump += distanceY;
				distanceY += stretchY;
				event.offsetY -= stretchY;
				if (distanceY > 0f) {
					event.offsetY += distanceY;
					distanceY = 0f;
				}
			}
			event.offsetY -= yJump*10 / centerXY;
			return;
		}
		if (!checkPerformed) {
			distanceX = other.getX() - event.getX();
			distanceY = other.getY() - event.getY();
			absX = Math.abs(distanceX);
			absY = Math.abs(distanceY);
			centerXY = (absX + absY) * .5f;
			offsetOldX = event.offsetX;
			offsetOldY = event.offsetY;
			event.offsetX -= distanceX;
			event.offsetY -= distanceY;
		}
		super.tryMove(event, deltaTime);
		if (finished) {
			jumpSuccess = true;
			finished = false;
		}
	}

	@Override
	public void reset() {
		super.reset();
		jumpSuccess = false;
	}

	@Override
	public void moveBlocked(Movable event) {
		super.moveBlocked(event);
		event.offsetX = offsetOldX;
		event.offsetY = offsetOldY;
	}
}
