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
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.map.tiled.TiledMapWithEvents;

/**
 * This class executes JavaScript on touch, push, timer, load, store and custom
 * trigger events. Particularly one script function is generated for every event
 * mentioned above.<br>
 * All script function names are equivalent to the corresponding method names
 * and also the parameter names are equivalent to the corresponding methods
 * parameter names (see javadoc or java code). Additionally to this parameters,
 * two more parameters are passed to the script.<br>
 * The first additional parameter is named eventState and holds the
 * corresponding events {@link ObjectState}.<br>
 * The second additional parameter is named globalState and holds the global
 * shared {@link ObjectState}.<br>
 * <br>
 * 
 * For example, the script function touch has the following 4 parameters:<br>
 * <ol>
 * <li>eventSelf = This {@link EventObject}</li>
 * <li>eventTrigger = The {@link EventObject} which touched this
 * {@link EventObject}</li>
 * <li>eventState = {@link #getActualState()} - The corresponding events
 * {@link ObjectState}</li>
 * <li>globalState = {@link GameBase#getGlobalState()} - The global shared
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

	private boolean push, touch, timer, load, customTrigger;
	private static transient String CUSTOMTRIGGER_TEMPLATE;
	private static transient String LOAD_TEMPLATE;
	private static transient String PUSH_TEMPLATE;
	private static transient String TIMER_TEMPLATE;
	private static transient String TOUCH_TEMPLATE;
	private transient Invocable timerEngine;
	private SortedMap<Integer, String> onPush = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTouch = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTimer = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onLoad = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onCustomTrigger = new TreeMap<Integer, String>();

	@Override
	public void init() {
		if (CUSTOMTRIGGER_TEMPLATE == null) {
			CUSTOMTRIGGER_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventCustomTriggerTemplate).readString(
					GameBase.$options().encoding);
			LOAD_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventLoadTemplate).readString(
					GameBase.$options().encoding);
			PUSH_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventPushTemplate).readString(
					GameBase.$options().encoding);
			TIMER_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventTimerTemplate).readString(
					GameBase.$options().encoding);
			TOUCH_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventTouchTemplate).readString(
					GameBase.$options().encoding);
		}
		if (timer) {
			try {
				timerEngine = GameBase.$scriptFactory().obtainInvocable(
						GameBase.$scriptFactory().prepareScriptFunction(
								onTimer, TIMER_TEMPLATE));
			} catch (ScriptException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean onPush(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		if (!push)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onPush, PUSH_TEMPLATE);
			return (Boolean) GameBase.$().invokeFunction(script, "onPush",
					eventSelf, eventTrigger, getActualState());
		} catch (NoSuchMethodException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public boolean onTouch(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		if (!touch)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onTouch, TOUCH_TEMPLATE);
			return (Boolean) GameBase.$().invokeFunction(script, "onTouch",
					eventSelf, eventTrigger, getActualState());
		} catch (NoSuchMethodException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public boolean onTimer(EventObject eventSelf, float deltaTime)
			throws ScriptException {
		if (!timer)
			return false;
		try {
			return (Boolean) timerEngine.invokeFunction("onTimer", eventSelf,
					deltaTime, getActualState());
		} catch (NoSuchMethodException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public boolean onCustomTrigger(EventObject eventSelf, int triggerId)
			throws ScriptException {
		if (!customTrigger)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onCustomTrigger, CUSTOMTRIGGER_TEMPLATE);
			return (Boolean) GameBase.$().invokeFunction(script,
					"onCustomTrigger", eventSelf, triggerId, getActualState());
		} catch (NoSuchMethodException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public void onLoad(EventObject eventSelf) throws ScriptException {
		if (!load)
			return;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onLoad, LOAD_TEMPLATE);
			GameBase.$().invokeFunction(script, "onLoad", eventSelf,
					getActualState());
		} catch (NoSuchMethodException e) {
			throw new ScriptException(e);
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
		if (val.trim().length() > 0) {
			onPush.put(index, val);
			push = true;
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
	public final void execOnTouch(String val, int index) {
		val = GameBase.$scriptFactory().loadScript(val);
		if (val.trim().length() > 0) {
			onTouch.put(index, val);
			touch = true;
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
	public final void execOnTimer(String val, int index) {
		val = GameBase.$scriptFactory().loadScript(val);
		if (val.trim().length() > 0) {
			onTimer.put(index, val);
			timer = true;
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
	public final void execOnCustomTrigger(String val, int index) {
		val = GameBase.$scriptFactory().loadScript(val);
		if (val.trim().length() > 0) {
			onCustomTrigger.put(index, val);
			customTrigger = true;
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
	public final void execOnLoad(String val, int index) {
		val = GameBase.$scriptFactory().loadScript(val);
		if (val.trim().length() > 0) {
			onLoad.put(index, val);
			load = true;
		}
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
		this.customTrigger = other.customTrigger;
		this.onPush = other.onPush;
		this.onTouch = other.onTouch;
		this.onTimer = other.onTimer;
		this.onLoad = other.onLoad;
		this.onCustomTrigger = other.onCustomTrigger;
		init();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		init();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (timerEngine != null) {
			((ScriptEngine) timerEngine)
					.getBindings(ScriptContext.ENGINE_SCOPE).clear();
			timerEngine = null;
		}
		onPush = null;
		onTouch = null;
		onTimer = null;
		onLoad = null;
		onCustomTrigger = null;
	}
}
