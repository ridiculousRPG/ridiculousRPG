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
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.net.URL;

import com.badlogic.gdx.utils.Disposable;
import com.fluendo.jst.Message;

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
public class VideoPlayerAppletWrapper implements AppletStub, Disposable {
	private static VideoPlayerAppletWrapper instance;
	private VideoPlayerApplet player;
	private URL url;
	private Frame frame;
	private boolean playing;

	/**
	 * Instantiates a new video player. Use {@link #$(URL, Rectangle, boolean)}
	 * if you don't need to play more than one video at the same time!
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 * @param autoClose
	 *            if true, the player will close automatically after reaching
	 *            the end of the stream/file or if an error occurred. BUT: You
	 *            have to dispose the player yourself!
	 */
	public VideoPlayerAppletWrapper(URL url, Rectangle screenBounds,
			boolean withAudio, final boolean autoClose) {
		this.url = url;
		player = new VideoPlayerApplet() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (autoClose
						&& (msg.getType() == Message.EOS || msg.getType() == Message.ERROR)) {
					VideoPlayerAppletWrapper.this.stop();
				}
			}

			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
			}

		};
		frame = new Frame();
		initFrame();
		frame.setBounds(screenBounds);
		frame.add(player);
		initPlayer();
		player.setParam("url", url.toString());
		player.setParam("audio", String.valueOf(withAudio));
		player.setStub(this);
		player.init();
		player.start();
	}

	/**
	 * Initializes the frame. Override if you want a window wit borders
	 */
	protected void initFrame() {
		frame.setBackground(Color.BLACK);
		frame.setEnabled(false);
		frame.setAlwaysOnTop(true);
		// frame.setResizable(false);
		frame.setUndecorated(true);
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
	}

	/**
	 * Returns the default instance. If it's the first call, the default
	 * instance will be allocated.
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 * @param autoClose
	 *            if true, the player will close automatically after reaching
	 *            the end of the stream/file or if an error occurred.
	 * @return
	 */
	public static VideoPlayerAppletWrapper $(URL url, Rectangle screenBounds,
			boolean withAudio, boolean autoClose) {
		if (instance == null) {
			instance = new VideoPlayerAppletWrapper(url, screenBounds,
					withAudio, autoClose);
		} else {
			if (!url.sameFile(instance.url)) {
				instance.changeMedia(url, withAudio);
			}
			instance.resize(screenBounds);
		}
		return instance;
	}

	/**
	 * Changes the media file (ogg / ogv)<br>
	 * If you change the media and resize the screen, always
	 * first change the media and than resize the screen. 
	 */
	public void changeMedia(URL url, boolean withAudio) {
		player.setParam("audio", String.valueOf(withAudio));
		player.setParam("url", url.toString());
		player.restart();
		if (playing)
			play();
	}

	/**
	 * Starts the video (and audio) playback
	 */
	public void play() {
		frame.setVisible(true);
		player.doPlay();
		playing = true;
	}

	public boolean isPlaying() {
		return playing;
	}

	/**
	 * Pauses or resumes the playback
	 * 
	 * @param toggleVisibility
	 *            true if the frame should be hidden until the next call of
	 *            {@link #pause(boolean) pause(true)}
	 */
	public void pause(boolean toggleVisibility) {
		player.doPause();
		if (toggleVisibility)
			frame.setVisible(!frame.isVisible());
	}

	/**
	 * Stops the video (and audio) playback
	 */
	public void stop() {
		playing = false;
		player.doStop();
		frame.setVisible(false);
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
		frame.setBounds(screenBounds);
		while (player.getWidth() != screenBounds.width
				|| player.getHeight() != screenBounds.height)
			Thread.yield();
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void appletResize(int width, int height) {
		frame.setSize(width, height);
		while (player.getWidth() != width || player.getHeight() != height)
			Thread.yield();
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

	/**
	 * Disposes the default instance. this method is automatically when the
	 * {@link MultimediaService} is disposed. Normally you don't need to call
	 * this.
	 */
	public static void dispose$() {
		if (instance != null) {
			instance.dispose();
			instance = null;
		}
	}

	@Override
	public void dispose() {
		stop();
		player.stop();
		player.destroy();
		frame.remove(player);
		if (player.isActive()) {
			// force exit
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
	}
}
