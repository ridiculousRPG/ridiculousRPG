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

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.event.Speed;
import com.madthrax.ridiculousRPG.movement.CombinedMovesAdapter;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter doesn't move the event. It fades the color until the
 * color matches an other (given) color by the given speed.<br>
 * Simply use the alpha channel to fade in/out an event.<br>
 * It will finish after the color transition completed.<br>
 * You may use this {@link MovementHandler} stand alone without an event (see
 * {@link #tryMove(Movable, float)}). Also you are allowed to concatenate
 * {@link MoveFadeColorAdapter}s inside an {@link CombinedMovesAdapter} and use
 * this concatenation stand alone. Maybe in an day/night service.
 * 
 * @author Alexander Baumgartner
 */
public class MoveFadeColorAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private Speed transitionSpeed;
	private SerializableColor to;
	private boolean tintEntireGame;

	protected MoveFadeColorAdapter(Speed transitionSpeed, Color to,
			boolean tintEntireGame) {
		this.transitionSpeed = transitionSpeed;
		this.to = new SerializableColor(to);
		this.tintEntireGame = tintEntireGame;
	}

	/**
	 * This MovementAdapter doesn't move the event. It fades the color until the
	 * color matches an other (given) color by the given speed.<br>
	 * It will finish after the color transition completed.
	 * 
	 * @param transitionSpeed
	 *            The speed for fading the events color to the given color or
	 *            null if it should jump immediately to the given color.
	 * @param toColor
	 *            The end color after the transition.
	 * @param tintEntireGame
	 *            If true, the tint will be applied to the entire game.<br>
	 *            If you use the alpha channel on the entire game, all layers of
	 *            a map will become visible. That's probably not what you want.<br>
	 *            If you want to fade the entire game out, make a transition to
	 *            {@link Color#BLACK}. If you want to fade the entire game in
	 *            make a transition from {@link Color#BLACK} to
	 *            {@link Color#WHITE}.
	 * @see {@link GameBase#setGameColorTint(Color)}
	 * @see {@link GameBase#getGameColorTint()}
	 * @return
	 */
	public static MovementHandler $(Speed transitionSpeed, Color toColor,
			boolean tintEntireGame) {
		return new MoveFadeColorAdapter(transitionSpeed, toColor,
				tintEntireGame);
	}

	/**
	 * You may use this {@link MovementHandler} to fade the entire game. If so,
	 * the parameter movable will not be needed and may be null.
	 */
	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (!finished && (tintEntireGame || movable instanceof EventObject)) {
			float changeSpeed = transitionSpeed == null ? 1f : transitionSpeed
					.computeStretch(deltaTime) * .1f;
			Color from;
			if (tintEntireGame) {
				from = GameBase.$().getGameColorTint();
			} else {
				from = ((EventObject) movable).getColor();
			}
			Color to = this.to;
			finished = true; // reset by transition(...) if not finished
			from.r = transition(from.r, to.r, changeSpeed);
			from.g = transition(from.g, to.g, changeSpeed);
			from.b = transition(from.b, to.b, changeSpeed);
			from.a = transition(from.a, to.a, changeSpeed);
			if (tintEntireGame) {
				GameBase.$().setGameColorTint(from);
			} else {
				((EventObject) movable).setColor(from);
			}
		} else {
			finished = true;
		}
	}

	private float transition(float from, float to, float changeSpeed) {
		if (from < to) {
			from += changeSpeed;
			if (from < to) {
				finished = false;
				return from;
			}
		} else if (from > to) {
			from -= changeSpeed;
			if (from > to) {
				finished = false;
				return from;
			}
		}
		return to;
	}
	public static class SerializableColor extends Color implements Serializable {
		private static final long serialVersionUID = 1L;

		public SerializableColor() {
			super();
		}

		public SerializableColor(Color color) {
			super(color);
		}

		public SerializableColor(float r, float g, float b, float a) {
			super(r, g, b, a);
		}
	}
}
