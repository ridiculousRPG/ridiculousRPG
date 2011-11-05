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

package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * Displays a red error message on the screen.
 * 
 * @author Alexander Baumgartner
 */
public class DisplayErrorService extends DisplayTextService implements
		Computable {
	private float displayTime = 10f;
	private String msg;
	private BitmapFontCache fontCache;

	/**
	 * Displays the specified error centered on the screen.<br>
	 * The message is public and may be changed.
	 * 
	 * @param msg
	 *            The message to display or null if no message should be drawn.
	 */
	public DisplayErrorService(String msg) {
		this.msg = msg;
	}

	public void compute(float deltaTime, boolean actionKeyPressed) {
		String msg = this.msg;
		int oldTime = (int) displayTime;
		displayTime -= deltaTime;
		// change the message every second
		if (oldTime != (int) displayTime) {
			if (fontCache != null)
				removeMessage(fontCache);
			if (displayTime < 0) {
				displayTime = 0;
				msg = "\nPress the action key to exit the game!\n\n" + msg;
				if (actionKeyPressed)
					Gdx.app.exit();
			} else {
				msg = "\nERROR [" + ((int) (displayTime + 1f)) + "]\n\n" + msg;
			}
			fontCache = addMessage(msg, Color.RED.toFloatBits(),
					Alignment.CENTER, Alignment.CENTER, 0f);
		}
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
