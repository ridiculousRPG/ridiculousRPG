package com.madthrax.ridiculousRPG.movement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.events.Speed;
import com.madthrax.ridiculousRPG.movement.misc.MoveNullAdapter;

public abstract class Movable {
	/**
	 * Indicates if a move is outstanding.<br>
	 * All {@link #offerMove} methods should set this to true.<br>
	 * The method {@link #commitMove} should only perform a move
	 * if this is set to true. Furthermore {@link #commitMove}
	 * should reset this switch to false.
	 */
	public boolean moves = false;
	/**
	 * The speed may be changed at any time by the game.
	 */
	public Speed moveSpeed = Speed.S00_ZERO;
	/**
	 * The touching bounds for moving this object around.
	 */
	public Rectangle touchBound = new Rectangle();

	private MovementHandler moveHandler = MoveNullAdapter.$();

	/**
	 * Offer a move to this movable. Moves may be blocked if this
	 * objects touchBound overlaps an other touchBound.
	 */
	public void offerMoveTo(float x, float y) {
		offerMove(x-touchBound.x, y-touchBound.y);
	}
	/**
	 * Offer a move to this movable. Moves may be blocked if this
	 * objects touchBound overlaps an other touchBound.<br>
	 * The object will be moved by the given (x,y) amounts.
	 * @param x
	 * The distance in direction x
	 * @param y
	 * The distance in direction y
	 */
	public abstract void offerMove(float x, float y);
	/**
	 * Offer a move to this movable. Moves may be blocked if this
	 * objects touchBound overlaps an other touchBound.
	 * @return the computed distance
	 */
	public abstract float offerMove(Direction dir, float deltaTime);
	/**
	 * Executes the last outstanding move.
	 * @return true if there was an outstanding move to execute.
	 */
	public abstract boolean commitMove();
	/**
	 * You can use this method if you have a running animation
	 * and you want to reset (stop) it after this movement stopped.
	 */
	public abstract void stop();
	/**
	 * Returns the center of the touchBound
	 */
	public float getCenterX() {
		return touchBound.x+touchBound.width*.5f;
	}
	/**
	 * Returns the center of the touchBound
	 */
	public float getCenterY() {
		return touchBound.y+touchBound.height*.5f;
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
	 * The MovementAdapter can never be null unless the Movable
	 * is disposed already.
	 * @return The currently installed MovementAdapter for this event.
	 */
	public MovementHandler getMoveHandler() {
		return moveHandler;
	}
	/**
	 * Installs the specified MovementAdapter for this event or the
	 * {@link MoveNullAdapter} if the parameter is null.
	 * The MoveHandler can never be null unless the Movable
	 * is disposed already.
	 * @param moveHandler
	 * the new MovementAdapter
	 */
	public void setMoveHandler(MovementHandler moveHandler) {
		if (moveHandler==null) {
			this.moveHandler = MoveNullAdapter.$();
		} else {
			this.moveHandler = moveHandler;
		}
	}
	/**
	 * Computes the distance between two {@link Movable}s
	 * @return the computed distance
	 */
	public float computeDistance(Movable other) {
		return computeDistance(other.touchBound.x, other.touchBound.y);
	}
	/**
	 * Computes the distance between this {@link Movable}
	 * and the given coordinates.
	 * @return the computed distance
	 */
	public float computeDistance(float x, float y) {
		x -= touchBound.x;
		y -= touchBound.y;
		return (float) Math.sqrt(x*x+y*y);
	}
	/**
	 * Computes the relative x position from an absolute one
	 * @return x position relative to this movable
	 */
	public float computeRelativX(int screenAbsolutX) {
		Camera camera = GameBase.camera;
		float centerX = getCenterX()-camera.position.x;
		centerX *= Gdx.graphics.getWidth()/camera.viewportWidth;
		return screenAbsolutX - centerX;
	}
	/**
	 * Computes the relative y position from an absolute one
	 * @return y position relative to this movable
	 */
	public float computeRelativY(int screenAbsolutY) {
		Camera camera = GameBase.camera;
		float centerY = getCenterY()-camera.position.y;
		centerY *= Gdx.graphics.getHeight()/camera.viewportHeight;
		return (Gdx.graphics.getHeight()-screenAbsolutY)-centerY;
	}
}
