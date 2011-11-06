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

package com.madthrax.ridiculousRPG.events.handler;

import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.events.EventObject;

/**
 * This interface defines all callback methods for events.
 * 
 * @author Alexander Baumgartner
 */
public interface EventHandler {
	/**
	 * This method is called if the event is touchable and an touch event
	 * occurred.
	 * 
	 * @param self
	 *            The event which has been touched
	 * @param touchedBy
	 *            The event which triggered this touch (most likely the player)
	 * @return true if the input has been consumed
	 */
	public boolean touch(EventObject self, EventObject touchedBy);

	/**
	 * This method is called if the event is touchable and an push event
	 * occurred. (The action key was pressed and the event was reachable)
	 * 
	 * @param self
	 *            The event which has been pushed
	 * @param pushedBy
	 *            The event which triggered this push (most likely the player)
	 * @return true if the input has been consumed
	 */
	public boolean push(EventObject self, EventObject pushedBy);

	/**
	 * This method is called if the events timer is running. It's your
	 * responsibility to add or subtract the deltaTime from an value which
	 * should be stored inside the {@link ObjectState}
	 * 
	 * @param self
	 *            The event
	 * @param deltaTime
	 *            time elapsed since the last call of this method
	 * @return true if the input has been consumed
	 * @see #getActualState()
	 */
	public boolean timer(EventObject self, float deltaTime);

	/**
	 * This method is not called by the engines default implementation. You can
	 * use this to handle custom events.
	 * 
	 * @param self
	 *            The event
	 * @param triggerId
	 *            This id allows you to specify multiple custom events
	 * @return true if the custom event ate up this triggerId
	 * @see #getActualState()
	 */
	public boolean customTrigger(EventObject self, int triggerId);

	/**
	 * @return the actual state of this object
	 */
	public ObjectState getActualState();

	/**
	 * Load your own state from the parent's child states!<br>
	 * Make sure that you do not collide with an other event state.
	 * 
	 * @param self
	 * @param parentState
	 */
	public void load(EventObject self, ObjectState parentState);

	/**
	 * Save your own state into the parent's child states!<br>
	 * Make sure that you do not collide with an other event state.
	 * 
	 * @param self
	 * @param parentState
	 * @param currentlyExecuted
	 *            true if this event is currently active. E.g. the one map is
	 *            rendered currently, where this event is placed on.
	 */
	public void store(EventObject self, ObjectState parentState,
			boolean currentlyExecuted);
}
