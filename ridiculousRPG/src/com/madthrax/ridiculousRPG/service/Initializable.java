package com.madthrax.ridiculousRPG.service;

/**
 * The initialization is automatically performed for all {@link GameService}s.<br>
 * The {@link #init} method is called by adding the GameService
 * to the GameServiceProvider if the method {@link #isInitialized} returns false.<br>
 * The initialization waits for global game initialization to finish.
 * You are welcome to add {@link Initializable} to the GameServiceProvider before
 * starting the game.
 */
public interface Initializable {
	/**
	 * Initializes the GameService and/or a part of the game.
	 */
	public void init();
	/**
	 * Indicates if this GameService is already initialized.
	 * @return true if the GameService is initialized
	 */
	public boolean isInitialized();
}
