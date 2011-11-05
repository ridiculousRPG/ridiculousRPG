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

package com.madthrax.ridiculousRPG;

/**
 * @author Alexander Baumgartner
 */
public interface LoadAndStorable {
	/**
	 * Loads the requested state for this object.
	 * 
	 * @param myState
	 *            The last state which was returned by {@link #store()} or a new
	 *            empty {@link ObjectState} (This parameter should never be
	 *            null)
	 */
	public void load(ObjectState myState);

	/**
	 * The method {@link #store()} is called for saving this objects state.<br>
	 * If you hold a reference to {@link ObjectState} inside your object, you
	 * can always hold this {@link ObjectState} up to date. There is no need to
	 * instantiate a new {@link ObjectState} Object. Use the one you get from
	 * {@link #load(ObjectState)}.
	 * 
	 * @param currentlyExecuted
	 *            Indicates if the object is currently executed (active).<br>
	 *            E.g. The switch is true for active events and false for
	 *            inactive events. Events are active if they are on the
	 *            currently displayed map.
	 * @return The current state of this object
	 */
	public ObjectState store(boolean currentlyExecuted);
}
