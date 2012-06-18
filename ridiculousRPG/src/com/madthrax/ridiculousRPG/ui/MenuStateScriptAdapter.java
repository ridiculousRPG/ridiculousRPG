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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class MenuStateScriptAdapter implements MenuStateHandler {
	private Invocable scriptEngine;
	private boolean freezeTheWorld;
	private boolean clearTheScreen;
	private boolean clearTheMenu;
	private boolean catchBackKey;
	private boolean catchMenuKey;
	private TextureRegionRef background;

	/**
	 * Creates a new {@link MenuStateHandler} which is customizable via a call
	 * back script.
	 * 
	 * @param callBackScript
	 *            The script file which defines the two methods
	 *            {@link #processInput(int, MenuService)} and
	 *            {@link #createGui(MenuService)}
	 * @param freezeTheWorld
	 *            See {@link #isFreezeTheWorld()}
	 * @param clearTheScreen
	 *            See {@link #isClearTheScreen()}
	 * @param clearTheMenu
	 *            See {@link #isClearTheMenu()}
	 * @throws ScriptException
	 *             If an error occurs while loading the script.
	 */
	public MenuStateScriptAdapter(FileHandle callBackScript,
			boolean freezeTheWorld, boolean clearTheScreen,
			boolean clearTheMenu, boolean catchBackKey, boolean catchMenuKey)
			throws ScriptException {
		this.freezeTheWorld = freezeTheWorld;
		this.clearTheScreen = clearTheScreen;
		this.catchBackKey = catchBackKey;
		this.catchMenuKey = catchMenuKey;
		this.clearTheMenu = clearTheMenu;
		this.scriptEngine = GameBase.$scriptFactory().obtainInvocable(
				callBackScript);
	}

	@Override
	public void createGui(MenuService menu) {
		try {
			if (background != null) {
				Image bg = new Image(background);
				bg.width = menu.getWidth();
				bg.height = menu.getHeight();
				menu.addGUIcomponent(bg);
			}
			scriptEngine.invokeFunction("createGui", menu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean processInput(int keycode, MenuService menu) {
		try {
			Object ret = scriptEngine.invokeFunction("processInput", keycode,
					menu);
			return (ret instanceof Boolean) && ((Boolean) ret);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isClearTheMenu() {
		return clearTheMenu;
	}

	@Override
	public boolean isClearTheScreen() {
		return clearTheScreen;
	}

	@Override
	public boolean isFreezeTheWorld() {
		return freezeTheWorld;
	}

	public void setBackground(TextureRegionRef background) {
		this.background = background;
	}

	public TextureRegionRef getBackground() {
		return background;
	}

	@Override
	public void dispose() {
		if (background != null)
			background.dispose();
	}

	@Override
	public boolean isCatchBackKey() {
		return catchBackKey;
	}

	@Override
	public boolean isCatchMenuKey() {
		return catchMenuKey;
	}

	/**
	 * Sets whether the BACK button on Android should be caught. This will
	 * prevent the app from being paused. Will have no effect on the desktop.
	 * 
	 * @see Gdx#input
	 * @see Input#setCatchBackKey(boolean)
	 * @param catchBack
	 *            whether to catch the back button
	 */
	public void setCatchBackKey(boolean catchBackKey) {
		this.catchBackKey = catchBackKey;
	}

	/**
	 * Sets whether the MENU button on Android should be caught. This will
	 * prevent the onscreen keyboard to show up. Will have no effect on the
	 * desktop.
	 * 
	 * @see Gdx#input
	 * @see Input#setCatchMenuKey(boolean)
	 * @param catchMenu
	 *            whether to catch the menu button
	 */
	public void setCatchMenuKey(boolean catchMenuKey) {
		this.catchMenuKey = catchMenuKey;
	}
}
