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
 * Displays the frame rate per second at one corner of the screen.
 * 
 * @author Alexander Baumgartner
 */
public class DisplayFPSService extends DisplayPlainTextService implements Computable {
	private float colorBits;
	private Alignment horiAlign, vertAlign;
	private int oldFPS;
	private BitmapFontCache fontCache;

	/**
	 * Displays the rendering speed in frames per second.
	 */
	public DisplayFPSService() {
		this(Color.WHITE, Alignment.LEFT, Alignment.TOP);
	}

	/**
	 * Displays the rendering speed in frames per second.
	 * 
	 * @param font
	 *            The font will automatically be disposed when disposing this
	 *            service.
	 * @param alignRight
	 *            If true the text will be aligned right.
	 * @param valignBottom
	 *            If true the text will be displayed at the bottom of the
	 *            screen.
	 */
	public DisplayFPSService(Color color, Alignment horiAlign,
			Alignment vertAlign) {
		this.colorBits = color.toFloatBits();
		this.horiAlign = horiAlign;
		this.vertAlign = vertAlign;
	}

	public void compute(float deltaTime, boolean actionKeyDown) {
		if (oldFPS != Gdx.graphics.getFramesPerSecond()) {
			oldFPS = Gdx.graphics.getFramesPerSecond();
			if (fontCache != null)
				removeMessage(fontCache);
			fontCache = addMessage("FPS: " + oldFPS,
					colorBits, horiAlign, vertAlign, 5f);
		}
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
