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

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Graphics;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.fluendo.jst.Message;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;

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
 * @see http://www.theora.org/cortado/
 * @author Alexander Baumgartner
 */
public class CortadoPlayerAppletWrapper implements AppletStub, Disposable {
	private CortadoPlayerApplet player;
	private URL url;
	private boolean playing;
	private boolean relativeBounds;
	private Rectangle screenBounds;
	private CortadoPixmapWrapper graphicsPixmap;
	private TextureRegionRef textureRef;

	/**
	 * Instantiates a new video player. Don't forget to dispose the player!
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 * @param fullscreen
	 * @param relativeBounds
	 *            if the bounds are relative, the video will automatically be
	 *            stretched on resize
	 */
	public CortadoPlayerAppletWrapper(URL url, Rectangle screenBounds,
			boolean withAudio, boolean relativeBounds) {
		this.url = url;
		this.screenBounds = new Rectangle(screenBounds);
		this.relativeBounds = relativeBounds;
		if (relativeBounds) {
			this.screenBounds.width /= Gdx.graphics.getWidth();
			this.screenBounds.height /= Gdx.graphics.getHeight();
			this.screenBounds.x /= Gdx.graphics.getWidth();
			this.screenBounds.y /= Gdx.graphics.getHeight();
		}
		int width = (int) screenBounds.width;
		int height = (int) screenBounds.height;

		textureRef = TextureRegionLoader.obtainEmptyRegion(width, height,
				Format.RGBA8888);
		graphicsPixmap = new CortadoPixmapWrapper();
		player = new CortadoPlayerApplet() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleMessage(Message msg) {
				if (msg.getType() == Message.EOS
						|| msg.getType() == Message.ERROR) {
					CortadoPlayerAppletWrapper.this.stop();
				}
				super.handleMessage(msg);
			}

			@Override
			public Graphics getGraphics() {
				return graphicsPixmap;
			}
		};
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

	/**
	 * Starts the video (and audio) playback
	 */
	public void play() {
		player.doPlay();
		playing = true;
	}

	public boolean isPlaying() {
		return playing;
	}

	/**
	 * Pauses or resumes the playback
	 */
	public void pause() {
		player.doPause();
	}

	/**
	 * Stops the video (and audio) playback
	 */
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
		return url;
	}

	/**
	 * Returns getCodeBase
	 * 
	 * @return URL
	 */
	public URL getDocumentBase() {
		return getCodeBase();
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

	public void draw(SpriteBatch spriteBatch, boolean debug) {
		if (graphicsPixmap.streamStoped()) {
			stop();
			return;
		}
		if (!graphicsPixmap.isReady()) {
			return;
		}

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
		if (relativeBounds) {
			spriteBatch.draw(textureRef, screenBounds.x
					* Gdx.graphics.getWidth(), screenBounds.y
					* Gdx.graphics.getHeight(), screenBounds.width
					* Gdx.graphics.getWidth(), screenBounds.height
					* Gdx.graphics.getHeight());
		} else {
			spriteBatch.draw(textureRef, screenBounds.x, screenBounds.y,
					screenBounds.width, screenBounds.height);
		}
	}

	@Override
	public void dispose() {
		// Cortado strikes to release it's resourecs,
		// thats why we have to do a lot of crazy stuff below
		try {
			stop();
			player.stop();
			player.destroy();
			player.setStub(null);
			graphicsPixmap.dispose();
			textureRef.dispose();
			screenBounds = null;
			graphicsPixmap = null;
			textureRef = null;
			if (player.isActive()) {
				player.shutDown(null);
				player.removeAll();
				// crash it
				player.doPlay();
			}
		} catch (Throwable ignored) {
		}
		// spawn thread to force jvm exit
		if (player.isActive()) {
			final Thread current = Thread.currentThread();
			new Thread() {
				@Override
				public void run() {
					do
						try {
							sleep(2000);
						} catch (InterruptedException e) {
						}
					while (current.isAlive());
					System.exit(0);
				}
			}.start();
		}
		player = null;
	}
}
