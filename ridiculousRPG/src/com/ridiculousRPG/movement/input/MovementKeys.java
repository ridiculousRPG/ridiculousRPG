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

import java.io.Serializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * The default movement keys are W,A,S,D and the Arrow keys.<br>
 * If you indent to make a 2 player game, split 'em up and instantiate the
 * {@link MovementHandler} with your own MovementKeys instance.
 * 
 * @author Alexander Baumgartner
 */
public final class MovementKeys implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The default movement keys are W,A,S,D and the Arrow keys.<br>
	 * If you indent to make a 2 player game, split 'em up and instantiate the
	 * {@link MovementHandler} with your own MovementKeys instance.
	 */
	private static MovementKeys instance;

	private int[] upKeys = { Input.Keys.UP, Input.Keys.W };
	private int[] downKeys = { Input.Keys.DOWN, Input.Keys.S };
	private int[] leftKeys = { Input.Keys.LEFT, Input.Keys.A };
	private int[] rightKeys = { Input.Keys.RIGHT, Input.Keys.D };
	private int[][] allKeys = { upKeys, downKeys, leftKeys, rightKeys };

	public static MovementKeys $() {
		if (instance == null)
			instance = new MovementKeys();
		return instance;
	}

	public void setUpKeys(int... upKeys) {
		this.upKeys = upKeys;
		allKeys[0] = upKeys;
	}

	public void setDownKeys(int... downKeys) {
		this.downKeys = downKeys;
		allKeys[1] = downKeys;
	}

	public void setLeftKeys(int... leftKeys) {
		this.leftKeys = leftKeys;
		allKeys[2] = leftKeys;
	}

	public void setRightKeys(int... rightKeys) {
		this.rightKeys = rightKeys;
		allKeys[3] = rightKeys;
	}

	/**
	 * Returns all supported keys in a two dimensional array.
	 * 
	 * @return
	 */
	public int[][] getSupportedKeys() {
		return allKeys;
	}

	/**
	 * Checks if one of the supported up keys is pressed.
	 * 
	 * @return The pressed key or 0 if no up key is pressed.
	 */
	public int isUpKeyPressed() {
		return checkKeys(upKeys);
	}

	/**
	 * Checks if one of the supported down keys is pressed.
	 * 
	 * @return The pressed key or 0 if no down key is pressed.
	 */
	public int isDownKeyPressed() {
		return checkKeys(downKeys);
	}

	/**
	 * Checks if one of the supported left keys is pressed.
	 * 
	 * @return The pressed key or 0 if no left key is pressed.
	 */
	public int isLeftKeyPressed() {
		return checkKeys(leftKeys);
	}

	/**
	 * Checks if one of the supported right keys is pressed.
	 * 
	 * @return The pressed key or 0 if no right key is pressed.
	 */
	public int isRightKeyPressed() {
		return checkKeys(rightKeys);
	}

	private int checkKeys(int[] keys) {
		for (int key : keys)
			if (Gdx.input.isKeyPressed(key))
				return key;
		return 0;
	}
}
