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

package com.ridiculousRPG.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * @author Alexander Baumgartner
 */
public class GameIconService extends GameServiceDefaultImpl {
	private Pixmap applIcon;

	public GameIconService() {
		// TODO: hard coded :( ==> options :)
		FileHandle applIconFile = Gdx.files.internal("data/icon.png");
		if (applIconFile.exists()) {
			// TODO: fix this
			// 1) Load Pixmap via TextureRegionLoader
			// 2) Different Icons for different platforms???
			applIcon = new Pixmap(applIconFile);
			if (applIcon.getWidth() == applIcon.getHeight()
					&& MathUtils.isPowerOfTwo(applIcon.getWidth())) {
				try {
					Gdx.graphics.setIcon(new Pixmap[] { applIcon });
				} catch (Throwable notTooBad) {
					applIcon.dispose();
					applIcon = null;
				}
			} else {
				applIcon.dispose();
				applIcon = null;
			}
		}
	}

	public void dispose() {
		if (applIcon != null)
			applIcon.dispose();
	}
}
