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

import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.audio.JukeboxService;

/**
 * Interface for all services
 * 
 * @see GameServiceDefaultImpl
 * @author Alexander Baumgartner
 */
public interface GameService extends Disposable {

	/**
	 * This method is called if an other service freezes the world. Normally you
	 * don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some freezing actions
	 * like stopping the background music (see {@link JukeboxService}).
	 */
	public void freeze();

	/**
	 * This method is called if an other service releases its attention from a
	 * frozen world. It's the counterpart of the freeze() method.<br>
	 * Normally you don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some unfreezing
	 * actions like restarting the background music (see {@link JukeboxService}
	 * ).
	 */
	public void unfreeze();

	/**
	 * An essential {@link GameService} cannot be freezed. It will always run no
	 * matter if an other {@link GameService} has frozen the world.
	 */
	public boolean essential();
}
