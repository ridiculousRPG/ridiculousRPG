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

package com.madthrax.ridiculousRPG.animation;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * This class projects images onto the screen or viewport.
 * 
 * @author Alexander Baumgartner
 */
public abstract class ImageProjectionService extends GameServiceDefaultImpl
		implements Drawable {

	private Array<BoundedImage> images = new Array<BoundedImage>();

	/**
	 * A singleton instance for simply drawing background images onto the
	 * screen.<br>
	 * This service is {@link GameService#essential()}. The background will be
	 * drawn even when an other service clears the screen. If you don't want
	 * this behavior you may call {@link ImageProjectionService#freeze()} (and
	 * {@link ImageProjectionService#unfreeze()}) to avoid drawing the
	 * background (if one is assigned to this service)
	 */
	public static final ImageProjectionService $background = new ImageProjectionService() {
		boolean frozen = false;

		@Override
		public Matrix4 projectionMatrix(Camera camera) {
			return camera.view;
		}

		@Override
		public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
			if (!frozen) {
				super.draw(spriteBatch, camera, debug);
			}
		}

		@Override
		public void freeze() {
			frozen = true;
		}

		@Override
		public void unfreeze() {
			frozen = false;
		}

		@Override
		public boolean essential() {
			return true;
		}
	};
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

	/**
	 * Don't forget to dispose all images you remove and don't need anymore!<br>
	 * This service doesn't dispose any image.
	 * 
	 * @return
	 */
	public Array<BoundedImage> getImages() {
		return images;
	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		for (BoundedImage img : images) {
			img.draw(spriteBatch);
		}
	}

	@Override
	public void dispose() {
	}
}
