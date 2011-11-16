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

import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.ResizeListener;

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
public class MultimediaService implements
		ResizeListener, GameService {

	private VideoPlayerAppletWrapper p;

	public void play(FileHandle file) {
		try {
			play(new File(file.path()).toURI().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void play(URL url) {
		Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (w==null) w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		int x = 0;
		int y = 0;
		if (w==null) {
			System.out.println("NULL");
		} else {
			x = w.getX();
			y = w.getY();
			System.out.println("X="+x);
			System.out.println("Y="+y);
		}
		Rectangle bounds = new Rectangle(x, y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		p = VideoPlayerAppletWrapper.$(url, bounds, true, true);
		p.play();
		while (p.isPlaying()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}
	public void stop() {
		if (p!=null) p.stop();
	}

	@Override
	public void dispose() {
		if (p!=null) p.dispose();
	}

	@Override
	public void resize(int width, int height) {
		// TODO resize

	}

	@Override
	public void freeze() {
		// TODO stop playing
		
	}

	@Override
	public void unfreeze() {
		// TODO restart playing
		
	}

	@Override
	public boolean essential() {
		return false;
	}
}
