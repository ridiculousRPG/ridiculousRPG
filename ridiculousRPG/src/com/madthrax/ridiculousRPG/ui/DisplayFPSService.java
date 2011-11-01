package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.service.Computable;

/**
 * Displays the frame rate per second at one corner of the screen.
 */
public class DisplayFPSService extends DisplayTextService implements Computable {
	private float colorBits;
	private Alignment horiAlign, vertAlign;
	/**
	 * Displays the rendering speed in frames per second.
	 */
	public DisplayFPSService() {
		this(Color.WHITE, Alignment.LEFT, Alignment.TOP);
	}
	/**
	 * Displays the rendering speed in frames per second.
	 * @param font
	 * The font will automatically be disposed when disposing this service.
	 * @param alignRight
	 * If true the text will be aligned right.
	 * @param valignBottom
	 * If true the text will be displayed at the bottom of the screen.
	 */
	public DisplayFPSService(Color color, Alignment horiAlign, Alignment vertAlign) {
		this.colorBits = color.toFloatBits();
		this.horiAlign = horiAlign;
		this.vertAlign = vertAlign;
	}
	@Override
	public void compute(float deltaTime, boolean actionKeyPressed) {
		message("FPS: "+Gdx.graphics.getFramesPerSecond(), colorBits, horiAlign, vertAlign, 5f);
	}
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
}
