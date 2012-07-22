package com.ridiculousRPG.event;

import java.util.List;

import com.badlogic.gdx.utils.Disposable;

public interface EventTrigger extends Disposable {
	public void compute(float deltaTime, boolean actionKeyDown,
			List<EventObject> events, List<PolygonObject> polys);

	public void postScriptToExec(String description, String script,
			String invokeFnc, Object... invokeParm);

	public boolean isScriptQueueEmpty();
}
