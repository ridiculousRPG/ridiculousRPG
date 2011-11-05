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

package com.madthrax.ridiculousRPG.events;

/**
 * <ul>
 * <li>Use FLYING_HIGH or FLYING_LOW for flying objects like birds,
 * airplanes,...</li>
 * <li>Use PASSES_ALL_BARRIERS, PASSES_LOW_BARRIER, PASSES_NO_BARRIER for moving
 * objects on the ground.</li>
 * <li>Use BARRIER_LOW, BARRIER_HIGH for barriers on the ground. With barriers
 * you can lock up random moving objects or the player until she unlocks the
 * barrier.</li>
 * <li>Use BUILDING_LOW and BUILDING_HIGH for your buildings. A FLYING_LOW
 * object can fly over low buildings and a FLYING_HIGH object over high ones.<br>
 * &nbsp;</li>
 * <li>The value ALL blocks all but NONE. With NONE you can move your events out
 * of the drawing-area (you can move out of the map and out of the screen as far
 * as you wish)</li>
 * </ul>
 * A barrier usage example:<br>
 * Let's assume we have a map which is split in two areas. The player can unlock
 * the second area by finishing a task. In both areas we have random moving
 * events. The events from the locked area should never leave their area but the
 * other events may use the entire map.<br>
 * We can solve this by statically using PASSES_LOW_BARRIER for the player and
 * set a (unvisible) BARRIER_HIGH to split the Map. In one area we use
 * PASSES_ALL_BARRIER for our events and in the other area all events get the
 * value PASSES_NO_BARRIER. After the player finishes her task we simply change
 * the barrier to a low one. Now the player can pass the barrier and we don't
 * change any other blocking behaviour.
 * 
 * @author Alexander Baumgartner
 */
public enum BlockingBehaviour {
	NONE(0), FLYING_HIGH(5), FLYING_LOW(15), BARRIER_LOW(35), BARRIER_HIGH(45), PASSES_ALL_BARRIERS(
			50), PASSES_LOW_BARRIER(60), PASSES_NO_BARRIER(70), BUILDING_LOW(80), BUILDING_HIGH(
			90), ALL(99);

	private final int value;

	private BlockingBehaviour(int value) {
		this.value = value;
	}

	public boolean blocks(BlockingBehaviour other) {
		return value + other.value > 99;
	}
}
