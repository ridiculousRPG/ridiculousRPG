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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionCache;
import com.madthrax.ridiculousRPG.ui.DisplayTextService.Alignment;

/**
 * This class provides a standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class StandardMenuService extends ActorsOnStageService {
	private enum ServiceState {
		PAUSED, START_MENU, GAME_MENU, IDLE
	};

	private TextureRegionCache background;
	private ServiceState serviceState = ServiceState.IDLE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.madthrax.ridiculousRPG.ui.ActorsOnStageService#init()
	 */
	@Override
	public void init() {
		super.init();
		changeState(ServiceState.START_MENU);
		if (GameBase.$().getOptions().titleBackground != null) {
			setBackground(GameBase.$().getOptions().titleBackground);
		}
	}

	public static void exit() {
		Gdx.app.exit();
	}

	public static void toggleCursor() {
		Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
	}

	public void setBackground(String path) {
		if (background != null)
			background.dispose();
		background = TextureRegionLoader.load(path);
	}

	@Override
	public boolean keyUp(int keycode) {
		return processInput(keycode) || super.keyUp(keycode);
	}

	private boolean processInput(int keycode) {
		switch (serviceState) {
		case PAUSED:
			return processPausedState(keycode);
		case IDLE:
			return processIdleState(keycode);
		case GAME_MENU:
			return processGameMenuState(keycode);
		case START_MENU:
			return processStartMenuState(keycode);
		}
		return false;
	}

	private boolean processStartMenuState(int keycode) {
		if (keycode == Input.Keys.ENTER) {
			return changeState(ServiceState.IDLE);
		}
		if (keycode == Input.Keys.ESCAPE) {
			exit();
			return true;
		}
		return false;
	}

	private boolean processGameMenuState(int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			return changeState(ServiceState.IDLE);
		}
		if (keycode == Input.Keys.ENTER) {
			return changeState(ServiceState.START_MENU);
		}
		return false;
	}

	private boolean processIdleState(int keycode) {
		if (keycode == Input.Keys.P) {
			return changeState(ServiceState.PAUSED);
		}
		if (keycode == Input.Keys.ESCAPE) {
			return changeState(ServiceState.GAME_MENU);
		}
		return false;
	}

	private boolean processPausedState(int keycode) {
		if (keycode == Input.Keys.P) {
			return changeState(ServiceState.IDLE);
		}
		return false;
	}

	public boolean changeState(ServiceState newState) {
		boolean consumed = false;
		if (serviceState != ServiceState.IDLE) {
			consumed = GameBase.$serviceProvider().releaseAttention(this);
		}
		if (newState != ServiceState.IDLE) {
			consumed = GameBase.$serviceProvider().requestAttention(this, true,
					newState == ServiceState.START_MENU)
					| consumed;
		}
		serviceState = newState;
		return consumed;
	}

	@Override
	public void freeze() {
	}

	@Override
	public void unfreeze() {
	}

	@Override
	public void dispose() {
		if (background != null)
			background.dispose();
	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		switch (serviceState) {
		case PAUSED:
			DisplayTextService.$screen.addMessage("PAUSE", Alignment.CENTER,
					Alignment.CENTER, true);
			break;
		case START_MENU:
			if (background != null) {
				spriteBatch.draw(background, 0, 0, GameBase.$()
						.getScreenWidth(), GameBase.$().getScreenHeight());
			}
			DisplayTextService.$screen.addMessage(
					"START MENU\nEsc to exit\nEnter to continue",
					Alignment.CENTER, Alignment.CENTER, true);
			break;
		case GAME_MENU:
			DisplayTextService.$screen.addMessage(
					"GAME MENU\nEsc to exit\nEnter for start menu",
					Alignment.CENTER, Alignment.CENTER, true);
			break;
		}
	}

	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
