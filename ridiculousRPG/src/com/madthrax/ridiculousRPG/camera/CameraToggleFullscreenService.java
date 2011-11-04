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
 * @author Alexander Baumgartner
 */
public class CameraToggleFullscreenService extends InputAdapter implements GameService {
	
	@Override
	public boolean keyUp(int keycode) {
		if (GameBase.$().isControlKeyPressed() || Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			if (keycode==Input.Keys.F || keycode==Input.Keys.ENTER) {
				return GameBase.$().toggleFullscreen();
			}
		}
		return false;
	}
	
	public void freeze() {}
	
	public void unfreeze() {}
	
	public void dispose() {}
}
