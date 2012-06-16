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

package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.GameService;

/**
 * This service allows to toggle between fullscreen mode and windowed mode.<br>
 * Toggling is performed if the left Alt key and Enter is pressed.<br>
 * <br>
 * This is an {@link GameService#essential()} service and therefore the toggling
 * will always succeed. No matter if an other service has the attention or not.
 * 
 * @author Alexander Baumgartner
 */
public class CameraToggleFullscreenService extends InputAdapter implements
		GameService {

	/**
	 * Performs toggling between fullscreen and windowed mode.
	 */
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ENTER
				&& Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			// restore default resolution in windowed mode
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
				GameBase.$().restoreDefaultResolution();
				return true;
			}
			return GameBase.$().toggleFullscreen();
		}
		return false;
	}

	/**
	 * Consumes corresponding key up event.
	 */
	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Input.Keys.ENTER
				&& Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			return true;
		}
		return false;
	}

	public void freeze() {
	}

	public void unfreeze() {
	}

	public boolean essential() {
		return true;
	}

	public void dispose() {
	}
}
