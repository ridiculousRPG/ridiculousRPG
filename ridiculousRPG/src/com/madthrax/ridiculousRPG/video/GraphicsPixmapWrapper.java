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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.AttributedCharacterIterator;
import java.util.Hashtable;

import com.badlogic.gdx.graphics.Pixmap;
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
		ImageConsumer {
	private Graphics rasterImageGraphics;
	private CortadoPixmap pm;
	private IntBuffer ib;
	private boolean ready = false;

	public GraphicsPixmapWrapper() {
	}

	private boolean pushImg(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) throws RuntimeException {
		img.getSource().startProduction(this);
		return true;
	}

	public Pixmap getPixmap() {
		return pm;
	}

	public boolean isReady() {
		return ready;
	}

	@Override
	public void dispose() {
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

	@Override
	public void setDimensions(int width, int height) {
		if (pm == null || pm.getWidth() != width || pm.getHeight() != height) {
			pm = new CortadoPixmap(width, height);
			ib = pm.getPixels().asIntBuffer();
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
			synchronized (pm) {
				ib.position(0);
				ib.put(pixels, off, w * h);
				pm.shiftARGBtoRGBA();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageComplete(int status) {
	}

	public class CortadoPixmap extends Pixmap {
		int lastAlphaAfterShift;

		public CortadoPixmap(int width, int height) {
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
