package com.madthrax.ridiculousRPG.pixelwrap;

import java.awt.Point;
import java.awt.image.WritableRaster;

public class PixmapRaster extends WritableRaster {

	protected PixmapRaster(int width, int height) {
		super(new PixmapSampleModel(width, height), new Point(0, 0));
	}
}
