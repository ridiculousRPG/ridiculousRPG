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

package com.ridiculousRPG.movement;

import java.io.Serializable;

/**
 * All possible MovementAdapters must extend this class.<br>
 * All MovementAdapters without any status-information should be implement a
 * static $(...) - method to obtain a singleton object.
 * 
 * @author Alexander Baumgartner
 */
// TODO: Maybe: Derive from com.badlogic.gdx.scenes.scene2d.Action
public abstract class MovementHandler implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * If this movement can finish (e.g. move to coordinate x,y), the
	 * implementation should set this switch.
	 */
	public boolean finished = false;

	/**
	 * This method moves the event but does not commit the move.<br>
	 * The move is computed but not performed.
	 * 
	 * @param event
	 * @param deltaTime
	 */
	public abstract void tryMove(Movable event, float deltaTime);

	/**
	 * Resets the state of this MovementAdapter. The default implementation sets
	 * the finished state to false.
	 */
	public void reset() {
		finished = false;
	}

	/**
	 * If the move handler is pooled it can be freed (returned to the pool) by
	 * overriding this method.
	 * @see Movable Movable for example
	 */
	public void free() {
	}

	/**
	 * This method is called if the game is in idle state. E.g. it's paused or
	 * the main-menu is open...<br>
	 * (Empty default implementation)
	 */
	public void freeze() {
	}

	/**
	 * This method is called if the move couldn't be performed. The move has
	 * been canceled instead of commited.<br>
	 * (Empty default implementation)
	 */
	public void moveBlocked(Movable event) {
	}
}
