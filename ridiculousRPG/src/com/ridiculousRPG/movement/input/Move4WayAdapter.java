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

package com.ridiculousRPG.movement.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.Direction;

/**
 * @author Alexander Baumgartner
 */
public class Move4WayAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private static MovementHandler instance = new Move4WayAdapter();
	private MovementKeys movementKeys;
	public float touchEpsilon = 5f;

	public Move4WayAdapter(MovementKeys movementKeys) {
		this.movementKeys = movementKeys;
	}

	protected Move4WayAdapter() {
		this(MovementKeys.$);
	}

	public static MovementHandler $() {
		return instance;
	}

	private int lastDirKey;
	private Direction lastDir;

	@Override
	public void freeze() {
		lastDirKey = 0;
	}

	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (GameBase.$serviceProvider().queryAttention() != null) {
			movable.stop();
			freeze();
			return;
		}
		if (lastDirKey != 0) {
			if (!Gdx.input.isKeyPressed(lastDirKey)) {
				lastDirKey = 0;
			}
			movable.offerMove(lastDir, deltaTime);
		} else if (GameBase.$().isLongPress()) {
			Direction touchDir = computeDirection(Gdx.input.getX(0), Gdx.input
					.getY(0), movable);
			if (touchDir == null) {
				movable.stop();
				return;
			}
			movable.offerMove(touchDir, deltaTime);
		} else {
			if (GameBase.$().isControlKeyPressed()
					|| Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
				lastDirKey = 0;
				movable.stop();
				return;
			}
			if ((lastDirKey = movementKeys.isUpKeyPressed()) != 0) {
				lastDir = Direction.N;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey = movementKeys.isDownKeyPressed()) != 0) {
				lastDir = Direction.S;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey = movementKeys.isLeftKeyPressed()) != 0) {
				lastDir = Direction.W;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey = movementKeys.isRightKeyPressed()) != 0) {
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
		float absX = Math.abs(x);
		float absY = Math.abs(y);
		if (absX + absY < touchEpsilon)
			return null;
		return Math.abs(x) > Math.abs(y) ? (x > 0 ? Direction.E : Direction.W)
				: (y > 0 ? Direction.N : Direction.S);
	}

	@Override
	public void reset() {
		super.reset();
		lastDirKey = 0;
	}
}
