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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

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
public class VideoPlayerAppletWrapper implements AppletStub {
	private static VideoPlayerAppletWrapper instance;
	private VideoPlayerApplet player;
	private URL url;
	private Frame frame;

	protected VideoPlayerAppletWrapper(URL url, Rectangle screenBounds,
			boolean withAudio) {
		player = VideoPlayerApplet.obtainPlayerApplet(url, withAudio);
		this.url = url;
		frame = new Frame();
		frame.setBounds(screenBounds);
		frame.setBackground(Color.BLACK);
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				player.stop();
				player.destroy();
				System.exit(0);
			}
		});
		frame.add("Center", player);
		player.setStub(this);
		player.init();
		player.start();
	}

	public static VideoPlayerAppletWrapper obtainPlayer(URL url,
			Rectangle screenBounds, boolean withAudio) {
		if (instance == null) {
			instance = new VideoPlayerAppletWrapper(url, screenBounds,
					withAudio);
		} else {
			if (!url.sameFile(instance.url)) {
				instance.player.doStop();
				instance.player.setParam("url", url.toString());
				instance.player.setParam("audio", String.valueOf(withAudio));
			}
			instance.frame.setBounds(screenBounds);
		}
		return instance;
	}

	/**
	 * Starts the video (and audio) playback
	 */
	public void play() {
		frame.setVisible(true);
		player.doPlay();
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
		player.resize(screenBounds.width, screenBounds.height);
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void appletResize(int width, int height) {
		player.resize(width, height);
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
}
