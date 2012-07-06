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

package com.ridiculousRPG.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Matrix4;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.service.Computable;

/**
 * Displays a red error message on the screen.
 * 
 * @author Alexander Baumgartner
 */
public class DisplayErrorService extends DisplayPlainTextService implements
		Computable {
	private float displayTime = 5.99f;
	private String msg;
	private BitmapFontCache fontCache;

	private static boolean showingError;

	private DisplayErrorService() {
	}

	public void compute(float deltaTime, boolean actionKeyDown) {
		String msg = this.msg;
		if (msg == null)
			return;
		int oldTime = (int) displayTime;
		displayTime -= deltaTime;
		// change the message every second
		if (oldTime != (int) displayTime) {
			if (fontCache != null)
				removeMessage(fontCache);
			if (displayTime < 1) {
				displayTime = 0;
				msg = "\nPress action key to continue!\n\n" + msg;
			} else {
				msg = "\nOOOPS, an ERROR occurred [Countdown: "
						+ ((int) displayTime) + "]\n\n" + msg;
			}
			fontCache = addMessage(msg, Color.RED.toFloatBits(),
					Alignment.CENTER, Alignment.CENTER, 0f, GameBase.$()
							.getScreen().getWidth(), false);
		}
		if (displayTime < 1 && actionKeyDown) {
			if (fontCache != null)
				super.removeMessage(fontCache);
			GameBase.$serviceProvider().releaseAttention(this);
			this.dispose();
			showingError = false;
		}
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}

	/**
	 * Displays the specified error centered on the screen.
	 * 
	 * @param msg
	 *            The message to display or null if no message should be drawn.
	 */
	public static void forceMessage(final String msg) {
		Gdx.app.postRunnable(new Runnable() {
			// max. 30 attempts to avoid live lock
			// (about 0.5 sec on a frame rate of 60fps)
			int count = 30;
			DisplayErrorService service = new DisplayErrorService();

			@Override
			public void run() {
				if (GameBase.$serviceProvider().requestAttention(service, true,
						true)) {
					showingError = true;
					service.msg = msg;
				} else if (!showingError && count <= 0) {
					showingError = true;
					while (!GameBase.$serviceProvider().requestAttention(
							service, true, true))
						GameBase.$serviceProvider().forceAttentionReset();
					service.msg = msg;
				} else {
					count--;
					Thread.yield();
					Gdx.app.postRunnable(this);
				}
			}
		});
	}
}
