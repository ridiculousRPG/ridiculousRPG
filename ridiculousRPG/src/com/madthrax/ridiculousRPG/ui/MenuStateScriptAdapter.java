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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	private static final long serialVersionUID = 1L;

	private int stateId;
	private boolean freezeTheWorld;
	private boolean clearTheScreen;
	private boolean clearTheMenu;
	private boolean catchBackKey;
	private boolean catchMenuKey;
	private String callBackScript;

	private transient Invocable scriptEngine;
	private transient Array<TextureRegionRef> managedTextures;

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
	public MenuStateScriptAdapter(String callBackScript, int stateId,
			boolean freezeTheWorld, boolean clearTheScreen,
			boolean clearTheMenu, boolean catchBackKey, boolean catchMenuKey) {
		this.stateId = stateId;
		this.freezeTheWorld = freezeTheWorld;
		this.clearTheScreen = clearTheScreen;
		this.catchBackKey = catchBackKey;
		this.catchMenuKey = catchMenuKey;
		this.clearTheMenu = clearTheMenu;
		this.callBackScript = callBackScript;

		initTransient();
	}

	public void initTransient() {
		managedTextures = new Array<TextureRegionRef>();
		try {
			FileHandle callBackScript = Gdx.files.internal(this.callBackScript);
			scriptEngine = GameBase.$scriptFactory().obtainInvocable(
					callBackScript);
			((ScriptEngine) scriptEngine).put(ScriptEngine.FILENAME,
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
	 * Creates an image, which will automatically disposed when the menu is
	 * closed.<br>
	 * The image is set to the given size
	 * 
	 * @param internalPath
	 * @param width
	 * @param height
	 * @return An image, which can be added to the menu.
	 */
	public Image createImage(String internalPath, float width, float height) {
		return createImage(TextureRegionLoader.load(internalPath), width,
				height, true);
	}

	/**
	 * Creates an image from a texture. Disposing the texture can be managed
	 * automatically when the menu is closed.<br>
	 * The image is set to the given size<br>
	 * Use autoFree=true if you don't know what to do!
	 * 
	 * @param tRef
	 * @param width
	 * @param height
	 * @param autoFree
	 * @return An image, which can be added to the menu.
	 */
	public Image createImage(TextureRegionRef tRef, float width, float height,
			boolean autoFree) {
		if (autoFree)
			managedTextures.add(tRef);
		Image img = new Image(tRef);
		img.setWidth(width);
		img.setHeight(height);
		return img;
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

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public int getStateId() {
		return stateId;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		initTransient();
	}
}
