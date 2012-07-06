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

package com.ridiculousRPG.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.service.GameService;
import com.ridiculousRPG.util.ExecuteInMainThread;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class StandardMenuService extends ActorsOnStageService implements
		MenuService {
	private static final long serialVersionUID = 1L;

	private String startNewGameScript;
	private boolean dirty;
	private int lastStatePos = 0;
	private MenuStateHandler[] lastState = new MenuStateHandler[10];
	private MenuStateHandler activeState;
	private transient IntMap<MenuStateHandler> stateHandlerMap;

	public StandardMenuService() {
		super();
		initTransient();
	}

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
		// If the game-state has been reset, there is also a new instance of
		// StandardMenuService, which we have to use instead of this,
		// already abandoned, one.
		GameService hasAttention = GameBase.$serviceProvider().queryAttention();
		if (hasAttention != this && hasAttention instanceof StandardMenuService) {
			return ((StandardMenuService) hasAttention).changeState(newState
					.getStateId());
		}
		boolean releaseAttention = hasAttention == this;
		// First try to request attention (again) if needed.
		// Attention is counted. This guarantees that no other service
		// can request attention between the following 2 statements.
		if (newState != null
				&& newState.isFreezeTheWorld()
				&& !GameBase.$serviceProvider().requestAttention(this, true,
						newState.isClearTheScreen())) {
			return false;
		}
		if (releaseAttention
				&& !GameBase.$serviceProvider().releaseAttention(this)) {
			// Should never happen! See comment above.
			GameBase.$error("MenuService.changeState",
					"Failed to change the menu state",
					new IllegalStateException("Oooops, couldn't release the "
							+ "attention. Something got terribly wrong!"));
			GameBase.$serviceProvider().forceAttentionReset();
			clear();
			activeState.freeResources();
			activeState = null;
			return false;
		}
		if (newState == null) {
			if (dirty)
				setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
						false);
			fadeOutAllActors();
			if (activeState != null)
				activeState.freeResources();
		} else {
			if (newState.isClearTheMenu()) {
				if (dirty)
					setViewport(Gdx.graphics.getWidth(), Gdx.graphics
							.getHeight(), false);
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
	public void resizeDone(int width, int height) {
		if (activeState != null && activeState.isClearTheMenu()) {
			setViewport(width, height, false);
			rebuildMenu();
		} else {
			dirty = true;
		}
	}

	/**
	 * Replaces or adds a state with an associated {@link MenuStateHandler}.
	 * 
	 * @param stateHandler
	 *            The state handler associated with the state
	 * @return The old {@link MenuStateHandler}
	 */
	public MenuStateHandler putStateHandler(MenuStateHandler stateHandler) {
		return stateHandlerMap.put(stateHandler.getStateId(), stateHandler);
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
		GameBase.$().resetEngine();
		new ExecuteInMainThread() {
			@Override
			public void exec() throws Exception {
				GameBase.$().eval(startNewGameScript);
			}
		}.run();
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

	public void initTransient() {
		stateHandlerMap = new IntMap<MenuStateHandler>(16);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(stateHandlerMap.size);
		for (Entry<MenuStateHandler> e : stateHandlerMap.entries()) {
			out.writeObject(e.value);
		}
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		initTransient();
		for (int i = in.readInt(); i > 0; i--) {
			putStateHandler((MenuStateHandler) in.readObject());
		}
		rebuildMenu();
	}
}
