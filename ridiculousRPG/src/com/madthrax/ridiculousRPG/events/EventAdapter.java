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

package com.madthrax.ridiculousRPG.events;

import com.madthrax.ridiculousRPG.ObjectState;

/**
 * @author Alexander Baumgartner
 */
public class EventAdapter implements EventHandler {
	public ObjectState myState;

	@Override
	public boolean touch(EventObject self, EventObject touchedBy) {
		return false;
	}
	@Override
	public boolean push(EventObject self, EventObject pushedBy) {
		System.out.println("push "+self.name);
		return false;
	}
	@Override
	public void load(EventObject self, ObjectState parentState) {
		this.myState = parentState.getChild(self.id);
		// load position, texture, movehandler ...
	}
	@Override
	public void store(EventObject self, ObjectState parentState, boolean currentlyExecuted) {
		if (currentlyExecuted) {
			// store position, texture, movehandler ...
		} else {
			// clear position, texture, movehandler ... from myState
		}
		parentState.setChild(self.id, myState);
	}
	@Override
	public void customTrigger(EventObject self, int triggerId) {}
	@Override
	public boolean timer(EventObject self, float deltaTime) {
		return false;
	}
	@Override
	public ObjectState getActualState() {
		return myState;
	}
}
