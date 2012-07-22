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

package com.ridiculousRPG.movement.auto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.event.PolygonObject;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.map.MapRenderService;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.ObjectState;

/**
 * This {@link MovementHandler} tries to move an event along the given polygon.
 * The move waits if a blocking event exists on it's way.<br>
 * After succeeding the switch finished is set to true.
 * 
 * @author Alexander Baumgartner
 */
public class MovePolygonAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private PolygonObject polygon;
	private String execScript;
	private String polygonName;
	private boolean polygonChanged;
	private boolean rewind;
	private boolean crop;
	private boolean moveFinished;
	private boolean waitScriptExec;

	private static String NODE_TEMPLATE;

	public MovePolygonAdapter(PolygonObject polygon) {
		this(polygon, false);
	}

	public MovePolygonAdapter(PolygonObject polygon, boolean rewind) {
		this(polygon, false, true);
	}

	public MovePolygonAdapter(PolygonObject polygon, boolean rewind,
			boolean crop) {
		this.rewind = rewind;
		this.crop = crop;
		setPolygon(polygon);
	}

	public MovePolygonAdapter(String polyName) {
		this(polyName, false);
	}

	public MovePolygonAdapter(String polyName, boolean rewind) {
		this(polyName, false, true);
	}

	public MovePolygonAdapter(String polyName, boolean rewind, boolean crop) {
		this.rewind = rewind;
		this.crop = crop;
		this.polygonName = polyName;
	}

	public boolean isRewind() {
		return rewind;
	}

	public void setRewind(boolean rewind) {
		this.rewind = rewind;
		if (moveFinished)
			polygonChanged = true;
	}

	public boolean isCrop() {
		return crop;
	}

	public void setCrop(boolean crop) {
		this.crop = crop;
	}

	public PolygonObject getPolygon() {
		return polygon;
	}

	public void setPolygon(PolygonObject polygon) {
		this.polygon = (PolygonObject) polygon.clone();
		this.polygonName = polygon.getName();
		polygonChanged = true;
	}

	public String getPolygonName() {
		return polygonName;
	}

	public void setPolygonName(String polygonName) {
		this.polygonName = polygonName;
	}

	private boolean initPolygon() {
		if (polygonName == null)
			return false;
		Array<MapRenderService> maps = GameBase.$serviceProvider().getServices(
				MapRenderService.class);
		for (int i = maps.size - 1; i > -1; i--) {
			PolygonObject polygon = maps.get(i).getMap().findPolygon(
					polygonName);
			if (polygon != null) {
				setPolygon(polygon);
				return true;
			}
		}
		return false;
	}

	@Override
	public void tryMove(Movable event, float deltaTime,
			EventTrigger eventTrigger) {
		// Polygons are defined on the map and may not be available at event
		// creation time, that's why we have to initialize them here.
		if (polygon == null && !initPolygon())
			return;
		if (finished) {
			event.stop();
			return;
		}

		// Post script to execute (execution is performed in a separate thread)
		if (execScript != null) {
			if (NODE_TEMPLATE == null)
				NODE_TEMPLATE = Gdx.files.internal(
						GameBase.$options().eventNodeTemplate).readString(
						GameBase.$options().encoding);
			String script = GameBase.$scriptFactory().prepareScriptFunction(
					execScript, NODE_TEMPLATE);
			ObjectState state = null;
			if (event instanceof EventObject) {
				EventHandler h = ((EventObject) event).eventHandler;
				if (h != null)
					state = h.getActualState();
			}
			eventTrigger.postScriptToExec(
					"MovePolygonAdapter(" + polygon + ")", script, "onNode",
					event, state, polygon, this);
			execScript = null;
			if (crop)
				waitScriptExec = true;
		}
		// Wait for script execution if the crop-switch is true
		if (waitScriptExec && !eventTrigger.isScriptQueueEmpty()) {
			event.stop();
			return;
		}
		// Reset polygon if it has changed (e.g. by the just executed script)
		if (polygonChanged)
			reset();
		// Move along the polygon while not finished
		if (!moveFinished) {
			float distance = event.getMoveSpeed().computeStretch(deltaTime);
			if (distance > 0) {
				waitScriptExec = false;
				if (rewind)
					execScript = polygon.moveAlong(-distance, crop);
				else
					execScript = polygon.moveAlong(distance, crop);
				event.offerMove(polygon.getRelativeX(), polygon.getRelativeY());
				if (event instanceof EventObject) {
					((EventObject) event).animate(polygon.getRelativeX(),
							polygon.getRelativeY(), deltaTime);
				}
				moveFinished = polygon.isFinished();
			}
		} else {
			event.stop();
			finished = true;
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		if (polygon != null)
			polygon.undoMove();
		execScript = null;
		event.stop();
	}

	@Override
	public void reset() {
		super.reset();
		polygonChanged = false;
		moveFinished = false;
		waitScriptExec = false;
		execScript = null;
		if (polygon != null)
			polygon.start(rewind);
	}
}
