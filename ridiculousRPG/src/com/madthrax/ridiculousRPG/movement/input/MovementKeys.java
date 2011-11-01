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
import com.badlogic.gdx.Input;

/**
 * @author Alexander Baumgartner
 */
public final class MovementKeys { private MovementKeys() {} // static container
	/**
	 * You should not change the reference, but you are allowed
	 * to change the values inside the array to any other
	 * key code.<br>
	 * Three different key codes are supported for any direction.
	 * Per default only two are used (the first and second value are the same).<br>
	 * If you want a third alternative you can simply set the first values.
	 * @see {@link Input.Keys}
	 */
	public static final int[] upKeys = {Input.Keys.UP, Input.Keys.UP, Input.Keys.W},
		downKeys = {Input.Keys.DOWN, Input.Keys.DOWN, Input.Keys.S},
		leftKeys = {Input.Keys.LEFT, Input.Keys.LEFT, Input.Keys.A},
		rightKeys = {Input.Keys.RIGHT, Input.Keys.RIGHT, Input.Keys.D};
	private static final int[][] allKeys = {upKeys,downKeys,leftKeys,rightKeys};

	/**
	 * Returns all supported keys in a two dimensional array.
	 * @return
	 */
	public static int[][] getSupportedKeys() {
		return allKeys;
	}
	/**
	 * Checks if one of the supported up keys is pressed.
	 * @return
	 * The pressed key or 0 if no up key is pressed.
	 */
	public static int isUpKeyPressed() {
		return checkKeys(upKeys);
	}
	/**
	 * Checks if one of the supported down keys is pressed.
	 * @return
	 * The pressed key or 0 if no down key is pressed.
	 */
	public static int isDownKeyPressed() {
		return checkKeys(downKeys);
	}
	/**
	 * Checks if one of the supported left keys is pressed.
	 * @return
	 * The pressed key or 0 if no left key is pressed.
	 */
	public static int isLeftKeyPressed() {
		return checkKeys(leftKeys);
	}
	/**
	 * Checks if one of the supported right keys is pressed.
	 * @return
	 * The pressed key or 0 if no right key is pressed.
	 */
	public static int isRightKeyPressed() {
		return checkKeys(rightKeys);
	}
	private static int checkKeys(int[] keys) {
		for (int key : keys)
			if (Gdx.input.isKeyPressed(key)) return key;
		return 0;
	}
}
