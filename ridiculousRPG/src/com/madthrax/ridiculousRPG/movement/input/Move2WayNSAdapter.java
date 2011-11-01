package com.madthrax.ridiculousRPG.movement.input;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.events.Direction;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

public class Move2WayNSAdapter extends MovementHandler {
	private static MovementHandler instance = new Move2WayNSAdapter();

	protected Move2WayNSAdapter(){}
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
			if ((lastDirKey = MovementKeys.isUpKeyPressed()) != 0) {
				lastDir = Direction.N;
				movable.offerMove(lastDir, deltaTime);
			} else if ((lastDirKey = MovementKeys.isDownKeyPressed()) != 0) {
				lastDir = Direction.S;
				movable.offerMove(lastDir, deltaTime);
			} else {
				movable.stop();
			}
		}
	}
	private Direction computeDirection(int absolutX, int absolutY, Movable movable) {
		float y = movable.computeRelativY(absolutY);
		return (y>0 ? Direction.N : Direction.S);
	}
	@Override
	public void reset() {
		super.reset();
		lastDirKey = 0;
	}
}
