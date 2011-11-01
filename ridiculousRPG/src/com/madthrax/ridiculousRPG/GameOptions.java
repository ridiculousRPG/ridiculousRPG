package com.madthrax.ridiculousRPG;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.ui.DisplayErrorService;

/**
 * This class is used to define the initial values for your game
 */
public class GameOptions {
	public enum Backend {
		LWJGL, JOGL //, ANDROID, APPLET, ANGLE, GWT
	}
	public Backend backend = Backend.LWJGL;
	public String title = "Game running";
	public int width = 640, height = 480;
	public boolean fullscreen = false;
	public boolean useGL20 = false;
	public boolean resize = false;
	public boolean vSyncEnabled = false;
	public boolean debug = false;
	public GameService initGameService;

	/**
	 * Constructs an option object with the specified
	 * game service for initializing the game.
	 * @param initGameService
	 */
	public GameOptions(GameService initGameService) {
		if (initGameService==null) {
			throw new NullPointerException("The argument \"initGameService\" is mandatory.");
		}
		this.initGameService = initGameService;
	}

	/**
	 * This constructor parses the options from the command line.
	 * @param argv
	 */
	public GameOptions(FileHandle iniFile) {
		Properties props = new Properties();
		String propTmp;
		try {
			props.load(iniFile.read());

			propTmp = props.getProperty("TITLE");
			if (propTmp!=null && propTmp.trim().length()>0) {
				title = propTmp.trim();
			}
			
			propTmp = props.getProperty("FULLSCREEN");
			if (propTmp!=null && propTmp.trim().length()>0) {
				fullscreen = "true".equalsIgnoreCase(propTmp.trim());
			}
			
			propTmp = props.getProperty("DEBUG");
			if (propTmp!=null && propTmp.trim().length()>0) {
				debug = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("BACKEND");
			if (propTmp!=null && propTmp.trim().length()>0) {
				if ("JOGL".equalsIgnoreCase(propTmp.trim()))
					backend = Backend.JOGL;
			}

			propTmp = props.getProperty("RESIZE");
			if (propTmp!=null && propTmp.trim().length()>0) {
				resize = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("USEGL20");
			if (propTmp!=null && propTmp.trim().length()>0) {
				useGL20 = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("VSYNC");
			if (propTmp!=null && propTmp.trim().length()>0) {
				vSyncEnabled = "true".equalsIgnoreCase(propTmp.trim());
			}

			propTmp = props.getProperty("WIDTH");
			if (propTmp!=null && propTmp.trim().length()>0) {
				width = Integer.parseInt(propTmp.trim());
			}
			
			propTmp = props.getProperty("HEIGHT");
			if (propTmp!=null && propTmp.trim().length()>0) {
				height = Integer.parseInt(propTmp.trim());
			}

			propTmp = props.getProperty("INITGAMESERVICE");
			if (propTmp!=null && propTmp.trim().length()>0) {
				initGameService = (GameService) Class.forName(propTmp.trim()).newInstance();
			} else {
				initGameService = new DisplayErrorService("Please specify the startup service for the game.\n" +
						"The startup service is specified by the property\n" +
						"INITGAMESERVICE inside the File data/game.ini");
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			initGameService = new DisplayErrorService("The following error occured while loading the game:\n"
					+e.getMessage()+"\n\n"+stackTrace);
		}
	}
}
