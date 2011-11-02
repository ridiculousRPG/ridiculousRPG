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

package com.madthrax.ridiculousRPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * @author Alexander Baumgartner
 */
public class GameIconService extends GameServiceDefaultImpl implements Initializable {
	private boolean initialized = false;
	private Pixmap applIcon;

	@Override
	public void init() {
		if (isInitialized()) return;
		FileHandle applIconFile = Gdx.files.internal("data/icon.png");
		if (applIconFile.exists()) {
			applIcon = new Pixmap(applIconFile);
			if (applIcon.getWidth()==applIcon.getHeight() &&
				MathUtils.isPowerOfTwo(applIcon.getWidth())) {
				try {
					Gdx.graphics.setIcon(new Pixmap[]{applIcon});
				} catch (Throwable notTooBad) {
					applIcon.dispose();
					applIcon = null;
				}
			} else {
				applIcon.dispose();
				applIcon = null;
			}
		}

		initialized = true;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
	@Override
	public void dispose() {
		if (applIcon!=null) applIcon.dispose();
	}
}
