package com.madthrax.ridiculousRPG;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.backends.jogl.JoglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

/**
 * Launches the game from either an android handy or a desktop pc.<br>
 * Simply create your own Launcher if you don't want the branding
 * &quot;(powered by ridiculousRPG)&quot;
 */
public class GameLauncher extends AndroidApplication {
	protected static final String BRANDING_NORMAL = " (powered by ridiculousRPG)";
	protected static final String BRANDING_DEBUG = " (powered by ridiculousRPG - DEBUGMODE)";
	/**
	 * AUTOMATICALLY CALLED BY ANDROID<br>
	 * The game engine is able to start any game by an Android activity.<br>
	 * You don't need an other Android-activity inside your game.
	 * @param savedInstanceState
	 */
	public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		GameOptions options = new GameOptions(new AndroidFiles(getAssets()).internal("data/game.ini"));
        setTitle(options.title+(options.debug?BRANDING_DEBUG:BRANDING_NORMAL));
        initialize(new GameBase(options), options.useGL20);
    }
	/**
	 * CALLED BY JVM ON STARTUP<br>
	 * The game engine is able to start any game from the command line.<br>
	 * You don't need an other main method inside your game.<br>
	 * Simply set up your start parameters and run the game from the
	 * command line.<br>
	 * @param argv
	 */
	public static void main(String[] argv) {
		GameOptions options = new GameOptions(new LwjglFiles().internal("data/game.ini"));
		switch (options.backend) {
		case LWJGL: {
			LwjglApplicationConfiguration conf = new LwjglApplicationConfiguration();
			conf.title = options.title+(options.debug?BRANDING_DEBUG:BRANDING_NORMAL);
			conf.width = options.width;
			conf.height = options.height;
			conf.useGL20 = options.useGL20;
			conf.fullscreen = options.fullscreen;
			conf.vSyncEnabled = options.vSyncEnabled;
			new LwjglApplication(new GameBase(options), conf);
		}	break;
		case JOGL: {
			JoglApplicationConfiguration conf = new JoglApplicationConfiguration();
			conf.title = options.title+(options.debug?BRANDING_DEBUG:BRANDING_NORMAL);
			conf.width = options.width;
			conf.height = options.height;
			conf.useGL20 = options.useGL20;
			conf.fullscreen = options.fullscreen;
			conf.vSyncEnabled = options.vSyncEnabled;
			new JoglApplication(new GameBase(options), conf);
		}	break;
		}
	}
}
