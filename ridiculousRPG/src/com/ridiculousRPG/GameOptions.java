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

package com.ridiculousRPG;

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
	public String engineVersion = "0.3 prealpha (incomplete)";
	public String encoding = "UTF-8";
	public int width = 640, height = 480;
	public boolean fullscreen = false;
	public boolean useGL20 = false;
	public boolean resize = false;
	public boolean vSyncEnabled = false;
	public boolean debug = false;
	public String savePath = "ridiculousRPG/";
	public String i18nPath = "data/i18n";
	public String i18nDefault = "en";
	public ScriptFactory scriptFactory = new ScriptFactory();
	public String scriptLanguage = "JavaScript";
	public String[] scriptFileExtension = { ".js", ".jscript" };
	public String initScript = "data/script/initGameEngine.js";
	public String messageCallBackScript = "data/script/engine/messaging/defaultMessageBox.js";
	public String uiSkinNormalJson = "data/uiskin/skinNormal.json";
	public String uiSkinNormalAtlas = "data/uiskin/skinNormal.atlas";
	public String uiSkinFocusJson = "data/uiskin/skinFocus.json";
	public String uiSkinFocusAtlas = "data/uiskin/skinFocus.atlas";
	public String eventCustomTriggerTemplate = "data/script/engine/eventTemplate/onCustomTrigger.template";
	public String eventLoadTemplate = "data/script/engine/eventTemplate/onLoad.template";
	public String eventPushTemplate = "data/script/engine/eventTemplate/onPush.template";
	public String eventTimerTemplate = "data/script/engine/eventTemplate/onTimer.template";
	public String eventStateChangeTemplate = "data/script/engine/eventTemplate/onStateChange.template";
	public String eventTouchTemplate = "data/script/engine/eventTemplate/onTouch.template";
	public String weatherEffectSnow = "data/effect/weather/snow.png";
	public String weatherEffectRainNE = "data/effect/weather/rainNE.png";
	public String weatherEffectRainNW = "data/effect/weather/rainNW.png";
}
