/*
 * Copyright 2011 Alexander Baumgartner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ridiculousRPG.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.GameBase;

/**
 * Executes a piece of code within the main thread at a safe position.
 * 
 * @see ExecWithGlContext
 * @author Alexander Baumgartner
 */
public abstract class ExecInMainThread implements Runnable, Disposable {
	private Object syncObj = new Object();

	/**
	 * The run method is called by the main thread.
	 */
	@Override
	public void run() {
		execCatchException();
		synchronized (syncObj) {
			syncObj.notify();
		}
	}

	/**
	 * Posts this runnable and waits until {@link #exec()} has finished it's
	 * execution inside the main thread.
	 */
	public void runWait() {
		if (GameBase.$().isMainThread()) {
			execCatchException();
		} else {
			synchronized (syncObj) {
				Gdx.app.postRunnable(this);
				try {
					syncObj.wait();
				} catch (InterruptedException e) {
					GameBase.$info("ExecWithGlContext.interrupt",
							"Wait interrupted - continuing", e);
				}
			}
		}
	}

	private void execCatchException() {
		try {
			exec();
		} catch (Exception e) {
			GameBase.$error("ExecWithGlContext.exec",
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

	@Override
	public void dispose() {
		synchronized (syncObj) {
			syncObj.notifyAll();
		}
	}
}
