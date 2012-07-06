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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
 * This service is capable to play video files. It's a wrapper for different
 * video players.<br>
 * At the time there exists a compatible implementation for the cortado video
 * player (see ridiculousRPGcortado). The cortado video player supports the
 * following formats:<br>
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
		Drawable, Serializable {
	private static final long serialVersionUID = 1L;

	private VideoplayerFactory playerFactory;
	private transient Videoplayer player;

	private URL url;
	private Rectangle bounds;
	private boolean withAudio;
	private boolean freezeTheWorld;
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
	 * Instantiates the {@link MultimediaService} with a specified factory to
	 * create new video players.
	 */
	public MultimediaService(VideoplayerFactory playerFactory) {
		this.playerFactory = playerFactory;
	}

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode WITHOUT
	 * playing the audio sequence.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param file
	 *            The ogg theora video file
	 */
	public void play(FileHandle file) {
		play(file, GameBase.$().getScreen(), false, false, true, -1f, false);
	}

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param file
	 *            The ogg theora video file
	 * @param audio
	 *            Wether or not to stream the audio sequence
	 */
	public void play(FileHandle file, boolean audio) {
		play(file, GameBase.$().getScreen(), false, audio, true, -1f, false);
	}

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param file
	 *            The ogg theora video file
	 * @param audio
	 *            Wether or not to stream the audio sequence
	 * @param playTime
	 *            If > -1, the video will stop at the given time (Seconds).<br>
	 *            Use -1 to play the entire video
	 * @param loop
	 *            If true, the playback will loop forever (until stop() is
	 *            called).
	 */
	public void play(FileHandle file, boolean audio, float playTime,
			boolean loop) {
		play(file, GameBase.$().getScreen(), false, audio, true, playTime, loop);
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
		play(file, bounds, true, false, false, -1f, true);
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
		play(file, bounds, true, false, false, playTime, loop);
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
			GameBase.$error("MultimediaService.play", "Failed to play '"
					+ file.path() + "'", e);
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
	public void play(final URL url, final Rectangle bounds,
			final boolean projectToMap, final boolean withAudio,
			final boolean freezeTheWorld, final float playTime,
			final boolean loop) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (freezeTheWorld
						&& !GameBase.$serviceProvider().requestAttention(
								MultimediaService.this, true, true)) {
					Gdx.app.postRunnable(this);
				} else {
					createAndStartPlayer(url, bounds, projectToMap, withAudio,
							freezeTheWorld, playTime, loop);
				}
			}
		});
	}

	private void createAndStartPlayer(URL url, Rectangle bounds,
			boolean projectToMap, boolean withAudio, boolean freezeTheWorld,
			float playTime, boolean loop) {
		if (playing && player != null)
			player.dispose();
		try {
			player = playerFactory.createPlayer(url, bounds, projectToMap,
					withAudio, !freezeTheWorld && projectToMap);
			player.play();
			this.playing = true;
			this.loop = loop;
			this.playTime = playTime;
			this.position = 0;
			this.projectToMap = projectToMap;
			this.url = url;
			this.bounds = bounds;
			this.withAudio = withAudio;
			this.freezeTheWorld = freezeTheWorld;
		} catch (Exception e) {
			GameBase.$error("MultimediaService.play", "Failed to play '" + url
					+ "'", e);
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
				if (!frozen && player.isSignalReceived()) {
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

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		if (playing) {
			play(url, bounds, projectToMap, withAudio, freezeTheWorld,
					playTime, loop);
		}
	}
}
