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
import java.awt.image.DataBuffer;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.text.AttributedCharacterIterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

/**
 * This class implements the interface {@link Graphics} and draws onto a
 * {@link Pixmap}. Drawings may be performed via a {@link BufferedImage} with a
 * custom {@link DataBuffer} and {@link Raster}.
 * 
 * @author Alexander Baumgartner
 */
public class GraphicsPixmapWrapper extends Graphics implements Disposable {
	protected Graphics rasterImageGraphics;
	protected PixmapDataBuffer rasterDataBuffer;

	/**
	 * You have to call {@link #setSize(int, int)} before you start drawing
	 */
	public GraphicsPixmapWrapper() {
	}

	/**
	 * Same effect as calling {@link #GraphicsPixmapWrapper()} and
	 * {@link #setSize(int, int)}
	 * 
	 * @param width
	 * @param height
	 */
	public GraphicsPixmapWrapper(int width, int height) {
		setSize(width, height);
	}

	public Pixmap getPixmap() {
		return rasterDataBuffer.getPixmap();
	}

	public void setSize(int width, int height) {
		WritableRaster raster = new PixmapRaster(width, height);
		rasterImageGraphics = new BufferedImage(new PixmapColorModel(), raster,
				false, null).createGraphics();
		rasterDataBuffer = (PixmapDataBuffer) raster.getDataBuffer();
	}

	@Override
	public void dispose() {
		rasterImageGraphics.dispose();
		rasterDataBuffer.dispose();
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
		return rasterImageGraphics.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return rasterImageGraphics
				.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return rasterImageGraphics.drawImage(img, x, y, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		return rasterImageGraphics.drawImage(img, x, y, width, height, bgcolor,
				observer);
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
