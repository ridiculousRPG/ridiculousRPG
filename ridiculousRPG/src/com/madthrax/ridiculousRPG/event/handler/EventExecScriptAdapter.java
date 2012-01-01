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
package com.madthrax.ridiculousRPG.event.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.script.Invocable;
import javax.script.ScriptException;

import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.ScriptFactory;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.map.tiled.TiledMapWithEvents;

/**
 * This class executes JavaScript on touch, push, timer, load, store and custom
 * trigger events. Particularly one script function is generated for every event
 * mentioned above.<br>
 * All script function names are equivalent to the corresponding method names
 * and also the parameter names are equivalent to the corresponding methods
 * parameter names (see javadoc or java code). Additionally to this parameters,
 * one more parameter is passed to the script.<br>
 * This additional parameter is named eventState and holds the corresponding
 * events {@link ObjectState}.<br>
 * <br>
 * 
 * For example, the script function touch has the following 3 parameters:<br>
 * <ol>
 * <li>eventSelf = This {@link EventObject}</li>
 * <li>eventTrigger = The {@link EventObject} which touched this
 * {@link EventObject}</li>
 * <li>eventState = {@link #getActualState()} - The corresponding events
 * {@link ObjectState}</li>
 * </ol>
 * 
 * Of course the script is able to (and should) modify the eventState, to
 * control the game.
 * 
 * @author Alexander Baumgartner
 */
public class EventExecScriptAdapter extends EventAdapter {
	private static final long serialVersionUID = 1L;

	private boolean push, touch, timer, load, store, customTrigger;
	private transient Invocable engine;
	private SortedMap<Integer, String> scriptCode = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onPush = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTouch = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTimer = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onLoad = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onCustomTrigger = new TreeMap<Integer, String>();

	@Override
	public void init() {
		try {
			initEngine();
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean push(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		if (!push)
			return false;
		try {
			return (Boolean) engine.invokeFunction("push", eventSelf,
					eventTrigger, getActualState());
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
					eventTrigger, getActualState());
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
					deltaTime, getActualState());
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
					triggerId, getActualState());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}

	@Override
	public void load(EventObject eventSelf) throws ScriptException {
		if (!load) {
			super.load(eventSelf);
		} else {
			try {
				engine.invokeFunction("load", eventSelf, getActualState());
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
		val = GameBase.$scriptFactory().loadScript(val);
		onPush.put(index, val);
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
		val = GameBase.$scriptFactory().loadScript(val);
		onTouch.put(index, val);
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
		val = GameBase.$scriptFactory().loadScript(val);
		onTimer.put(index, val);
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
		val = GameBase.$scriptFactory().loadScript(val);
		onCustomTrigger.put(index, val);
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
		val = GameBase.$scriptFactory().loadScript(val);
		onLoad.put(index, val);
		load = true;
	}

	/**
	 * This method allows to add some additional script code to this
	 * {@link EventExecScriptAdapter}. The code has to be well formed in the
	 * specified languages syntax.<br>
	 * You are also allowed to define variables outside of a function body. This
	 * variables and all the defined functions are accessible from all the
	 * script functions inside this {@link EventExecScriptAdapter}.<br>
	 * <br>
	 * The script code will be evaluated at the end of the initialization.<br>
	 * Therefore you can also add some initialization code by this method.
	 * 
	 * @param scriptCode
	 *            One or more well formed script function(s) and/or variables
	 *            matching the script language {@link #getScriptLanguage()}
	 * @param index
	 *            The index position of this script. All scripts are sorted by
	 *            their index position.
	 */
	public final void addScriptCode(String scriptCode, int index) {
		scriptCode = GameBase.$scriptFactory().loadScript(scriptCode);
		this.scriptCode.put(index, scriptCode);
	}

	private void initEngine() throws ScriptException {
		ScriptFactory factory = GameBase.$scriptFactory();
		StringBuilder script = new StringBuilder();
		for (String code : scriptCode.values()) {
			script.append(code).append('\n');
		}
		// WE NEED THIS FOR SERIALIZATION
		// this.scriptCode.clear();

		script.append(factory.createScriptFunction(onPush, "push", true,
				"eventSelf", "eventTrigger", "eventState"));
		script.append(factory.createScriptFunction(onTouch, "touch", true,
				"eventSelf", "eventTrigger", "eventState"));
		script.append(factory.createScriptFunction(onTimer, "timer", false,
				"eventSelf", "deltaTime", "eventState"));
		script.append(factory.createScriptFunction(onCustomTrigger,
				"customTrigger", true, "eventSelf", "triggerId", "eventState"));
		script.append(factory.createScriptFunction(onLoad, "load", false,
				"eventSelf", "eventState"));
		// WE NEED THIS FOR SERIALIZATION
		// let gc do it's work
		// onPush.clear();
		// onTouch.clear();
		// onTimer.clear();
		// onCustomTrigger.clear();
		// onLoad.clear();
		// onStore.clear();

		this.engine = factory.obtainInvocable(script);
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
		this.onCustomTrigger = other.onCustomTrigger;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		try {
			initEngine();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
}
