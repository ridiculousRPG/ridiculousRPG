package com.madthrax.ridiculousRPG.util;

import com.badlogic.gdx.Gdx;
import com.madthrax.ridiculousRPG.GameBase;

public abstract class ExecuteInMainThread implements Runnable {

	/**
	 * The run method is called by the main thread.
	 */
	@Override
	public void run() {
		exec();
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
			exec();
		} else {
			synchronized (this) {
				Gdx.app.postRunnable(this);
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Implement the code to execute here.
	 */
	public abstract void exec();
}
