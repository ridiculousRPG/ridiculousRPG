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

package com.madthrax.ridiculousRPG.events;

import java.util.List;

import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * All {@link EventHandler} are called and the specified actions are performed.<br>
 * The execution of {@link EventHandler} is done in a separate thread because
 * {@link EventHandler} are allowed to block (e.g. until the user made a decision).
 * @author Alexander Baumgartner
 */
public class TriggerEventHandler extends Thread implements Disposable, Computable {
	private List<EventObject> events;
	private boolean disposed = false;
	private float deltaTime;
	private boolean computationReady = false;
	private boolean actionKeyPressed = false;

	public TriggerEventHandler(List<EventObject> events) {
		this.events = events;
		start();
	}
	
	@Override
	public void run() {
		while (!disposed) {
			while (!computationReady) yield();
			float deltaTime = this.deltaTime;
			this.deltaTime = 0f;
			try {
				callEventHandler(deltaTime, events, actionKeyPressed);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.computationReady = false;
			yield();
		}
	}
	// Call all event handler
	private void callEventHandler(float deltaTime, List<EventObject> dynamicRegions, boolean actionKeyPressed) {
		EventObject obj1;
		EventObject obj2;
		int i, j;
		int dynSize = dynamicRegions.size();
		boolean consumed = false;
		for (i = 0; i < dynSize && !consumed && !disposed; i++) {
			obj1 = dynamicRegions.get(i);
			consumed = obj1.getEventHandler()!=null && obj1.getEventHandler().timer(obj1, deltaTime);
			if (!consumed && obj1.consumeInput) {
				int tmpSize = obj1.collision.size();
				for (j = 0; j < tmpSize && !consumed && !disposed; j++) {
					obj2 = obj1.collision.get(j);
					consumed = obj2.getEventHandler()!=null && obj2.getEventHandler().touch(obj2, obj1);
				}
				if (!consumed && actionKeyPressed) {
					for (EventObject pushed : obj1.reachable) {
						consumed = pushed.getEventHandler()!=null && pushed.getEventHandler().push(pushed, obj1);
						if (consumed || disposed) break;
					}
				}
			}
		}
	}
	/**
	 * Compute collisions and move the events.<br>
	 * Invoke parallel execution of the {@link EventHandler}.
	 */
	
	public void compute(float deltaTime, boolean actionKeyPressed) {
		// Load frequently used pointers/variables into register
		List<EventObject> events = this.events;
		int dynSize = events.size();
		int i,j;
		EventObject obj1, obj2;
		boolean checkReachability;

		// compute all moves
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			obj1.collision.clear();
			obj1.getMoveHandler().tryMove(obj1, deltaTime);
		}

		// move if no collision
		for (i = 0; i < dynSize; i++) {
			obj1 = events.get(i);
			// increase performance by only computing this once
			checkReachability = obj1.pushable || !obj1.reachable.isEmpty();
			for (j = i+1; j < dynSize; j++) {
				obj2 = events.get(j);
				if (obj1.overlaps(obj2)) {
					obj1.collision.add(obj2);
					obj2.collision.add(obj1);
					if (obj2.pushable) obj1.reachable.add(obj2);
					if (obj1.pushable) obj2.reachable.add(obj1);
					if (obj1.blockingBehaviour.blocks(obj2.blockingBehaviour)) {
						if (obj1.moves) {
							obj1.moves=false;
							if (obj2.moves && obj1.overlaps(obj2)) {
								obj1.moves=true;
								obj2.moves=false;
								obj2.getMoveHandler().moveBlocked(obj2);
								if (obj1.overlaps(obj2)) {
									obj1.moves=false;
									obj1.getMoveHandler().moveBlocked(obj1);
								}
							} else {
								obj1.getMoveHandler().moveBlocked(obj1);
							}
						} else if (obj2.moves) {
							obj2.moves=false;
							obj2.getMoveHandler().moveBlocked(obj2);
						}
					}
				} else if (checkReachability && 
						(obj2.pushable || !obj2.reachable.isEmpty())) {
					if (!obj1.reaches(obj2)) {
						obj1.reachable.remove(obj2);
						obj2.reachable.remove(obj1);
					}
				}
			}
			obj1.commitMove();
		}
		// shared variables for parallel computation
		this.actionKeyPressed = actionKeyPressed;
		this.deltaTime += deltaTime;
		this.computationReady = true;
	}
	
	public void dispose() {
		disposed = true;
	}
}
