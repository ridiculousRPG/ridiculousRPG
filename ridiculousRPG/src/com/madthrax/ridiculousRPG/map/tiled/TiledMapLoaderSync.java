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

package com.madthrax.ridiculousRPG.map.tiled;

import javax.script.ScriptException;

import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.map.MapLoader;
import com.madthrax.ridiculousRPG.map.MapWithEvents;

/**
 * This map loader allows to store the old maps state asynchronously while
 * blending in the new map.
 * 
 * @author Alexander Baumgartner
 */
public class TiledMapLoaderSync extends Thread implements
		MapLoader<EventObject> {

	private boolean disposeMap = false;
	private boolean disposed = false;
	private boolean done = true;
	private MapWithEvents<EventObject> map;
	private String loadMapPath;

	TiledMapLoaderSync() {
		start();
	}

	@Override
	public void run() {
		while (true) {
			while (done) {
				if (disposed)
					return;
				yield();
			}
			if (map != null) {
				// store map
				map.saveStateToFS();
				if (disposeMap)
					map.dispose(true);
				map = null;
			}
			done = true;
		}
	}

	public synchronized void startLoadMap(String tmxPath) {
		loadMapPath = tmxPath;
	}

	public synchronized MapWithEvents<EventObject> endLoadMap()
			throws ScriptException {
		return loadTiledMap();
	}

	private MapWithEvents<EventObject> loadTiledMap() throws ScriptException {
		return new TiledMapWithEvents(loadMapPath);
	}

	public synchronized void storeMapState(MapWithEvents<EventObject> map,
			boolean disposeMap) {
		// Wait until outstanding operation has completed.
		while (!done && map != null) {
			if (disposed)
				return;
			Thread.yield();
		}
		this.disposeMap = disposeMap;
		this.map = map;
		this.loadMapPath = null;
		this.done = false;
	}

	@Override
	public void dispose() {
		disposed = true;
	}
}
