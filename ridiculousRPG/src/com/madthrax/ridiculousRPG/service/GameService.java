package com.madthrax.ridiculousRPG.service;

import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.audio.JukeboxService;

public interface GameService extends Disposable {

	/**
	 * This method is called if an other service freezes the world.
	 * Normally you don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some freezing actions
	 * like stopping the background music (see {@link JukeboxService}).
	 */
	public void freeze();
	/**
	 * This method is called if an other service releases its attention from a frozen world.
	 * It's the counterpart of the freeze() method.<br>
	 * Normally you don't need to do something special inside this method.<br>
	 * But there are some situations where you have to do some unfreezing actions
	 * like restarting the background music (see {@link JukeboxService}).
	 */
	public void unfreeze();
}
