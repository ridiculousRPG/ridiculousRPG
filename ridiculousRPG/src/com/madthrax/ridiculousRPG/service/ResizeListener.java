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

import com.madthrax.ridiculousRPG.GameServiceProvider;

/**
 * The method {@link #resize(int, int)} is automatically called for all
 * {@link GameService}s which are added to the {@link GameServiceProvider}.<br>
 * It's guaranteed that the {@link #resize(int, int)} method of one
 * {@link GameService} is called when a resize event occurred.<br>
 * Changing from windowed to fullscreen mode and vice versa will also trigger
 * the resize event.
 * 
 * @author Alexander Baumgartner
 */
public interface ResizeListener {
	/**
	 * Automatically called when a resize event occurred.
	 */
	public void resize(int width, int height);
}
