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

import com.madthrax.ridiculousRPG.animations.TileAnimation;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter doesn't move the event but animates it.<br>
 * <br>
 * Tip: If you need one animation frequently, build a loop with the
 * CombinedMovementAdapter.<br>
 * If so, you can start the animation by simply setting the finished-state for
 * the other MovementAdapter.<br>
 * After the animation finished, the other MovementAdapter will automatically
 * regain control.
 * 
 * @author Alexander Baumgartner
 */
public class MoveAnimateEventAdapter extends MovementHandler {
	private EventObject oldEvent;
	private TileAnimation oldAnimation;
	private TileAnimation animation;
	private int animationTextureRow;

	protected MoveAnimateEventAdapter(TileAnimation animation,
			int animationTextureRow) {
		this.animation = animation;
		this.animationTextureRow = animationTextureRow;
	}

	/**
	 * ATTENTION: The TileAnimation will not be disposed by the
	 * MovementAdapter!!!<br>
	 * You have to dispose the Animation yourself to avoid memory leaks!!!<br>
	 * The used {@link TileAnimation} needs an
	 * {@link TileAnimation#animationSpeed}, because the animationSpeed is
	 * <code>null</code> per default.
	 * 
	 * @param animation
	 * @param animationTextureRow
	 *            The row index or -1 if the animation should run over all rows.
	 * @return
	 */
	public static MovementHandler $(TileAnimation animation,
			int animationTextureRow) {
		return new MoveAnimateEventAdapter(animation, animationTextureRow);
	}

	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (!finished && movable instanceof EventObject) {
			EventObject newEvent = (EventObject) movable;
			if (newEvent != oldEvent) {
				// restore the old animation
				if (oldEvent != null) {
					oldEvent.setAnimation(oldAnimation, false);
					oldEvent.stop();
				}
				oldEvent = newEvent;
				oldAnimation = newEvent.getAnimation();
				newEvent.setAnimation(animation, false);
			}
			newEvent.animate(animationTextureRow, null, deltaTime);
			if (newEvent.getAnimation().animationCycleFinished) {
				finished = true;
			}
		} else {
			finished = true;
		}
	}

	@Override
	public void reset() {
		super.reset();
		// restore the old animation
		if (oldEvent != null) {
			oldEvent.setAnimation(oldAnimation, false);
			oldEvent.stop();
		}
		oldEvent = null;
		oldAnimation = null;
	}
}
