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

package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Drawable;

/**
 * Only the projection and view matrices are computed. We don't need to compute
 * all the other stuff.<br>
 * Therefore (per default implementation) only the matrices view and projection
 * are useful for drawing.
 * 
 * @see Drawable#projectionMatrix(Camera)
 * @see SpriteBatch#setProjectionMatrix(Matrix4)
 * @author Alexander Baumgartner
 */
public class CameraSimpleOrtho2D extends Camera {
	private float x, y;

	@Override
	public void translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		position.x = adjustX();
		position.y = adjustY();
	}

	@Override
	public void lookAt(float x, float y, float z) {
		this.x = x - viewportWidth * .5f;
		this.y = y - viewportHeight * .5f;
		position.x = adjustX();
		position.y = adjustY();
	}

	@Override
	public void update() {
		view.setToOrtho2D(0, 0, GameBase.$().getScreen().width, GameBase.$()
				.getScreen().height);
		projection.setToOrtho2D(position.x, position.y, viewportWidth,
				viewportHeight);
		//combined.set(projection);
	}

	private float adjustX() {
		// keep view inside the plane
		float planeWidth = GameBase.$().getPlane().width;
		float x = this.x;
		if (viewportWidth >= planeWidth) {
			x = (planeWidth - viewportWidth) / 2;
		} else if (x < 0) {
			x = 0;
		} else if (x > planeWidth - viewportWidth) {
			x = planeWidth - viewportWidth;
		}
		GameBase.$().getPlane().x = x;
		return x;
	}

	private float adjustY() {
		// keep view inside the plane
		float planeHeight = GameBase.$().getPlane().height;
		float y = this.y;
		if (viewportHeight >= planeHeight) {
			y = (planeHeight - viewportHeight) / 2;
		} else if (y < 0) {
			y = 0;
		} else if (y > planeHeight - viewportHeight) {
			y = planeHeight - viewportHeight;
		}
		GameBase.$().getPlane().y = y;
		return y;
	}

	@Override
	public void update(boolean updateFrustum) {
		update();
	}
}
