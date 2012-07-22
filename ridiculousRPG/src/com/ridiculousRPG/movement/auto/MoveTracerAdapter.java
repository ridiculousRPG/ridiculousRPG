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

package com.ridiculousRPG.movement.auto;

import java.awt.geom.Rectangle2D;
import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Pool;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * With this {@link MovementHandler} one event can follow an other event.<br>
 * The following-algorithm works independently from the movement actions, which
 * are performed from the traced event. Therefore any Combination of
 * {@link MovementHandler}s can be traced by an other event. Of course you can
 * trace an event which already traces an other event...<br>
 * Feel free to build a chain of events ;) <br>
 * <br>
 * This class changes the visibility of Movables. If you don't want this
 * functionality please extend this class and override the method
 * setVisibility(...). Also you can override the method setVisibility(...) if
 * you need this functionality for other Movable objects (other than Movables)<br>
 * <br>
 * <h1>ATTENTION!!!</h1> Use a none-blocking follower otherwise it could be
 * shaken off or the events could block mutually.<br>
 * A defaultdistance for following the other event will be computed from the
 * touch-bounds of both events.
 * 
 * @author Alexander Baumgartner
 */
public class MoveTracerAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private Movable eventToTrace;
	private float followDistance;
	private boolean estimateDistance = false;

	private Deque<Rectangle2D.Float> movementQueue = new LinkedList<Rectangle2D.Float>();
	private float distanceCount;

	private Pool<Rectangle2D.Float> rectPool = new Pool<Rectangle2D.Float>() {
		@Override
		protected Rectangle2D.Float newObject() {
			return new Rectangle2D.Float();
		}
	};

	/**
	 * <h1>ATTENTION!!!</h1> Use a non-blocking eventToTrace otherwise the
	 * follower could be shaken off or the events could block mutually.<br>
	 * You can simply set <code>event.blocks=false</code> before installing this
	 * Movementadapter (and reset <code>event.blocks=true</code> after removing
	 * this Movementadapter)
	 * 
	 * @param eventToTrace
	 *            The event to trace
	 * @param followDistance
	 *            The distance to follow the other event
	 */
	public MoveTracerAdapter(Movable eventToTrace, float followDistance) {
		this.eventToTrace = eventToTrace;
		this.followDistance = followDistance;
	}

	/**
	 * <h1>ATTENTION!!!</h1> Use a none-blocking {@link #eventToTrace} otherwise
	 * the follower could be shaken off or the events could block mutually.<br>
	 * A default distance for following the other event will be computed from
	 * the touch-bounds of both events.
	 * 
	 * @param eventToTrace
	 *            The event to trace
	 */
	public MoveTracerAdapter(Movable eventToTrace) {
		this(eventToTrace, 0);
		this.estimateDistance = true;
	}

	@Override
	public void tryMove(Movable event, float deltaTime,
			EventTrigger eventTrigger) {
		if (estimateDistance) {
			followDistance = eventToTrace.getWidth() + eventToTrace.getHeight()
					+ event.getWidth() + event.getHeight();
			followDistance /= 2.5f;
			estimateDistance = false;
		}
		Rectangle2D.Float actualMove = eventToTrace.getTouchBound();
		if (movementQueue.isEmpty()) {
			Rectangle2D.Float r = rectPool.obtain();
			r.setRect(actualMove.x, actualMove.y, actualMove.width,
					actualMove.height);
			movementQueue.offer(r);
			setVisibility(event, false);
		} else {
			Rectangle2D.Float lastMove = movementQueue.peekLast();
			if (actualMove.x == lastMove.x && actualMove.y == lastMove.y) {
				// Other event did not move
				if (movementQueue.size() > 1) {
					consumeMoves(event, 1, deltaTime);
				} else {
					setVisibility(event, false);
				}
			} else {
				// Other event moved
				produceMove(lastMove, actualMove);
				if (followDistance < distanceCount) {
					consumeMoves(event,
							followDistance * 1.2 < distanceCount ? 2 : 1,
							deltaTime);
					setVisibility(event, true);
				}
			}
		}
	}

	/**
	 * Override this method if you have other Movable objects to set the
	 * visibility or you want to disable setting the visibility in general.
	 * 
	 * @param event
	 * @param visible
	 */
	protected void setVisibility(Movable event, boolean visible) {
		if (event instanceof EventObject)
			((EventObject) event).visible = visible;
	}

	private void produceMove(Rectangle2D.Float lastMove,
			Rectangle2D.Float actualMove) {
		distanceCount += Math.abs(actualMove.x - lastMove.x);
		distanceCount += Math.abs(actualMove.y - lastMove.y);
		Rectangle2D.Float r = rectPool.obtain();
		r.setRect(actualMove.x, actualMove.y, actualMove.width,
				actualMove.height);
		movementQueue.offer(r);
	}

	private void consumeMoves(Movable event, int steps, float deltaTime) {
		float x = 0;
		float y = 0;
		for (; steps > 0 && movementQueue.size() > 1; steps--) {
			Rectangle2D.Float firstMove = movementQueue.pollFirst();
			Rectangle2D.Float secondMove = movementQueue.peekFirst();
			x += secondMove.x - firstMove.x;
			y += secondMove.y - firstMove.y;
			distanceCount -= Math.abs(secondMove.x - firstMove.x);
			distanceCount -= Math.abs(secondMove.y - firstMove.y);
			rectPool.free(firstMove);
		}
		event.offerMove(x, y);
		if (event instanceof EventObject) {
			((EventObject) event).animate(x, y, deltaTime);
		}
	}

	@Override
	public void reset() {
		super.reset();
		distanceCount = 0f;
		movementQueue.clear();
	}
}
