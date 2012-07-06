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

package com.ridiculousRPG.service;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ridiculousRPG.GameServiceProvider;

/**
 * The {@link #draw} method is automatically called for all {@link GameService}s
 * which are added to the {@link GameServiceProvider}.<br>
 * It's guaranteed that the {@link #draw} method of one {@link GameService} is
 * called after all provided {@link Computable#compute} methods.
 * 
 * @author Alexander Baumgartner
 */
public interface Drawable {
	/**
	 * Draws the current state of this GameService onto the screen<br>
	 * The {@link #draw} method is automatically called for all
	 * {@link GameService}s which are added to the {@link GameServiceProvider}.<br>
	 */
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug);

	/**
	 * Returns the projection matrix which is used for drawing this Drawable.<br>
	 * If you are not sure what to do, use camera.view for drawing on the screen
	 * or camera.projection for drawing on the actual rendered map.
	 * 
	 * @param camera
	 * @return
	 */
	public Matrix4 projectionMatrix(Camera camera);
}
