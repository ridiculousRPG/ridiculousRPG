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

package com.ridiculousRPG.util;

/**
 * @author Alexander Baumgartner
 */
public enum Speed {
	S00_ZERO(0), S01_CRAWL(12), S02_STROLL(20), S03_XXX_SLOW(30), S04_XX_SLOW(
			45), S05_X_SLOW(68), S06_SLOW(100), S07_NORMAL(140), S08_FAST(190), S09_X_FAST(
			250), S10_XX_FAST(320), S11_XXX_FAST(420), S12_EXTREME_SPEED(600), S13_SOUND_SPEED(
			850), S14_LIGHT_SPEED(1200), S15_RIDICULOUS_SPEED(2000);

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

	public float computeStretchJump(float deltaTime) {
		return Math.max(pixelPerSecond * 1.4f, 120f) * deltaTime;
	}

	public static Speed parse(String s) {
		if (s.length() < 3) {
			return Speed.values()[Integer.parseInt(s)];
		}
		return valueOf(s);
	}
}
