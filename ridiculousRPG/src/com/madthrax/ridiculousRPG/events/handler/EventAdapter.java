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
 * This is a default implementation of the interface {@link EventHandler}. Use
 * this class if you don't want to implement all the stuff yourself.
 * 
 * @author Alexander Baumgartner
 */
public class EventAdapter implements EventHandler {
	private ObjectState myState;

	public boolean touch(EventObject self, EventObject touchedBy) {
		return false;
	}

	public boolean push(EventObject self, EventObject pushedBy) {
		System.out.println("push " + self.name);
		return false;
	}

	public void load(EventObject self, ObjectState parentState) {
		this.myState = parentState.getChild(self.id);
		// load position, texture, movehandler ...
	}

	public void store(EventObject self, ObjectState parentState,
			boolean currentlyExecuted) {
		if (currentlyExecuted) {
			// store position, texture, movehandler ...
		} else {
			// clear position, texture, movehandler ... from myState
		}
		parentState.setChild(self.id, myState);
	}

	public boolean customTrigger(EventObject self, int triggerId) {
		return false;
	}

	public boolean timer(EventObject self, float deltaTime) {
		return false;
	}

	public ObjectState getActualState() {
		return myState;
	}
}
