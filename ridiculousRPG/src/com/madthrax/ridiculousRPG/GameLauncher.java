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

import java.io.File;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.SharedDrawable;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.backends.jogl.JoglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Launches the game from either an android handy or a desktop pc.<br>
 * Simply create your own Launcher if you don't want the branding &quot;(powered
 * by ridiculousRPG)&quot;.
 * 
 * @author Alexander Baumgartner
 */
public class GameLauncher extends AndroidApplication {
	public static String BRANDING = " (powered by ridiculousRPG)";
	public static String DEBUG_TEXT = " *DEBUGMODE*";
	/**
	 * Dafault = "data/game.ini"
	 */
	public static String OPTIONS_FILE = "data/game.ini";

	/**
	 * AUTOMATICALLY CALLED BY ANDROID<br>
	 * The game engine is able to start any game by an Android activity.<br>
	 * You don't need an other Android-activity inside your game.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GameOptions options = new GameOptionsDefaultConfigReader(
				new AndroidFiles(getAssets()).internal(OPTIONS_FILE).file()).options;
		options.fullscreen = true;
		setTitle(buildTitle(options.title, options.debug));
		initialize(new GameBase(options) {
			@Override
			public boolean shareGLContext() {
				return false;
			}
		}, options.useGL20);
	}

	/**
	 * Builds the title for the window.
	 * 
	 * @param title
	 * @param debug
	 * @return
	 */
	protected static String buildTitle(String title, boolean debug) {
		if (debug)
			title += GameLauncher.DEBUG_TEXT;
		title += BRANDING;
		return title;
	}

	/**
	 * Equivalent to {@link #onCreate(Bundle)} for the desktop.
	 */
	public static void onCreateDesktop() {
		GameOptions options = new GameOptionsDefaultConfigReader(new File(
				OPTIONS_FILE)).options;
		switch (options.backend) {
		case LWJGL: {
			LwjglApplicationConfiguration conf = new LwjglApplicationConfiguration();
			conf.title = buildTitle(options.title, options.debug);
			conf.width = options.width;
			conf.height = options.height;
			conf.useGL20 = options.useGL20;
			conf.fullscreen = options.fullscreen;
			conf.vSyncEnabled = options.vSyncEnabled;
			new LwjglApplication(new GameBase(options) {
				@Override
				public boolean shareGLContext() {
					try {
						new SharedDrawable(Display.getDrawable()).makeCurrent();
						return true;
					} catch (Exception e) {
						System.out.println("Couldn't share context.");
						return false;
					}
				}
			}, conf);
		}
			break;
		case JOGL: {
			JoglApplicationConfiguration conf = new JoglApplicationConfiguration();
			conf.title = buildTitle(options.title, options.debug);
			conf.width = options.width;
			conf.height = options.height;
			conf.useGL20 = options.useGL20;
			conf.fullscreen = options.fullscreen;
			conf.vSyncEnabled = options.vSyncEnabled;
			new JoglApplication(new GameBase(options) {
				@Override
				public boolean shareGLContext() {
					return false;
				}
			}, conf);
		}
			break;
		}
	}

	/**
	 * CALLED BY JVM ON STARTUP<br>
	 * The game engine is able to start any game from the command line.<br>
	 * You don't need an other main method inside your game.<br>
	 * Simply set up your start parameters and run the game from the command
	 * line.<br>
	 * 
	 * @param argv
	 */
	public static void main(String[] argv) {
		onCreateDesktop();
	}
}
