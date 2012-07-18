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

package com.ridiculousRPG.event.handler;

import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.util.ObjectState;

/**
 * This is a default implementation of the interface {@link EventHandler}. Use
 * this class if you don't want to implement all the stuff yourself.
 * 
 * @author Alexander Baumgartner
 */
public class EventAdapter implements EventHandler {
	private static final long serialVersionUID = 1L;

	private ObjectState myState = new ObjectState();
	private Object belongingObject;

	public EventAdapter(Object belongingObject) {
		this.belongingObject = belongingObject;
	}

	public boolean onTouch(EventObject eventTrigger) {
		return false;
	}

	public boolean onPush(EventObject eventTrigger) {
		return false;
	}

	public void onLoad() {
	}

	public boolean onCustomTrigger(int triggerId) {
		return false;
	}

	public boolean onTimer(float deltaTime) {
		return false;
	}

	public void onStateChange(ObjectState globalState) {
	}

	@Override
	public ObjectState getActualState() {
		return myState;
	}

	@Override
	public void setState(ObjectState objectState) {
		myState = objectState;
	}

	@Override
	public void init() {
	}

	@Override
	public void dispose() {
		myState = null;
	}

	@Override
	public Object getBelongingObject() {
		return belongingObject;
	}
}
