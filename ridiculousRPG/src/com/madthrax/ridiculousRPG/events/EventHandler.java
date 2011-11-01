package com.madthrax.ridiculousRPG.events;

import com.madthrax.ridiculousRPG.ObjectState;

public interface EventHandler {
	/**
	 * 
	 * @param self The event which has been touched
	 * @param touchedBy The event which triggered this touch (most likely the player)
	 * @return true if the input has been consumed
	 */
	public boolean touch(EventObject self, EventObject touchedBy);
	/**
	 * 
	 * @param self The event which has been pushed
	 * @param pushedBy The event which triggered this push (most likely the player)
	 * @return true if the input has been consumed
	 */
	public boolean push(EventObject self, EventObject pushedBy);
	/**
	 * 
	 * @param self The event
	 * @param deltaTime
	 * @return true if the input has been consumed
	 */
	public boolean timer(EventObject self, float deltaTime);
	public void customTrigger(EventObject self, int triggerId);
	public ObjectState getActualState();
	/**
	 * Load your own state from the parent's child states!<br>
	 * Make sure that you do not collide with an other event state.
	 * @param self
	 * @param parentState
	 */
	public void load(EventObject self, ObjectState parentState);
	/**
	 * Save your own state into the parent's child states!<br>
	 * Make sure that you do not collide with an other event state.
	 * @param self
	 * @param parentState
	 * @param currentlyExecuted
	 * true if this event is currently active. E.g. the one map is rendered
	 * currently, where this event is placed on.
	 */
	public void store(EventObject self, ObjectState parentState, boolean currentlyExecuted);
}
