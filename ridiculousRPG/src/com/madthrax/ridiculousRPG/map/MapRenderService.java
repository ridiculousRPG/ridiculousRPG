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

import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * This service renders (tiled) maps with events on them.
 * 
 * @see {@link MapWithEvents}
 * @author Alexander Baumgartner
 */
public class MapRenderService extends GameServiceDefaultImpl implements
		Computable, Drawable {
	private MapWithEvents<?> map;

	/**
	 * Returns the actually displayed Map
	 * @return
	 */
	public MapWithEvents<?> getMap() {
		return map;
	}

	/**
	 * Loads a new map to render and calls
	 * {@link GameBase#resizePlane(int, int)} which triggers all
	 * {@link ResizeListener}s to be called by the {@link GameServiceProvider}.
	 * 
	 * @param map
	 * @return The old map or null. Don't forget to dispose the old map!
	 */
	public MapWithEvents<?> loadMap(MapWithEvents<?> map) {
		MapWithEvents<?> old = this.map;
		this.map = map;
		GameBase.$().resizePlane(map.getWidth(), map.getHeight());
		return old;
	}

	public void dispose() {
		if (map!=null) map.dispose();
	}

	public void compute(float deltaTime, boolean pushButtonPressed) {
		map.compute(deltaTime, pushButtonPressed);
	}

	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		map.draw(spriteBatch, camera, debug);
	}

	@Override
	public void freeze() {
		if (map == null)
			return;
		List<? extends Movable> events = map.getAllEvents();
		for (int i = 0, len = events.size(); i < len; i++) {
			events.get(i).getMoveHandler().freeze();
		}
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.projection;
	}
}
