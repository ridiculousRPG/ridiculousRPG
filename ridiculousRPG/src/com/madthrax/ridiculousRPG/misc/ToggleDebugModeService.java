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

package com.madthrax.ridiculousRPG.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.GameService;

/**
 * This service allows to toggle between debug mode and normal mode.<br>
 * Toggling is performed if the left Alt key is pressed and D is pressed.<br>
 * <br>
 * This is an {@link GameService#essential()} service and therefore the toggling
 * will always succeed. No matter if an other service has the attention or not.
 * 
 * @author Alexander Baumgartner
 */
public class ToggleDebugModeService extends InputAdapter implements GameService {

	/**
	 * Performs toggling between debug and normal mode.
	 */
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.D
				&& Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			GameBase.$options().debug = !GameBase.$options().debug;
			return true;
		}
		return false;
	}

	/**
	 * Consumes corresponding key up event.
	 */
	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Input.Keys.D
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
