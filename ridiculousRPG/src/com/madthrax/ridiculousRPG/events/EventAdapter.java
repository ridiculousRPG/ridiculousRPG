package com.madthrax.ridiculousRPG.events;

import com.madthrax.ridiculousRPG.ObjectState;

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
