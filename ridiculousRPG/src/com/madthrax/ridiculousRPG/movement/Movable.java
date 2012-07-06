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

package com.madthrax.ridiculousRPG.movement;

import java.io.Serializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.movement.misc.MoveNullAdapter;
import com.madthrax.ridiculousRPG.util.Direction;
import com.madthrax.ridiculousRPG.util.Speed;

/**
 * Base class for events.
 * 
 * @author Alexander Baumgartner
 */
//TODO: Maybe: Derive from com.badlogic.gdx.scenes.scene2d.Actor
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

	protected Speed moveSpeed = Speed.S00_ZERO;
	protected Rectangle touchBound = new Rectangle();

	private MovementHandler moveHandler = MoveNullAdapter.$();

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
	 * game.
	 * 
	 * @param moveSpeed
	 */
	public void setMoveSpeed(Speed moveSpeed) {
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
	public Rectangle getTouchBound() {
		return touchBound;
	}

	/**
	 * Sets the touch bounds for collision detection.
	 * 
	 * @param touchBound
	 */
	public void setTouchBound(Rectangle touchBound) {
		this.touchBound = touchBound;
	}

	public void setX(float x) {
		this.touchBound.x = x;
	}

	public void setY(float y) {
		this.touchBound.y = y;
	}

}
