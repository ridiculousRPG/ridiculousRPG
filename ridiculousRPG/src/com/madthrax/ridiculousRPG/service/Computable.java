package com.madthrax.ridiculousRPG.service;

import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;

/**
 * The {@link #compute} method is automatically called for all {@link GameService}s which are added
 * to the {@link GameServiceProvider}.<br>
 * It's guaranteed that the {@link #compute} method of one {@link GameService} is called before
 * all provided {@link Drawable#draw} methods. 
 */
public interface Computable {
	/**
	 * Computes one loop iteration by the given deltaTime<br>
	 * The {@link #compute} method is automatically called for all {@link GameService}s which are added
	 * to the {@link GameServiceProvider}.<br>
	 * @param deltaTime
	 * time elapsed since last call (in seconds)
	 * @param actionKeyPressed
	 * indicates if the player wants an action to be performed
	 * @see GameBase#isActionKeyPressed()
	 */
	public void compute(float deltaTime, boolean actionKeyPressed);
}
