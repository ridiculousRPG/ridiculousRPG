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

import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.IntMap;
import com.madthrax.ridiculousRPG.GameBase;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class StandardMenuService extends ActorsOnStageService implements
		MenuService {

	private IntMap<MenuStateHandler> stateHandlerMap = new IntMap<MenuStateHandler>(
			16);
	private MenuStateHandler activeState;
	private String startNewGameScript;
	private int lastStatePos = 0;
	private MenuStateHandler[] lastState = new MenuStateHandler[10];
	private boolean dirty;

	@Override
	public boolean keyUp(int keycode) {
		return (activeState != null && activeState.processInput(keycode, this))
				|| super.keyUp(keycode);
	}

	/**
	 * Refresh the menu. Call this method after you performed an action which
	 * changes the behavior of the menu creation itself. The menu will be
	 * recreated.
	 */
	public void rebuildMenu() {
		changeState(activeState);
	}

	/**
	 * Clear the menu. If you have to rebuild the menu (see
	 * {@link #rebuildMenu()}), maybe you want to clean it first. This method
	 * removes all GUI elements from the screen.
	 */
	public void clearAllMenus() {
		super.clear();
	}

	public boolean changeState(MenuStateHandler newState) {
		if (newState != null
				&& newState.isFreezeTheWorld()
				&& !GameBase.$serviceProvider().requestAttention(this, true,
						newState.isClearTheScreen())) {
			return false;
		}
		if (activeState != null && activeState.isFreezeTheWorld()
				&& !GameBase.$serviceProvider().releaseAttention(this)) {
			GameBase.$error("MenuService.changeState",
					"Failed to change the menu state",
					new IllegalStateException("Oooops, couldn't release the "
							+ "attention. Something got terribly wrong!"));
			GameBase.$serviceProvider().forceTotalReset();
			clear();
			activeState.freeResources();
			activeState = null;
			return false;
		}
		if (newState == null) {
			if (dirty)
				super.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			fadeOutAllActors();
			if (activeState != null)
				activeState.freeResources();
		} else {
			if (newState.isClearTheMenu()) {
				if (dirty)
					super.resize(Gdx.graphics.getWidth(), Gdx.graphics
							.getHeight());
				fadeOutAllActors();
				if (activeState != null && activeState != newState)
					activeState.freeResources();
			} else {
				setKeyboardFocus(null);
				if (activeState == newState && getActors().size > 0) {
					getActors().removeIndex(getActors().size - 1);
				}
				ActorFocusUtil.disableRecursive(getActors());
			}
			newState.freeResources();
			newState.createGui(this);
		}
		if (activeState != newState) {
			lastState[incLastStateCount()] = activeState;
			if (newState != null) {
				Gdx.input.setCatchBackKey(newState.isCatchBackKey());
				Gdx.input.setCatchMenuKey(newState.isCatchMenuKey());
			}
			activeState = newState;
		}
		return true;
	}

	@Override
	public void addGUIcomponent(Object component) {
		if (component instanceof Actor)
			addActor((Actor) component);
	}

	@Override
	public void center(Object obj) {
		if (obj instanceof Actor) {
			super.center((Actor) obj);
		}
	}

	@Override
	public void focus(Object guiElement) {
		if (guiElement instanceof Actor) {
			super.focus((Actor) guiElement);
		}
	}

	@Override
	public void resize(int width, int height) {
		if (activeState != null && activeState.isClearTheMenu()) {
			super.resize(width, height);
			rebuildMenu();
		} else {
			dirty = true;
		}
	}

	/**
	 * Replaces or adds a state with an associated {@link MenuStateHandler}.
	 * 
	 * @param state
	 *            An int value representing one state
	 * @param stateHandler
	 *            The state handler associated with the state
	 * @return The old {@link MenuStateHandler}
	 */
	public MenuStateHandler putStateHandler(int state,
			MenuStateHandler stateHandler) {
		return stateHandlerMap.put(state, stateHandler);
	}

	/**
	 * Returns the state handler associated with the given state.
	 */
	public MenuStateHandler getStateHandler(int state) {
		return stateHandlerMap.get(state);
	}

	/**
	 * Removes and returns the state handler associated with the state.
	 */
	public MenuStateHandler removeStateHandler(int state) {
		return stateHandlerMap.remove(state);
	}

	@Override
	public boolean changeState(int newState) {
		return changeState(stateHandlerMap.get(newState));
	}

	@Override
	public MenuStateHandler getActiveStateHandler() {
		return activeState;
	}

	public void startNewGame() throws ScriptException {
		GameBase.$().eval(startNewGameScript);
	}

	public void setStartNewGameScript(String startNewGameScript) {
		this.startNewGameScript = startNewGameScript;
	}

	@Override
	public void dispose() {
		super.dispose();
		for (MenuStateHandler handler : stateHandlerMap.values()) {
			handler.dispose();
		}
	}

	@Override
	public boolean resumeLastState() {
		return changeState(lastState[decLastStateCount()]);
	}

	private int decLastStateCount() {
		lastStatePos--;
		if (lastStatePos < 0)
			lastStatePos += lastState.length;
		return lastStatePos;
	}

	private int incLastStateCount() {
		int oldCount = lastStatePos;
		lastStatePos++;
		if (lastStatePos == lastState.length)
			lastStatePos = 0;
		return oldCount;
	}
}
