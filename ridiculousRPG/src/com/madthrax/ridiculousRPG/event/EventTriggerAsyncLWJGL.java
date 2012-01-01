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

package com.madthrax.ridiculousRPG.event;

import java.util.List;

import javax.script.ScriptException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.SharedDrawable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.event.handler.EventHandler;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * All {@link EventHandler} are called and the specified actions are performed.<br>
 * The execution of {@link EventHandler} is done in a separate thread because
 * {@link EventHandler} are allowed to block (e.g. until the user made a
 * decision).
 * 
 * @author Alexander Baumgartner
 */
public class EventTriggerAsyncLWJGL extends Thread implements Disposable,
		Computable {
	private List<EventObject> events;
	private boolean disposed = false;
	private float deltaTime;
	private boolean computationReady = false;
	private boolean actionKeyDown = false;

	public EventTriggerAsyncLWJGL(List<EventObject> events) {
		this.events = events;
		start();
	}

	@Override
	public void run() {
		if (Gdx.app instanceof LwjglApplication) {
			try {
				new SharedDrawable(Display.getDrawable()).makeCurrent();
			} catch (LWJGLException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException(getClass().getName()+" only works with LWJGL");
		}
		while (!disposed) {
			while (!computationReady) {
				yield();
				if (disposed)
					return;
			}
			float deltaTime = this.deltaTime;
			this.deltaTime = 0f;
			try {
				callEventHandler(deltaTime, events, actionKeyDown);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.computationReady = false;
			yield();
		}
	}

	// Call all event handler
	private void callEventHandler(float deltaTime,
			List<EventObject> dynamicRegions, boolean actionKeyDown)
			throws ScriptException {
		EventObject obj1;
		EventObject obj2;
		int i, j;
		int dynSize = dynamicRegions.size();
		boolean consumed = false;
		for (i = 0; i < dynSize && !consumed && !disposed; i++) {
			obj1 = dynamicRegions.get(i);
			consumed = obj1.getEventHandler() != null
					&& obj1.getEventHandler().timer(obj1, deltaTime);
			if (!consumed && obj1.consumeInput) {
				int tmpSize = obj1.collision.size;
				for (j = 0; j < tmpSize && !consumed && !disposed; j++) {
					obj2 = obj1.collision.get(j);
					if (obj2.touchable
							&& !obj1.justTouching.contains(obj2, true)) {
						consumed = obj2.getEventHandler().touch(obj2, obj1);
						if (consumed) {
							obj1.justTouching.add(obj2);
						}
					}
				}
				if (!consumed && actionKeyDown) {
					for (EventObject pushed : obj1.reachable) {
						consumed = pushed.pushable
								&& pushed.getEventHandler().push(pushed, obj1);
						if (consumed || disposed)
							break;
					}
				}
			}
		}
	}

	/**
	 * Compute collisions and move the events.<br>
	 * Invoke parallel execution of the {@link EventHandler}.
	 */
	@Override
	public void compute(float deltaTime, boolean actionKeyDown) {
		// Load frequently used pointers/variables into register
		List<EventObject> events = this.events;
		int dynSize = events.size();
		int i, j;
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
			checkReachability = obj1.pushable || obj1.reachable.size > 0;
			for (j = i + 1; j < dynSize; j++) {
				obj2 = events.get(j);
				if (obj1.overlaps(obj2)) {
					obj1.collision.add(obj2);
					obj2.collision.add(obj1);
					if (obj2.pushable)
						obj1.reachable.add(obj2);
					if (obj1.pushable)
						obj2.reachable.add(obj1);
					if (obj1.blockingBehaviour.blocks(obj2.blockingBehaviour)) {
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
		}
		// shared variables for parallel computation
		this.actionKeyDown = actionKeyDown;
		this.deltaTime += deltaTime;
		this.computationReady = true;
	}

	public void dispose() {
		disposed = true;
	}
}
