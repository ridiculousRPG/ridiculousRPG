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

	private CortadoPlayerAppletWrapper p;
	private boolean playing;
	private boolean projectToMap;

	public void play(FileHandle file) {
		try {
			play(new File(file.path()).toURI().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void play(FileHandle file, Rectangle screenBounds,
			boolean relativeBounds, boolean withAudio, boolean freezeTheWorld) {
		try {
			play(new File(file.path()).toURI().toURL(), screenBounds,
					relativeBounds, withAudio, freezeTheWorld);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void play(URL url) {
		play(url, new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics
				.getHeight()), true, true, true);
	}

	public void play(URL url, Rectangle screenBounds, boolean relativeBounds,
			boolean withAudio, boolean freezeTheWorld) {
		if (!freezeTheWorld
				|| GameBase.$serviceProvider().requestAttention(this, true,
						true)) {
			if (playing)
				p.dispose();
			p = new CortadoPlayerAppletWrapper(url, screenBounds, withAudio,
					relativeBounds);
			p.play();
			playing = true;
		}
	}

	public void stop() {
		if (p != null)
			p.stop();
	}

	@Override
	public void dispose() {
		if (p != null)
			p.dispose();
	}

	@Override
	public void freeze() {
		if (playing)
			p.stop();
	}

	@Override
	public void unfreeze() {
		if (playing)
			p.play();
	}

	@Override
	public boolean essential() {
		return false;
	}

	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		if (!playing)
			return;
		if (p.isPlaying()) {
			p.draw(spriteBatch, debug);
		} else {
			GameBase.$serviceProvider().releaseAttention(this);
			playing = false;
			p.dispose();
			p = null;
		}
	}

	public void setProjectToMap(boolean projectToMap) {
		this.projectToMap = projectToMap;
	}

	public boolean isProjectToMap() {
		return projectToMap;
	}

	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return projectToMap ? camera.projection : camera.view;
	}
}
