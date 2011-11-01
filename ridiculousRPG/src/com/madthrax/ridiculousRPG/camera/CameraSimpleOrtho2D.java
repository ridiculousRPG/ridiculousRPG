package com.madthrax.ridiculousRPG.camera;

import com.badlogic.gdx.graphics.Camera;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Drawable;
/**
 * Only the projection and view matrices are computed.
 * We don't need to compute all the other stuff.<br>
 * Therefore (per default implementation) only the matrices
 * view and projection are useful for drawing.
 * @see Drawable#projectionMatrix(Camera)
 */
public class CameraSimpleOrtho2D extends Camera {
	float x,y;
	@Override
	public void translate(float x, float y, float z) {
		this.x+=x;
		this.y+=y;
		position.x=adjustX();
		position.y=adjustY();
	}
	@Override
	public void lookAt(float x, float y, float z) {
		this.x=x-viewportWidth*.5f;
		this.y=y-viewportHeight*.5f;
		position.x=adjustX();
		position.y=adjustY();
	}
	@Override
	public void update() {
		view.setToOrtho2D(0, 0, GameBase.screenWidth, GameBase.screenHeight);
		projection.setToOrtho2D(position.x, position.y, viewportWidth, viewportHeight);
	}
	private float adjustX() {
		// keep view inside the plane
		float planeWidth = GameBase.planeWidth;
		float x = this.x;
		if (viewportWidth >= planeWidth) {
			x = (planeWidth-viewportWidth)/2;
		} else if (x < 0) {
			x = 0;
		} else if (x > planeWidth-viewportWidth) {
			x = planeWidth-viewportWidth;
		}
		return x;
	}
	private float adjustY() {
		float planeHeight = GameBase.planeHeight;
		float y = this.y;
		if (viewportHeight >= planeHeight) {
			y = (planeHeight-viewportHeight)/2;
		} else if (y < 0) {
			y = 0;
		} else if (y > planeHeight-viewportHeight) {
			y = planeHeight-viewportHeight;
		}
		return y;
	}
}
