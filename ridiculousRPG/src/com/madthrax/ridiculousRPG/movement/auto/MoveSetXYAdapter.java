package com.madthrax.ridiculousRPG.movement.auto;

import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event to the given
 * position. The move is blocked while a blocking event
 * exists on the given position.<br>
 * After succeeding the switch finished is set to true.<br>
 * If there exists a none-moving blocking event at the given
 * position, this movement will never finish.
 */
public class MoveSetXYAdapter extends MovementHandler {
	private boolean checkPerformed;
	private Movable other;

	protected MoveSetXYAdapter(Movable other) {
		this.other = other;
	}
	/**
	 * This MovementAdapter tries to move an event to the given
	 * position. The move is blocked while a blocking event
	 * exists on the given position.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given
	 * position, this movement will never finish.
	 */
	public static MovementHandler $(float x, float y) {
		Movable mv = new Movable() {
			@Override
			public void stop() {}
			@Override
			public void offerMove(float x, float y) {}
			@Override
			public float offerMove(Direction dir, float deltaTime) {return 0f;}
			@Override
			public boolean commitMove() {return false;}
		};
		mv.touchBound.x = x;
		mv.touchBound.y = y;
		return new MoveSetXYAdapter(mv);
	}
	/**
	 * This MovementAdapter tries to move an event to a given
	 * other Movable. The move is blocked forever
	 * if two events bolcks mutually.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given
	 * position, this movement will never finish.
	 */
	public static MovementHandler $(Movable other) {
		return new MoveSetXYAdapter(other);
	}
	@Override
	public void tryMove(Movable event, float deltaTime) {
		event.stop();
		// move could be blocked
		if (checkPerformed || finished) {
			finished = true;
		} else {
			event.offerMove(other.getX()-event.getX(), other.getY()-event.getY());
			checkPerformed = true;
		}
	}
	@Override
	public void moveBlocked(Movable event) {
		checkPerformed=false;
	}
	@Override
	public void reset() {
		super.reset();
		checkPerformed=false;
	}
}
