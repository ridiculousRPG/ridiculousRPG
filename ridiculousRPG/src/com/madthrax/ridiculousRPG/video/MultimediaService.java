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

import java.awt.Rectangle;
import java.net.URL;

import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
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
public class MultimediaService extends GameServiceDefaultImpl implements ResizeListener {

	static String f1 = "file:///home/alex/ridiculousRPG.mpeg";
	static String f2 = "file:///media/EXTERN_200/movies/test.avi";
	static String f3 = "file:///home/alex/Desktop/JMF-2.1.1e/test.ogv";
	static String f4 = "file:///home/alex/Desktop/JMF-2.1.1e/test2.ogg";
	static String toPlay = f3;

	public void play() {
	}

	@Override
	public void dispose() {
	}

	public static void main(String[] args) {
		try {
			VideoPlayerAppletWrapper p ;
			p  = VideoPlayerAppletWrapper.obtainPlayer(new URL(f3),
					new Rectangle(0, 0, 900, 400), true);
			Thread.sleep(2000);
			p.play();
			Thread.sleep(1000);
			p.stop();
			Thread.sleep(1000);
			p = VideoPlayerAppletWrapper.obtainPlayer(new URL(f4),
					new Rectangle(0, 0, 900, 400), true);
			p.play();
			Thread.sleep(3000);
			p.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
