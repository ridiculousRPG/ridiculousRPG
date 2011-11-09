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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.ui.DisplayErrorService;

/**
 * This class is used to define the initial values for your game
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
	public int width = 640, height = 480;
	public boolean fullscreen = false;
	public boolean useGL20 = false;
	public boolean resize = false;
	public boolean vSyncEnabled = false;
	public boolean debug = false;
	public GameService[] initGameService;
	public ScriptFactory scriptFactory;

	/**
	 * Constructs an option object with the specified game services for
	 * initializing the game.<br>
	 * The {@link GameService} will be executed in the specified order.
	 * 
	 * @param initGameService
	 */
	public GameOptions(GameService... initGameService) {
		if (initGameService == null) {
			throw new NullPointerException(
					"The argument \"initGameService\" is mandatory.");
		}
		this.initGameService = initGameService;
	}

	/**
	 * This constructor parses the options from an properties file.
	 * The name of the file is specified by {@link GameLauncher#GAME_OPTIONS_FILE}
	 * @param iniFile
	 */
	public GameOptions(FileHandle iniFile) {
		Properties props = new Properties();
		String propTmp;
		try {
			props.load(new InputStreamReader(iniFile.read(), "UTF-8"));

			propTmp = props.getProperty("TITLE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				title = propTmp.trim();
			}

			propTmp = props.getProperty("FULLSCREEN");
			if (propTmp != null && propTmp.trim().length() > 0) {
				fullscreen = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("DEBUG");
			if (propTmp != null && propTmp.trim().length() > 0) {
				debug = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("BACKEND");
			if (propTmp != null && propTmp.trim().length() > 0) {
				if ("JOGL".equalsIgnoreCase(propTmp.trim()))
					backend = Backend.JOGL;
			}

			propTmp = props.getProperty("RESIZE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				resize = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("USEGL20");
			if (propTmp != null && propTmp.trim().length() > 0) {
				useGL20 = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("VSYNC");
			if (propTmp != null && propTmp.trim().length() > 0) {
				vSyncEnabled = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("WIDTH");
			if (propTmp != null && propTmp.trim().length() > 0) {
				width = Integer.parseInt(propTmp.trim());
			}

			propTmp = props.getProperty("HEIGHT");
			if (propTmp != null && propTmp.trim().length() > 0) {
				height = Integer.parseInt(propTmp.trim());
			}

			propTmp = props.getProperty("SCRIPT_FACTORY");
			if (propTmp != null && propTmp.trim().length() > 0) {
				scriptFactory = (ScriptFactory) Class.forName(
						propTmp.trim()).newInstance();
			} else {
				scriptFactory = new ScriptFactory();
			}

			propTmp = props.getProperty("INITGAMESERVICE");
			if (propTmp != null && propTmp.trim().length() > 0) {
				String[] multiPropTmp = propTmp.split("[,\\s]+");
				initGameService = new GameService[multiPropTmp.length];
				for (int i = 0; i < multiPropTmp.length; i++) {
					initGameService[i] = (GameService) Class.forName(
							multiPropTmp[i].trim()).newInstance();
				}
			} else {
				initGameService = new GameService[] { new DisplayErrorService(
						"Please specify the startup service for the game.\n"
								+ "The startup service is specified by the property\n"
								+ "INITGAMESERVICE inside the File data/game.ini") };
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			initGameService = new GameService[] { new DisplayErrorService(
					"The following error occured while loading the game:\n"
							+ e.getMessage() + "\n\n" + stackTrace) };
		}
	}
}
