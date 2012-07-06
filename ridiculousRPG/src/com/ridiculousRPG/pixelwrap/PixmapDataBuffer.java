package com.ridiculousRPG.pixelwrap;

import java.awt.image.DataBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Disposable;

public class PixmapDataBuffer extends DataBuffer implements Disposable {
	private Pixmap pixmap;

	public PixmapDataBuffer(int width, int height) {
		super(DataBuffer.TYPE_INT, width, height, 1);
		pixmap = new Pixmap(width, height, Format.RGBA8888);
	}

	public Pixmap getPixmap() {
		return pixmap;
	}

	/**
	 * Sets the new {@link Pixmap} and returns the old one.
	 * @param pixmap
	 * @return
	 */
	public Pixmap setPixmap(Pixmap pixmap) {
		Pixmap old = this.pixmap;
		this.pixmap = pixmap;
		return old;
	}

	@Override
	public int getElem(int bank, int i) {
		int width = pixmap.getWidth();
		return pixmap.getPixel(i % width, i / width);
	}

	@Override
	public void setElem(int bank, int i, int val) {
		int width = pixmap.getWidth();
		pixmap.drawPixel(i % width, i / width, val >>> 24 | val << 8);
	}

	@Override
	public void dispose() {
		pixmap.dispose();
	}
}
