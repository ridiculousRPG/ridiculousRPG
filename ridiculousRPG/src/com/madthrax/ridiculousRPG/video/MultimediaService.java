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

package com.madthrax.ridiculousRPG.video;

import java.net.MalformedURLException;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * This service is capable to play video files.<br>
 * It's a wrapper for the Cortado video player.<br>
 * The following formats are supported:<br>
 * <ul>
 * <li>Ogg Theora</li>
 * <li>Ogg Vorbis</li>
 * <li>Mulaw audio</li>
 * <li>MJPEG</li>
 * <li>Smoke codec</li>
 * </ul>
 * 
 * @see http://www.theora.org/cortado/
 * @author Alexander Baumgartner
 */
public class MultimediaService extends GameServiceDefaultImpl implements
		Drawable {
	private CortadoPlayerAppletWrapper player;
	private boolean playing;
	private boolean projectToMap;
	private boolean loop;
	private float playTime;
	private float position;

	/**
	 * Milliseconds to estimate if the stream has stopped.<br>
	 * Default = 1000
	 */
	public static long EOS_TIMEOUT_MILLIS = 1000;

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param file
	 *            The ogg theora video file
	 */
	public void play(FileHandle file) {
		play(file, new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics
				.getHeight()), false, true, true, -1f, false);
	}

	/**
	 * Plays the (ogg-theora) video in embedded mode.<br>
	 * The video is embedded into the games world. The game is not stopped. The
	 * video integrates smoothly into the game.
	 * 
	 * @param file
	 *            The ogg theora video file
	 * @param bounds
	 *            The position, width and height for embedding the video into
	 *            the games world coordinates
	 */
	public void play(FileHandle file, Rectangle bounds) {
		play(file, bounds, true, true, false, -1f, true);
	}

	/**
	 * Plays the (ogg-theora) video in embedded mode.<br>
	 * The video is embedded into the games world. The game is not stopped. The
	 * video integrates smoothly into the game.
	 * 
	 * @param file
	 *            The ogg theora video file
	 * @param bounds
	 *            The position, width and height for embedding the video into
	 *            the games world coordinates
	 * @param playTime
	 *            If > -1, the video will stop at the given time (Seconds).<br>
	 *            Use -1 to play the entire video
	 * @param loop
	 *            If true, the playback will loop forever (until stop() is
	 *            called).
	 */
	public void play(FileHandle file, Rectangle bounds, float playTime,
			boolean loop) {
		play(file, bounds, true, true, false, playTime, loop);
	}

	/**
	 * Plays an ogg theora video
	 * 
	 * @param file
	 *            The file for streaming the ogg theora video
	 * @param bounds
	 *            The position, width and height for embedding the video
	 * @param projectToMap
	 *            Defines whether to project the video onto the map or onto the
	 *            screen coordinates
	 * @param withAudio
	 *            Defines if the audio output should be muted or not.
	 * @param freezeTheWorld
	 *            If true, the game will be frozen.
	 * @param playTime
	 *            If > -1, the video will stop at the given time (Seconds).<br>
	 *            Use -1 to play the entire video
	 * @param loop
	 *            If true, the playback will loop forever (until stop() is
	 *            called).
	 */
	public void play(FileHandle file, Rectangle bounds, boolean projectToMap,
			boolean withAudio, boolean freezeTheWorld, float playTime,
			boolean loop) {
		try {
			play(file.file().toURI().toURL(), bounds, projectToMap, withAudio,
					freezeTheWorld, playTime, loop);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Streams an ogg theora video
	 * 
	 * @param url
	 *            The url for streaming the ogg theora video
	 * @param bounds
	 *            The position, width and height for embedding the video
	 * @param projectToMap
	 *            Defines whether to project the video onto the map or onto the
	 *            screen coordinates
	 * @param withAudio
	 *            Defines if the audio output should be muted or not.
	 * @param freezeTheWorld
	 *            If true, the game will be frozen.
	 * @param playTime
	 *            If > -1, the video will stop at the given time (Seconds).<br>
	 *            Use -1 to play the entire video
	 * @param loop
	 *            If true, the playback will loop forever (until stop() is
	 *            called).
	 */
	public void play(URL url, Rectangle bounds, boolean projectToMap,
			boolean withAudio, boolean freezeTheWorld, float playTime,
			boolean loop) {
		if (!freezeTheWorld
				|| GameBase.$serviceProvider().requestAttention(this, true,
						true)) {
			if (playing)
				player.dispose();
			player = new CortadoPlayerAppletWrapper(url, bounds, projectToMap,
					withAudio);
			player.play();
			this.playing = true;
			this.loop = loop;
			this.playTime = playTime;
			this.position = 0;
			this.projectToMap = projectToMap;
		}
	}

	/**
	 * Stops the video playback
	 */
	public void stop() {
		if (player != null)
			player.stop();
	}

	@Override
	public void dispose() {
		if (player != null)
			player.dispose();
		player = null;
	}

	@Override
	public void freeze() {
		super.freeze();
		if (playing)
			player.pause();
	}

	@Override
	public void unfreeze() {
		super.unfreeze();
		if (playing)
			player.play();
	}

	@Override
	public boolean essential() {
		return false;
	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		if (!playing)
			return;
		if (player.isPlaying()) {
			player.draw(spriteBatch, debug);
			if (playTime > -1) {
				if (!frozen) {
					position += Gdx.graphics.getDeltaTime();
				}
				if (position > playTime) {
					player.stop();
				}
			} else if (player.estimateEOS(EOS_TIMEOUT_MILLIS)) {
				player.stop();
			}
		} else if (loop) {
			position = 0;
			player.play();
			player.draw(spriteBatch, debug);
		} else {
			GameBase.$serviceProvider().releaseAttention(this);
			playing = false;
			player.dispose();
			player = null;
		}
	}

	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return projectToMap ? camera.projection : camera.view;
	}
}
