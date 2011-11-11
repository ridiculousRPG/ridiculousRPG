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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.madthrax.ridiculousRPG.camera.CameraSimpleOrtho2D;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.events.Speed;
import com.madthrax.ridiculousRPG.movement.misc.MoveFadeColorAdapter;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.ui.DisplayErrorService;
import com.madthrax.ridiculousRPG.ui.DisplayTextService;

/**
 * @author Alexander Baumgartner
 */
public class GameBase extends GameServiceDefaultImpl implements
		ApplicationListener {
	private static GameBase instance;

	private SpriteBatch spriteBatch;
	private Camera camera;
	private ObjectState globalState;
	private GameServiceProvider serviceProvider;
	private ScriptFactory scriptFactory;
	private ScriptEngine sharedEngine;
	private GameOptions options;

	private int screenWidth, screenHeight;
	private int planeWidth, planeHeight;
	private int originalWidth, originalHeight;

	private boolean fullscreen, debugMode, resizeView;
	private boolean controlKeyPressedOld, controlKeyPressed,
			actionKeyPressedOld, actionKeyPressed;

	private Color gameColorTint = new Color(1f, 1f, 1f, 1f);
	private float gameColorBits = gameColorTint.toFloatBits();

	private final String engineVersion = "0.3 prealpha (incomplete)";

	public GameBase(GameOptions options) {
		this.options = options;
	}

	public final void create() {
		debugMode = options.debug;
		fullscreen = options.fullscreen;
		resizeView = options.resize;
		spriteBatch = new SpriteBatch();
		camera = new CameraSimpleOrtho2D();
		globalState = new ObjectState();
		camera.viewportWidth = planeWidth = screenWidth = originalWidth = Gdx.graphics
				.getWidth();
		camera.viewportHeight = planeHeight = screenHeight = originalHeight = Gdx.graphics
				.getHeight();
		// instance != null indicates that GameBase is initialized
		if (!isInitialized())
			instance = this;

		try {
			scriptFactory = options.scriptFactory.newInstance();
			serviceProvider = new GameServiceProvider();
			serviceProvider.init();
			// offer some essential services
			serviceProvider.putService(scriptFactory);
			serviceProvider.putService(DisplayTextService.$map);
			serviceProvider.putService(DisplayTextService.$screen);
			for (Constructor<GameService> service : options.initGameService) {
				serviceProvider.putService(service.newInstance());
			}
			// reorder text services to avoid painting over them
			serviceProvider.putService(DisplayTextService.$map);
			serviceProvider.putService(DisplayTextService.$screen);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			serviceProvider.dispose();
			String msg = "The following error occured while initializing the services:\n"
					+ e.getMessage() + "\n\n" + stackTrace;
			serviceProvider.requestAttention(new DisplayErrorService(msg),
					true, true);
		}

		camera.update();
	}

	/**
	 * Returns the FIRST GameBase instance which has been initialized to
	 * simplify the access
	 * 
	 * @return The first instance, which has been initialized.
	 */
	public static GameBase $() {
		if (!isInitialized())
			throw new IllegalStateException("GameBase not initialized!");
		return instance;
	}

	/**
	 * The {@link GameServiceProvider} from the first GameBase instance which
	 * has been initialized.<br>
	 * A shortcut for calling {@link GameBase#$()}{@link #getServiceProvider()
	 * .getServiceProvider()}
	 */
	public static GameServiceProvider $serviceProvider() {
		return $().getServiceProvider();
	}

	/**
	 * The {@link ScriptFactory} from the first GameBase instance which has been
	 * initialized.<br>
	 * A shortcut for calling {@link GameBase#$()}{@link #getScriptFactory()
	 * .getScriptFactory()}
	 */
	public static ScriptFactory $scriptFactory() {
		return $().getScriptFactory();
	}

	public final void render() {
		try {
			controlKeyPressedOld = controlKeyPressed;
			controlKeyPressed = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
					|| Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)
					|| Gdx.input.isTouched(2);
			actionKeyPressedOld = actionKeyPressed;
			actionKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE)
					|| Gdx.input.isKeyPressed(Input.Keys.ENTER)
					|| Gdx.input.isTouched(1)
					|| Gdx.input.isButtonPressed(Buttons.RIGHT);

			serviceProvider.computeAll();
			Thread.yield();
			serviceProvider.drawAll(debugMode);
		} catch (Exception e) {
			try {
				spriteBatch.end();
			} catch (Exception ignored) {
			}
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			serviceProvider.dispose();
			String msg = "The following error occured while executing the game:\n"
					+ e.getMessage() + "\n\n" + stackTrace;
			serviceProvider.requestAttention(new DisplayErrorService(msg),
					true, true);
		}
		Thread.yield();
	}

	/**
	 * Indicates if the first {@link GameBase} instance has been initialized.<br>
	 * (The first instance which is initzalized becomes the default)
	 * 
	 * @return true if initialized
	 */
	public static boolean isInitialized() {
		return instance != null;
	}

	/**
	 * Signals if the control key is just pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if left or right control key is just pressed or the third
	 *         finger touches the touchpad
	 */
	public boolean isControlKeyDown() {
		return controlKeyPressed && !controlKeyPressedOld;
	}

	/**
	 * Evaluates the given script term and returns the result.<br>
	 * The same engine is used for all evaluations!
	 * 
	 * @param script
	 *            The script to evaluate
	 * @return The result from this evaluation.
	 * @throws ScriptException
	 */
	public Object eval(String script) throws ScriptException {
		if (sharedEngine == null) {
			sharedEngine = getScriptFactory().obtainEngine();
		}
		Object result = sharedEngine
				.eval(getScriptFactory().loadScript(script));
		try {
			sharedEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE)
					.clear();
		} catch (Exception ignored) {
		}
		return result;
	}

	/**
	 * Signals if the action key is just pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if space, enter or the right mouse button is just pressed
	 *         (left mouse button is used for movement) or the second finger
	 *         touches the touchpad (first finger is used for movement)
	 */
	public boolean isActionKeyDown() {
		return actionKeyPressed && !actionKeyPressedOld;
	}

	/**
	 * Signals if the control key is pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if left or right control key is pressed or the third finger
	 *         touches the touchpad
	 */
	public boolean isControlKeyPressed() {
		return controlKeyPressed && controlKeyPressedOld;
	}

	/**
	 * Signals if the action key is pressed.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if space, enter or the right mouse button is pressed (left
	 *         mouse button is used for movement) or the second finger touches
	 *         the touchpad (first finger is used for movement)
	 */
	public boolean isActionKeyPressed() {
		return actionKeyPressed && actionKeyPressedOld;
	}

	/**
	 * Signals if the control key is just released.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if left or right control key is just released or the third
	 *         finger released the touchpad
	 */
	public boolean isControlKeyUp() {
		return controlKeyPressedOld && !controlKeyPressed;
	}

	/**
	 * Signals if the action key is just released.<br>
	 * This method is safe for hiding the keyboard on mobile devices
	 * 
	 * @return true if space, enter or the right mouse button is just released
	 *         (left mouse button is used for movement) or the second finger
	 *         released the touchpad (first finger is used for movement)
	 */
	public boolean isActionKeyUp() {
		return actionKeyPressedOld && !actionKeyPressed;
	}

	public void resize(int width, int height) {
		Camera cam = camera;
		if (resizeView) {
			float centerX = cam.viewportWidth * .5f;
			float centerY = cam.viewportHeight * .5f;
			cam.viewportWidth *= (float) Gdx.graphics.getWidth()
					/ (float) screenWidth;
			cam.viewportHeight *= (float) Gdx.graphics.getHeight()
					/ (float) screenHeight;
			centerX -= cam.viewportWidth * .5f;
			centerY -= cam.viewportHeight * .5f;
			cam.translate(centerX, centerY, 0);
		}
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		cam.update();
	}

	public void pause() {
		// TODO: save state
		serviceProvider.requestAttention(this, true, false);
	}

	public void resume() {
		// TODO: load state
		serviceProvider.releaseAttention(this);
	}

	/**
	 * The default tint is {@link Color#WHITE}. That means, that everything is
	 * drawn as it is.<br>
	 * Reset to {@link Color#WHITE} if you want to remove the coloring.<br>
	 * If you use the alpha channel on the entire game, all layers of a map will
	 * become visible. That's probably not what you want.<br>
	 * If you want to fade the entire game out, make a transition to
	 * {@link Color#BLACK}.<br>
	 * Tip: Use an {@link EventObject} in combination with the
	 * {@link MoveFadeColorAdapter} to create color animations (e.g. day and
	 * night effects).
	 * 
	 * @see {@link MoveFadeColorAdapter#$(Speed, Color, boolean)}
	 */
	public void setGameColorTint(Color tint) {
		gameColorTint = tint;
		gameColorBits = tint.toFloatBits();
	}

	public Color getGameColorTint() {
		return gameColorTint;
	}

	public float getGameColorBits() {
		return gameColorBits;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * The {@link GameServiceProvider} contains all services which are managed
	 * inside this engine instance.
	 */
	public GameServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	/**
	 * The {@link #globalState} can be used to share global variables
	 */
	public ObjectState getGlobalState() {
		return globalState;
	}

	public boolean isResizeView() {
		return resizeView;
	}

	public void setResizeView(boolean resizeView) {
		this.resizeView = resizeView;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * Don't forget to update the camera after changing the screen dimension!
	 */
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	/**
	 * Don't forget to update the camera after changing the screen dimension!
	 */
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getPlaneWidth() {
		return planeWidth;
	}

	/**
	 * Don't forget to update the camera after changing the drawing planes size!
	 */
	public void setPlaneWidth(int planeWidth) {
		this.planeWidth = planeWidth;
	}

	public int getPlaneHeight() {
		return planeHeight;
	}

	/**
	 * Don't forget to update the camera after changing the drawing planes size!
	 */
	public void setPlaneHeight(int planeHeight) {
		this.planeHeight = planeHeight;
	}

	/**
	 * True if the game is running in fullscreen mode
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	/**
	 * Toggle the fullscreen mode
	 * 
	 * @return true if succeeded, false otherwise
	 */
	public boolean toggleFullscreen() {
		try {
			fullscreen = !fullscreen;
			// resize is called
			Gdx.graphics.setDisplayMode(originalWidth, originalHeight,
					fullscreen);
			return true;
		} catch (Throwable notTooBad) {
		}
		return false;
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public int getOriginalWidth() {
		return originalWidth;
	}

	public int getOriginalHeight() {
		return originalHeight;
	}

	public ScriptFactory getScriptFactory() {
		return scriptFactory;
	}

	/**
	 * True if the game is running in debug mode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * The version of the engine as string
	 */
	public String getEngineVersion() {
		return engineVersion;
	}

	public void dispose() {
		if (fullscreen)
			toggleFullscreen();
		serviceProvider.dispose();
		if (spriteBatch != null)
			spriteBatch.dispose();
		GameConfig.get().dispose();
	}
}
