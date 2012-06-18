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

package com.madthrax.ridiculousRPG.movement.input;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;
import com.madthrax.ridiculousRPG.util.Direction;

/**
 * @author Alexander Baumgartner
 */
public class Move8WayAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private static MovementHandler instance = new Move8WayAdapter();
	private MovementKeys movementKeys;

	public Move8WayAdapter(MovementKeys movementKeys) {
		this.movementKeys = movementKeys;
	}

	protected Move8WayAdapter() {
		this(MovementKeys.$);
	}

	public static MovementHandler $() {
		return instance;
	}

	private int lastDirKey1;
	private int lastDirKey2;
	private Direction lastDir;

	@Override
	public void freeze() {
		lastDirKey1 = 0;
	}

	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (GameBase.$serviceProvider().queryAttention() != null) {
			movable.stop();
			freeze();
			return;
		}
		if (lastDirKey1 != 0) {
			if (!Gdx.input.isKeyPressed(lastDirKey1)) {
				lastDirKey1 = 0;
			} else {
				if (lastDirKey2 != 0) {
					if (!Gdx.input.isKeyPressed(lastDirKey2)) {
						lastDirKey1 = 0;
					}
				} else {
					mainLoop: for (int[] keys : movementKeys.getSupportedKeys())
						for (int key : keys)
							if (key != lastDirKey1
									&& Gdx.input.isKeyPressed(key)) {
								lastDirKey1 = 0;
								break mainLoop;
							}
				}
			}
			movable.offerMove(lastDir, deltaTime);
		} else if (Gdx.input.isTouched(0)) {
			Direction touchDir = computeDirection(Gdx.input.getX(0), Gdx.input
					.getY(0), movable);
			movable.offerMove(touchDir, deltaTime);
		} else {
			lastDirKey2 = 0;
			if (GameBase.$().isControlKeyPressed()) {
				lastDirKey1 = 0;
				movable.stop();
				return;
			}
			if ((lastDirKey1 = movementKeys.isUpKeyPressed()) != 0) {
				if ((lastDirKey2 = movementKeys.isLeftKeyPressed()) != 0) {
					lastDir = Direction.NW;
				} else if ((lastDirKey2 = movementKeys.isRightKeyPressed()) != 0) {
					lastDir = Direction.NE;
				} else {
					lastDir = Direction.N;
				}
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey1 = movementKeys.isDownKeyPressed()) != 0) {
				if ((lastDirKey2 = movementKeys.isLeftKeyPressed()) != 0) {
					lastDir = Direction.SW;
				} else if ((lastDirKey2 = movementKeys.isRightKeyPressed()) != 0) {
					lastDir = Direction.SE;
				} else {
					lastDir = Direction.S;
				}
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey1 = movementKeys.isLeftKeyPressed()) != 0) {
				lastDir = Direction.W;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey1 = movementKeys.isRightKeyPressed()) != 0) {
				lastDir = Direction.E;
				movable.offerMove(lastDir, deltaTime);
			} else {
				movable.stop();
			}
		}
	}

	private Direction computeDirection(int absolutX, int absolutY,
			Movable movable) {
		float x = movable.computeRelativX(absolutX);
		float y = movable.computeRelativY(absolutY);
		float ratio = Math.abs(x / y);
		if (ratio > .5f && ratio < 2f) {
			return x > 0 ? (y > 0 ? Direction.NE : Direction.SE)
					: (y > 0 ? Direction.NW : Direction.SW);
		}
		return Math.abs(x) > Math.abs(y) ? (x > 0 ? Direction.E : Direction.W)
				: (y > 0 ? Direction.N : Direction.S);
	}

	@Override
	public void reset() {
		super.reset();
		lastDirKey1 = 0;
	}
}
