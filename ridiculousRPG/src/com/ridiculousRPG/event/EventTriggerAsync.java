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

package com.ridiculousRPG.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;

import com.badlogic.gdx.Gdx;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.util.ObjectState;

/**
 * All {@link EventHandler} are called and the specified actions are performed.<br>
 * The execution of {@link EventHandler} is done in a separate thread because
 * {@link EventHandler} are allowed to block (e.g. until the user made a
 * decision).
 * 
 * @author Alexander Baumgartner
 */
public class EventTriggerAsync extends Thread implements EventTrigger {
	private List<String> execScriptsDesc = new ArrayList<String>();
	private List<Object[]> execScriptsParm = new ArrayList<Object[]>();
	private List<String> execScriptsFnc = new ArrayList<String>();
	private List<String> execScripts = new ArrayList<String>();
	private final Runnable execScriptNotifier = new Runnable() {
		@Override
		public void run() {
			synchronized (execScriptNotifier) {
				execScriptNotifier.notify();
			}
		}
	};
	private Thread execScriptThread = new Thread("ExecScript-EventTrigger") {
		@Override
		public void run() {
			GameBase.$().registerGlContextThread();
			while (true) {
				synchronized (execScriptNotifier) {
					if (disposed)
						return;
					try {
						if (execScripts.size() == 0)
							execScriptNotifier.wait();
					} catch (InterruptedException e) {
						GameBase.$info("EventTriggerAsync.interrupt",
								"The EventTrigger thread has been "
										+ "interrupted - continuing", e);
					}
				}
				if (disposed)
					return;
				execScriptQueue();
			}
		}
	};
	private List<EventObject> events;
	private List<PolygonObject> polys;
	private boolean disposed = false;
	private float deltaTime = 0f;
	private boolean actionKeyDown = false;
	// Count to determine if the state has changed
	private int lastGlobalChangeCount = -1;

	public EventTriggerAsync() {
		super("ExecHandler-EventTrigger");
		setDaemon(true);
		start();
		execScriptThread.setDaemon(true);
		execScriptThread.start();
	}

	private void execScriptQueue() {
		int len = execScripts.size();
		for (int i = 0; i < len; i++) {
			String exe = execScripts.get(i);
			String exeFnc = execScriptsFnc.get(i);
			String exeDesc = execScriptsDesc.get(i);
			Object[] exeParm = execScriptsParm.get(i);
			try {
				GameBase.$().getSharedEngine().put(ScriptEngine.FILENAME,
						(exeFnc == null ? "" : exeFnc + "-") + exeDesc);
				if (exeFnc != null) {
					if (exeParm == null)
						GameBase.$().invokeFunction(exe, exeFnc);
					else
						GameBase.$().invokeFunction(exe, exeFnc, exeParm);
				} else {
					GameBase.$().eval(exe);
				}
			} catch (Exception e) {
				GameBase.$error("EventTrigger." + exeFnc,
						"Could not execute script \"" + exeDesc + "\" (PARAM="
								+ Arrays.toString(exeParm)
								+ ")\nSCRIPT-CODE:\n" + exe, e);
			}
		}
		synchronized (execScripts) {
			if (len == execScripts.size()) {
				// perform fast clear if possible
				execScripts.clear();
				execScriptsFnc.clear();
				execScriptsDesc.clear();
				execScriptsParm.clear();
			} else {
				execScripts.subList(0, len).clear();
				execScriptsFnc.subList(0, len).clear();
				execScriptsDesc.subList(0, len).clear();
				execScriptsParm.subList(0, len).clear();
			}
		}
	}

	@Override
	public void run() {
		GameBase.$().registerGlContextThread();
		while (true) {
			synchronized (this) {
				if (disposed)
					return;
				try {
					wait();
				} catch (InterruptedException e) {
					GameBase.$info("EventTriggerAsync.interrupt",
							"The EventTrigger thread has been "
									+ "interrupted - continuing", e);
					continue;
				}
			}
			if (disposed)
				return;
			float deltaTime = this.deltaTime;
			this.deltaTime = 0f;

			callEventHandler(deltaTime, events, polys, actionKeyDown);
		}
	}

	// Call all event handler
	private void callEventHandler(float deltaTime, List<EventObject> events,
			List<PolygonObject> polys, boolean actionKeyDown) {
		EventObject obj1;
		EventHandler handler2;
		int dynSize = events.size();
		boolean globalChange = false;
		ObjectState globalState = GameBase.$state();
		if (lastGlobalChangeCount != globalState.getChangeCount()) {
			lastGlobalChangeCount = globalState.getChangeCount();
			globalChange = true;
		}
		for (int i = 0; i < dynSize && !disposed; i++) {
			obj1 = events.get(i);
			if (obj1.eventHandler != null) {
				if (obj1.eventHandler.onTimer(deltaTime))
					return;
				if (globalChange)
					obj1.eventHandler.onStateChange(globalState);
			}
			if (obj1.consumesEvent) {
				for (int j = 0; j < obj1.collision.size && !disposed; j++) {
					handler2 = obj1.collision.get(j);
					if (!obj1.justTouching.contains(handler2, true)) {
						if (handler2.onTouch(obj1)) {
							obj1.justTouching.add(handler2);
							return;
						}
					}
				}
				if (actionKeyDown) {
					for (int j = 0; j < obj1.reachable.size && !disposed; j++) {
						handler2 = obj1.reachable.get(j);
						if (handler2.onPush(obj1))
							return;
					}
				}
			}
		}
		int polySize = polys.size();
		for (int i = 0; i < polySize && !disposed; i++) {
			PolygonObject p = polys.get(i);
			if (p.eventHandler != null) {
				if (p.eventHandler.onTimer(deltaTime))
					return;
				if (globalChange)
					p.eventHandler.onStateChange(globalState);
			}
		}
	}

	/**
	 * Compute collisions and move the events.<br>
	 * Invoke parallel execution of the {@link EventHandler}.
	 */
	@Override
	public void compute(float deltaTime, boolean actionKeyDown,
			List<EventObject> events, List<PolygonObject> polys) {
		int evSize = events.size();
		int polySize = polys.size();

		// compute all moves
		for (int i = 0; i < evSize; i++)
			events.get(i).compute(deltaTime, this);

		// collision detection
		for (int i = 0; i < evSize; i++) {
			EventObject obj1 = events.get(i);
			EventHandler obj1Ev = obj1.eventHandler;
			if (obj1.moves) {
				for (int j = 0; j < evSize; j++) {
					EventObject obj2 = events.get(j);
					if (j <= i && obj2.moves)
						continue;
					EventHandler obj2Ev = obj2.eventHandler;
					if (obj1.intersects(obj2)) {
						if (obj2Ev != null && obj1.consumesEvent) {
							if (obj2.touchable)
								obj1.collision.add(obj2Ev);
							if (obj2.pushable
									&& !obj1.reachable.contains(obj2Ev, true))
								obj1.reachable.add(obj2Ev);
						}
						if (obj1Ev != null && obj2.consumesEvent) {
							if (obj1.touchable)
								obj2.collision.add(obj1Ev);
							if (obj1.pushable
									&& !obj2.reachable.contains(obj1Ev, true))
								obj2.reachable.add(obj1Ev);
						}
						if (obj1.blockingBehavior.blocks(obj2.blockingBehavior)) {
							// Player events never block mutually
							if (!(obj1.isPlayerEvent() && obj2.isPlayerEvent())) {
								// Another "obj2" could have blocked "obj1"
								// already
								if (obj1.moves) {
									obj1.moves = false;
									if (obj2.moves && obj1.intersects(obj2)) {
										obj1.moves = true;
										obj2.moves = false;
										obj2.getMoveHandler().moveBlocked(obj2);
										if (obj1.intersects(obj2)) {
											obj1.moves = false;
											obj1.getMoveHandler().moveBlocked(
													obj1);
										}
									} else {
										obj1.getMoveHandler().moveBlocked(obj1);
									}
								} else {
									obj2.moves = false;
									obj2.getMoveHandler().moveBlocked(obj2);
								}
							}
						}
					} else {
						if (obj2Ev != null && obj1.consumesEvent) {
							if (obj2.touchable && obj1.justTouching.size > 0)
								obj1.justTouching.removeValue(obj2Ev, true);
							if (obj2.pushable && obj1.reachable.size > 0
									&& !obj1.reaches(obj2))
								obj1.reachable.removeValue(obj2Ev, true);
						}
						if (obj1Ev != null && obj2.consumesEvent) {
							if (obj1.touchable && obj2.justTouching.size > 0)
								obj2.justTouching.removeValue(obj1Ev, true);
							if (obj1.pushable && obj2.reachable.size > 0
									&& !obj2.reaches(obj1))
								obj2.reachable.removeValue(obj1Ev, true);
						}
					}
				}
				// move state has possibly been changed
				if (obj1.moves) {
					// collision detection for polygons
					for (int j = 0; j < polySize; j++) {
						PolygonObject p = polys.get(j);
						if (p.touchable && obj1.consumesEvent) {
							if (obj1.intersects(p)) {
								obj1.collision.add(p.eventHandler);
								if (p.blockingBehavior
										.blocks(obj1.blockingBehavior)) {
									obj1.moves = false;
									obj1.getMoveHandler().moveBlocked(obj1);
								}
							} else {
								obj1.justTouching.removeValue(p.eventHandler,
										true);
							}
						} else if (p.blockingBehavior
								.blocks(obj1.blockingBehavior)
								&& obj1.intersects(p)) {
							obj1.moves = false;
							obj1.getMoveHandler().moveBlocked(obj1);
						}
					}
					obj1.commitMove();
				}
			}
		}
		synchronized (this) {
			// shared variables for parallel computation
			this.events = events;
			this.polys = polys;
			this.actionKeyDown = actionKeyDown;
			this.deltaTime += deltaTime;
			notify();
		}
	}

	public synchronized void dispose() {
		disposed = true;
		notifyAll();
		synchronized (execScriptNotifier) {
			execScriptNotifier.notifyAll();
		}
	}

	/**
	 * If invokeFnc is null the script will not be invoked but evaluated.
	 * 
	 * @param description
	 *            Short description of the trigger/event
	 * @param script
	 *            The script to execute
	 * @param invokeFnc
	 *            Function to invoke - may be null
	 * @param invokeParm
	 *            Parameters for invoked function
	 */
	@Override
	public void postScriptToExec(String description, String script,
			String invokeFnc, Object... invokeParm) {
		synchronized (execScripts) {
			execScriptsDesc.add(description);
			execScriptsFnc.add(invokeFnc);
			execScriptsParm.add(invokeParm);
			execScripts.add(script);
		}
		Gdx.app.postRunnable(execScriptNotifier);
	}

	/**
	 * Note: The answer is not 100% time-accurate! It may be delayed.
	 */
	@Override
	public boolean isScriptQueueEmpty() {
		return execScripts.size() == 0;
	}
}
