package com.ridiculousRPG.video;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Videoplayer {

	/**
	 * Starts the video (and audio) playback
	 */
	public abstract void play();

	public abstract boolean isPlaying();

	/**
	 * Stops the video (and audio) playback
	 */
	public abstract void stop();

	/**
	 * Pauses or resumes the playback
	 */
	public abstract void pause();

	public abstract void draw(SpriteBatch spriteBatch, boolean debug);

	public abstract boolean isSignalReceived();

	public abstract void dispose();

	/**
	 * Estimates if the end of the stream is reached
	 * 
	 * @param timeout
	 *            Timeout in milliseconds to switch into EOS state
	 * @return
	 */
	public boolean estimateEOS(long timeout);

}