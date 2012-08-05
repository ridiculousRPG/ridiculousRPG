/*
 * ATTENTION: This class links against cortado! If i got it right,
 * this means that the below licenses will be brought down to a
 * common denominator. GPLv3 is such a common denominator!
 * 
 * THIS FILE IS LICENSED AS NEEDED TO FULFILL GPL-LINKING REQUIREMENTS
 * (GPLv3 should meet this needs if I got it right)
 */

/*
 * A) CORTADO LICENSE:
 * 
 * Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

/*
 * B) RIDICULOUS-RPG LICENSE:
 * 
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

package com.ridiculousRPG.video.cortado;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.util.TextureRegionLoader;
import com.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;
import com.ridiculousRPG.video.VideoARGBintPixmapWrapper;
import com.ridiculousRPG.video.Videoplayer;

/**
 * This class wraps the Cortado video player {@link Applet} inside an
 * {@link AppletStub} and creates a frame to display the video.<br>
 * The frame will cover the game screen. It's certainly not the best solution
 * but it works. Suggestions for better solutions welcome!<br>
 * The following formats are supported:<br>
 * <ul>
 * <li>Ogg Theora</li>
 * <li>Ogg Vorbis</li>
 * <li>Mulaw audio</li>
 * <li>MJPEG</li>
 * <li>Smoke codec</li>
 * </ul>
 * 
 * @see {@link CortadoPlayerApplet}
 * @author Alexander Baumgartner
 */
public class CortadoPlayerAppletWrapper implements AppletStub, Disposable,
		Videoplayer {
	private CortadoPlayerApplet player;
	private boolean playing;
	private boolean signalReceived;
	private boolean projectToMap;
	private Rectangle screenBounds;
	private VideoARGBintPixmapWrapper graphicsPixmap;
	private TextureRegionRef textureRef;

	/**
	 * Instantiates a new video player. Don't forget to dispose the player!
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param projectToMap
	 *            Defines whether to project the video onto the map or onto the
	 *            screen coordinates
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 */
	public CortadoPlayerAppletWrapper(URL url, Rectangle screenBounds,
			boolean projectToMap, boolean withAudio, boolean drawPlaceholder) {
		this.screenBounds = new Rectangle(screenBounds);
		this.projectToMap = projectToMap;
		if (!projectToMap) {
			GameBase gb = GameBase.$();
			this.screenBounds.width /= gb.getScreen().width;
			this.screenBounds.height /= gb.getScreen().height;
			this.screenBounds.x /= gb.getScreen().width;
			this.screenBounds.y /= gb.getScreen().height;
		}
		int width = (int) screenBounds.width;
		int height = (int) screenBounds.height;

		textureRef = TextureRegionLoader.obtainEmptyRegion(width, height,
				Format.RGBA8888);
		if (drawPlaceholder) {
			Pixmap placeholder = new Pixmap(width, height, Format.RGBA8888);
			placeholder.setColor(0, 0, 0, 1);
			placeholder.fillRectangle(0, 0, width, height);
			placeholder.setColor(.7f, .7f, .7f, 1);
			placeholder.fillCircle(width / 2, height / 2, Math.min(width,
					height) / 3);
			placeholder.setColor(.4f, .4f, .4f, 1);
			placeholder.drawRectangle(0, 0, width, height);
			placeholder.drawRectangle(2, 2, width - 4, height - 4);
			placeholder.drawLine(1, 0, width, height - 1);
			placeholder.drawLine(0, 1, width - 1, height);
			placeholder.drawLine(1, height, width, 1);
			placeholder.drawLine(0, height - 1, width - 1, 0);
			textureRef.draw(placeholder);
			placeholder.dispose();
		}
		graphicsPixmap = new VideoARGBintPixmapWrapper();
		player = new CortadoPlayerApplet(graphicsPixmap);
		initPlayer();
		player.setParam("url", url.toString());
		player.setParam("audio", String.valueOf(withAudio));
		player.setSize(width, height);
		player.setStub(this);
		player.init();
		player.start();
	}

	/**
	 * Initializes the player. Override if you want a status bar, an other
	 * buffer size,...
	 */
	protected void initPlayer() {
		player.setParam("bufferSize", "200");
		player.setParam("showStatus", "hide");
		player.setParam("showSpeaker", "false");
		player.setParam("showSubtitles", "false");
		player.setParam("autoPlay", "false");
		player.setParam("debug", "0");
		player.setParam("keepAspect", "false");
	}

	public void play() {
		player.doPlay();
		playing = true;
		// reset timer
		graphicsPixmap.streamStoped(true, 0L);
	}

	public boolean isPlaying() {
		return playing;
	}

	public void pause() {
		player.doPause();
	}

	public void stop() {
		playing = false;
		player.doStop();
	}

	/**
	 * Seeks the video file with the given amount of milliseconds
	 * 
	 * @param value
	 *            milliseconds to seek
	 */
	public void seek(double value) {
		player.doSeek(value);
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void resize(Rectangle screenBounds) {
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void appletResize(int width, int height) {
	}

	/**
	 * Returns the applet's context, which is null in this case. This is an area
	 * where more creative programming work can be done to try and provide a
	 * context
	 * 
	 * @return AppletContext Always null
	 */
	public AppletContext getAppletContext() {
		return null;
	}

	/**
	 * Returns the CodeBase. If a host parameter isn't provided in the command
	 * line arguments, the URL is based on InetAddress.getLocalHost(). The
	 * protocol is "file:"
	 * 
	 * @return URL
	 */
	public URL getCodeBase() {
		return null;
	}

	/**
	 * Returns getCodeBase
	 * 
	 * @return URL
	 */
	public URL getDocumentBase() {
		return null;
	}

	/**
	 * Returns the corresponding command line value
	 * 
	 * @return String
	 */
	public String getParameter(String p) {
		return null;
	}

	/**
	 * Applet is always true
	 * 
	 * @return boolean True
	 */
	public boolean isActive() {
		return true;
	}

	public boolean estimateEOS(long timeout) {
		return graphicsPixmap.streamStoped(player.paused, timeout);
	}

	public void draw(SpriteBatch spriteBatch, boolean debug) {
		if (graphicsPixmap.isReady()) {
			signalReceived = true;
			Pixmap toDraw = graphicsPixmap.getPixmap();
			int width, height;
			synchronized (toDraw) {
				width = toDraw.getWidth();
				height = toDraw.getHeight();
				if (textureRef.getRegionWidth() != width
						|| textureRef.getRegionHeight() != height) {
					textureRef.dispose();
					textureRef = TextureRegionLoader.obtainEmptyRegion(width,
							height, Format.RGBA8888);
				}
				textureRef.draw(toDraw);
			}
		}
		drawTexture(spriteBatch, textureRef);
	}

	private void drawTexture(SpriteBatch spriteBatch, TextureRegionRef tRef) {
		float x = screenBounds.x;
		float y = screenBounds.y;
		float w = screenBounds.width;
		float h = screenBounds.height;
		if (!projectToMap) {
			// auto stretch on window resize
			GameBase gb = GameBase.$();
			x *= gb.getScreen().width;
			y *= gb.getScreen().height;
			w *= gb.getScreen().width;
			h *= gb.getScreen().height;
		}
		spriteBatch.draw(tRef, x, y, w, h);
	}

	public boolean isSignalReceived() {
		return signalReceived;
	}

	@Override
	public void dispose() {
		player.destroy();
		graphicsPixmap.dispose();
		textureRef.dispose();
		signalReceived = false;
		screenBounds = null;
		graphicsPixmap = null;
		textureRef = null;
		player = null;
	}
}
