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

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;

import javax.media.Manager;
import javax.media.Player;

import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * This service is capable to play video files.<br>
 * The following formats are supported:<br>
 * <ul>
 * <li>mp4</li>
 * </ul>
 * 
 * @author Alexander Baumgartner
 */

public class MultimediaService extends GameServiceDefaultImpl {

	static String f1 = "file:///home/alex/ridiculousRPG.mpeg";
	static String f2 = "file:///media/EXTERN_200/movies/test.avi";
	static String toPlay = f2;

	public void play() {
	}

	@Override
	public void dispose() {
	}

	public static void main(String[] args) {
		try {
			URL mediaURL = new URL(toPlay);
			Player player = Manager.createRealizedPlayer(mediaURL);
			Component c = player.getVisualComponent();
			if (c==null) {
				System.out.println("No visual comp available!!");
				return;
			}
			c.addComponentListener(new ComponentListener() {
				@Override
				public void componentShown(ComponentEvent e) {
					System.out.println(e);
				}

				@Override
				public void componentResized(ComponentEvent e) {
					System.out.println(e);
				}

				@Override
				public void componentMoved(ComponentEvent e) {
					System.out.println(e);
				}

				@Override
				public void componentHidden(ComponentEvent e) {
					System.out.println(e);
				}
			});
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
