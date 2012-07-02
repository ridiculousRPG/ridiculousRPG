package com.madthrax.ridiculousRPG.animation;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.ResizeListener;

public abstract class EffectLayer implements Disposable, Computable, Drawable,
		ResizeListener, Serializable {
	private static final long serialVersionUID = 1L;

	private boolean stopRequested = false;

	/**
	 * Stops the effect-layer.<br>
	 * Note that the layer is not removed immediately. It takes some time while
	 * the last snow flake falls from the sky to the ground - like in real
	 * nature ;)
	 * 
	 * @see dispose()
	 */
	public abstract void stop();

	/**
	 * Indicates if this layer is ready to be disposed and removed.
	 * 
	 * @return true if the layer is ready to be disposed and removed.
	 */
	public abstract boolean isFinished();

	public boolean isStopRequested() {
		return stopRequested;
	}

	public void setStopRequested(boolean stopRequested) {
		this.stopRequested = stopRequested;
	}

	/**
	 * Normally you don't need to implement this for a layer because the
	 * projection is specified in {@link ParticleEffectService}
	 */
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return GameBase.$().getCamera().projection;
	}
}
