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

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.Properties;

import com.ridiculousRPG.GameOptions.Backend;

/**
 * This class takes a simple configuration text file and reads the properties
 * into the {@link GameOptions} object.
 * 
 * @author Alexander Baumgartner
 */
public class GameOptionsDefaultConfigReader {
	public GameOptions options = new GameOptions();

	/**
	 * This constructor parses the options from an properties file. The name of
	 * the file is specified inside the launcher by
	 * {@link GameLauncher#OPTIONS_FILE}.<br>
	 * Feel free to write your own launcher if you prefer an XML structure or
	 * whatever.
	 * 
	 * @param iniFile
	 */
	@SuppressWarnings("unchecked")
	public GameOptionsDefaultConfigReader(File iniFile) {
		Properties props = new Properties();
		String propTmp;
		try {
			props.load(new FileReader(iniFile));

			propTmp = props.getProperty("TITLE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.title = propTmp.trim();
			}

			propTmp = props.getProperty("FULLSCREEN");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.fullscreen = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("DEBUG");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.debug = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("BACKEND");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.backend = Backend.valueOf(propTmp.trim());
			}

			propTmp = props.getProperty("RESIZE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.resize = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("USEGL20");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.useGL20 = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("VSYNC");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.vSyncEnabled = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("WIDTH");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.width = Integer.parseInt(propTmp.trim());
			}

			propTmp = props.getProperty("HEIGHT");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.height = Integer.parseInt(propTmp.trim());
			}

			propTmp = props.getProperty("ENCODING");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.encoding = propTmp.trim();
			}

			propTmp = props.getProperty("I18N_PATH");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.i18nPath = propTmp.trim();
			}

			propTmp = props.getProperty("I18N_DEFAULT_LANG");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.i18nDefault = propTmp.trim();
			}

			propTmp = props.getProperty("SAVE_PATH");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.savePath = propTmp.trim();
			}

			propTmp = props.getProperty("SCRIPT_LANGUAGE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.scriptLanguage = propTmp.trim();
			}

			propTmp = props.getProperty("SCRIPT_EXTENSIONS");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.scriptFileExtension = propTmp.trim().split("\\s*,\\s*");
			}

			propTmp = props.getProperty("WEATHER_EFFECT_SNOW");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.weatherEffectSnow = propTmp.trim();
			}

			propTmp = props.getProperty("WEATHER_EFFECT_RAIN_NE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.weatherEffectRainNE = propTmp.trim();
			}

			propTmp = props.getProperty("WEATHER_EFFECT_RAIN_NW");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.weatherEffectRainNW = propTmp.trim();
			}

			propTmp = props.getProperty("SCRIPT_FACTORY");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.scriptFactory = ((Constructor<ScriptFactory>) Class
						.forName(propTmp.trim()).getConstructor())
						.newInstance();
			}
			options.scriptFactory.setScriptLanguage(options.scriptLanguage);
			options.scriptFactory
					.setScriptFileExtension(options.scriptFileExtension);

			propTmp = props.getProperty("INIT_SCRIPT");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.initScript = propTmp;
			}

			propTmp = props.getProperty("MESSAGE_CALLBACK_SCRIPT");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.messageCallBackScript = propTmp.trim();
			}

			propTmp = props.getProperty("UI_SKIN_NORMAL");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.uiSkinNormalJson = propTmp + ".json";
				options.uiSkinNormalAtlas = propTmp + ".atlas";
			}

			propTmp = props.getProperty("UI_SKIN_FOCUS");
			if (propTmp != null && propTmp.trim().length() > 0) {
				options.uiSkinFocusJson = propTmp + ".json";
				options.uiSkinFocusAtlas = propTmp + ".atlas";
			}
		} catch (Exception e) {
			System.err.println("Could not load file '" + iniFile.getPath()
					+ "' - Fallback to default options!");
			e.printStackTrace();
		}
	}
}
