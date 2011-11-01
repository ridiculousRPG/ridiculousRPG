package com.madthrax.ridiculousRPG.movement;

/**
 * All possible MovementAdapters must extend this class.<br>
 * For performance-reasons all MovementAdapters which don't
 * need any status-information should be implemented as singleton.<br>
 * To be uniform all implementations should offer a static
 * $(...) - method and all constructors should be protected.
 * This implementation strategy also allows the use of "object pooling",
 * which may be used to increase the performance.
 */
public abstract class MovementHandler {
	/**
	 * If this movement can finish (e.g. move to coordinate x,y),
	 * the implementation should set this switch.
	 */
	public boolean finished = false;
	/**
	 * This method moves the event but does not commit the move.<br>
	 * The move is computed but not performed.
	 * @param event
	 * @param deltaTime
	 */
	public abstract void tryMove(Movable event, float deltaTime);
	/**
	 * Resets the state of this MovementAdapter.
	 * The default implementation sets the finished state to false.
	 */
	public void reset() {
		finished = false;
	}
	/**
	 * This method is called if the game is in idle state.
	 * E.g. it's paused or the main-menu is open...<br>
	 * (Empty default implementation)
	 */
	public void freeze() {}
	/**
	 * This method is called if the move couldn't be performed.
	 * The move has been canceled instead of commited.<br>
	 * (Empty default implementation)
	 */
	public void moveBlocked(Movable event) {}
}
