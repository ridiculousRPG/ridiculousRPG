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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.ridiculousRPG.camera.CameraSimpleOrtho2D;
import com.ridiculousRPG.camera.CameraTrackMovableService;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.i18n.TextLoader;
import com.ridiculousRPG.map.MapRenderService;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.misc.MoveFadeColorAdapter;
import com.ridiculousRPG.service.GameService;
import com.ridiculousRPG.service.GameServiceDefaultImpl;
import com.ridiculousRPG.service.GestureDetectorService;
import com.ridiculousRPG.ui.DisplayErrorService;
import com.ridiculousRPG.ui.MenuService;
import com.ridiculousRPG.util.ColorSerializable;
import com.ridiculousRPG.util.ExecInMainThread;
import com.ridiculousRPG.util.ExecWithGlContext;
import com.ridiculousRPG.util.ObjectState;
import com.ridiculousRPG.util.Speed;
import com.ridiculousRPG.util.Zipper;

/**
 * @author Alexander Baumgartner
 */
public abstract class GameBase extends GameServiceDefaultImpl implements
		ApplicationListener {
	private static GameBase instance;
	private SpriteBatch spriteBatch;
	private Camera camera;
	private ObjectState globalState;
	private GameServiceProvider serviceProvider;
	private ScriptFactory scriptFactory;
	private ScriptEngine sharedEngine;
	private GameOptions options;
	private TextLoader i18n;

	private Rectangle plane = new Rectangle();
	private Rectangle screen = new Rectangle();

	private List<Thread> glContextThread = new ArrayList<Thread>();
	private Map<String, EventObject> globalEvents = new HashMap<String, EventObject>();

	private boolean exitForced = true;
	private boolean triggerActionKeyPressed;
	private boolean fullscreen;
	private boolean controlKeyPressedOld, controlKeyPressed,
			actionKeyPressedOld, actionKeyPressed;
	private boolean glAsyncLoadable;

	private Color backgroundColor = new ColorSerializable(0f, 0f, 0f, 1f);
	private Color gameColorTint = new ColorSerializable(1f, 1f, 1f, 1f);
	private float gameColorBits = gameColorTint.toFloatBits();
	private float longPressTime;

	private boolean terminating;

	private boolean tap;
	private boolean longPress;

	public GameBase(GameOptions options) {
		this.options = options;
	}

	public void create() {
		glContextThread.add(Thread.currentThread());
		fullscreen = options.fullscreen;
		scriptFactory = options.scriptFactory;
		rebuildSpriteBatch();
		camera = new CameraSimpleOrtho2D();
		globalState = new ObjectState();
		serviceProvider = new GameServiceProvider();
		options.width = Gdx.graphics.getWidth();
		options.height = Gdx.graphics.getHeight();
		plane.width = camera.viewportWidth = screen.width = Gdx.graphics
				.getWidth();
		plane.height = camera.viewportHeight = screen.height = Gdx.graphics
				.getHeight();
		// Compute language locale
		FileHandle fh = Gdx.files.internal(options.i18nPath);
		FileHandle i18nPath = fh.child(options.i18nDefault);
		if (!i18nPath.exists() && options.i18nDefault.length() > 2) {
			i18nPath = fh.child(options.i18nDefault.substring(0, 2));
		}
		if (!i18nPath.exists()) {
			GameBase.$error("GameBase.i18nmissing", "The default language "
					+ "file is missing (invalid game.ini)", null);
		}
		i18n = new TextLoader(i18nPath, 5);
		setLanguage(Locale.getDefault());

		// instance != null indicates that GameBase is initialized
		if (!isInitialized())
			instance = this;

		try {
			scriptFactory.evalAllScripts(getSharedEngine(),
					GameBase.$options().initScript, false);
		} catch (Exception e) {
			GameBase.$error("GameBase.create",
					"Error occured while initializing the game", e);
		}

		// restore last display mode and language
		loadUserContext();
		camera.update();
	}

	public synchronized static void $error(String tag, String message,
			Exception e) {
		if ($().terminating)
			return;
		Gdx.app.error(tag, message, e);
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		String msg = "<" + tag + ">: " + message;
		msg += "\n\n" + stackTrace;
		msg += "\n\nEngine version: " + $options().engineVersion;
		DisplayErrorService.forceMessage(msg);
	}

	public static void $info(String tag, String message, Exception ex) {
		if ($().terminating)
			return;
		Gdx.app.log(tag, message, ex);
	}

	public static ObjectState $state() {
		return $().globalState;
	}

	public String getText(String container, String key) throws IOException {
		return i18n.getText(container, key);
	}

	public void setLanguage(Locale locale) {
		FileHandle fh = Gdx.files.internal(options.i18nPath);
		FileHandle i18nPath = fh.child(locale.getISO3Language());
		if (i18nPath.exists()) {
			setLanguageDir(i18nPath);
		} else {
			i18nPath = fh.child(locale.getLanguage());
			if (i18nPath.exists())
				setLanguageDir(i18nPath);
		}
	}

	public void setLanguageDir(FileHandle directory) {
		i18n.setDirectory(directory);
		if (isInitialized())
			saveUserContext();
	}

	public void setLanguageISO(String iso639_1) {
		setLanguage(new Locale(iso639_1));
	}

	public void rebuildSpriteBatch() {
		if (spriteBatch != null)
			spriteBatch.dispose();
		spriteBatch = new SpriteBatch();
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

	/**
	 * Returns a path to store temporary files while the game is running
	 * 
	 * @return Path to store temporary files
	 */
	public static FileHandle $tmpPath() {
		FileHandle fh = Gdx.files.external($().options.savePath).child(
				".tmpStorage");
		if (!fh.exists())
			fh.mkdirs();
		return fh;
	}

	/**
	 * Deletes all temporary files
	 */
	public void clearTmpFiles() {
		Gdx.files.external($().options.savePath).child(".tmpStorage")
				.deleteDirectory();
	}

	public final void render() {
		try {
			if (Gdx.input.isTouched(0)
					&& Gdx.input.isButtonPressed(Buttons.LEFT)) {
				longPressTime += Gdx.graphics.getDeltaTime();
				if (!longPress && longPressTime > .3f) {
					longPress = true;
					Array<GestureDetectorService> services = serviceProvider
							.getServices(GestureDetectorService.class);
					for (int i = 0; i < services.size; i++) {
						services.get(i).reset();
					}
				}
			} else {
				if (longPressTime > 0f && longPressTime <= .3f)
					tap = true;
				longPress = false;
				longPressTime = 0f;
			}

			controlKeyPressedOld = controlKeyPressed;
			controlKeyPressed = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
					|| Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)
					|| Gdx.input.isTouched(1);
			actionKeyPressedOld = actionKeyPressed;
			actionKeyPressed = tap || triggerActionKeyPressed
					|| Gdx.input.isKeyPressed(Input.Keys.SPACE)
					|| Gdx.input.isKeyPressed(Input.Keys.ENTER);

			triggerActionKeyPressed &= !actionKeyPressedOld;
			tap &= !actionKeyPressedOld;

			serviceProvider.computeAll();
			Thread.yield();
			serviceProvider.drawAll(options.debug);
		} catch (Exception e) {
			GameBase.$error("GameBase.render",
					"Error occured while executing the game", e);
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
	 *            The script to evaluate (Either the path to the script or the
	 *            script itself)
	 * @return The result from this evaluation.
	 * @throws ScriptException
	 */
	public Object eval(String script) throws ScriptException {
		ScriptEngine sharedEngine = getSharedEngine();
		Object result = sharedEngine
				.eval(loadWithLogInfo(script, sharedEngine));
		sharedEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
		return result;
	}

	private String loadWithLogInfo(String script, ScriptEngine sharedEngine) {
		String s = getScriptFactory().loadScript(script);
		if (s != script) {
			sharedEngine.put(ScriptEngine.FILENAME, script);
		} else if (sharedEngine.get(ScriptEngine.FILENAME) == null) {
			String excerpt = script.replaceAll("\\s+", " ").trim();
			if (excerpt.length() > 30)
				excerpt = excerpt.substring(0, 30);
			sharedEngine.put(ScriptEngine.FILENAME, excerpt);
		}
		return s;
	}

	/**
	 * Invokes the function which is defined inside the given script, using the
	 * given arguments.<br>
	 * The same engine is used for all evaluations/invocations!
	 * 
	 * @param script
	 *            The script containing the function to invoke (Either the path
	 *            to the script or the script itself)
	 * @param fncName
	 *            The function to invoke
	 * @param args
	 *            Arguments for the function
	 * @return The result which was returned by the invoked function
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	public Object invokeFunction(String script, String fncName, Object... args)
			throws ScriptException, NoSuchMethodException {
		ScriptEngine sharedEngine = getSharedEngine();
		try {
			if (sharedEngine instanceof Invocable) {
				sharedEngine.eval(loadWithLogInfo(script, sharedEngine));
				return ((Invocable) sharedEngine).invokeFunction(fncName, args);
			} else {
				throw new ScriptException("ScriptEngine not Invocable!");
			}
		} finally {
			sharedEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
		}
	}

	/**
	 * Returns a shared script engine, which is used in many different cases.
	 * 
	 * @return A script engine instance
	 */
	public ScriptEngine getSharedEngine() {
		if (sharedEngine == null) {
			sharedEngine = getScriptFactory().obtainEngine();
		}
		return sharedEngine;
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

	public boolean isLongPress() {
		return longPress;
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
		if (!terminating)
			saveUserContext();
	}

	public void restoreDefaultResolution() {
		Gdx.graphics.setDisplayMode(options.width, options.height, fullscreen);
	}

	public void pause() {
		if (Gdx.app.getType() != ApplicationType.Desktop
				&& exitForced
				&& !(getServiceProvider().queryAttention() instanceof MenuService))
			autoSave();
	}

	public void resume() {
		if (lastExitForced())
			autoLoad();
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
		gameColorTint = ColorSerializable.wrap(tint);
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

	public static void toggleCursor() {
		Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
	}

	public void dispose() {
		try {
			terminating = true;
			if (fullscreen)
				toggleFullscreen();
			serviceProvider.dispose();
			if (spriteBatch != null)
				spriteBatch.dispose();
			clearTmpFiles();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Exits the running game
	 */
	public void exit() {
		terminating = true;
		exitForced = false;
		if (fullscreen)
			toggleFullscreen();
		new ExecWithGlContext() {
			@Override
			public void exec() {
				Gdx.app.exit();
			}
		}.runWait();
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = ColorSerializable.wrap(backgroundColor);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setGlobalEvents(HashMap<String, EventObject> globalEvents) {
		this.globalEvents = globalEvents;
	}

	/**
	 * Returns the map which stores the global events
	 * 
	 * @return A map with all global events
	 * @see #getGlobalEventsClone()
	 */
	public Map<String, EventObject> getGlobalEvents() {
		return globalEvents;
	}

	/**
	 * Returns a shallow clone of the global events map, which may be
	 * manipulated without effecting the main map.
	 * 
	 * @return A map with all global events
	 * @see #getGlobalEvents()
	 */
	@SuppressWarnings("unchecked")
	public Map<String, EventObject> getGlobalEventsClone() {
		// the clone method should be faster than instantiating a new map
		if (globalEvents instanceof HashMap<?, ?>) {
			return (Map<String, EventObject>) ((HashMap<String, EventObject>) globalEvents)
					.clone();
		}
		return new HashMap<String, EventObject>(globalEvents);
	}

	/**
	 * Simulates the action key pressed event.
	 */
	public void triggerActionKeyPressed() {
		this.triggerActionKeyPressed = true;
	}

	/**
	 * You have to implement sharing the rendering context yourself.<br>
	 * E.g. In LWJGL you can share the context by the following code:<br>
	 * <code>new SharedDrawable(Display.getDrawable()).makeCurrent();</code><br>
	 * <br>
	 * If sharing the context fails or is not supported, all texture loading and
	 * drawing is done in the main thread.
	 * 
	 * @return false If sharing the context failed or is not supported by the
	 *         backend, true otherwise.
	 */
	protected abstract boolean shareGLContext();

	/**
	 * Register a thread context to allow parallel texture loading if possible.
	 */
	public void registerGlContextThread() {
		if (shareGLContext()) {
			glAsyncLoadable = true;
			synchronized (glContextThread) {
				glContextThread.add(Thread.currentThread());
			}
		}
	}

	public boolean isGlContextThread() {
		return glContextThread.contains(Thread.currentThread());
	}

	public boolean isMainThread() {
		return Thread.currentThread() == glContextThread.get(0);
	}

	public boolean isGlAsyncLoadable() {
		return glAsyncLoadable;
	}

	private String getUserContextPath() {
		return GameBase.$options().savePath + "userContext.sav";
	}

	private FileHandle getServiceStateTmpPath() {
		return $tmpPath().child("restoreGameServiceState.ser.sav");
	}

	public String getScreenThumbnailName() {
		return "screenShot.cim";
	}

	public FileHandle getSaveFile(int i) {
		return Gdx.files.external(GameBase.$options().savePath + "gameState"
				+ i + ".sav");
	}

	/**
	 * Calls {@link #listSaveFiles(int, int, int)} with the default values (2,
	 * 1, 1)
	 * 
	 * @see #listSaveFiles(int, int)
	 * @return An array of files, containing null values for empty save slots
	 */
	public FileHandle[] listSaveFiles() {
		return listSaveFiles(2, 1, 1);
	}

	/**
	 * Reads all files at the specified save directory.<br>
	 * Empty files are indicated with null. The maximum number of files is
	 * 100*cols+2, where the first file is reserved for auto save (e.g. incoming
	 * call on android device). The second file is reserved for a quick save
	 * feature and should be treated specially by the load/save menu (the auto
	 * save file can be entirely ignored or can also be treated as a special
	 * save file by the menu).<br>
	 * In fact, the amount of 100*cols+2 represents a maximum of 100 rows.
	 * 
	 * @param cols
	 *            Amount of columns per row
	 * @param emptyTailRows
	 *            Amount of empty rows appended at the end
	 * @param minRows
	 *            Minimum amount of rows
	 * @return An array of files, containing null values for empty save slots
	 */
	public FileHandle[] listSaveFiles(int cols, int emptyTailRows, int minRows) {
		return listSaveFiles(cols, emptyTailRows, minRows, 100);
	}

	/**
	 * Reads all files at the specified save directory.<br>
	 * Empty files are indicated with null. The maximum number of files is
	 * maxRows*cols+2, where the first file is reserved for auto save (e.g.
	 * incoming call on android device). The second file is reserved for a quick
	 * save feature and should be treated specially by the load/save menu (the
	 * auto save file can be entirely ignored or can also be treated as a
	 * special save file by the menu).<br>
	 * 
	 * @param cols
	 *            Amount of columns per row
	 * @param emptyTailRows
	 *            Amount of empty rows appended at the end
	 * @param minRows
	 *            Minimum amount of rows
	 * @param maxRows
	 *            Maximum amount of rows
	 * @return An array of files, containing null values for empty save slots
	 */
	public FileHandle[] listSaveFiles(int cols, int emptyTailRows, int minRows,
			int maxRows) {
		maxRows = maxRows * cols + 2;
		String[] fileNames = Gdx.files.external(GameBase.$options().savePath)
				.file().list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith("gameState")
								&& name.endsWith(".sav");
					}
				});
		// 1 auto save + 1 quick save + maxRows * cols save files
		FileHandle[] fh = new FileHandle[maxRows];
		int max = 0;
		for (String nm : fileNames) {
			int index = Integer.parseInt(nm.substring(9, nm.length() - 4));
			if (index < fh.length) {
				fh[index] = getSaveFile(index);
				int idxEnd = index + 2 + cols * emptyTailRows;
				if (idxEnd > max) {
					max = Math.min(idxEnd, fh.length);
				}
			}
		}
		if (max < 2 + minRows * cols) {
			max = 2 + minRows * cols;
		} else
			while ((max - 2) % cols != 0) {
				max++;
			}
		if (max < fh.length) {
			FileHandle[] newHandles = new FileHandle[max];
			System.arraycopy(fh, 0, newHandles, 0, max);
			fh = newHandles;
		}
		return fh;
	}

	private void saveUserContext() {
		FileHandle fh = Gdx.files.external(getUserContextPath());
		try {
			ObjectOutputStream oOut = new ObjectOutputStream(fh.write(false));
			oOut.writeObject(screen);
			oOut.writeBoolean(fullscreen);
			oOut.writeObject(i18n.getDirectory().path());
			oOut.close();
		} catch (IOException e) {
			GameBase.$info("GameBase.saveUserContext",
					"Error occured while saving the user context", e);
		}
	}

	private void loadUserContext() {
		FileHandle fh = Gdx.files.external(getUserContextPath());
		if (fh.exists()) {
			try {
				ObjectInputStream oIn = new ObjectInputStream(fh.read());
				Rectangle newScreen = (Rectangle) oIn.readObject();
				fullscreen = oIn.readBoolean();
				String i18nPath = (String) oIn.readObject();
				oIn.close();
				i18n.setDirectory(Gdx.files.internal(i18nPath));
				Gdx.graphics.setDisplayMode((int) newScreen.width,
						(int) newScreen.height, false);
				if (fullscreen) {
					Gdx.graphics.setDisplayMode((int) newScreen.width,
							(int) newScreen.height, true);
				}
			} catch (Exception e) {
				GameBase.$info("GameBase.loadUserContext",
						"Error occured while loading the user context", e);
			}
		}
	}

	/**
	 * Saves the state to the save-file number 1, which is the quick-load/save
	 * file
	 * 
	 * @see #saveFile(int)
	 * @return true if save is successful, false otherwise
	 */
	public boolean quickSave() {
		return saveFile(1);
	}

	/**
	 * Saves the state to the save-file number 0, which is the auto-load/save
	 * file. This will automatically be called on android device, when the game
	 * is paused by an incoming call.
	 * 
	 * @see #saveFile(int)
	 * @return true if save is successful, false otherwise
	 */
	public boolean autoSave() {
		return saveFile(0);
	}

	/**
	 * Loads the state from save-file number 1, which is the quick-load/save
	 * file
	 * 
	 * @see #loadFile(int)
	 * @return true if load is successful, false otherwise
	 */
	public boolean quickLoad() {
		return loadFile(1);
	}

	/**
	 * Loads the state from save-file number 0, which is the auto-load/save
	 * file. This will automatically be called on android device, when the game
	 * is resumed (e.g. after an incoming call).
	 * 
	 * @see #saveFile(int)
	 * @return true if save is successful, false otherwise
	 */
	public boolean autoLoad() {
		return loadFile(0);
	}

	/**
	 * Determines if last exit has been forced by a phone call
	 * 
	 * @return true if exit has been forced
	 */
	protected boolean lastExitForced() {
		FileHandle fh = getSaveFile(0);
		if (fh.exists()) {
			try {
				clearTmpFiles();
				Zipper.unzip(fh, $tmpPath());

				InputStream is = getServiceStateTmpPath().read();
				ObjectInputStream oIn = new ObjectInputStream(is);
				boolean exitForced = oIn.readBoolean();
				oIn.close();
				clearTmpFiles();
				return exitForced;
			} catch (Exception e) {
				GameBase.$info("GameBase.lastExitForced",
						"Error occured while verifying the last exit state", e);
			}
		}
		return false;
	}

	/**
	 * Reset the entire engine. The initialization script(s) will be executed to
	 * set the engine into the same state as starting it from scratch.
	 */
	public void resetEngine() {
		try {
			new ExecInMainThread() {
				@Override
				public void exec() {
					clearTmpFiles();
					globalEvents.clear();
					globalState.clear();
					serviceProvider.clearServices();
					scriptFactory.clearGlobalState();
					DebugHelper.clear();

					scriptFactory.evalAllScripts(sharedEngine, GameBase
							.$options().initScript, false);
				}
			}.runWait();
		} catch (Exception e) {
			GameBase.$error("GameBase.create",
					"Error occured while initializing the game", e);
		}
	}

	/**
	 * Saves the state to the specified save-file
	 * 
	 * @return true if save is successful, false otherwise
	 */
	public boolean saveFile(int fileNumber) {
		try {
			OutputStream os = getServiceStateTmpPath().write(false);
			ObjectOutputStream oOut = new ObjectOutputStream(os);
			oOut.writeBoolean(exitForced);
			oOut.writeObject(globalState);
			oOut.writeObject(globalEvents);
			oOut.writeObject(camera);
			oOut.writeObject(plane);
			oOut.writeObject(screen);
			oOut.writeObject(backgroundColor);
			oOut.writeObject(gameColorTint);
			$serviceProvider().saveSerializableServices(oOut);
			oOut.close();

			Zipper.zip($tmpPath(), getSaveFile(fileNumber));
			return true;
		} catch (IOException e) {
			GameBase.$error("GameBase.saveFile",
					"Error occured while saving the game", e);
		}
		return false;
	}

	/**
	 * Takes a screenshot from the current frame buffer. The screenshot will be
	 * stretched to fit into the Rectangle specified by dstW and dstH
	 * 
	 * @param srcX
	 * @param srcY
	 * @param srcW
	 * @param srcH
	 * @param dstW
	 * @param dstH
	 * @return A Pixmap containing the screenshot. The screenshot will be
	 *         flipped at the y-axis.
	 * @throws IOException
	 */
	public Pixmap takeScreenshot(int srcX, int srcY, int srcW, int srcH,
			int dstW, int dstH) throws IOException {
		Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
		Pixmap pixmap = new Pixmap(srcW, srcH, Format.RGBA8888);
		Gdx.gl.glReadPixels(srcX, srcY, srcW, srcH, GL10.GL_RGBA,
				GL10.GL_UNSIGNED_BYTE, pixmap.getPixels());

		// scale the picture
		if (srcW != dstW || srcH != dstH) {
			Pixmap scale = new Pixmap(dstW, dstH, Format.RGBA8888);
			Blending old = Pixmap.getBlending();
			Pixmap.setBlending(Blending.None);
			scale.drawPixmap(pixmap, 0, 0, srcW, srcH, 0, 0, dstW, dstH);
			Pixmap.setBlending(old);
			pixmap.dispose();
			pixmap = scale;
		}

		return pixmap;
	}

	/**
	 * Takes a screenshot from the current player position and stores it as CIM
	 * file into the temporary save directory.<br>
	 * The following default values will be used:<br>
	 * width=64, height=48, reduction=5, lookAt=player
	 * 
	 * @see #writeScreenshot(int, int, float, Movable, FileHandle)
	 * @throws IOException
	 */
	public void writeScreenshot() throws IOException {
		// first try with common name "cameraTrack"
		CameraTrackMovableService s = serviceProvider.getService(
				CameraTrackMovableService.class, "cameraTrack");
		if (s.getTrackObj() == null)
			s = null;
		if (s == null) {
			Array<CameraTrackMovableService> s2 = serviceProvider
					.getServices(CameraTrackMovableService.class);
			for (int i = 0; s == null && i < s2.size; i++) {
				s = s2.get(i);
				if (s.getTrackObj() == null)
					s = null;
			}
		}
		writeScreenshot(64, 48, 5, s == null ? null : s.getTrackObj(),
				$tmpPath().child(getScreenThumbnailName()));
	}

	/**
	 * Takes a screenshot using the parameters below. If the given
	 * {@link FileHandle} has the extension png, the image will be stored as
	 * PNG-image. Otherwise the internal CIM format will be used.
	 * 
	 * @param width
	 *            The width of the screenshot
	 * @param height
	 *            The height of the screenshot
	 * @param reduction
	 *            Zoom value. The higher the value, the more the image will be
	 *            shrinked.<br>
	 *            1 means no shrinking. &lt; 1 will zoom in. &gt; 1 will zoom
	 *            out.
	 * @param lookAt
	 *            Optional parameter. If null, the center of the screen will be
	 *            the reference point
	 * @param saveTo
	 *            The path to save the image
	 * @throws IOException
	 */
	public void writeScreenshot(int width, int height, float reduction,
			Movable lookAt, FileHandle saveTo) throws IOException {
		Pixmap thumbnail = takeScreenshot(width, height, reduction, lookAt);
		if ("png".equalsIgnoreCase(saveTo.extension())) {
			PixmapIO.writePNG(saveTo, thumbnail);
		} else {
			PixmapIO.writeCIM(saveTo, thumbnail);
		}
		thumbnail.dispose();
	}

	/**
	 * Takes a screenshot using the parameters below.
	 * 
	 * @param width
	 *            The width of the screenshot
	 * @param height
	 *            The height of the screenshot
	 * @param reduction
	 *            Zoom value. The higher the value, the more the image will be
	 *            shrinked.<br>
	 *            1 means no shrinking. &lt; 1 will zoom in. &gt; 1 will zoom
	 *            out.
	 * @param lookAt
	 *            Optional parameter. If null, the center of the screen will be
	 *            the reference point
	 * @return A Pixmap containing the screenshot. The screenshot will be
	 *         flipped at the y-axis.
	 * @throws IOException
	 */
	public Pixmap takeScreenshot(int width, int height, float reduction,
			Movable lookAt) throws IOException {
		// revise distortion from zooming and changing window size
		float ratioX = screen.width / camera.viewportWidth;
		float ratioY = screen.height / camera.viewportHeight;
		float clipW = width * reduction * ratioX;
		float clipH = height * reduction * ratioY;
		// border at the left/right of the screen
		float camX = Math.max(0f, -camera.position.x * ratioX);
		// border at the top/bottom of the screen
		float camY = Math.max(0f, -camera.position.y * ratioY);

		if (clipW == 0 || clipW > Gdx.graphics.getWidth() - camX * 2f) {
			clipW = Gdx.graphics.getWidth() - camX * 2f;
		}
		if (clipH == 0 || clipH > Gdx.graphics.getHeight() - camY * 2f) {
			clipH = Gdx.graphics.getHeight() - camY * 2f;
		}
		int x;
		int y;
		if (lookAt != null) {
			x = (int) (lookAt.getScreenX() - clipW * .5f);
			y = (int) (lookAt.getScreenY() - clipH * .5f);
		} else {
			x = (int) (Gdx.graphics.getWidth() - clipW * .5f);
			y = (int) (Gdx.graphics.getHeight() - clipH * .5f);
		}
		int w = (int) clipW;
		int h = (int) clipH;
		if (x < camX) {
			x = (int) camX;
		} else if (Gdx.graphics.getWidth() - camX < x + w) {
			x = Gdx.graphics.getWidth() - (int) camX - w;
		}
		if (y < camY) {
			y = (int) camY;
		} else if (Gdx.graphics.getHeight() - camY < y + h) {
			y = Gdx.graphics.getHeight() - (int) camY - h;
		}
		Pixmap thumbnail = takeScreenshot(x, y, w, h, width, height);
		return thumbnail;
	}

	/**
	 * Loads the state from the specified save-file
	 * 
	 * @return true if save is successful, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public boolean loadFile(int fileNumber) {
		FileHandle fh = getSaveFile(fileNumber);
		if (fh.exists()) {
			try {
				resetEngine();
				Zipper.unzip(fh, $tmpPath());
			} catch (Exception e) {
				GameBase.$error("GameBase.loadFile",
						"Error occured while loading the game", e);
			}

			new ExecWithGlContext() {
				@Override
				public void exec() throws Exception {
					InputStream is = getServiceStateTmpPath().read();
					ObjectInputStream oIn = new ObjectInputStream(is);
					oIn.readBoolean(); // exitForced
					globalState = (ObjectState) oIn.readObject();
					globalEvents = (Map<String, EventObject>) oIn.readObject();
					camera = (Camera) oIn.readObject();
					plane = (Rectangle) oIn.readObject();
					Rectangle oldScreen = screen;
					screen = (Rectangle) oIn.readObject();
					resize((int) oldScreen.width, (int) oldScreen.height);
					backgroundColor = (Color) oIn.readObject();
					gameColorTint = (Color) oIn.readObject();
					gameColorBits = gameColorTint.toFloatBits();
					$serviceProvider().forceAttentionReset();
					$serviceProvider().loadSerializableServices(oIn);
					oIn.close();

					camera.update();
					serviceProvider.resize((int) screen.width,
							(int) screen.height);
				}
			}.runWait();
			return true;
		}
		return false;
	}
}
