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

package com.madthrax.ridiculousRPG.animations;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * This class projects images onto the screen or viewport.
 * 
 * @author Alexander Baumgartner
 */
public abstract class ImageProjectionService extends GameServiceDefaultImpl
		implements Computable, Drawable {

	/**
	 * A singleton instance for simply drawing images onto the screen.
	 */
	public static final ImageProjectionService $screen = new ImageProjectionService() {
		public Matrix4 projectionMatrix(Camera camera) {
			return camera.view;
		}
	};
	/**
	 * A singleton instance for simply drawing images onto the map.
	 */
	public static final ImageProjectionService $map = new ImageProjectionService() {
		public Matrix4 projectionMatrix(Camera camera) {
			return camera.projection;
		}
	};

	private Array<BoundedImage> images = new Array<BoundedImage>();

	@Override
	public void compute(float deltaTime, boolean actionKeyDown) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
