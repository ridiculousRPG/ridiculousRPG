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

package com.madthrax.ridiculousRPG.movement.auto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;
import com.madthrax.ridiculousRPG.util.Direction;

/**
 * With this {@link MovementHandler} you are able to let one event move towards
 * an other one, or run away from the other event. One event is attracted (or
 * pushed away) by the other event.<br>
 * The strength of the attraction is defined by the inner enumeration
 * Attraction.<br>
 * Some parameters are equivalent to the parameters of the class
 * {@link MoveRandomAdapter}.
 * 
 * @see {@link MoveRandomAdapter}
 * @author Alexander Baumgartner
 */
public class MoveMagneticAdapter extends MoveRandomAdapter {
	private static final long serialVersionUID = 1L;


	public enum Attraction {
		NONE(0), LOW(150), MEDIUM(300), STRONG(500), MAXIMUM(900);
		private int val;

		Attraction(int val) {
			this.val = val;
		}
	}

	private Movable attractingEvent;
	private int attractionRadius;
	private Attraction attraction;
	private boolean repulsive;

	private ArrayList<Direction> attractingDirections = new ArrayList<Direction>();
	private Direction alternateDir;

	/**
	 * If you extend this MovementHandler, you are able to create an inner class
	 * which extends {@link MagneticEffect} and set your own (more intelligent)
	 * MagneticEffect behavior for global use.
	 */
	protected static MagneticEffect globalMagneticEffect = new MagneticEffect();
	private MagneticEffect magneticEffect = globalMagneticEffect;

	protected MoveMagneticAdapter(Direction[] allowedDirections,
			int changeDirectionSlackness, Movable attractingEvent,
			int attractionRadius, Attraction attraction, boolean repulsive) {
		super(allowedDirections, changeDirectionSlackness);
		this.attractingEvent = attractingEvent;
		this.attractionRadius = attractionRadius;
		this.attraction = attraction;
		this.repulsive = repulsive;
		if (attraction == null)
			attraction = Attraction.NONE;
	}

	/**
	 * This constructor uses a default slackness-value of 128 for
	 * direction-changing.<br>
	 * Defaultdirections are N, E, S and W<br>
	 */
	public static MovementHandler $(Movable attractingEvent,
			Attraction attraction) {
		return $(attractingEvent, attraction, false);
	}

	/**
	 * This constructor uses a default slackness-value of 128 for
	 * direction-changing.<br>
	 * Defaultdirections are N, E, S and W<br>
	 */
	public static MovementHandler $(Movable attractingEvent,
			Attraction attraction, boolean repulsive) {
		return $(128, attractingEvent, attraction, repulsive);
	}

	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes. Defaultdirections
	 * are N, E, S and W<br>
	 * 
	 * @param changeDirectionSlackness
	 */
	public static MovementHandler $(int changeDirectionSlackness,
			Movable attractingEvent, Attraction attraction, boolean repulsive) {
		return $(new Direction[] { Direction.N, Direction.E, Direction.S,
				Direction.W }, changeDirectionSlackness, attractingEvent,
				attraction.val, attraction, repulsive);
	}

	/**
	 * Which directions should be allowed.<br>
	 * This constructor uses a default slackness-value of 128 for
	 * direction-changing.<br>
	 * 
	 * @param allowedDirections
	 */
	public static MovementHandler $(Direction[] allowedDirections,
			Movable attractingEvent, Attraction attraction, boolean repulsive) {
		return $(allowedDirections, 128, attractingEvent, attraction.val,
				attraction, repulsive);
	}

	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes.<br>
	 * Which directions should be allowed. Defaultdirections are N, E, S and W
	 * 
	 * @param allowedDirections
	 * @param changeDirectionSlackness
	 */
	public static MovementHandler $(Direction[] allowedDirections,
			int changeDirectionSlackness, Movable attractingEvent,
			int attractionRadius, Attraction attraction, boolean repulsive) {
		return new MoveMagneticAdapter(allowedDirections,
				changeDirectionSlackness, attractingEvent, attractionRadius,
				attraction, repulsive);
	}

	/**
	 * Disables all magnetic behaviors for all {@link MoveMagneticAdapter}s. Use
	 * {@link #enableAllMagneticBehaviors()} to enable all magnetic effect.
	 */
	public static void disableAllMagneticBehaviors() {
		globalMagneticEffect.enabled = false;
	}

	/**
	 * Enables all magnetic behaviors which was disabled by
	 * {@link #disableAllMagneticBehaviors()}.<br>
	 * Doesn't enable magnetic behavior which was disabled by
	 * {@link #disableMagneticBehavior()}.
	 */
	public static void enableAllMagneticBehaviors() {
		globalMagneticEffect.enabled = true;
	}

	/**
	 * Disables the magnetic behavior for this {@link MovementHandler}.<br>
	 * Use {@link #enableMagneticBehavior()} to enable the magnetic effect.
	 */
	public void disableMagneticBehavior() {
		magneticEffect = null;
	}

	/**
	 * Enables the magnetic behavior if it was disabled by
	 * {@link #disableMagneticBehavior()}.<br>
	 * Doesn't enable magnetic behavior which was disabled by
	 * {@link #disableAllMagneticBehaviors()}.
	 */
	public void enableMagneticBehavior() {
		magneticEffect = globalMagneticEffect;
	}

	@Override
	public void moveBlocked(Movable event) {
		if (alternateDir == null) {
			lastDir = null;
		} else {
			lastDir = alternateDir;
			alternateDir = null;
		}
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (magneticEffect == null
				|| !magneticEffect.tryMagneticMove(this, event, deltaTime))
			// fall back to random movement
			super.tryMove(event, deltaTime);
	}

	public static class MagneticEffect implements Serializable {
		private static final long serialVersionUID = 1L;

		boolean enabled = true;

		public boolean tryMagneticMove(MoveMagneticAdapter adapter,
				Movable event, float deltaTime) {
			if (enabled && adapter.lastDir == null || adapter.minWidth < 0) {
				adapter.alternateDir = null;
				Movable attractingEvent = adapter.attractingEvent;
				int attractionRadius = adapter.attractionRadius;
				if (attractingEvent != null
						&& adapter.attraction != Attraction.NONE
						&& attractionRadius > 0) {
					float distX = attractingEvent.getX() - event.getX();
					float distY = attractingEvent.getY() - event.getY();
					if (adapter.repulsive) {
						distX = -distX;
						distY = -distY;
					}
					float distance = (float) Math.sqrt(distX * distX + distY
							* distY);
					if (distance < attractionRadius) {
						float distanceRelativAttraction = 100 - 100f * distance
								/ attractionRadius;
						int totalAttraction = (int) distanceRelativAttraction
								+ adapter.attraction.val;
						// random: min=150 max=1000
						int randNum = randomNumberGenerator
								.nextInt(totalAttraction);
						if (randNum > 100) {
							computeAttractingDirections(adapter, distX, distY);
							List<Direction> attractingDirections = adapter.attractingDirections;
							if (randNum < 200)
								adapter.alternateDir = null;
							adapter.lastDir = attractingDirections.get(randNum
									% attractingDirections.size());
							adapter.minWidth = (adapter.changeDirectionSlackness + 1000 - randNum) / 33;
							event.offerMove(adapter.lastDir, deltaTime);
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * Computes the attracting directions and sets an alternative direction
		 * if the move is blocked.<br>
		 * Override this method if you want a more intelligent behaviour.
		 * 
		 * @param distX
		 * @param distY
		 */
		protected void computeAttractingDirections(MoveMagneticAdapter adapter,
				float distX, float distY) {
			List<Direction> attractingDirections = adapter.attractingDirections;
			attractingDirections.clear();
			float distXX = distX * distX;
			float distYY = distY * distY;
			if (distXX > 10) {
				Direction dirX = Direction.fromMovement(distX, 0);
				if (distYY > 10) {
					Direction dirY = Direction.fromMovement(0, distY);
					if (isAllowed(adapter, dirX)) {
						if (isAllowed(adapter, dirY) && distYY > distXX) {
							attractingDirections.add(dirY);
							adapter.alternateDir = dirX;
						} else {
							attractingDirections.add(dirX);
							if (isAllowed(adapter, dirY))
								adapter.alternateDir = dirY;
						}
					} else if (isAllowed(adapter, dirY)) {
						attractingDirections.add(dirY);
					}
					// 8 way movement
					dirX = Direction.fromMovement(distX, distY);
					if (isAllowed(adapter, dirX))
						attractingDirections.add(dirX);
				} else if (isAllowed(adapter, dirX))
					attractingDirections.add(dirX);
			} else {
				Direction dir = Direction.fromMovement(0, distY);
				if (isAllowed(adapter, dir))
					attractingDirections.add(dir);
			}
		}

		private boolean isAllowed(MoveMagneticAdapter adapter, Direction dir) {
			Direction[] allowedDirections = adapter.allowedDirections;
			for (int i = 0, len = allowedDirections.length; i < len; i++) {
				if (allowedDirections[i] == dir)
					return true;
			}
			return false;
		}
	}
}
