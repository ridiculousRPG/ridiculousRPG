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

import java.io.File;
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
import com.madthrax.ridiculousRPG.service.GameService;

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
public class MultimediaService implements GameService, Drawable {

	private CortadoPlayerAppletWrapper player;
	private boolean playing;
	private boolean projectToMap;

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param file
	 *            The ogg theora video file
	 */
	public void play(FileHandle file) {
		try {
			play(new File(file.path()).toURI().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
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
		try {
			play(new File(file.path()).toURI().toURL(), bounds);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
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
	 */
	public void play(FileHandle file, Rectangle bounds, boolean projectToMap,
			boolean withAudio, boolean freezeTheWorld) {
		try {
			play(new File(file.path()).toURI().toURL(), bounds, projectToMap,
					withAudio, freezeTheWorld);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Plays the (ogg-theora) video in full screen exclusive mode.<br>
	 * The game is paused while the video is running.
	 * 
	 * @param url
	 *            The url for streaming the ogg theora video
	 */
	public void play(URL url) {
		play(url, new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics
				.getHeight()), false, true, true);
	}

	/**
	 * Plays the (ogg-theora) video in embedded mode.<br>
	 * The video is embedded into the games world. The game is not stopped. The
	 * video integrates smoothly into the game.
	 * 
	 * @param url
	 *            The url for streaming the ogg theora video
	 * @param bounds
	 *            The position, width and height for embedding the video into
	 *            the games world coordinates
	 */
	public void play(URL url, Rectangle bounds) {
		play(url, bounds, true, true, false);
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
	 */
	public void play(URL url, Rectangle bounds, boolean projectToMap,
			boolean withAudio, boolean freezeTheWorld) {
		if (!freezeTheWorld
				|| GameBase.$serviceProvider().requestAttention(this, true,
						true)) {
			if (playing)
				player.dispose();
			player = new CortadoPlayerAppletWrapper(url, bounds, projectToMap,
					withAudio);
			player.play();
			playing = true;
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
		if (playing)
			player.pause();
	}

	@Override
	public void unfreeze() {
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
