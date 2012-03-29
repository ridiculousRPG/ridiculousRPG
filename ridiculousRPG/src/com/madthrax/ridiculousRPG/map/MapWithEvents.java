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

package com.madthrax.ridiculousRPG.map;

import java.io.Serializable;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * Interface for all map implementations with movable events on it.
 * 
 * @author Alexander Baumgartner
 */
public interface MapWithEvents<T extends Movable> extends Disposable,
		Computable, Serializable {

	/**
	 * Puts a named event onto the loaded map.<br>
	 * Use the method <code>get(String name)</code> to dereference this named
	 * event. Named events can also be removed from the map by the methode
	 * <code>remove(String name)</code>.<br>
	 * Don't use the event's name if it may change.
	 * 
	 * @param name
	 * @param event
	 * @return the old event or null if there was no event with the same name
	 *         Don't forget to dispose the old event!
	 */
	public T put(String name, T event);

	/**
	 * puts an event onto the map
	 * 
	 * @param event
	 */
	public void put(T event);

	/**
	 * Returns the named T for manipulation.<br>
	 * (You don't need to put it onto the map again after manipulation)
	 * 
	 * @param name
	 * @return the named event or null if it does not exist
	 */
	public T get(String name);

	/**
	 * Returns the {@link List} with all events on this map.
	 * 
	 * @return all events
	 */
	public List<T> getAllEvents();

	/**
	 * Removes the named event from the map.<br>
	 * Don't forget to dispose the event!
	 * 
	 * @param name
	 * @return the removed event or null in no event with the given name existed
	 */
	public T remove(String name);

	/**
	 * Width of the entire map
	 * 
	 * @return
	 */
	public int getWidth();

	/**
	 * Height of the entire map
	 * 
	 * @return
	 */
	public int getHeight();

	/**
	 * Draws the map with all events on it.
	 * 
	 * @param spriteBatch
	 * @param camera
	 * @param debug
	 */
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug);

	public String getExternalSavePath();

	public void dispose(final boolean recycle);
}
