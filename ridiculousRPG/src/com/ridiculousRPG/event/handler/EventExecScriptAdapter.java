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
package com.ridiculousRPG.event.handler;

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
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.map.tiled.TiledMapWithEvents;
import com.ridiculousRPG.util.ObjectState;

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

	private boolean push, touch, timer, stateChange, load, customTrigger;
	private static transient String CUSTOMTRIGGER_TEMPLATE;
	private static transient String LOAD_TEMPLATE;
	private static transient String PUSH_TEMPLATE;
	private static transient String TIMER_TEMPLATE;
	private static transient String STATECHANGE_TEMPLATE;
	private static transient String TOUCH_TEMPLATE;
	private transient Invocable localEngine;
	private SortedMap<Integer, String> onPush = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTouch = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onTimer = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onStateChange = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onLoad = new TreeMap<Integer, String>();
	private SortedMap<Integer, String> onCustomTrigger = new TreeMap<Integer, String>();

	/**
	 * Creates an script executing event handler for the specified object
	 * 
	 * @param belongingObject
	 */
	public EventExecScriptAdapter(Object belongingObject) {
		super(belongingObject);
	}

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
			STATECHANGE_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventStateChangeTemplate).readString(
					GameBase.$options().encoding);
			TOUCH_TEMPLATE = Gdx.files.internal(
					GameBase.$options().eventTouchTemplate).readString(
					GameBase.$options().encoding);
		}
		if (timer || stateChange) {
			try {
				String invocableFunctions = "";
				if (timer) {
					invocableFunctions += GameBase.$scriptFactory()
							.prepareScriptFunction(onTimer, TIMER_TEMPLATE);
				}
				if (stateChange) {
					invocableFunctions += GameBase.$scriptFactory()
							.prepareScriptFunction(onStateChange,
									STATECHANGE_TEMPLATE);
				}
				localEngine = GameBase.$scriptFactory().obtainInvocable(
						invocableFunctions,
						"Could not execute continuous event!");
			} catch (ScriptException e) {
				GameBase.$error("EventObject.initEvents",
						"Could not initialize/compile continuous events", e);
			}
		}
	}

	@Override
	public boolean onPush(EventObject eventTrigger) {
		if (!push)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onPush, PUSH_TEMPLATE);
			GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
					"onPush-Event");
			return (Boolean) GameBase.$().invokeFunction(script, "onPush",
					getBelongingObject(), eventTrigger, getActualState());
		} catch (Exception e) {
			logError("push", e);
			return false;
		}
	}

	@Override
	public boolean onTouch(EventObject eventTrigger) {
		if (!touch)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onTouch, TOUCH_TEMPLATE);
			GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
					"onTouch-Event");
			return (Boolean) GameBase.$().invokeFunction(script, "onTouch",
					getBelongingObject(), eventTrigger, getActualState());
		} catch (Exception e) {
			logError("touch", e);
			return false;
		}
	}

	@Override
	public boolean onTimer(float deltaTime) {
		if (!timer)
			return false;
		try {
			return (Boolean) localEngine.invokeFunction("onTimer",
					getBelongingObject(), deltaTime, getActualState());
		} catch (Exception e) {
			logError("timer", e);
			return false;
		}
	}

	@Override
	public void onStateChange(ObjectState globalState) {
		if (!stateChange)
			return;
		try {
			localEngine.invokeFunction("onStateChange", getBelongingObject(),
					getActualState(), globalState);
		} catch (Exception e) {
			logError("stateChange", e);
		}
	}

	@Override
	public boolean onCustomTrigger(int triggerId) {
		if (!customTrigger)
			return false;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onCustomTrigger, CUSTOMTRIGGER_TEMPLATE);
			GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
					"onCustomTrigger" + triggerId + "-Event");
			return (Boolean) GameBase.$().invokeFunction(script,
					"onCustomTrigger", getBelongingObject(), triggerId,
					getActualState());
		} catch (Exception e) {
			logError("customTrigger", e);
			return false;
		}
	}

	@Override
	public void onLoad() {
		if (!load)
			return;
		try {
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					onLoad, LOAD_TEMPLATE);
			GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
					"onLoad-Event");
			GameBase.$().invokeFunction(script, "onLoad", getBelongingObject(),
					getActualState());
		} catch (Exception e) {
			logError("load", e);
		}
	}

	private void logError(String eventType, Exception e) {
		GameBase.$error("EventObject.on" + eventType, "Could not execute "
				+ eventType + " script for " + getBelongingObject(), e);
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

	public void execOnStateChange(String val, int index) {
		val = GameBase.$scriptFactory().loadScript(val);
		if (val.trim().length() > 0) {
			onStateChange.put(index, val);
			stateChange = true;
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
		if (localEngine != null) {
			((ScriptEngine) localEngine)
					.getBindings(ScriptContext.ENGINE_SCOPE).clear();
			localEngine = null;
		}
		onPush = null;
		onTouch = null;
		onTimer = null;
		onLoad = null;
		onCustomTrigger = null;
	}
}
