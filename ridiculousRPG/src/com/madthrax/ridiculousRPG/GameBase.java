package com.madthrax.ridiculousRPG;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.madthrax.ridiculousRPG.camera.CameraSimpleOrtho2D;
import com.madthrax.ridiculousRPG.camera.CameraToggleFullscreenService;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.events.Speed;
import com.madthrax.ridiculousRPG.movement.misc.MoveFadeColorAdapter;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.ui.DisplayErrorService;
import com.madthrax.ridiculousRPG.ui.DisplayFPSService;
import com.madthrax.ridiculousRPG.ui.DisplayTextService;

public class GameBase extends GameServiceDefaultImpl implements ApplicationListener {
	public static SpriteBatch spriteBatch;
	public static Camera camera;
	private static Color gameColorTint = new Color(1f,1f,1f,1f);
	private static float gameColorBits = gameColorTint.toFloatBits();
	/**
	 * The {@link #globalState} can be used to share global variables
	 */
	public static ObjectState globalState;
	public static boolean resizeView;
	/**
	 * Don't forget to update the camera after changing
	 * the screen dimension!
	 */
	public static int screenWidth, screenHeight;
	/**
	 * Don't forget to update the camera after changing
	 * the drawing planes size!
	 */
	public static int planeWidth, planeHeight;
	/**
	 * Don't change this value!<br>
	 * It should always store the original size from
	 * initializing the game.
	 */
	public static int originalWidth, originalHeight;
	/**
	 * True if the game is running in debug mode
	 */
	public static boolean debugMode;
	/**
	 * True if the game is running in fullscreen mode
	 */
	public static boolean fullscreen;
	/**
	 * The version of the engine as string
	 */
	public static final String engineVersion = "0.3 prealpha (incomplete)";

	private static boolean controlKeyPressed, pushActionPressed;

	public GameBase(GameOptions options){
		debugMode = options.debug;
		fullscreen = options.fullscreen;
		resizeView = options.resize;
		if (options.initGameService instanceof DisplayErrorService) {
			GameServiceProvider.requestAttention(options.initGameService, true, true);
		} else {
			GameServiceProvider.putService(options.initGameService);
		}
	}

	@Override
	public final void create() {
		spriteBatch = new SpriteBatch();
		camera = new CameraSimpleOrtho2D();
		globalState = new ObjectState();

		camera.viewportWidth =planeWidth =screenWidth =originalWidth =Gdx.graphics.getWidth();
		camera.viewportHeight=planeHeight=screenHeight=originalHeight=Gdx.graphics.getHeight();

		try {
			GameServiceProvider.init();
			GameServiceProvider.putService(DisplayTextService.$map);
			GameServiceProvider.putService(DisplayTextService.$screen);
			if (debugMode) {
				GameServiceProvider.putService(new DisplayFPSService());
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			GameServiceProvider.dispose();
			String msg = "The following error occured while initializing the services:\n"
					+e.getMessage()+"\n\n"+stackTrace;
			GameServiceProvider.requestAttention(new DisplayErrorService(msg), true, true);
		}

		camera.update();
	}

	@Override
	public final void render() {
		try {
			controlKeyPressed = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
				|| Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)
				|| Gdx.input.isTouched(2);
			pushActionPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE)
				|| Gdx.input.isKeyPressed(Input.Keys.ENTER)
				|| Gdx.input.isTouched(1)
				|| Gdx.input.isButtonPressed(Buttons.RIGHT);
	
			GameServiceProvider.computeAll();
			Thread.yield();
			GameServiceProvider.drawAll(debugMode);
		} catch (Exception e) {
			try {
				spriteBatch.end();
			} catch (Exception ignored) {}
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			GameServiceProvider.dispose();
			String msg = "The following error occured while executing the game:\n"
					+e.getMessage()+"\n\n"+stackTrace;
			GameServiceProvider.requestAttention(new DisplayErrorService(msg), true, true);
		}
		Thread.yield();
	}

	public static boolean isGameInitialized() {
		return globalState!=null;
	}
	/**
	 * Signals if the control key is pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * @return true if left or right control key is pressed or the 
	 * third finger touches the touchpad
	 */
	public static boolean isControlKeyPressed() {
		return controlKeyPressed;
	}
	/**
	 * Signals if the action key is pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * @return true if space, enter or the right mouse button is pressed
	 * (left mouse button is used for movement) or the second finger touches
	 * the touchpad (first finger is used for movement)
	 */
	public static boolean isActionKeyPressed() {
		return pushActionPressed;
	}
	@Override
	public void resize(int width, int height) {
		Camera cam = GameBase.camera;
		if (resizeView) {
			float centerX = cam.viewportWidth*.5f;
			float centerY = cam.viewportHeight*.5f;
			cam.viewportWidth *= (float)Gdx.graphics.getWidth()/(float)screenWidth;
			cam.viewportHeight*= (float)Gdx.graphics.getHeight()/(float)screenHeight;
			centerX -= cam.viewportWidth*.5f;
			centerY -= cam.viewportHeight*.5f;
			cam.translate(centerX, centerY, 0);
		}
		screenWidth = Gdx.graphics.getWidth();
		screenHeight= Gdx.graphics.getHeight();
		cam.update();
	}

	@Override
	public void pause() {
		//TODO: save state
		GameServiceProvider.requestAttention(this, true, false);
	}
	@Override
	public void resume() {
		//TODO: load state
		GameServiceProvider.releaseAttention(this);
	}

	/**
	 * The default tint is {@link Color#WHITE}. That means, that everything is
	 * drawn as it is.<br>
	 * Reset to {@link Color#WHITE} if you want to remove the coloring.<br>
	 * If you use the alpha channel on the entire game, all layers of a map
	 * will become visible. That's probably not what you want.<br>
	 * If you want to fade
	 * the entire game out, make a transition to {@link Color#BLACK}.<br>
	 * Tip: Use an {@link EventObject} in combination with the {@link MoveFadeColorAdapter}
	 * to create color animations (e.g. day and night effects).
	 * @see {@link MoveFadeColorAdapter#$(Speed, Color, boolean)}
	 */
	public static void setGameColorTint(Color tint) {
		gameColorTint = tint;
		gameColorBits = tint.toFloatBits();
	}
	public static Color getGameColorTint() {
		return gameColorTint;
	}
	public static float getGameColorBits() {
		return gameColorBits;
	}
	@Override
	public void dispose() {
		if (fullscreen) CameraToggleFullscreenService.toggleFullscreen();
		GameServiceProvider.dispose();
		if (spriteBatch!=null) spriteBatch.dispose();
		GameConfig.get().dispose();
	}
}
