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

/**
 * The initialization is automatically performed for all {@link GameService}s.<br>
 * The {@link #init} method is called by adding the GameService to the
 * GameServiceProvider if the method {@link #isInitialized} returns false.<br>
 * The initialization waits for global game initialization to finish. You are
 * welcome to add {@link Initializable} to the GameServiceProvider before
 * starting the game.
 * 
 * @author Alexander Baumgartner
 */
public interface Initializable {
	/**
	 * Initializes the GameService and/or a part of the game.
	 */
	public void init();

	/**
	 * Indicates if this GameService is already initialized.
	 * 
	 * @return true if the GameService is initialized
	 */
	public boolean isInitialized();
}
