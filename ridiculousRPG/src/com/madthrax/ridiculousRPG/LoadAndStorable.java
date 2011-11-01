package com.madthrax.ridiculousRPG;

public interface LoadAndStorable {
	/**
	 * Loads the requested state for this object.
	 * @param myState
	 * The last state which was returned by {@link #store()} or a new empty {@link ObjectState}
	 * (This parameter should never be null)
	 */
	public void load(ObjectState myState);
	/**
	 * The method {@link #store()} is called for saving this objects state.<br>
	 * If you hold a reference to {@link ObjectState} inside your object,
	 * you can always hold this {@link ObjectState} up to date. There is no need
	 * to instantiate a new {@link ObjectState} Object. Use the one you get from
	 * {@link #load(ObjectState)}.
	 * @param currentlyExecuted
	 * Indicates if the object is currently executed (active).<br>
	 * E.g. The switch is true for active events and false for inactive events.
	 * Events are active if they are on the currently displayed map.
	 * @return The current state of this object
	 */
	public ObjectState store(boolean currentlyExecuted);
}
