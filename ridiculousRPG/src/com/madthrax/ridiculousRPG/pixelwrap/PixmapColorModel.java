package com.madthrax.ridiculousRPG.pixelwrap;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

public class PixmapColorModel extends ColorModel {

	public PixmapColorModel() {
		super(32);
	}

	@Override
	public int getRGB(int pixel) {
		return pixel;
	}

	@Override
	public int getRGB(Object inData) {
		return ((int[]) inData)[0];
	}

	@Override
	public int getTransparency() {
		return super.getTransparency();
	}

	@Override
	public Object getDataElements(int rgb, Object pixel) {
		int[] result;
		if (pixel == null)
			result = new int[1];
		else
			result = (int[]) pixel;
		result[0] = rgb;
		return result;
	}

	@Override
	public boolean isCompatibleRaster(Raster raster) {
		return raster instanceof PixmapRaster;
	}

	@Override
	public boolean isCompatibleSampleModel(SampleModel sm) {
		return sm instanceof PixmapSampleModel;
	}

	@Override
	public int getAlpha(int pixel) {
		return pixel & 0x000000ff;
	}

	@Override
	public int getBlue(int pixel) {
		return (pixel >>> 8) & 0x000000ff;
	}

	@Override
	public int getGreen(int pixel) {
		return (pixel >>> 16) & 0x000000ff;
	}

	@Override
	public int getRed(int pixel) {
		return pixel >>> 24;
	}
}
