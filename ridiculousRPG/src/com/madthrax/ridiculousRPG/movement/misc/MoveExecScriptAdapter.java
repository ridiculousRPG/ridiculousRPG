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

package com.madthrax.ridiculousRPG.movement.misc;

import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter allows you to execute some script code.<br>
 * Feel free to do whatever you want;)<br>
 * This move cannot be blocked.<br>
 * You may use this {@link MovementHandler} stand alone (without an event).
 * 
 * @author Alexander Baumgartner
 */
public class MoveExecScriptAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private String script;

	protected MoveExecScriptAdapter(String script) {
	}

	/**
	 * This MovementAdapter allows you to execute some script code.<br>
	 * Feel free to do whatever you want;)<br>
	 * This move cannot be blocked.
	 * 
	 * @param script
	 *            The script code to execute.
	 */
	public static MovementHandler $(String script) {
		return new MoveExecScriptAdapter(script);
	}

	@Override
	public void tryMove(Movable movable, float deltaTime) {
		try {
			GameBase.$().eval(script);
		} catch (Exception e) {
			GameBase.$error("MoveExecScriptAdapter.eval",
					"Could not evaluate move-script for " + movable, e);
		}
		finished = true;
	}
}
