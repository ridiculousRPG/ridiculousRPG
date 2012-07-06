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

import java.io.Serializable;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ridiculousRPG.service.GameService;

/**
 * This interface is used to connect state handler with their menu service.
 * 
 * @see MenuStateHandler
 * @author Alexander Baumgartner
 */
public interface MenuService extends GameService, Serializable {
	/**
	 * Returns the active state handler.
	 */
	public MenuStateHandler getActiveStateHandler();

	/**
	 * Method to obtain the state handler for the given state.
	 */
	public MenuStateHandler getStateHandler(int state);

	/**
	 * Method to change the state of the menu.
	 */
	public boolean changeState(MenuStateHandler newState);

	/**
	 * Method to change the state of the menu.
	 */
	public boolean changeState(int newState);

	/**
	 * Method to resume the last state.
	 */
	public boolean resumeLastState();

	/**
	 * Returns the normal (default) skin.
	 */
	public Skin getSkinNormal();

	/**
	 * Returns the skin, used for focused GUI elements.
	 */
	public Skin getSkinFocused();

	/**
	 * Returns the screen width.
	 */
	public float getWidth();

	/**
	 * Returns the screen height.
	 */
	public float getHeight();

	/**
	 * Centers the given GUI object to the screen.
	 */
	public void center(Object guiElement);

	/**
	 * Set the focus to the given GUI object.
	 */
	public void focus(Object guiElement);

	/**
	 * Refresh the menu. Call this method after you performed an action which
	 * changes the behavior of the menu creation itself. The menu will be
	 * recreated.
	 */
	public void rebuildMenu();

	/**
	 * Adds a GUI component to this menu service. The service renders the GUI
	 * components.
	 */
	public void addGUIcomponent(Object component);

	/**
	 * Clear the menu. If you have to rebuild the menu (see
	 * {@link #rebuildMenu()}), maybe you want to clean it first. This method
	 * removes all GUI elements from the screen.
	 */
	public void clearAllMenus();

	/**
	 * Shows an default info box with the normal skin.
	 */
	public void showInfoNormal(String info);

	/**
	 * Shows an default info box with the focused skin.
	 */
	public void showInfoFocused(String info);
}
