package com.madthrax.ridiculousRPG.movement.auto;

import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.CombinedMovesAdapter;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event around
 * the given rectangle. The move waits if a blocking event
 * exists on it's way.<br>
 * After succeeding the switch finished is set to true.<br>
 * It's only a convenience {@link MovementHandler} which chains
 * a {@link MoveSetXYAdapter} together with four {@link MoveDistanceAdapter}s.
 * @see {@link CombinedMovesAdapter}, {@link MoveSetXYAdapter}, {@link MoveDistanceAdapter}
 */
public class MoveRectangleAdapter extends CombinedMovesAdapter {
	/**
	 * {@link StartPoint#BOTTOM_LEFT} {@link StartPoint#BOTTOM_RIGHT} {@link StartPoint#TOP_LEFT} {@link StartPoint#TOP_RIGHT}
	 */
	public enum StartPoint {BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT}

	protected MoveRectangleAdapter(Rectangle rect, StartPoint startAt, boolean clockwise) {
		super(false, false);
		switch (startAt) {
		case BOTTOM_LEFT:
			// starting position of the event
			addMoveToExecute(MoveSetXYAdapter.$(rect.x, rect.y));
			if (clockwise)
				addDirections(rect, Direction.N, Direction.E, Direction.S, Direction.W);
			else
				addDirections(rect, Direction.E, Direction.N, Direction.W, Direction.S);
			return;
		case BOTTOM_RIGHT:
			// starting position of the event
			addMoveToExecute(MoveSetXYAdapter.$(rect.x+rect.width, rect.y));
			if (clockwise)
				addDirections(rect, Direction.W, Direction.N, Direction.E, Direction.S);
			else
				addDirections(rect, Direction.N, Direction.W, Direction.S, Direction.E);
			return;
		case TOP_LEFT:
			// starting position of the event
			addMoveToExecute(MoveSetXYAdapter.$(rect.x, rect.y+rect.height));
			if (clockwise)
				addDirections(rect, Direction.E, Direction.S, Direction.W, Direction.N);
			else
				addDirections(rect, Direction.S, Direction.E, Direction.N, Direction.W);
			return;
		case TOP_RIGHT:
			// starting position of the event
			addMoveToExecute(MoveSetXYAdapter.$(rect.x+rect.width, rect.y+rect.height));
			if (clockwise)
				addDirections(rect, Direction.S, Direction.W, Direction.N, Direction.E);
			else
				addDirections(rect, Direction.W, Direction.S, Direction.E, Direction.N);
			return;
		}
	}
	private void addDirections (Rectangle rect, Direction... dirs) {
		for (Direction dir : dirs) {
			float distance = dir==Direction.N || dir==Direction.S ? rect.height : rect.width;
			addMoveToExecute(MoveDistanceAdapter.$(distance, dir));
		}
	}
	/**
	 * This {@link MovementHandler} tries to move an event around
	 * the given rectangle. The move waits if a blocking event
	 * exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 */
	public static MovementHandler $(Rectangle rect, boolean clockwise) {
		return $(rect, StartPoint.BOTTOM_LEFT, clockwise);
	}
	/**
	 * This {@link MovementHandler} tries to move an event around
	 * the given rectangle. The move waits if a blocking event
	 * exists on it's way.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * Use one of the defined starting points 
	 * {@link StartPoint#BOTTOM_LEFT} {@link StartPoint#BOTTOM_RIGHT} {@link StartPoint#TOP_LEFT} {@link StartPoint#TOP_RIGHT}
	 * @see {@link StartPoint}
	 */
	public static MovementHandler $(Rectangle rect, StartPoint startAt, boolean clockwise) {
		return new MoveRectangleAdapter(rect, startAt, clockwise);
	}
}
