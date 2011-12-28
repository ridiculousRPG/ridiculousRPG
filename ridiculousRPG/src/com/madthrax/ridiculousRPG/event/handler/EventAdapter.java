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

package com.madthrax.ridiculousRPG.event.handler;

import javax.script.ScriptException;

import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.event.EventObject;

/**
 * This is a default implementation of the interface {@link EventHandler}. Use
 * this class if you don't want to implement all the stuff yourself.
 * 
 * @author Alexander Baumgartner
 */
public class EventAdapter implements EventHandler {
	private ObjectState myState;

	public boolean touch(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		return false;
	}

	public boolean push(EventObject eventSelf, EventObject eventTrigger)
			throws ScriptException {
		System.out.println("push " + eventSelf.name);
		return false;
	}

	public void load(EventObject eventSelf, ObjectState parentState)
			throws ScriptException {
		this.myState = parentState.getChild(eventSelf.id);
		// load position, texture, movehandler ...
	}

	public void store(EventObject eventSelf, ObjectState parentState,
			boolean currentlyExecuted) throws ScriptException {
		if (currentlyExecuted) {
			// store position, texture, movehandler ...
		} else {
			// clear position, texture, movehandler ... from myState
		}
		parentState.setChild(eventSelf.id, myState);
	}

	public boolean customTrigger(EventObject eventSelf, int triggerId)
			throws ScriptException {
		return false;
	}

	public boolean timer(EventObject eventSelf, float deltaTime)
			throws ScriptException {
		return false;
	}

	public ObjectState getActualState() {
		return myState;
	}

	@Override
	public void init() {
	}
}