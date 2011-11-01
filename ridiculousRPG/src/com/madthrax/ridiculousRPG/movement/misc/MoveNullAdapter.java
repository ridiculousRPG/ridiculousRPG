package com.madthrax.ridiculousRPG.movement.misc;

import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter never finishes doing nothing.
 * It loops in idle state forever.
 */
public class MoveNullAdapter extends MovementHandler {
	private static MovementHandler instance = new MoveNullAdapter();

	private MoveNullAdapter(){}
	/**
	 * This MovementAdapter never finishes doing nothing.
	 * It loops in idle state forever.
	 */
	public static MovementHandler $() {
		return instance;
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		if (event!=null) event.stop();
	}
}
