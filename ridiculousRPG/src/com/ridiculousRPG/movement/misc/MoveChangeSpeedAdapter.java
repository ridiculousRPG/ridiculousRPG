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

import com.ridiculousRPG.animation.TileAnimation;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.Speed;

/**
 * This MovementAdapter allows you to change the move speed and the animation
 * speed.<br>
 * This move cannot be blocked.
 * 
 * @author Alexander Baumgartner
 */
public class MoveChangeSpeedAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private Speed newMoveSpeed, newAnimationSpeed;

	/**
	 * If the animationSpeed is set to null, it will automatically be computed
	 * from the move-distance. (You can always use null if you don't want to
	 * worry about the characters walk and run - animations)
	 */
	public MoveChangeSpeedAdapter(Speed newMoveSpeed, Speed newAnimationSpeed) {
		this.newMoveSpeed = newMoveSpeed;
		this.newAnimationSpeed = newAnimationSpeed;
	}

	@Override
	public void tryMove(Movable movable, float deltaTime,
			EventTrigger eventTrigger) {
		if (newMoveSpeed != null)
			movable.setMoveSpeed(newMoveSpeed);
		if (movable instanceof EventObject) {
			TileAnimation anim = ((EventObject) movable).getAnimation();
			if (anim != null)
				anim.animationSpeed = newAnimationSpeed;
		}
		finished = true;
	}
}
