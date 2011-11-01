package com.madthrax.ridiculousRPG.movement.auto;

import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event by the given
 * distance and direction. The move waits if a blocking event
 * exists on it's way.<br>
 * After succeeding the switch finished is set to true.
 */
public class MoveDistanceAdapter extends MovementHandler {
	private float distance;
	private float lastDistance;
	private float distanceCount;
	private Direction dir;

	protected MoveDistanceAdapter(float distance, Direction dir) {
		this.distance = distance;
		this.dir = dir;
	}
	/**
	 * This MovementAdapter tries to move an event by the given
	 * distance and direction. The move waits if a blocking event
	 * exists on it's way.<br>
	 * After succeeding the switch finished is set to true.
	 */
	public static MovementHandler $(float distance, Direction dir) {
		return new MoveDistanceAdapter(distance, dir);
	}
	@Override
	public void tryMove(Movable event, float deltaTime) {
		// move could be blocked
		if (distanceCount >= distance || finished) {
			if (distanceCount == 0f) event.stop();
			finished = true;
		} else {
			lastDistance = event.offerMove(dir, deltaTime);
			distanceCount+=lastDistance;
		}
	}
	@Override
	public void moveBlocked(Movable event) {
		distanceCount-=lastDistance;
		event.stop();
	}
	@Override
	public void reset() {
		super.reset();
		distanceCount = 0f;
		lastDistance = 0f;
	}
}
