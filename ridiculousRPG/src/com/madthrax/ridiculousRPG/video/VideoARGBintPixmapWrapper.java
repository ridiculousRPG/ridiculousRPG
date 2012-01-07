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

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import com.badlogic.gdx.graphics.Pixmap;
import com.madthrax.ridiculousRPG.pixelwrap.GraphicsPixmapWrapper;

/**
 * This is a highly optimized subclass of {@link GraphicsPixmapWrapper} for
 * playing videos with are in ARGB format and use integer pixels.
 * 
 * @author Alexander Baumgartner
 */
public class VideoARGBintPixmapWrapper extends GraphicsPixmapWrapper implements
		ImageConsumer {

	private ARGBintPixmap pixmap;
	private IntBuffer intPixelBuffer;
	private boolean ready = false;
	private long lastFrameReceived;

	public VideoARGBintPixmapWrapper() {
	}

	private boolean pushImg(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) throws RuntimeException {
		lastFrameReceived = System.currentTimeMillis();
		img.getSource().startProduction(this);
		return true;
	}

	@Override
	public Pixmap getPixmap() {
		return pixmap;
	}

	public boolean isReady() {
		return ready;
	}

	/**
	 * Returns true if we have already received some frames and the video player
	 * didn't send a frame for more than one second.<br>
	 * (This should only happen if the stream is broken or ended unexpected)
	 * 
	 * @param paused
	 *            indicates if the player is paused
	 * @param timeoutMillis
	 *            Timeout in milliseconds to switch into EOS state
	 * 
	 * @return true if<br>
	 *         <code>ready && lastFrameReceived + timeout < System.currentTimeMillis()</code>
	 */
	public boolean streamStoped(boolean paused, long timeoutMillis) {
		if (paused) {
			lastFrameReceived = System.currentTimeMillis();
			return false;
		}
		return ready && lastFrameReceived + timeoutMillis < System.currentTimeMillis();
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return drawImage(img, x, y, null, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return drawImage(img, x, y, width, height, null, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return drawImage(img, x, y, img.getWidth(observer), img
				.getHeight(observer), bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		return pushImg(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public void setDimensions(int width, int height) {
		if (pixmap == null || pixmap.getWidth() != width
				|| pixmap.getHeight() != height) {
			setSize(width, height);
			pixmap = new ARGBintPixmap(width, height);
			// Yeah - it's ridiculous, but we are able to draw some other stuff
			// onto the rendered picture before pushing it onto the screen ;)
			rasterDataBuffer.setPixmap(pixmap).dispose();
			intPixelBuffer = pixmap.getPixels().asIntBuffer();
			ready = true;
		}
	}

	@Override
	public void setProperties(Hashtable<?, ?> props) {
	}

	@Override
	public void setColorModel(ColorModel model) {
	}

	@Override
	public void setHints(int hintflags) {
	}

	@Override
	public void setPixels(int x, int y, int w, int h, ColorModel model,
			byte[] pixels, int off, int scansize) {
	}

	@Override
	public void setPixels(int x, int y, int w, int h, ColorModel model,
			int[] pixels, int off, int scansize) {
		try {
			synchronized (pixmap) {
				intPixelBuffer.position(0);
				intPixelBuffer.put(pixels, off, w * h);
				pixmap.shiftARGBtoRGBA();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageComplete(int status) {
	}

	@Override
	public void dispose() {
		// pixmap.dispose is called by PixmapDataBuffer
		pixmap = null;
		intPixelBuffer = null;
	}

	public class ARGBintPixmap extends Pixmap {
		int lastAlphaAfterShift;

		public ARGBintPixmap(int width, int height) {
			super(Math.max(width, 1), height + 1, Format.RGBA8888);
			lastAlphaAfterShift = width * height * 4;
		}

		/**
		 * Of course this causes an pixel error at transparent pixels. Every
		 * pixel gets the alpha value of it's neighbour. But i think this
		 * shouldn't matter in our case and it's brutally fast ;)
		 */
		public void shiftARGBtoRGBA() {
			ByteBuffer bb = getPixels();
			bb.put(lastAlphaAfterShift, bb.get(0));
			bb.position(1);
		}

		@Override
		public int getHeight() {
			return super.getHeight() - 1;
		}
	}
}
