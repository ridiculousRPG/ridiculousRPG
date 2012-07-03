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
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
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
	private String background;
	private Array<TextureRegionRef> managedTextures = new Array<TextureRegionRef>();

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
			boolean clearTheMenu, boolean catchBackKey, boolean catchMenuKey) {
		this.freezeTheWorld = freezeTheWorld;
		this.clearTheScreen = clearTheScreen;
		this.catchBackKey = catchBackKey;
		this.catchMenuKey = catchMenuKey;
		this.clearTheMenu = clearTheMenu;
		try {
			this.scriptEngine = GameBase.$scriptFactory().obtainInvocable(
					callBackScript);
			((ScriptEngine) this.scriptEngine).put(ScriptEngine.FILENAME,
					callBackScript);
		} catch (ScriptException e) {
			GameBase.$error("MenuState.init",
					"Error loading (compiling) callback script for menu "
							+ "service. File: " + callBackScript, e);
		}
	}

	@Override
	public void createGui(MenuService menuService) {
		try {
			if (background != null) {
				Image bg = createImage(background);
				bg.setWidth(menuService.getWidth());
				bg.setHeight(menuService.getHeight());
				menuService.addGUIcomponent(bg);
			}
			if (scriptEngine != null)
				scriptEngine.invokeFunction("createGui", menuService, this);
		} catch (Exception e) {
			GameBase.$error("MenuState.createGui",
					"Could not create GUI for requested menu", e);
		}
	}

	@Override
	public boolean processInput(int keycode, MenuService menuService) {
		try {
			if (scriptEngine == null)
				return false;
			return Boolean.TRUE.equals(scriptEngine.invokeFunction(
					"processInput", keycode, menuService, this));
		} catch (Exception e) {
			GameBase.$error("MenuState.processInput",
					"Error processing input for active menu", e);
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

	public void setBackground(String background) {
		this.background = background;
	}

	public String getBackground() {
		return background;
	}

	/**
	 * Creates an image, which will automatically disposed when the menu is
	 * closed.
	 * 
	 * @param internalPath
	 * @return An image, which can be added to the menu.
	 */
	public Image createImage(String internalPath) {
		return createImage(TextureRegionLoader.load(internalPath), true);
	}

	/**
	 * Creates an image from a texture. Disposing the texture can be managed
	 * automatically when the menu is closed.<br>
	 * Use autoFree=true if you don't know what to do!
	 * 
	 * @param tRef
	 * @param autoFree
	 * @return An image, which can be added to the menu.
	 */
	public Image createImage(TextureRegionRef tRef, boolean autoFree) {
		if (autoFree)
			managedTextures.add(tRef);
		return new Image(tRef);
	}

	@Override
	public void freeResources() {
		for (TextureRegionRef tRef : managedTextures) {
			tRef.dispose();
		}
		managedTextures.clear();
	}

	@Override
	public void dispose() {
		freeResources();
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
