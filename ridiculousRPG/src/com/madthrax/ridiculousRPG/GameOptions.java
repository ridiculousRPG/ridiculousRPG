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

package com.madthrax.ridiculousRPG;


/**
 * This class is used to define the initial values for your game.
 * 
 * @author Alexander Baumgartner
 */
public class GameOptions {
	public enum Backend {
		LWJGL, JOGL
		// , ANDROID, APPLET, ANGLE, GWT
	}
	public Backend backend = Backend.LWJGL;
	public String title = "Game running";
	public String encoding = "UTF-8";
	public int width = 640, height = 480;
	public boolean fullscreen = false;
	public boolean useGL20 = false;
	public boolean resize = false;
	public boolean vSyncEnabled = false;
	public boolean debug = false;
	public ScriptFactory scriptFactory = new ScriptFactory();
	public String initScript = "data/scripts/initGame.js";
	public String uiSkinNormalConfig = "data/uiskin/skinNormal.json";
	public String uiSkinNormalImage = "data/uiskin/skinNormal.png";
	public String uiSkinFocusConfig = "data/uiskin/skinFocus.json";
	public String uiSkinFocusImage = "data/uiskin/skinFocus.png";
}
