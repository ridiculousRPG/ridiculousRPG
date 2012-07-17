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

import java.util.Random;

import com.badlogic.gdx.utils.Pool.Poolable;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.Direction;

/**
 * With this {@link MovementHandler} you can add random moves to events.
 * 
 * @author Alexander Baumgartner
 */
public class MoveRandomAdapter extends MovementHandler implements Poolable {
	private static final long serialVersionUID = 1L;

	protected static final Random randomNumberGenerator = new Random();
	public int changeDirectionSlackness;
	public Direction[] allowedDirections;

	protected Direction lastDir;
	protected float minWidth;

	/**
	 * This constructor uses a default slackness-value of 128 for
	 * direction-changing.<br>
	 * Defaultdirections are N, E, S and W<br>
	 */
	public MoveRandomAdapter() {
		this(128);
	}

	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes. Defaultdirections
	 * are N, E, S and W<br>
	 * 
	 * @param changeDirectionSlackness
	 */
	public MoveRandomAdapter(int changeDirectionSlackness) {
		this(new Direction[] { Direction.N, Direction.E, Direction.S,
				Direction.W }, changeDirectionSlackness);
	}

	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes.<br>
	 * Which directions should be allowed. Defaultdirections are N, E, S and W
	 * 
	 * @param allowedDirections
	 * @param changeDirectionSlackness
	 */
	public MoveRandomAdapter(Direction[] allowedDirections,
			int changeDirectionSlackness) {
		this.allowedDirections = allowedDirections;
		this.changeDirectionSlackness = Math.max(allowedDirections.length,
				changeDirectionSlackness);
	}

	@Override
	public void moveBlocked(Movable event) {
		lastDir = null;
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (lastDir == null || minWidth < 0) {
			int randNum = randomNumberGenerator
					.nextInt(changeDirectionSlackness);
			if (lastDir == null)
				randNum %= allowedDirections.length;
			if (randNum < allowedDirections.length) {
				lastDir = allowedDirections[randNum];
				minWidth = changeDirectionSlackness / 2;
			} else {
				minWidth = changeDirectionSlackness / 3;
			}
		}
		minWidth -= event.offerMove(lastDir, deltaTime);
	}

	@Override
	public void reset() {
		super.reset();
		lastDir = null;
		minWidth = 0;
	}
}
