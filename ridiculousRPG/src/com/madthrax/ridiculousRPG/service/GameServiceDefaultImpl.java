package com.madthrax.ridiculousRPG.service;

public abstract class GameServiceDefaultImpl implements GameService {

	/**
	 * This method is called if an other service freezes the world.
	 * Normally you don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some freezing actions
	 * like stopping the background music.
	 */
	public void freeze(){}
	/**
	 * This method is called if an other service releases its attention from a frozen world.
	 * It's the counterpart of the freeze() method.<br>
	 * Normally you don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some unfreezing actions
	 * like restarting the background music.
	 */
	public void unfreeze(){}
}
