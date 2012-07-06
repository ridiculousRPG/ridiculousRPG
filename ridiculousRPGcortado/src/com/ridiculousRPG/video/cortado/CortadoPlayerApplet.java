/*
 * ATTENTION: This class links against cortado! If i got it right,
 * this means that the below licenses will be brought down to a
 * common denominator. GPLv3 is such a common denominator!
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
import java.awt.Graphics;
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
public class CortadoPlayerApplet extends Cortado {
	private static final long serialVersionUID = 1L;

	private Graphics graphics;

	/**
	 * This is an applet! You should NEVER use this constructor manually!<br>
	 * Use
	 * {@link CortadoPlayerAppletWrapper#CortadoPlayerAppletWrapper(URL, com.badlogic.gdx.math.Rectangle, boolean, boolean)
	 * instead!
	 */
	public CortadoPlayerApplet(Graphics graphics) {
		this.graphics = graphics;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	@Override
	public void destroy() {
		// Cortado strikes to release it's resourecs,
		// thats why we have to do a lot of crazy stuff below
		try {
			stop();
			setStub(null);
			// stub = null;
			graphics = null;
			super.destroy();
			if (isActive()) {
				removeAll();
				// try to crash it
				doPlay();
				stop();
			}
		} catch (Throwable ignored) {
		}
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
