package com.madthrax.ridiculousRPG.events;

public enum Speed {
	S00_ZERO(0), S01_CRAWL(12), S02_STROLL(20), S03_XXX_SLOW(30), S04_XX_SLOW(45), S05_X_SLOW(68),
	S06_SLOW(100), S07_NORMAL(140), S08_FAST(190), S09_X_FAST(250), S10_XX_FAST(320), S11_XXX_FAST(420),
	S12_EXTREME_SPEED(600), S13_SOUND_SPEED(850), S14_LIGHT_SPEED(1200), S15_RIDICULOUS_SPEED(2000);

	private final int pixelPerSecond;

	private Speed(int pixelPerSecond) {
		this.pixelPerSecond = pixelPerSecond;
	}

	public int getPixelPerSecond() {
		return pixelPerSecond;
	}
	public float computeStretch(float deltaTime) {
		return pixelPerSecond * deltaTime;
	}
}
