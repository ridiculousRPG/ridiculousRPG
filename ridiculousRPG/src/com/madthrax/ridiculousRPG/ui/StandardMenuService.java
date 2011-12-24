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

import javax.script.Invocable;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Remove;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class StandardMenuService extends ActorsOnStageService implements
		MenuService, ResizeListener {

	private MenuStateHandler serviceState;

	/**
	 * Initializes the menus and shows the title screen.<br>
	 * The {@link StandardMenuService} immediately switches to
	 * {@link ServiceState#TITLE_SCREEN}
	 * 
	 * @param callBackScript
	 *            The script which contains all the callback functions which are
	 *            called by this service
	 * @throws ScriptException
	 */
	public StandardMenuService(FileHandle callBackScript)
			throws ScriptException {
		scriptEngine = GameBase.$scriptFactory()
				.obtainInvocable(callBackScript);
		changeState(ServiceState.TITLE_SCREEN);
	}

	@Override
	public boolean keyUp(int keycode) {
		return processInput(keycode) || super.keyUp(keycode);
	}

	private boolean processInput(int keycode) {
		try {
			switch (serviceState) {
			case PAUSED:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputPaused", this, keycode);
			case IDLE:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputIdle", this, keycode);
			case GAME_MENU1:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputGameMenu1", this, keycode);
			case GAME_MENU2:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputGameMenu2", this, keycode);
			case GAME_MENU3:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputGameMenu3", this, keycode);
			case GAME_MENU4:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputGameMenu3", this, keycode);
			case GAME_MENU5:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputGameMenu3", this, keycode);
			case TITLE_SCREEN:
				return (Boolean) scriptEngine.invokeFunction(
						"processInputTitleScreen", this, keycode);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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

	public boolean changeState(MenuStateHandler newState) {
		boolean consumed = false;
		if (serviceState != ServiceState.IDLE) {
			consumed = GameBase.$serviceProvider().releaseAttention(this);
			if (consumed) {
				super.clear();
			} else {
				return false;
			}
		} else {
			super.clear();
		}
		if (newState != ServiceState.IDLE) {
			boolean ok = GameBase.$serviceProvider().requestAttention(this,
					true, newState == ServiceState.TITLE_SCREEN);
			if (!ok) {
				serviceState = ServiceState.IDLE;
				return consumed;
			}
			consumed = true;

			switch (newState) {
			case PAUSED:
				createGuiPaused();
				break;
			case GAME_MENU1:
				createGuiGameMenu1();
				break;
			case GAME_MENU2:
				createGuiGameMenu2();
				break;
			case GAME_MENU3:
				createGuiGameMenu3();
				break;
			case GAME_MENU4:
				createGuiGameMenu4();
				break;
			case GAME_MENU5:
				createGuiGameMenu5();
				break;
			case TITLE_SCREEN:
				createGuiTitleScreen();
				break;
			}
		} else {
			createGuiIdle();
		}

		serviceState = newState;
		return consumed;
	}

	private void createGuiGameMenu1() {
		final Skin skin = getSkinNormal();
		Window w = new Window("Game menu", skin);
		addActor(w);

		TextButton resume = new TextButton("Resume (Esc)", skin);
		resume.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.IDLE);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(resume);

		TextButton bag = new TextButton("Open bag", skin);
		bag.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				showInfo(getSkinFocused(), "Bag is not implemented yet.\n"
						+ "This is an early alpha release!");
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(bag);

		TextButton save = new TextButton("Save", skin);
		save.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				showInfo(getSkinFocused(), "Save is not implemented yet.\n"
						+ "This is an early alpha release!");
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(save);

		TextButton load = new TextButton("Load", skin);
		load.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				showInfo(getSkinFocused(), "Load is not implemented yet.\n"
						+ "This is an early alpha release!");
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(load);

		TextButton toTitle = new TextButton("Return to title", skin);
		toTitle.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.TITLE_SCREEN);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(toTitle);

		TextButton exit = new TextButton("Exit game", skin);
		exit.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				GameBase.exit();
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(exit);

		w.pack();
		w.height = height();
		addActor(w);
		focus(resume);
	}

	private void createGuiTitleScreen() {
		Image bg = new Image(TextureRegionLoader.load("data/image/Title.png"));
		bg.width = width();
		bg.height = height();
		addActor(bg);

		final Skin skin = getSkinNormal();
		Window w = new Window("Start menu", skin);
		addActor(w);

		TextButton resume = new TextButton("Continue at last save point", skin);
		resume.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.IDLE);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(resume);

		TextButton load = new TextButton("Load game", skin);
		load.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				showInfo(getSkinFocused(), "Load is not implemented yet.\n"
						+ "This is an early alpha release!");
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(load);

		TextButton start = new TextButton("Start new game", skin);
		start.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.IDLE);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(start);

		TextButton toggleFull = new TextButton(
				GameBase.$().isFullscreen() ? "Window mode" : "Fullscreen mode",
				skin);
		toggleFull.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				GameBase.$().toggleFullscreen();
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(toggleFull);

		TextButton exit = new TextButton("Exit game (Esc)", skin);
		exit.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				GameBase.exit();
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(exit);

		w.pack();
		center(w);
		focus(resume);
	}

	public void center(Object actor) {
		actor.x = (int) (centerX() - actor.width * .5f);
		actor.y = (int) (centerY() - actor.height * .5f);
	}

	protected void createGuiPaused() {
		Skin skin = getSkinNormal();
		Window w = new Window("PAUSE", skin);
		addActor(w);

		TextButton resume = new TextButton("Resume (P)", skin);
		resume.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.IDLE);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(resume);

		TextButton exit = new TextButton("Return to title", skin);
		exit.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				changeState(ServiceState.TITLE_SCREEN);
			}
		});
		w.row().fill(true, true).expand(true, false);
		w.add(exit);

		w.pack();
		center(w);
		focus(resume);
	}

	@Override
	public void resize(int width, int height) {
		refreshMenu();
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
