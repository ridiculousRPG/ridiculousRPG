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

package com.madthrax.ridiculousRPG.service;

import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;

/**
 * The {@link #compute} method is automatically called for all
 * {@link GameService}s which are added to the {@link GameServiceProvider}.<br>
 * It's guaranteed that the {@link #compute} method of one {@link GameService}
 * is called before all provided {@link Drawable#draw} methods.
 * 
 * @author Alexander Baumgartner
 */
public interface Computable {
	/**
	 * Computes one loop iteration by the given deltaTime<br>
	 * The {@link #compute} method is automatically called for all
	 * {@link GameService}s which are added to the {@link GameServiceProvider}.<br>
	 * 
	 * @param deltaTime
	 *            time elapsed since last call (in seconds)
	 * @param actionKeyPressed
	 *            indicates if the player wants an action to be performed
	 * @see GameBase#isActionKeyPressed()
	 */
	public void compute(float deltaTime, boolean actionKeyPressed);
}
