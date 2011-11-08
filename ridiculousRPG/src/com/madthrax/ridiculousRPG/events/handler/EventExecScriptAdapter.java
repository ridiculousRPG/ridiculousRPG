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
package com.madthrax.ridiculousRPG.events.handler;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SortedIntList;
import com.badlogic.gdx.utils.SortedIntList.Node;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.map.TiledMapWithEvents;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * This class executes JavaScript on touch, push, timer, load, store and custom
 * trigger events. Particularly one script function is generated for every event
 * mentioned above.<br>
 * All script function names are equivalent to the corresponding method names
 * and also the parameter names are equivalent to the corresponding methods
 * parameter names (see javadoc or java code). Additionally to this parameters,
 * two more parameters are passed to the script.<br>
 * The first one is named with the Dollar sign $ and holds the actual
 * {@link GameBase} object. The second additional parameter is named eventState
 * and holds the corresponding events {@link ObjectState}.<br>
 * <br>
 * 
 * For example, the script function touch has the following 4 parameters:<br>
 * <ol>
 * <li>eventSelf = This {@link EventObject}</li>
 * <li>eventTrigger = The {@link EventObject} which touched this
 * {@link EventObject}</li>
 * <li>$ = {@link GameBase#$()} - The game instance itself</li>
 * <li>eventState = {@link #getActualState()} - The corresponding events
 * {@link ObjectState}</li>
 * </ol>
 * 
 * Of course the script is able to (and should) modify the eventState, to
 * control the game.
 * 
 * @author Alexander Baumgartner
 */
public class EventExecScriptAdapter extends EventAdapter implements
		Initializable {
	private boolean push, touch, timer, load, store, customTrigger,
			initialized;
	private Invocable engine;
	private SortedIntList<String> scriptCode = new SortedIntList<String>();
	private SortedIntList<String> onPush = new SortedIntList<String>();
	private SortedIntList<String> onTouch = new SortedIntList<String>();
	private SortedIntList<String> onTimer = new SortedIntList<String>();
	private SortedIntList<String> onLoad = new SortedIntList<String>();
	private SortedIntList<String> onStore = new SortedIntList<String>();
	private SortedIntList<String> onCustomTrigger = new SortedIntList<String>();
	private static final ScriptEngineManager ENGINE_FACTORY = new ScriptEngineManager();
	private static boolean globalInit = false;

	@Override
	public boolean push(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		if (!push)
			return false;
		try {
			return (Boolean) engine.invokeFunction("push", eventSelf,
					eventTrigger, GameBase.$(), getActualState());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean touch(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		if (!touch)
			return false;
		try {
			return (Boolean) engine.invokeFunction("touch", eventSelf,
					eventTrigger, GameBase.$(), getActualState());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean timer(EventObject eventSelf, float deltaTime)
			throws ScriptException {
		if (!timer)
			return false;
		try {
			return (Boolean) engine.invokeFunction("timer", eventSelf,
					deltaTime, GameBase.$(), getActualState());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean customTrigger(EventObject eventSelf, int triggerId)
			throws ScriptException {
		if (!customTrigger)
			return false;
		try {
			return (Boolean) engine.invokeFunction("customTrigger", eventSelf,
					triggerId, GameBase.$(), getActualState());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}

	@Override
	public void load(EventObject eventSelf, ObjectState parentState)
			throws ScriptException {
		if (!load) {
			super.load(eventSelf, parentState);
		} else {
			try {
				engine.invokeFunction("load", eventSelf, parentState, GameBase
						.$(), getActualState());
			} catch (NoSuchMethodException e) {
				// this should never happen
				throw new AssertionError(e);
			}
		}
	}

	@Override
	public void store(EventObject eventSelf, ObjectState parentState,
			boolean currentlyExecuted) throws ScriptException {
		if (!store) {
			super.store(eventSelf, parentState, currentlyExecuted);
		} else {
			try {
				engine.invokeFunction("store", eventSelf, parentState,
						currentlyExecuted, GameBase.$(), getActualState());
			} catch (NoSuchMethodException e) {
				// this should never happen
				throw new AssertionError(e);
			}
		}
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnPush(String val, int index) {
		val = readIfPath(val);
		onPush.insert(index, val);
		push = true;
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnTouch(String val, int index) {
		val = readIfPath(val);
		onTouch.insert(index, val);
		touch = true;
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnTimer(String val, int index) {
		val = readIfPath(val);
		onTimer.insert(index, val);
		timer = true;
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnCustomTrigger(String val, int index) {
		val = readIfPath(val);
		onCustomTrigger.insert(index, val);
		customTrigger = true;
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnLoad(String val, int index) {
		val = readIfPath(val);
		onLoad.insert(index, val);
		load = true;
	}

	/**
	 * This method adds some script lines at the specified line index to be
	 * executed when the execution of this event is triggered.<br>
	 * <b>ATTENTION:</b> This method only takes effect before the method
	 * {@link #init()} is called on this object. Usually this method should ONLY
	 * BE USED shortly after the event has been initialized.<br>
	 * After all script lines at all the methods starting with execOn... have
	 * been assigned you must call the method {@link #init()} to compile all the
	 * generated script functions!<br>
	 * <br>
	 * You can find a reference implementation in {@link TiledMapWithEvents}
	 * .parseProperties(...)
	 * 
	 * @param val
	 *            The script line(s) to be executed
	 * @param index
	 *            The line index of this script line(s)
	 */
	public final void execOnStore(String val, int index) {
		val = readIfPath(val);
		onStore.insert(index, val);
		store = true;
	}

	/**
	 * This method allows to add some additional script code to this
	 * {@link EventExecScriptAdapter}. The code has to be well formed in the
	 * specified languages syntax.<br>
	 * You are also allowed to define variables outside of a function body. This
	 * variables and all the defined functions are accessible from all the
	 * script functions inside this {@link EventExecScriptAdapter}.
	 * 
	 * @param scriptCode
	 *            One or more well formed script function(s) and/or variables
	 *            matching the script language {@link #getScriptLanguage()}
	 * @param index
	 *            The index position of this script. All scripts are sorted by
	 *            their index position.
	 */
	public final void addScriptCode(String scriptCode, int index) {
		scriptCode = readIfPath(scriptCode);
		this.scriptCode.insert(index, scriptCode);
	}

	private String readIfPath(String scriptCodeOrPath) {
		try {
			FileHandle fh = Gdx.files.internal(scriptCodeOrPath);
			if (fh.exists()) {
				return fh.readString("UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scriptCodeOrPath;
	}

	public void init() {
		try {
			if (!globalInit)
				initGlobals();
			initEngine();
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		initialized = true;
	}

	private void initGlobals() throws ScriptException {
		globalInit = true;
		// TODO initialize some standard script functions
		// and / or import packages to simplify the scripting process
		ScriptEngine engine = ENGINE_FACTORY
				.getEngineByName(getScriptLanguage());
		engine.eval("", ENGINE_FACTORY.getBindings());
	}

	public boolean isInitialized() {
		return initialized;
	}

	private void initEngine() throws ScriptException {
		StringBuilder script = new StringBuilder();
		for (Node<String> code : scriptCode) {
			script.append(code.value).append('\n');
		}
		this.scriptCode.clear();
		script.append(createFunction(onPush, "push", "eventSelf",
				"eventTrigger", "$", "eventState"));
		script.append(createFunction(onTouch, "touch", "eventSelf",
				"eventTrigger", "$", "eventState"));
		script.append(createFunction(onTimer, "timer", "eventSelf",
				"deltaTime", "$", "eventState"));
		script.append(createFunction(onCustomTrigger, "customTrigger",
				"eventSelf", "triggerId", "$", "eventState"));
		script.append(createFunction(onLoad, "load", "eventSelf",
				"parentState", "$", "eventState"));
		script.append(createFunction(onStore, "store", "eventSelf",
				"parentState", "currentlyExecuted", "$", "eventState"));
		ScriptEngine engine = ENGINE_FACTORY
				.getEngineByName(getScriptLanguage());
		engine.eval(script.toString());
		if (engine instanceof Invocable) {
			this.engine = (Invocable) engine;
		} else {
			throw new ScriptException("Sorry engine is not invokable :(");
		}
	}

	/**
	 * Override this method if you want an other script language.<br>
	 * The script engine has to support invocation by implementing the interface
	 * {@link Invocable}<br>
	 * Maybe you also need to override
	 * {@link #createFunction(SortedIntList, String, String)}
	 * 
	 * @return The script language used by this script interpreter.
	 */
	public String getScriptLanguage() {
		return "JavaScript";
	}

	/**
	 * You are able to override this method if you want to generate a function
	 * for an other script language.<br>
	 * Don't forget to override {@link #getScriptLanguage()} too.
	 * 
	 * @param codeLines
	 *            The lines of script code sorted by the line number.
	 * @param fncName
	 *            The name of the script function.
	 * @param fncParam
	 *            All specified parameters.
	 * @return A {@link String} with the generated script function
	 */
	protected String createFunction(SortedIntList<String> codeLines,
			String fncName, String... fncParam) {
		StringBuilder script = new StringBuilder();
		script.append("\nfunction ").append(fncName).append('(');
		for (int i = 0, len = fncParam.length; i < len; i++) {
			if (i > 0)
				script.append(',');
			script.append(fncParam[i]);
		}
		script.append(") {");
		for (Node<String> line : codeLines) {
			script.append('\n').append(line.value).append(';');
		}
		// let gc do it's work
		codeLines.clear();
		script.append("\nreturn true; }");
		return script.toString();
	}

	/**
	 * Merges all the script code added with one of the methods named execOn...
	 * from the specified event handler into this event handler
	 * 
	 * @param other
	 *            The {@link EventExecScriptAdapter} to copy from
	 */
	public void merge(EventExecScriptAdapter other) {
		this.push = other.push;
		this.touch = other.touch;
		this.timer = other.timer;
		this.load = other.load;
		this.store = other.store;
		this.customTrigger = other.customTrigger;
		this.scriptCode = other.scriptCode;
		this.onPush = other.onPush;
		this.onTouch = other.onTouch;
		this.onTimer = other.onTimer;
		this.onLoad = other.onLoad;
		this.onStore = other.onStore;
		this.onCustomTrigger = other.onCustomTrigger;
	}
}
