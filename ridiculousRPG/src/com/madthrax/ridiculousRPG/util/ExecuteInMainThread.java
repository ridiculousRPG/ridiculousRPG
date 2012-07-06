package com.madthrax.ridiculousRPG.util;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.GameBase;

public abstract class ExecuteInMainThread implements Runnable {

	/**
	 * The run method is called by the main thread.
	 */
	@Override
	public void run() {
		execCatchException();
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Posts this runnable and waits until {@link #exec()} has finished it's
	 * execution inside the main thread.
	 */
	public void runWait() {
		if (GameBase.$().isGlContextThread()) {
			execCatchException();
		} else {
			synchronized (this) {
				Gdx.app.postRunnable(this);
				try {
					wait();
				} catch (InterruptedException e) {
					GameBase.$info("ExecuteInMainThread.interrupt",
							"Wait interrupted - continuing", e);
				}
			}
		}
	}

	private void execCatchException() {
		try {
			exec();
		} catch (Exception e) {
			GameBase.$error("ExecuteInMainThread.exec",
					"Exception in executed code: " + e.getMessage(), e);
		}
	}

	/**
	 * Implement the code to execute here.<br>
	 * If an {@link Exception} is thrown by the code, it will be
	 * handled(catched) and an error message will be printed onto the screen if
	 * possible.
	 */
	public abstract void exec() throws Exception;
}
