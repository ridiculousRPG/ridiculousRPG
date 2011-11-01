package com.madthrax.ridiculousRPG.movement.auto;

import java.util.Random;

import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * With this {@link MovementHandler} you can add random moves to events.
 */
public class MoveRandomAdapter extends MovementHandler {

	protected static final Random randomNumberGenerator = new Random();
	protected int changeDirectionSlackness;
	protected Direction[] allowedDirections;

	protected Direction lastDir;
	protected float minWidth;

	protected MoveRandomAdapter(Direction[] allowedDirections, int changeDirectionSlackness){
		this.allowedDirections = allowedDirections;
		this.changeDirectionSlackness = Math.max(allowedDirections.length, changeDirectionSlackness);
	}

	/**
	 * This constructor uses a default slackness-value of 128 for direction-changing.<br>
	 * Defaultdirections are N, E, S and W<br>
	 */
	public static MovementHandler $() {
		return $(128);
	}
	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes.
	 * Defaultdirections are N, E, S and W<br>
	 * @param changeDirectionSlackness
	 */
	public static MovementHandler $(int changeDirectionSlackness) {
		return $(new Direction[] { Direction.N, Direction.E,
				Direction.S, Direction.W }, changeDirectionSlackness);
	}
	/**
	 * Use this constructor if you want an other slackness.<br>
	 * The higher the value, the lesser the direction changes.<br>
	 * Which directions should be allowed. Defaultdirections are N, E, S and W
	 * @param allowedDirections
	 * @param changeDirectionSlackness
	 */
	public static MovementHandler $(Direction[] allowedDirections, int changeDirectionSlackness) {
		return new MoveRandomAdapter(allowedDirections, changeDirectionSlackness);
	}

	@Override
	public void moveBlocked(Movable event) {
		lastDir = null;
	}
	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (lastDir==null || minWidth < 0) {
			int randNum = randomNumberGenerator.nextInt(changeDirectionSlackness);
			if (lastDir==null) randNum %= allowedDirections.length;
			if (randNum < allowedDirections.length) {
				lastDir = allowedDirections[randNum];
				minWidth = changeDirectionSlackness/2;
			} else {
				minWidth = changeDirectionSlackness/3;
			}
		}
		minWidth -= event.offerMove(lastDir, deltaTime);
	}
	@Override
	public void reset() {
		super.reset();
		lastDir = null;
		minWidth = 0;
	}
}
