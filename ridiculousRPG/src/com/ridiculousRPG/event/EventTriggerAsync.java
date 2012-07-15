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

			callEventHandler(deltaTime, events, actionKeyDown);
		}
	}

	// Call all event handler
	private void callEventHandler(float deltaTime,
			List<EventObject> dynamicRegions, boolean actionKeyDown) {
		EventObject obj1;
		EventObject obj2;
		int dynSize = dynamicRegions.size();
		boolean globalChange = false;
		ObjectState globalState = GameBase.$state();
		if (lastGlobalChangeCount != globalState.getChangeCount()) {
			lastGlobalChangeCount = globalState.getChangeCount();
			globalChange = true;
		}
		boolean consumed = false;
		for (int i = 0; i < dynSize && !consumed && !disposed; i++) {
			obj1 = dynamicRegions.get(i);
			consumed = obj1.getEventHandler() != null
					&& obj1.getEventHandler().onTimer(obj1, deltaTime);

			if (!consumed && globalChange && obj1.reactOnGlobalChange) {
				obj1.getEventHandler().onStateChange(obj1, globalState);
			}
			if (!consumed && obj1.consumeInput) {
				int tmpSize = obj1.collision.size;
				for (int j = 0; j < tmpSize && !consumed && !disposed; j++) {
					obj2 = obj1.collision.get(j);
					if (obj2.touchable
							&& !obj1.justTouching.contains(obj2, true)) {
						consumed = obj2.getEventHandler().onTouch(obj2, obj1);
						if (consumed) {
							obj1.justTouching.add(obj2);
						}
					}
				}
				if (!consumed && actionKeyDown) {
					tmpSize = obj1.reachable.size;
					for (int j = 0; j < tmpSize && !consumed && !disposed; j++) {
						obj2 = obj1.reachable.get(j);
						consumed = obj2.pushable
								&& obj2.getEventHandler().onPush(obj2, obj1);
					}
				}
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
			List<EventObject> events) {
		// Load frequently used pointers/variables into register
		int dynSize = events.size();
		int i, j;
		EventObject obj1, obj2;
		boolean checkReachability;

		// compute all moves
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			obj1.collision.clear();
			if (!obj1.checkSleep(deltaTime))
				obj1.getMoveHandler().tryMove(obj1, deltaTime);
		}
		// move if no collision
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			// increase performance by only computing this once
			checkReachability = obj1.pushable || obj1.reachable.size > 0;
			for (j = i + 1; j < dynSize; j++) {
				obj2 = events.get(j);
				if (obj1.overlaps(obj2)) {
					obj1.collision.add(obj2);
					obj2.collision.add(obj1);
					if (obj2.pushable && !obj1.reachable.contains(obj2, true))
						obj1.reachable.add(obj2);
					if (obj1.pushable && !obj2.reachable.contains(obj1, true))
						obj2.reachable.add(obj1);
					if (obj1.blockingBehavior.blocks(obj2.blockingBehavior)) {
						if (!(obj1.isPlayerEvent() && obj2.isPlayerEvent())) {
							if (obj1.moves) {
								obj1.moves = false;
								if (obj2.moves && obj1.overlaps(obj2)) {
									obj1.moves = true;
									obj2.moves = false;
									obj2.getMoveHandler().moveBlocked(obj2);
									if (obj1.overlaps(obj2)) {
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
					obj1.justTouching.removeValue(obj2, true);
					obj2.justTouching.removeValue(obj1, true);
					if (checkReachability
							&& (obj2.pushable || obj2.reachable.size > 0)) {
						if (!obj1.reaches(obj2)) {
							obj1.reachable.removeValue(obj2, true);
							obj2.reachable.removeValue(obj1, true);
						}
					}
				}
			}
			obj1.commitMove();
			obj1.computeParticleEffect(deltaTime);
		}
		synchronized (this) {
			// shared variables for parallel computation
			this.events = events;
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
