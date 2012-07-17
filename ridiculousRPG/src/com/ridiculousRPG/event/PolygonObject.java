package com.ridiculousRPG.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.map.tiled.TiledMapWithEvents;

public class PolygonObject implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private boolean loop;
	private float[] vertexX;
	private float[] vertexY;
	private float[] segmentXlen;
	private float[] segmentYlen;
	private float[] segmentLen;
	public final String[] execAtNodeScript;
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public Map<String, String> properties = new HashMap<String, String>();

	private PolygonMoveState moveState = new PolygonMoveState();
	private PolygonMoveState moveStateOld = new PolygonMoveState();

	public PolygonObject(String name, float[] vertexX, float[] vertexY,
			boolean loop) {
		this.loop = loop;
		this.name = name;
		if (vertexX.length != vertexY.length)
			throw new IllegalArgumentException("Invalid polygon definition! "
					+ "Length of vertexX and vertexY has to be the same.");
		if (vertexX.length < 2)
			throw new IllegalArgumentException("Invalid polygon definition! "
					+ "A polygon has at least 2 nodes.");
		this.vertexX = vertexX;
		this.vertexY = vertexY;
		this.segmentLen = new float[vertexX.length - 1];
		this.segmentXlen = new float[vertexX.length - 1];
		this.segmentYlen = new float[vertexX.length - 1];
		float x1, y1, x2, y2, xLen, yLen;
		x1 = vertexX[vertexX.length - 1];
		y1 = vertexY[vertexY.length - 1];
		for (int i = vertexX.length - 2; i > -1; i--) {
			x2 = x1;
			y2 = y1;
			x1 = vertexX[i];
			y1 = vertexY[i];
			xLen = x2 - x1;
			yLen = y2 - y1;
			segmentXlen[i] = xLen;
			segmentYlen[i] = yLen;
			segmentLen[i] = (float) Math.sqrt(xLen * xLen + yLen * yLen);
		}
		this.execAtNodeScript = new String[vertexX.length];
	}

	/**
	 * A move along polygon implementation can/should use this to execute a
	 * specified script.
	 */
	public void setExecScript(int node, String script) {
		execAtNodeScript[node] = script;
		if (node == 0 && loop
				&& execAtNodeScript[execAtNodeScript.length - 1] == null)
			execAtNodeScript[execAtNodeScript.length - 1] = script;
	}

	/**
	 * x coordinate of vertex i
	 * 
	 * @param i
	 * @return
	 */
	public float getX(int i) {
		return vertexX[i];
	}

	/**
	 * y coordinate of vertex i
	 * 
	 * @param i
	 * @return
	 */
	public float getY(int i) {
		return vertexY[i];
	}

	/**
	 * x coordinate of the current position.
	 * 
	 * @see #moveAlong(float, boolean)
	 */
	public float getX() {
		return moveState.moveX;
	}

	/**
	 * y coordinate of the current position.
	 * 
	 * @see #moveAlong(float, boolean)
	 */
	public float getY() {
		return moveState.moveY;
	}

	/**
	 * relative x coordinate from last position.
	 * 
	 * @see #moveAlong(float, boolean)
	 */
	public float getRelX() {
		return moveState.moveRelX;
	}

	/**
	 * relative y coordinate from last position.
	 * 
	 * @see #moveAlong(float, boolean)
	 */
	public float getRelY() {
		return moveState.moveRelY;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Moves along this polygon and returns the script to execute, if a
	 * checkpoint with an applied script has been reached.
	 * 
	 * @param distance
	 *            The distance to move (negative distance is allowed to change
	 *            direction)
	 * @param crop
	 *            If true, the move will stop EXACTLY on every checkpoint with
	 *            an applied script (the given distance will be cropped). If
	 *            false, the point where the script is executed will be shortly
	 *            after the checkpoint, but the move will be smoother.
	 * @return The script applied to the reached checkpoint or null.
	 * @see #start(boolean)
	 */
	public String moveAlong(float distance, boolean crop) {
		if (isFinished())
			return null;
		moveStateOld.set(moveState);
		// compute segment index and distance
		String exec = computeSegmentPos(distance, crop);
		// compute current (x,y) position
		int i = moveState.moveIndex;
		float x = vertexX[i] + segmentXlen[i] * moveState.moveDistance
				/ segmentLen[i];
		float y = vertexY[i] + segmentYlen[i] * moveState.moveDistance
				/ segmentLen[i];
		moveState.moveRelX = x - moveState.moveX;
		moveState.moveRelY = y - moveState.moveY;
		moveState.moveX = x;
		moveState.moveY = y;
		return exec;
	}

	private String computeSegmentPos(float distance, boolean crop) {
		this.moveState.moveDistance += distance;
		float currSegLen = segmentLen[moveState.moveIndex];
		if (moveState.moveDistance <= 0) {
			String exec = execAtNodeScript[moveState.moveIndex];
			moveState.moveIndex--;
			if (moveState.moveIndex < 0) {
				if (!loop || crop) {
					if (moveState.finished) {
						crop = false;
						exec = null;
					} else {
						moveState.finished = true;
						moveState.moveIndex = 0;
						moveState.moveDistance = 0;
						return exec;
					}
				}
				moveState.moveIndex = segmentLen.length - 1;
			}
			if (crop && exec != null) {
				moveState.moveDistance = segmentLen[moveState.moveIndex];
			} else {
				moveState.moveDistance += segmentLen[moveState.moveIndex];
			}
			return exec;
		} else if (moveState.moveDistance >= currSegLen) {
			moveState.moveIndex++;
			String exec = execAtNodeScript[moveState.moveIndex];
			if (moveState.moveIndex > segmentLen.length - 1) {
				if (!loop || crop) {
					if (moveState.finished) {
						crop = false;
						exec = null;
					} else {
						moveState.finished = true;
						moveState.moveIndex = segmentLen.length - 1;
						moveState.moveDistance = currSegLen;
						return exec;
					}
				}
				moveState.moveIndex = 0;
			}
			if (crop && exec != null) {
				moveState.moveDistance = 0;
			} else {
				moveState.moveDistance -= currSegLen;
			}
			return exec;
		}
		return null;
	}

	public int getSegmentCount() {
		return vertexX.length;
	}

	public boolean isFinished() {
		return moveState.finished && !loop;
	}

	/**
	 * (Re)set the polygon position. If rewind is true, the position will be set
	 * to the end of this polygon. Otherwise it will be set to the start of the
	 * polygon.
	 * 
	 * @param rewind
	 * @see #moveAlong(float, boolean)
	 */
	public void start(boolean rewind) {
		if (rewind) {
			moveState.moveIndex = segmentLen.length - 1;
			moveState.moveDistance = segmentLen[moveState.moveIndex];
			moveState.moveX = vertexX[moveState.moveIndex]
					+ segmentXlen[moveState.moveIndex];
			moveState.moveY = vertexY[moveState.moveIndex]
					+ segmentYlen[moveState.moveIndex];
		} else {
			moveState.moveIndex = 0;
			moveState.moveDistance = 0;
			moveState.moveX = vertexX[0];
			moveState.moveY = vertexY[0];
		}
		moveState.moveRelX = moveState.moveRelY = 0;
		moveState.finished = false;
		moveStateOld.set(moveState);
	}

	public float getSegmentLen(int segmentIndex) {
		return segmentLen[segmentIndex];
	}

	public float getRelativeX() {
		return moveState.moveRelX;
	}

	public float getRelativeY() {
		return moveState.moveRelY;
	}

	public void undoMove() {
		moveState.set(moveStateOld);
	}

	public PolygonObject clone() {
		PolygonObject newPoly;
		try {
			newPoly = (PolygonObject) super.clone();
			newPoly.moveState = (PolygonMoveState) moveState.clone();
			newPoly.moveStateOld = (PolygonMoveState) moveStateOld.clone();
		} catch (CloneNotSupportedException e) {
			// It's cloneable, so this should never happen
			GameBase.$info("PolygonObject.clone", "Couldn't clone polygon", e);
			return this;
		}
		return newPoly;
	}

	public static class PolygonMoveState implements Cloneable, Serializable {
		private static final long serialVersionUID = 1L;

		public int moveIndex;
		public float moveDistance;
		public float moveX;
		public float moveY;
		public float moveRelX;
		public float moveRelY;
		public boolean finished;

		public void set(PolygonMoveState src) {
			moveIndex = src.moveIndex;
			moveDistance = src.moveDistance;
			moveX = src.moveX;
			moveY = src.moveY;
			moveRelX = src.moveRelX;
			moveRelY = src.moveRelY;
			finished = src.finished;
		}

		public PolygonMoveState clone() {
			try {
				return (PolygonMoveState) super.clone();
			} catch (CloneNotSupportedException e) {
				// It's cloneable, so this should never happen
				GameBase.$info("PolygonMoveState.clone",
						"Couldn't clone polygon move state", e);
				return this;
			}
		}
	}

	@Override
	public String toString() {
		return "polygon '" + name + " #nodes=" + vertexX.length + " (1st node="
				+ vertexX[0] + "/" + vertexY[0] + ")'";
	}
}
