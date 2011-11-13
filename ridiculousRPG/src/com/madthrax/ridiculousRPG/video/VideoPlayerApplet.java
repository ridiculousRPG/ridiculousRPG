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
import java.awt.event.MouseEvent;
import java.net.URL;

import com.fluendo.player.Cortado;

/**
 * This class extends the Cortado video player {@link Applet}.<br>
 * It hides some features like context menu and sets the used default
 * parameters.<br>
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
public class VideoPlayerApplet extends Cortado {
	/**
	 * This is an applet! You should NEVER use this constructor manually!<br>
	 * Use
	 * {@link VideoPlayerAppletWrapper#obtainPlayer(URL, java.awt.Rectangle, boolean)}
	 * instead!
	 */
	@Deprecated
	public VideoPlayerApplet() {
	}

	private static final long serialVersionUID = 1L;

	/**
	 * If possible, use
	 * {@link VideoPlayerAppletWrapper#obtainPlayer(URL, java.awt.Rectangle, boolean)}
	 * instead!
	 */
	protected static VideoPlayerApplet obtainPlayerApplet(URL url,
			boolean withAudio) {
		VideoPlayerApplet player = new VideoPlayerApplet();
		player.setParam("url", url.toString());
		player.setParam("bufferSize", "200");
		player.setParam("showStatus", "hide");
		player.setParam("showSpeaker", "false");
		player.setParam("showSubtitles", "false");
		player.setParam("autoPlay", String.valueOf(withAudio));
		player.setParam("debug", "0");
		return player;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
