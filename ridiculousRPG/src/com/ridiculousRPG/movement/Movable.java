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

package com.ridiculousRPG.movement;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Pool;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.movement.CombinedMovesAdapter.MoveSegment;
import com.ridiculousRPG.movement.auto.MoveJumpAdapter;
import com.ridiculousRPG.movement.auto.MovePolygonAdapter;
import com.ridiculousRPG.movement.auto.MoveRandomAdapter;
import com.ridiculousRPG.movement.misc.MoveNullAdapter;
import com.ridiculousRPG.util.Direction;
import com.ridiculousRPG.util.Speed;

/**
 * Base class for events.
 * 
 * @author Alexander Baumgartner
 */
// TODO: Maybe: Derive from com.badlogic.gdx.scenes.scene2d.Actor
public abstract class Movable implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates if a move is outstanding.<br>
	 * All {@link #offerMove} methods should set this to true.<br>
	 * The method {@link #commitMove} should only perform a move if this is set
	 * to true. Furthermore {@link #commitMove} should reset this switch to
	 * false.
	 */
	public boolean moves = false;
	/**
	 * Offset for drawing this event. Useful e.g. for jump movement, where the
	 * y-offset continuously grows and shrinks.
	 */
	public float offsetX, offsetY;

	protected Speed moveSpeed = Speed.S00_ZERO;
	protected Rectangle2D.Float touchBound = new Rectangle2D.Float();

	private MovementHandler moveHandler = MoveNullAdapter.$();
	private SortedMap<Integer, MoveSegment> moveSequence = new TreeMap<Integer, MoveSegment>();
	private boolean moveLoop;
	private boolean moveResetEventPosition;

	// To avoid garbage collection - ATTENTION: Pool is NOT thread safe, but we
	// have actually only one thread which handles all events. Therefore it
	// should be ok this way.
	private static final JumpPool MOVE_JUMP_POOL = new JumpPool();
	private static final RandomPool MOVE_RANDOM_4WAY_POOL = new RandomPool();

	/**
	 * Initializes the move sequence
	 */
	public void init() {
		if (moveSequence != null && moveSequence.size() > 0) {
			CombinedMovesAdapter combined = new CombinedMovesAdapter(moveLoop,
					moveResetEventPosition);
			if (moveHandler != MoveNullAdapter.$()) {
				combined.addMoveToExecute(moveHandler);
			}
			for (MoveSegment segment : moveSequence.values()) {
				combined.addMoveSegment(segment);
			}
			moveHandler = combined;
			moveSequence = null;
		}
	}

	/**
	 * Offer a move to this movable. Moves may be blocked if this objects
	 * touchBound overlaps an other touchBound.
	 */
	public synchronized void offerMoveTo(float x, float y) {
		offerMove(x - touchBound.x, y - touchBound.y);
	}

	/**
	 * Moves this movable to the given x,y - position.
	 */
	public synchronized void forceMoveTo(float x, float y) {
		offerMoveTo(x, y);
		commitMove();
	}

	/**
	 * Offer a move to this movable. Moves may be blocked if this objects
	 * touchBound overlaps an other touchBound.<br>
	 * The object will be moved by the given (x,y) amounts.
	 * 
	 * @param x
	 *            The distance in direction x
	 * @param y
	 *            The distance in direction y
	 */
	public abstract void offerMove(float x, float y);

	/**
	 * Offer a move to this movable. Moves may be blocked if this objects
	 * touchBound overlaps an other touchBound.
	 * 
	 * @return the computed distance
	 */
	public abstract float offerMove(Direction dir, float deltaTime);

	/**
	 * Executes the last outstanding move.
	 * 
	 * @return true if there was an outstanding move to execute.
	 */
	public abstract boolean commitMove();

	/**
	 * You can use this method if you have a running animation and you want to
	 * reset (stop) it after this movement stopped.
	 */
	public abstract void stop();

	/**
	 * Returns the center of the touchBound
	 */
	public float getCenterX() {
		return touchBound.x + touchBound.width * .5f;
	}

	/**
	 * Returns the center of the touchBound
	 */
	public float getCenterY() {
		return touchBound.y + touchBound.height * .5f;
	}

	public void translate(float x, float y) {
		addX(x);
		addY(y);
	}

	public void addX(float value) {
		touchBound.x += value;
	}

	public void addY(float value) {
		touchBound.y += value;
	}

	/**
	 * Returns the bottom left corner of the touchBound
	 */
	public float getX() {
		return touchBound.x;
	}

	/**
	 * Returns the bottom left corner of the touchBound
	 */
	public float getY() {
		return touchBound.y;
	}

	/**
	 * Returns the width of the touchBound
	 */
	public float getWidth() {
		return touchBound.width;
	}

	/**
	 * Returns the height of the touchBound
	 */
	public float getHeight() {
		return touchBound.height;
	}

	/**
	 * The MovementAdapter can never be null unless the Movable is disposed
	 * already.
	 * 
	 * @return The currently installed MovementAdapter for this event.
	 */
	public MovementHandler getMoveHandler() {
		return moveHandler;
	}

	/**
	 * Installs the specified MovementAdapter for this event or the
	 * {@link MoveNullAdapter} if the parameter is null. The MoveHandler can
	 * never be null unless the Movable is disposed already.
	 * 
	 * @param moveHandler
	 *            the new MovementAdapter
	 */
	public void setMoveHandler(MovementHandler moveHandler) {
		if (moveHandler == null) {
			this.moveHandler = MoveNullAdapter.$();
		} else {
			this.moveHandler = moveHandler;
		}
	}

	/**
	 * ATTENTION: This method only takes effect if it's called before
	 * {@link #init()}!<br>
	 * Adds a move segment to the sequence at the specified position. All the
	 * added moves will be chained at initialization time. This move segment
	 * will be executed until it's finished.
	 * 
	 * @param index
	 *            Position in the chain
	 * @param moveHandler
	 *            The new MovementAdapter
	 * @see #init()
	 */
	public void addMoveSegment(int index, MovementHandler moveHandler) {
		if (moveHandler != null) {
			addMoveSegment(index, CombinedMovesAdapter.MOVE_FINISH_POOL.obtain(
					moveHandler, 1));
		}
	}

	/**
	 * ATTENTION: This method only takes effect if it's called before
	 * {@link #init()}!<br>
	 * Adds a move sequence at the specified position. All the added moves will
	 * be chained at initialization time.
	 * 
	 * @param index
	 *            Position in the chain
	 * @param moveSegment
	 *            The {@link MoveSegment} to add
	 * @see #init()
	 */
	public void addMoveSegment(int index, MoveSegment moveSegment) {
		if (moveSequence != null && moveSegment != null) {
			moveSequence.put(index, moveSegment);
		}
	}

	/**
	 * Executes a jump movement by the given x and y values.
	 */
	public void jump(float distanceX, float distanceY) {
		jumpTo(getX() + distanceX, getY() + distanceY);
	}

	/**
	 * Executes a jump movement to the given other event.
	 */
	public void jumpTo(Movable movable) {
		jumpTo(movable.getX(), movable.getY());
	}

	/**
	 * Executes a jump movement to the given position.
	 */
	public void jumpTo(float x, float y) {
		exec(MOVE_JUMP_POOL.obtain(x, y));
	}

	/**
	 * Executes random movements for random time.
	 */
	public void random() {
		random(1, 5, 128);
	}

	/**
	 * Executes random movements for random time.
	 */
	public void random(float minSec, float maxSec, int slackness) {
		exec(CombinedMovesAdapter.MOVE_RANDOM_POOL.obtain(MOVE_RANDOM_4WAY_POOL
				.obtain(slackness), minSec, maxSec));
	}

	/**
	 * Executes a move along a given polygon.
	 */
	public void polygon(String polygonName) {
		polygon(polygonName, false);
	}

	/**
	 * Executes a move along a given polygon.
	 */
	public void polygon(String polygonName, boolean rewind) {
		exec(new MovePolygonAdapter(polygonName, rewind));
	}

	/**
	 * The event freezes at the current place for the given amount of seconds.<br>
	 */
	public void sleep(float seconds) {
		exec(CombinedMovesAdapter.MOVE_SECONDS_POOL.obtain(MoveNullAdapter.$(),
				seconds));
	}

	/**
	 * Executes the move until it's finished.
	 * 
	 * @param moveHandler
	 *            The new MovementAdapter
	 */
	public void exec(MovementHandler moveHandler) {
		if (moveHandler != null) {
			exec(CombinedMovesAdapter.MOVE_FINISH_POOL.obtain(moveHandler, 1));
		}
	}

	/**
	 * Executes the move segment until it's finished.
	 * 
	 * @param moveSegment
	 *            The {@link MoveSegment} to add
	 */
	public void exec(MoveSegment moveSegment) {
		if (moveSegment != null) {
			if (moveHandler instanceof CombinedMovesAdapter) {
				((CombinedMovesAdapter) moveHandler).execMoveOnce(moveSegment);
			} else {
				CombinedMovesAdapter combined = new CombinedMovesAdapter(
						moveLoop, moveResetEventPosition);
				combined.execMoveOnce(moveSegment);
				if (moveHandler != MoveNullAdapter.$()) {
					combined.addMoveToExecute(moveHandler);
				}
				moveHandler = combined;
			}
		}
	}

	/**
	 * Computes the distance between two {@link Movable}s
	 * 
	 * @return the computed distance
	 */
	public float computeDistance(Movable other) {
		return computeDistance(other.touchBound.x, other.touchBound.y);
	}

	/**
	 * Computes the distance between this {@link Movable} and the given
	 * coordinates.
	 * 
	 * @return the computed distance
	 */
	public float computeDistance(float x, float y) {
		x -= touchBound.x;
		y -= touchBound.y;
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Computes the screen position of this movable
	 * 
	 * @return x position on the screen
	 */
	public float getScreenX() {
		Camera camera = GameBase.$().getCamera();
		float centerX = getCenterX() - camera.position.x;
		centerX *= Gdx.graphics.getWidth() / camera.viewportWidth;
		return centerX;
	}

	/**
	 * Computes the relative x position from an absolute one
	 * 
	 * @return x position relative to this movable
	 */
	public float computeRelativX(int screenAbsolutX) {
		return screenAbsolutX - getScreenX();
	}

	/**
	 * Computes the screen position of this movable
	 * 
	 * @return y position on the screen
	 */
	public float getScreenY() {
		Camera camera = GameBase.$().getCamera();
		float centerY = getCenterY() - camera.position.y;
		centerY *= Gdx.graphics.getHeight() / camera.viewportHeight;
		return centerY;
	}

	/**
	 * Computes the relative y position from an absolute one
	 * 
	 * @return y position relative to this movable
	 */
	public float computeRelativY(int screenAbsolutY) {
		return (Gdx.graphics.getHeight() - screenAbsolutY) - getScreenY();
	}

	/**
	 * You can change the speed of a {@link Movable} object at any time in your
	 * game. The value null results in {@link Speed#S00_ZERO}
	 * 
	 * @param moveSpeed
	 */
	public void setMoveSpeed(Speed moveSpeed) {
		if (moveSpeed == null)
			this.moveSpeed = Speed.S00_ZERO;
		else
			this.moveSpeed = moveSpeed;
	}

	/**
	 * The speed of this movable object
	 * 
	 * @return
	 */
	public Speed getMoveSpeed() {
		return moveSpeed;
	}

	/**
	 * The touch bound is used to move the object around. It defines the x,y
	 * coordinates and a width and height. The touch bound is used for collision
	 * detection.
	 * 
	 * @return The touching bounds.
	 */
	public Rectangle2D.Float getTouchBound() {
		return touchBound;
	}

	/**
	 * Sets the touch bounds for collision detection.
	 * 
	 * @param touchBound
	 */
	public void setTouchBound(Rectangle2D.Float touchBound) {
		this.touchBound = touchBound;
	}

	public boolean isMoveLoop() {
		return moveLoop;
	}

	/**
	 * ATTENTION: This method only takes effect if it's called before
	 * {@link #init()}!
	 * 
	 * @param moveLoop
	 */
	public void setMoveLoop(boolean moveLoop) {
		this.moveLoop = moveLoop;
	}

	public boolean isMoveResetEventPosition() {
		return moveResetEventPosition;
	}

	/**
	 * ATTENTION: This method only takes effect if it's called before
	 * {@link #init()}!
	 * 
	 * @param moveResetEventPosition
	 */
	public void setMoveResetEventPosition(boolean moveResetEventPosition) {
		this.moveResetEventPosition = moveResetEventPosition;
	}

	public void setX(float x) {
		this.touchBound.x = x;
	}

	public void setY(float y) {
		this.touchBound.y = y;
	}

	public static class JumpPool extends Pool<MoveJumpAdapter> {
		@Override
		protected MoveJumpAdapter newObject() {
			return new MoveJumpAdapter(0, 0) {
				private static final long serialVersionUID = 1L;

				public void free() {
					MOVE_JUMP_POOL.free(this);
				}
			};
		}

		public MoveJumpAdapter obtain(float x, float y) {
			MoveJumpAdapter jump = obtain();
			jump.other.setX(x);
			jump.other.setY(y);
			return jump;
		}
	}

	public static class RandomPool extends Pool<MoveRandomAdapter> {
		@Override
		protected MoveRandomAdapter newObject() {
			return new MoveRandomAdapter() {
				private static final long serialVersionUID = 1L;

				public void free() {
					MOVE_RANDOM_4WAY_POOL.free(this);
				}
			};
		}

		public MoveRandomAdapter obtain(int changeDirectionSlackness) {
			MoveRandomAdapter mv = obtain();
			mv.changeDirectionSlackness = changeDirectionSlackness;
			return mv;
		}
	}
}
