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

package com.ridiculousRPG.util;

/**
 * @author Alexander Baumgartner
 */
public enum Direction {
	E(0, 1f, 0f), W(1, -1f, 0f), N(2, 0f, 1f), S(3, 0f, -1f), NE(4, .7f, .7f), SE(
			5, .7f, -.7f), NW(6, -.7f, .7f), SW(7, -.7f, -.7f);

	private float x, y;
	private int directionIndex;

	private Direction(int directionIndex, float x, float y) {
		this.directionIndex = directionIndex;
		this.x = x;
		this.y = y;
	}

	public float getDistanceX(float distance) {
		return x * distance;
	}

	public float getDistanceY(float distance) {
		return y * distance;
	}

	public int getIndex(int maxDirections) {
		if (maxDirections >= 8) {
			// 8 direction movement
			return directionIndex;
		} else if (maxDirections >= 4) {
			// 4 direction movement
			return directionIndex % 4;
		} else if (maxDirections >= 2) {
			// 2 direction movement
			return directionIndex % 2;
		}
		// direction fixed
		return 0;
	}

	/**
	 * Computes the direction from the given (x,y) - movement
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static Direction fromMovement(float x, float y) {
		if (x == 0)
			return y < 0 ? S : N;
		if (y == 0)
			return x < 0 ? W : E;

		float ratio = x / y - 1;
		if (Math.abs(ratio) > .5) {
			if (ratio < 0)
				return y < 0 ? S : N;
			return x < 0 ? W : E;
		}

		if (x < 0)
			return y < 0 ? SW : NW;
		return y < 0 ? SE : NE;
	}

	/**
	 * Computes the direction from the given (x,y) - movement
	 * 
	 * @param x
	 * @param y
	 * @param maxDirections
	 *            Maximum number of directions to use.<br>
	 *            maxDirections < 4 crops to W-E movement.
	 * @return
	 */
	public static Direction fromMovement(float x, float y, int maxDirections) {
		if (maxDirections >= 8)
			return fromMovement(x, y);

		if (maxDirections >= 4)
			if (Math.abs(y) > Math.abs(x))
				return y < 0 ? S : N;
			else
				return x < 0 ? W : E;

		if (maxDirections >= 2)
			return x < 0 ? W : E;

		return E;
	}

	public int getDirectionIndex() {
		return directionIndex;
	}
}
