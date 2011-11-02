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
import com.madthrax.ridiculousRPG.animations.WeatherEffectService;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * @author Alexander Baumgartner
 */
public class MapRenderService extends GameServiceDefaultImpl implements Computable, Drawable {
	private MapWithEvents<?> map;

	/**
	 * Loads a new map to render and automatically resizes the
	 * weather effect if there is one running.
	 * @param map
	 * @return The old map or null. Don't forget to dispose the old map!
	 */
	public MapWithEvents<?> loadMap(MapWithEvents<?> map) {
		MapWithEvents<?> old = this.map;
		this.map = map;
		GameBase.$().setPlaneWidth(map.getWidth());
		GameBase.$().setPlaneHeight(map.getHeight());
		WeatherEffectService wes = GameBase.$serviceProvider().getService(WeatherEffectService.class);
		if (wes!=null) {
			wes.resize(map.getWidth(), map.getHeight());
		}
		GameBase.$().getCamera().update();
		return old;
	}
	@Override
	public void dispose() {}
	@Override
	public void compute(float deltaTime, boolean pushButtonPressed) {
		map.compute(deltaTime, pushButtonPressed);
	}
	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		map.draw(spriteBatch, camera, debug);
	}
	@Override
	public void freeze() {
		List<? extends Movable> events = map.getAllEvents();
		for (int i=0, len=events.size(); i<len; i++) {
			events.get(i).getMoveHandler().freeze();
		}
	}
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.projection;
	}
}
