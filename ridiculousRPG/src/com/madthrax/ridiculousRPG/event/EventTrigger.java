package com.madthrax.ridiculousRPG.event;

import java.util.List;

import com.badlogic.gdx.utils.Disposable;

public interface EventTrigger extends Disposable {
	public void compute(float deltaTime, boolean actionKeyDown,
			List<EventObject> events);
}
