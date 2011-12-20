package com.madthrax.ridiculousRPG.animation;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParticleEffectLayer extends EffectLayer {
	ParticleEffect effect = new ParticleEffect();
	private boolean started;
	private float deltaTime;

	public ParticleEffectLayer(FileHandle effectFile, FileHandle imagesDir) {
		effect.load(effectFile, imagesDir);
	}

	@Override
	public boolean isFinished() {
		return effect.isComplete();
	}

	@Override
	public void stop() {
		setStopRequested(true);
		effect.allowCompletion();
	}

	@Override
	public void dispose() {
		effect.dispose();
	}

	@Override
	public void compute(float deltaTime, boolean actionKeyDown) {
		if (!started) effect.start();
		this.deltaTime = deltaTime;
	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		effect.draw(spriteBatch, deltaTime);
	}

	@Override
	public void resize(int width, int height) {
		// No scissor is implemented in libgdx ParticleEffect.
		// Therefore no resizing is needed / possible.
	}
}
