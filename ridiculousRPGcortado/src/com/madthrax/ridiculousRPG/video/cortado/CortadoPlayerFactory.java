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

package com.madthrax.ridiculousRPG.video.cortado;

import java.net.URL;

import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.video.Videoplayer;
import com.madthrax.ridiculousRPG.video.VideoplayerFactory;

/**
 * This is the factory class to obtain a Cortado video player.<br>
 * It implements the needed interface to use it with the ridiculousRPG game
 * engine.
 * 
 * @see http://www.theora.org/cortado/
 * @author Alexander Baumgartner
 */
public class CortadoPlayerFactory implements VideoplayerFactory {
	private static final long serialVersionUID = 1L;

	@Override
	public Videoplayer createPlayer(URL url, Rectangle screenBounds,
			boolean projectToMap, boolean withAudio, boolean drawPlaceholder) {
		return new CortadoPlayerAppletWrapper(url, screenBounds, projectToMap,
				withAudio, drawPlaceholder);
	}
}
