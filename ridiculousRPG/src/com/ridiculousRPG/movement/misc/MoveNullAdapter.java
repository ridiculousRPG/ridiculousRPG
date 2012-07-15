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

package com.ridiculousRPG.movement.misc;

import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter never finishes doing nothing. It loops in idle state
 * forever.
 * 
 * @author Alexander Baumgartner
 */
public class MoveNullAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private static MovementHandler instance = new MoveNullAdapter();

	/**
	 * This MovementAdapter never finishes doing nothing. It loops in idle state
	 * forever. Use singleton {@link #$()} - it's stateless.
	 */
	public MoveNullAdapter() {
	}

	/**
	 * This MovementAdapter never finishes doing nothing. It loops in idle state
	 * forever.
	 */
	public static MovementHandler $() {
		return instance;
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (event != null)
			event.stop();
	}
}
