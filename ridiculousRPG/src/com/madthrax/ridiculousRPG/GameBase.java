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
import java.util.HashMap;

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
import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.camera.CameraSimpleOrtho2D;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.event.Speed;
import com.madthrax.ridiculousRPG.map.MapRenderService;
import com.madthrax.ridiculousRPG.movement.misc.MoveFadeColorAdapter;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.ui.DisplayErrorService;

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

	private Rectangle plane = new Rectangle();
	private Rectangle screen = new Rectangle();

	private HashMap<String, EventObject> globalEvents = new HashMap<String, EventObject>();

	private boolean triggerActionKeyPressed;
	private boolean fullscreen;
	private boolean controlKeyPressedOld, controlKeyPressed,
			actionKeyPressedOld, actionKeyPressed;

	private Color backgroundColor = new Color(0f, 0f, 0f, 1f);
	private Color gameColorTint = new Color(1f, 1f, 1f, 1f);
	private float gameColorBits = gameColorTint.toFloatBits();

	private final String engineVersion = "0.3 prealpha (incomplete)";

	public GameBase(GameOptions options) {
		this.options = options;
	}

	public final void create() {
		fullscreen = options.fullscreen;
		scriptFactory = options.scriptFactory;
		spriteBatch = new SpriteBatch();
		camera = new CameraSimpleOrtho2D();
		globalState = new ObjectState();
		serviceProvider = new GameServiceProvider();
		plane.width = camera.viewportWidth = screen.width = options.width = Gdx.graphics
				.getWidth();
		plane.height = camera.viewportHeight = screen.height = options.height = Gdx.graphics
				.getHeight();
		// instance != null indicates that GameBase is initialized
		if (!isInitialized())
			instance = this;

		try {
			scriptFactory.init(options.initScript);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			serviceProvider.dispose();
			String msg = "The following error occured while initializing the game:\n"
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
	 * has been initialized is used to obtain the requested service.<br>
	 * A shortcut for calling {@link #$serviceProvider()
	 * (TYPE_CAST)$serviceProvider()}
	 * {@link GameServiceProvider#getService(String) .getService(String)}
	 * 
	 * @return The requested service or null if no service matches.
	 * @throws ClassCastException
	 *             If the named service is not from type T
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GameService> T $service(String name) {
		if (!isInitialized())
			throw new IllegalStateException("GameBase not initialized!");
		return (T) $serviceProvider().getService(name);
	}

	/**
	 * The {@link GameServiceProvider} from the first GameBase instance which
	 * has been initialized.<br>
	 * A shortcut for calling {@link GameBase#$()}{@link #getServiceProvider()
	 * .getServiceProvider()}
	 * 
	 * @return The standard game service provider
	 */
	public static GameServiceProvider $serviceProvider() {
		return $().getServiceProvider();
	}

	/**
	 * The {@link ScriptFactory} from the first GameBase instance which has been
	 * initialized.<br>
	 * A shortcut for calling {@link GameBase#$()}{@link #getScriptFactory()
	 * .getScriptFactory()}
	 * 
	 * @return The standard script factory
	 */
	public static ScriptFactory $scriptFactory() {
		return $().getScriptFactory();
	}

	/**
	 * The {@link GameOptions} from the first GameBase instance which has been
	 * initialized.<br>
	 * A shortcut for calling {@link GameBase#$()}{@link #getOptions()
	 * .getOptions()}
	 * 
	 * @return The standard script factory
	 */
	public static GameOptions $options() {
		return $().getOptions();
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
					|| Gdx.input.isButtonPressed(Buttons.RIGHT)
					|| triggerActionKeyPressed;

			triggerActionKeyPressed = false;

			serviceProvider.computeAll();
			Thread.yield();
			serviceProvider.drawAll(options.debug);
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
	 * The start up options for the game
	 * 
	 * @return start up options
	 */
	public GameOptions getOptions() {
		return options;
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

	/**
	 * Resizes the planes dimensions (e.g. the dimension of the tiled map).
	 */
	public void resizePlane(int planeWidth, int planeHeight) {
		plane.width = planeWidth;
		plane.height = planeHeight;
		camera.update();
		serviceProvider.resize((int) screen.width, (int) screen.height);
	}

	@Override
	public void resize(int width, int height) {
		Camera cam = camera;
		if (options.resize) {
			float centerX = cam.viewportWidth * .5f;
			float centerY = cam.viewportHeight * .5f;
			cam.viewportWidth *= width / screen.width;
			cam.viewportHeight *= height / screen.height;
			centerX -= cam.viewportWidth * .5f;
			centerY -= cam.viewportHeight * .5f;
			cam.translate(centerX, centerY, 0);
		}
		screen.width = width;
		screen.height = height;
		cam.update();
		serviceProvider.resize(width, height);
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

	/**
	 * Returns the screen bounds. x and y should always be zero.<br>
	 * Don't forget to update the camera if you change the bounds.
	 * 
	 * @return The windows (or full screens) dimension
	 */
	public Rectangle getScreen() {
		return screen;
	}

	/**
	 * Returns the planes position, which is actually shown by the camera and
	 * the planes width and height .<br>
	 * <br>
	 * The planes position will automatically be updated by
	 * {@link CameraSimpleOrtho2D}. So... use
	 * {@link CameraSimpleOrtho2D#translate(float, float, float)} or
	 * {@link CameraSimpleOrtho2D#lookAt(float, float, float)} instead setting
	 * it directly!<br>
	 * Furthermore you should use {@link #setPlaneWidth(int)} and
	 * {@link #setPlaneHeight(int)} to change the planes dimension! The
	 * dimension should be set to the displayed maps width and height. For
	 * example the class {@link MapRenderService} will automatically change the
	 * dimension when a new map is loaded.
	 * 
	 * @return The planes bounds actually displayed on the screen
	 */
	public Rectangle getPlane() {
		return plane;
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
			// resize is called
			Gdx.graphics.setDisplayMode(options.width, options.height,
					!fullscreen);
			// In Linux setDisplayMode returns false even though it succeeds
			// ==> DON'T make an if statement!
			fullscreen = !fullscreen;
			return true;
		} catch (Throwable notTooBad) {
		}
		return false;
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public int getOriginalWidth() {
		return options.width;
	}

	public int getOriginalHeight() {
		return options.height;
	}

	public ScriptFactory getScriptFactory() {
		return scriptFactory;
	}

	/**
	 * The version of the engine as string
	 */
	public String getEngineVersion() {
		return engineVersion;
	}

	public static void toggleCursor() {
		Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
	}

	public void dispose() {
		if (fullscreen)
			toggleFullscreen();
		serviceProvider.dispose();
		if (spriteBatch != null)
			spriteBatch.dispose();
		GameConfig.get().dispose();
	}

	/**
	 * Exits the running game
	 */
	public static void exit() {
		Gdx.app.exit();
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setGlobalEvents(HashMap<String, EventObject> globalEvents) {
		this.globalEvents = globalEvents;
	}

	public HashMap<String, EventObject> getGlobalEvents() {
		return globalEvents;
	}

	/**
	 * Simulates the action key pressed event.
	 */
	public void triggerActionKeyPressed() {
		this.triggerActionKeyPressed = true;
	}
}
