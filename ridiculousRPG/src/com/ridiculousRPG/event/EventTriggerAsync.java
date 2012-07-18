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

import java.util.List;

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
	private List<EventObject> events;
	private List<PolygonObject> polys;
	private boolean disposed = false;
	private float deltaTime;
	private boolean actionKeyDown = false;
	// Count to determine if the state has changed
	private int lastGlobalChangeCount = -1;

	public EventTriggerAsync() {
		start();
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
					GameBase
							.$info(
									"EventTriggerAsync.interrupt",
									"The EventTrigger thread has been interrupted - continuing",
									e);
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
			if (obj1.consumeInput) {
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

	// TODO: NEEDS REFACTORING
	/**
	 * Compute collisions and move the events.<br>
	 * Invoke parallel execution of the {@link EventHandler}.
	 */
	@Override
	public void compute(float deltaTime, boolean actionKeyDown,
			List<EventObject> events, List<PolygonObject> polys) {
		// Load frequently used pointers/variables into register
		int dynSize = events.size();
		int polySize = polys.size();
		int i, j;
		EventObject obj1, obj2;
		EventHandler obj1Ev, obj2Ev;
		boolean checkReachability;

		// compute all moves
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			obj1.collision.clear();
			obj1.getMoveHandler().tryMove(obj1, deltaTime);
			obj1.computeParticleEffect(deltaTime);
		}
		// move if no collision
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			obj1Ev = obj1.eventHandler;
			// increase performance by only computing this once per event
			checkReachability = obj1.pushable || obj1.reachable.size > 0;
			for (j = i + 1; j < dynSize; j++) {
				obj2 = events.get(j);
				obj2Ev = obj2.eventHandler;
				if (obj1.intersects(obj2)) {
					if (obj2Ev != null) {
						if (obj2.touchable)
							obj1.collision.add(obj2Ev);
						if (obj2.pushable
								&& !obj1.reachable.contains(obj2Ev, true))
							obj1.reachable.add(obj2Ev);
					}
					if (obj1Ev != null) {
						if (obj1.touchable)
							obj2.collision.add(obj1Ev);
						if (obj1.pushable
								&& !obj2.reachable.contains(obj1Ev, true))
							obj2.reachable.add(obj1Ev);
					}
					if (obj1.blockingBehavior.blocks(obj2.blockingBehavior)) {
						if (!(obj1.isPlayerEvent() && obj2.isPlayerEvent())) {
							if (obj1.moves) {
								obj1.moves = false;
								if (obj2.moves && obj1.intersects(obj2)) {
									obj1.moves = true;
									obj2.moves = false;
									obj2.getMoveHandler().moveBlocked(obj2);
									if (obj1.intersects(obj2)) {
										obj1.moves = false;
										obj1.getMoveHandler().moveBlocked(obj1);
									}
								} else {
									obj1.getMoveHandler().moveBlocked(obj1);
								}
							} else if (obj2.moves) {
								obj2.moves = false;
								obj2.getMoveHandler().moveBlocked(obj2);
							}
						}
					}
				} else {
					if (obj2Ev != null) {
						obj1.justTouching.removeValue(obj2Ev, true);
						boolean removeReach = checkReachability
								&& (obj2.pushable || obj2.reachable.size > 0)
								&& !obj1.reaches(obj2);
						if (removeReach)
							obj1.reachable.removeValue(obj2Ev, true);
						if (obj1Ev != null) {
							obj2.justTouching.removeValue(obj1Ev, true);
							if (removeReach)
								obj2.reachable.removeValue(obj1Ev, true);
						}
					} else if (obj1Ev != null) {
						obj2.justTouching.removeValue(obj1Ev, true);
						if (checkReachability
								&& (obj2.pushable || obj2.reachable.size > 0)
								&& !obj1.reaches(obj2))
							obj2.reachable.removeValue(obj1Ev, true);
					}
				}
			}
			if (obj1.moves) {
				// collision detection for polygons
				for (i = 0; i < polySize; i++) {
					PolygonObject p = polys.get(i);
					if (p.touchable) {
						if (obj1.intersects(p)) {
							obj1.collision.add(p.eventHandler);
							if (p.blockingBehavior
									.blocks(obj1.blockingBehavior)) {
								obj1.moves = false;
								obj1.getMoveHandler().moveBlocked(obj1);
							}
						} else {
							obj1.justTouching.removeValue(p.eventHandler, true);
						}
					} else if (p.blockingBehavior.blocks(obj1.blockingBehavior)
							&& obj1.intersects(p)) {
						obj1.moves = false;
						obj1.getMoveHandler().moveBlocked(obj1);
					}
				}
				obj1.commitMove();
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
	}
}
