package com.madthrax.ridiculousRPG.movement.input;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

public class Move2WayWEAdapter extends MovementHandler {
	private static MovementHandler instance = new Move2WayWEAdapter();

	protected Move2WayWEAdapter(){}
	public static MovementHandler $() {
		return instance;
	}

	private int lastDirKey;
	private Direction lastDir;

	@Override
	public void freeze() {lastDirKey = 0;}
	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (lastDirKey != 0) {
			if (!Gdx.input.isKeyPressed(lastDirKey)) {
				lastDirKey = 0;
			}
			movable.offerMove(lastDir, deltaTime);
		} else if (Gdx.input.isTouched(0)) {
			Direction touchDir = computeDirection(Gdx.input.getX(0), Gdx.input.getY(0), movable);
			movable.offerMove(touchDir, deltaTime);
		} else {
			if ((lastDirKey = MovementKeys.isLeftKeyPressed()) != 0) {
				lastDir = Direction.W;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey = MovementKeys.isRightKeyPressed()) != 0) {
				lastDir = Direction.E;
				movable.offerMove(lastDir, deltaTime);
			} else {
				movable.stop();
			}
		}
	}
	private Direction computeDirection(int absolutX, int absolutY, Movable movable) {
		float x = movable.computeRelativX(absolutX);
		return (x>0 ? Direction.E : Direction.W);
	}
	@Override
	public void reset() {
		super.reset();
		lastDirKey = 0;
	}
}
