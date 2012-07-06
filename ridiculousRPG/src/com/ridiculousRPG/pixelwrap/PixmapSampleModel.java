package com.ridiculousRPG.pixelwrap;

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

public class PixmapSampleModel extends SampleModel {

	public PixmapSampleModel(int width, int height) {
		super(DataBuffer.TYPE_INT, width, height, 1);
	}

	@Override
	public void setSample(int x, int y, int b, int s, DataBuffer data) {
		data.setElem(b, y * getWidth() + x, s);
	}

	@Override
	public void setDataElements(int x, int y, Object obj, DataBuffer data) {
		data.setElem(y * getWidth() + x, ((int[]) obj)[0]);
	}

	@Override
	public int getSampleSize(int band) {
		return 32;
	}

	@Override
	public int[] getSampleSize() {
		return new int[] { 32 };
	}

	@Override
	public int getSample(int x, int y, int b, DataBuffer data) {
		return data.getElem(b, y * getWidth() + x);
	}

	@Override
	public int getNumDataElements() {
		return 1;
	}

	@Override
	public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
		int[] result;
		if (obj == null)
			result = new int[1];
		else
			result = (int[]) obj;
		result[0] = data.getElem(y * getWidth() + x);
		return result;
	}

	@Override
	public SampleModel createSubsetSampleModel(int[] bands) {
		// we have only one band, which can be accessed with
		// any band index ;)
		return this;
	}

	@Override
	public DataBuffer createDataBuffer() {
		return new PixmapDataBuffer(width, height);
	}

	@Override
	public SampleModel createCompatibleSampleModel(int w, int h) {
		return new PixmapSampleModel(w, h);
	}
}
