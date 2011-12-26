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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Remove;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.animation.BoundedImage;
import com.madthrax.ridiculousRPG.animation.ImageProjectionService;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * This class provides a standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class StandardMenuServiceTest extends ActorsOnStageService implements
		ResizeListener {
	protected enum ServiceState {
		PAUSED, START_MENU, GAME_MENU, IDLE
	};

	private String bgServiceName;
	private BoundedImage background;
	private ServiceState serviceState = ServiceState.IDLE;

	public StandardMenuServiceTest(String backgroundProjectionService) {
		this.bgServiceName = backgroundProjectionService;
		if (GameBase.$().getOptions().titleBackground != null) {
			setBackground(GameBase.$().getOptions().titleBackground);
		}
		changeState(ServiceState.START_MENU);
	}

	public static void toggleCursor() {
		Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
	}

	public void setBackground(String path) {
		if (background != null)
			background.dispose();
		background = new BoundedImage(TextureRegionLoader.load(path));
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
		if (keycode == Input.Keys.ESCAPE) {
			GameBase.exit();
			return true;
		}
		return false;
	}

	private boolean processGameMenuState(int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			return changeState(ServiceState.IDLE);
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

	/**
	 * Refresh the menu
	 */
	public void refreshMenu() {
		changeState(serviceState);
	}

	@Override
	public void clear() {
		changeState(ServiceState.IDLE);
	}

	public boolean changeState(ServiceState newState) {
		boolean consumed = false;
		if (serviceState != ServiceState.IDLE) {
			consumed = GameBase.$serviceProvider().releaseAttention(this);
			if (consumed) {
				super.clear();
				GameService bgService = GameBase.$service(bgServiceName);
				if (bgService instanceof ImageProjectionService) {
					((ImageProjectionService) bgService).getImages()
							.removeValue(background, true);
				}
			} else {
				return false;
			}
		}
		if (newState != ServiceState.IDLE) {
			boolean ok = GameBase.$serviceProvider().requestAttention(this, true,
					newState == ServiceState.START_MENU);
			if (!ok) {
				serviceState = ServiceState.IDLE;
				return consumed;
			}
			consumed = true;

			switch (newState) {
			case PAUSED:
				createPauseMenu();
				break;
			case GAME_MENU:
				createGameMenu();
				break;
			case START_MENU:
				createStartMenu();
				break;
			}

		}

		serviceState = newState;
		return consumed;
	}


	private void center(Actor actor) {
		actor.x = (int) (centerX() - actor.width * .5f);
		actor.y = (int) (centerY() - actor.height * .5f);
	}

	protected void createPauseMenu() {
	}

	@Override
	public void resize(int width, int height) {
		refreshMenu();
	}

	@Override
	public void dispose() {
		if (background != null)
			background.dispose();
	}

	public void showInfoNormal(String info) {
		showInfo(getSkinNormal(), info);
	}

	public void showInfoFocused(String info) {
		showInfo(getSkinFocused(), info);
	}

	private void showInfo(final Skin skin, String info) {
		final Window w = new Window(skin);
		addActor(w);

		w.touchable = false;
		w.color.a = .1f;
		w.action(Sequence.$(FadeIn.$(.3f), Delay.$(FadeOut.$(.3f), 2f), Remove
				.$()));
		w.add(new Label(info, skin));

		w.pack();
		center(w);
	}
}
