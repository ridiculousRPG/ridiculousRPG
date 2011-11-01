package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * Displays a red error message on the screen.
 */
public class DisplayErrorService extends DisplayTextService implements Computable {
	private float displayTime = 10;
	private String msg;
	/**
	 * Displays the specified error centered on the screen.<br>
	 * The message is public and may be changed. 
	 * @param msg
	 * The message to display or null if no message should be drawn.
	 */
	public DisplayErrorService(String msg) {
		this.msg = msg;
	}
	@Override
	public void compute(float deltaTime, boolean actionKeyPressed) {
		String msg = this.msg;
		displayTime-=deltaTime;
		if (displayTime < 0) {
			displayTime = 0;
			msg = "\nPress the action key to exit the game!\n\n" + msg;
			if (actionKeyPressed) Gdx.app.exit();
		} else {
			msg = "\nERROR [" + ((int)(displayTime + 1f)) + "]\n\n" + msg;
		}
		message(msg, Color.RED.toFloatBits(), Alignment.CENTER, Alignment.CENTER, 0f);
	}
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
