package com.madthrax.ridiculousRPG.animation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParticleEffectLayer extends EffectLayer {
	private static final long serialVersionUID = 1L;

	private transient ParticleEffect effect;
	private String effectFile;
	private String imagesDir;
	private boolean started;
	private float deltaTime;

	/**
	 * Creates a particle effect from a configuration file
	 * 
	 * @param effectFile
	 *            The configuration file (an internal file)
	 * @param imagesDir
	 *            The directory for loading the used images (an internal file)
	 */
	public ParticleEffectLayer(String effectFile, String imagesDir) {
		this.effectFile = effectFile;
		this.imagesDir = imagesDir;
		loadEffect();
	}

	private void loadEffect() {
		effect = new ParticleEffect();
		effect.load(Gdx.files.internal(effectFile), Gdx.files
				.internal(imagesDir));
		if (started)
			effect.start();
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
		if (!started)
			effect.start();
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

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		loadEffect();
	}
}
