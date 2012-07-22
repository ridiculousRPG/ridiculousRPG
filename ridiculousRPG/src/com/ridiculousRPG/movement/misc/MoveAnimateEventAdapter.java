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

import com.badlogic.gdx.utils.Array;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.animation.TileAnimation;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.map.MapRenderService;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.util.Speed;

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
	private static final long serialVersionUID = 1L;

	private EventObject oldEvent;
	private TileAnimation oldAnimation;
	private Speed oldSpeed;
	private TileAnimation animation;
	private int animationTextureRow;

	/**
	 * Simply animates the event and uses it's own animation therefore.
	 */
	public MoveAnimateEventAdapter() {
		this(-1);
	}

	/**
	 * Simply animates the event and uses it's own animation therefore.
	 * 
	 * @param animationTextureRow
	 *            The row index or -1 if the animation should run over all rows.
	 */
	public MoveAnimateEventAdapter(int animationTextureRow) {
		this((TileAnimation) null, animationTextureRow);
	}

	/**
	 * ATTENTION: An animation should always belong to one visible event!!!<br>
	 * 
	 * @param animation
	 *            Named event which's animation should be used
	 * @param animationTextureRow
	 *            The row index or -1 if the animation should run over all rows.
	 */
	public MoveAnimateEventAdapter(String animation, int animationTextureRow) {
		Array<MapRenderService> services = GameBase.$serviceProvider()
				.getServices(MapRenderService.class);
		for (int i = services.size - 1; i >= 0; i--) {
			Object ev = services.get(i).getMap().get(animation);
			if (ev instanceof EventObject) {
				this.animation = ((EventObject) ev).getAnimation();
				this.animationTextureRow = animationTextureRow;
			}
		}
	}

	/**
	 * ATTENTION: The TileAnimation will not be disposed by the
	 * MovementAdapter!!!<br>
	 * You have to dispose the Animation yourself to avoid memory leaks!!!<br>
	 * 
	 * @param animation
	 * @param animationTextureRow
	 *            The row index or -1 if the animation should run over all rows.
	 */
	public MoveAnimateEventAdapter(TileAnimation animation,
			int animationTextureRow) {
		this.animation = animation;
		this.animationTextureRow = animationTextureRow;
	}

	@Override
	public void tryMove(Movable movable, float deltaTime,
			EventTrigger eventTrigger) {
		if (!finished && movable instanceof EventObject) {
			EventObject newEvent = (EventObject) movable;
			if (newEvent != oldEvent) {
				// restore the old animation
				if (oldEvent != null) {
					oldAnimation.animationSpeed = oldSpeed;
					oldEvent.setAnimation(oldAnimation, false, false);
					oldEvent.stop();
				}
				oldEvent = newEvent;
				oldAnimation = newEvent.getAnimation();
				oldSpeed = oldAnimation.animationSpeed;
				if (animation != null) {
					newEvent.setAnimation(animation, false, false);
				}
				if (newEvent.getAnimation().animationSpeed == null) {
					newEvent.getAnimation().animationSpeed = Speed.S07_NORMAL;
				}
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
			oldAnimation.animationSpeed = oldSpeed;
			oldEvent.setAnimation(oldAnimation, false, false);
			oldEvent.stop();
		}
		oldEvent = null;
		oldAnimation = null;
	}
}
