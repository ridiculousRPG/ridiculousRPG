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

package com.madthrax.ridiculousRPG.pixelwrap;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.text.AttributedCharacterIterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Disposable;

/**
 * This class implements the interface {@link Graphics} and draws onto a
 * {@link Pixmap}. Drawings may be performed via a {@link PixelGrabber} or via a
 * {@link BufferedImage} with a custom {@link DataBuffer} and {@link Raster}.<br>
 * The performance is bad because every pixel will be grabbed and drawn onto the
 * {@link Pixmap} by hand. To slightly increase the performance you may specify
 * a number of workers. This workers will perform the data conversion.
 * 
 * @author Alexander Baumgartner
 */
public class GraphicsPixmapWrapper extends Graphics implements Disposable,
		Runnable {
	private Graphics rasterImageGraphics;
	// for normal (not optimized) drawing
	private PixmapDataBuffer rasterDataBuffer;
	// for optimized drawing
	private boolean hasWorker;
	private boolean running = true;
	private boolean workerIdle;
	private int workerIdleTimeout;
	private long workerIdleTimeMark;
	private int bufferSize;
	private DataBufferElement producerDataBuffer;
	private DataBufferElement consumerDataBuffer;
	private PixmapBufferElement producerPixmapBuffer;
	private PixmapBufferElement consumerPixmapBuffer;

	public class DataBufferElement {
		private PixelGrabber grabber;
		int[] buf;
		int x, y, w, h;
		boolean ready;
		DataBufferElement next;
	}

	public class PixmapBufferElement {
		Pixmap buf;
		boolean ready;
		PixmapBufferElement next;

		public PixmapBufferElement(int width, int height) {
			buf = new Pixmap(width, height, Format.RGBA8888);
		}
	}

	public GraphicsPixmapWrapper(int width, int height) {
		this(width, height, 0, 0);
	}

	public GraphicsPixmapWrapper(int width, int height, int spawnWorker,
			int workerIdleTimeout) {
		this.hasWorker = spawnWorker > 0;
		this.workerIdleTimeout = workerIdleTimeout;
		this.bufferSize = Math.max(8, spawnWorker * 2);
		resize(width, height);
		// allow startup to consume 3 times normal timeout
		this.workerIdleTimeMark = System.currentTimeMillis() + 2
				* workerIdleTimeout;
		for (int i = 0; i < spawnWorker; i++) {
			new Thread(this).start();
		}
	}

	private PixmapBufferElement createPixmapBuffer(int capacity, int width,
			int height) {
		PixmapBufferElement first = new PixmapBufferElement(width, height);
		first.next = first;
		PixmapBufferElement last = first;
		for (int i = 1; i < capacity; i++) {
			PixmapBufferElement newLast = new PixmapBufferElement(width, height);
			last.next = newLast;
			newLast.next = first;
			last = newLast;
		}
		return first;
	}

	private DataBufferElement createDataBuffer(int capacity, int width,
			int height) {
		DataBufferElement first = new DataBufferElement();
		first.next = first;
		DataBufferElement last = first;
		for (int i = 1; i < capacity; i++) {
			DataBufferElement newLast = new DataBufferElement();
			last.next = newLast;
			newLast.next = first;
			last = newLast;
		}
		return first;
	}

	@Override
	public void run() {
		DataBufferElement workingDataBuffer;
		PixmapBufferElement workingPixmapBuffer;
		while (running) {
			// wait until other thread got the ticket
			synchronized (this) {
				if (!running)
					return;
				// wait for data producer and/or pixmap consumer
				while (!consumerDataBuffer.ready || producerPixmapBuffer.ready) {
					if (workerIdleTimeMark + workerIdleTimeout < System
							.currentTimeMillis()) {
						workerIdle = true;
						try {
							wait(); // stop spinning
						} catch (InterruptedException e) {
						}
						// set new time mark
						workerIdleTimeMark = System.currentTimeMillis();
					}
					if (!running)
						return;
					// System.out.println("worker waiting");
					Thread.yield();
				}
				// grab next ticket
				workingDataBuffer = consumerDataBuffer;
				workingPixmapBuffer = producerPixmapBuffer;
				// shift shared buffer pointers
				consumerDataBuffer = consumerDataBuffer.next;
				producerPixmapBuffer = producerPixmapBuffer.next;
			}
			// shift data from data buffer to pixmap - asynchron
			shiftData(workingDataBuffer, workingPixmapBuffer.buf);
			// mark buffers
			workingDataBuffer.ready = false;
			workingPixmapBuffer.ready = true;
			// set new time mark
			workerIdleTimeMark = System.currentTimeMillis();
		}
	}

	/**
	 * this method scales the image linear
	 * 
	 * @param imgData
	 * @param pixmap
	 */
	protected void shiftData(DataBufferElement imgData, Pixmap pixmap) {
		int[] pixels = imgData.buf;
		int w1 = pixmap.getWidth();
		int h1 = pixmap.getHeight();
		int w2 = imgData.w;
		int h2 = imgData.h;
		// assert w2 + 2 * imgData.x == h2 + 2 * imgData.y;
		float scaleFactor = Math.min(((float) w1) / ((float) w2), ((float) h1)
				/ ((float) h2));
		int x = (int) (w1 - w2 * scaleFactor);
		int y = (int) (h1 - h2 * scaleFactor);
		h1 -= y;
		w1 -= x;
		x /= 2;
		y /= 2;

		for (int i = 0; i < h1; i++) {
			int scaledLine = ((int) (i / scaleFactor)) * w2;
			int iy = i + y;
			for (int j = 0; j < w1; j++) {
				int val = pixels[scaledLine + (int) (j / scaleFactor)];
				pixmap.drawPixel(j + x, iy, val >>> 24 | val << 8);
			}
		}
	}

	private boolean pushImg(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) throws RuntimeException {
		if (!hasWorker) {
			rasterDataBuffer.setPixmap(consumerPixmapBuffer.next.buf);
			boolean ret = rasterImageGraphics.drawImage(img, x, y, width,
					height, bgcolor, observer);
			consumerPixmapBuffer = consumerPixmapBuffer.next;
			return ret;
		}

		// register image consumer
		if (producerDataBuffer.grabber == null) {
			// not very dressy but it works :/
			new PixelGrabber(img, x, y, -1, -1, false) {
				@Override
				public void setDimensions(int width, int height) {
					producerDataBuffer.w = width;
					producerDataBuffer.h = height;
					producerDataBuffer.buf = new int[width * height];
					abortGrabbing();
				}

				@Override
				public void setPixels(int srcX, int srcY, int srcW, int srcH,
						ColorModel model, int[] pixels, int srcOff, int srcScan) {
				}
			}.startGrabbing();
			while (producerDataBuffer.buf == null)
				Thread.yield();
			producerDataBuffer.grabber = new PixelGrabber(img, x, y,
					producerDataBuffer.w, producerDataBuffer.h,
					producerDataBuffer.buf, 0, producerDataBuffer.w);
			img.getSource().addConsumer(producerDataBuffer.grabber);
		}
		// wait for consumer
		while (producerDataBuffer.ready) {
			// System.out.println("producer waiting");
			Thread.yield();
		}

		img.getSource().startProduction(producerDataBuffer.grabber);
		producerDataBuffer.x = x;
		producerDataBuffer.y = y;
		producerDataBuffer.ready = true;
		// shift ring buffer pointer
		producerDataBuffer = producerDataBuffer.next;
		if (workerIdle)
			synchronized (this) {
				notify();
			}
		return true;
	}

	public Pixmap getPixmap() {
		if (consumerPixmapBuffer.next.ready) {
			// shift buffer pointer
			consumerPixmapBuffer.ready = false;
			consumerPixmapBuffer = consumerPixmapBuffer.next;
		}
		return consumerPixmapBuffer.buf;
	}

	public void resize(int width, int height) {
		WritableRaster raster = new PixmapRaster(width, height);
		rasterImageGraphics = new BufferedImage(new PixmapColorModel(), raster,
				false, null).createGraphics();

		producerPixmapBuffer = createPixmapBuffer(bufferSize, width, height);
		consumerPixmapBuffer = producerPixmapBuffer;
		if (hasWorker) {
			producerDataBuffer = createDataBuffer(bufferSize, width, height);
			consumerDataBuffer = producerDataBuffer;
		}
		rasterDataBuffer = (PixmapDataBuffer) raster.getDataBuffer();
		rasterDataBuffer.setPixmap(consumerPixmapBuffer.buf);
	}

	public boolean isWorkerIdle() {
		return workerIdle;
	}

	@Override
	public void dispose() {
		running = false;
		if (workerIdle)
			synchronized (this) {
				notify();
			}
		rasterImageGraphics.dispose();
	}

	@Override
	public void drawString(String str, int x, int y) {
		rasterImageGraphics.drawString(str, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		rasterImageGraphics.drawString(iterator, x, y);
	}

	@Override
	public void translate(int x, int y) {
		rasterImageGraphics.translate(x, y);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		rasterImageGraphics.clearRect(x, y, width, height);
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		rasterImageGraphics.clipRect(x, y, width, height);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		rasterImageGraphics.copyArea(x, y, width, height, dx, dy);
	}

	@Override
	public Graphics create() {
		return rasterImageGraphics.create();
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		rasterImageGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
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
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return rasterImageGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1,
				sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		return rasterImageGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1,
				sx2, sy2, bgcolor, observer);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		rasterImageGraphics.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		rasterImageGraphics.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		rasterImageGraphics.drawPolygon(xPoints, yPoints, nPoints);
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		rasterImageGraphics.drawPolyline(xPoints, yPoints, nPoints);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		rasterImageGraphics.drawRoundRect(x, y, width, height, arcWidth,
				arcHeight);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		rasterImageGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		rasterImageGraphics.fillOval(x, y, width, height);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		rasterImageGraphics.fillPolygon(xPoints, yPoints, nPoints);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		rasterImageGraphics.fillRect(x, y, width, height);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		rasterImageGraphics.fillRoundRect(x, y, width, height, arcWidth,
				arcHeight);
	}

	@Override
	public Shape getClip() {
		return rasterImageGraphics.getClip();
	}

	@Override
	public Rectangle getClipBounds() {
		return rasterImageGraphics.getClipBounds();
	}

	@Override
	public Color getColor() {
		return rasterImageGraphics.getColor();
	}

	@Override
	public Font getFont() {
		return rasterImageGraphics.getFont();
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return rasterImageGraphics.getFontMetrics(f);
	}

	@Override
	public void setClip(Shape clip) {
		rasterImageGraphics.setClip(clip);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		rasterImageGraphics.setClip(x, y, width, height);
	}

	@Override
	public void setColor(Color c) {
		rasterImageGraphics.setColor(c);
	}

	@Override
	public void setFont(Font font) {
		rasterImageGraphics.setFont(font);
	}

	@Override
	public void setPaintMode() {
		rasterImageGraphics.setPaintMode();
	}

	@Override
	public void setXORMode(Color c1) {
		rasterImageGraphics.setXORMode(c1);
	}
}
