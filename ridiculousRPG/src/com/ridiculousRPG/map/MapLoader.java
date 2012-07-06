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

package com.ridiculousRPG.map;

import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.movement.Movable;

/**
 * Interface for asynchronous map loading.
 * 
 * @author Alexander Baumgartner
 */
public interface MapLoader<T extends Movable> extends Disposable {
	/**
	 * Starts loading the given map asynchronously.<br>
	 * Creates a new map with the specified events from a file. tmx files can be
	 * created by using the Tiled editor.
	 * 
	 * @param filePath
	 */
	public void startLoadMap(String filePath);

	/**
	 * Waits until loading is finished and returns the map.
	 * 
	 * @throws Exception
	 */
	public MapWithEvents<T> endLoadMap() throws Exception;

	/**
	 * Stores the given map asynchronously.
	 * 
	 * @param map
	 */
	public void storeMapState(MapWithEvents<T> map, boolean disposeMap);
}
