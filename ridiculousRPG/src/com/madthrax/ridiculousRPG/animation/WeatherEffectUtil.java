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

package com.madthrax.ridiculousRPG.animation;

import com.madthrax.ridiculousRPG.GameBase;

/**
 * This class simplifies the usage of some frequently used weather effects. It
 * defines default implementations for rain and snow.
 * 
 * @author Alexander Baumgartner
 */
public abstract class WeatherEffectUtil {
	public enum Wind {
		NONE(0), NW_LOW(1), NW_MEDIUM(3), NW_STRONG(7), NW_MAXIMUM(12), NE_LOW(
				-1), NE_MEDIUM(-3), NE_STRONG(-7), NE_MAXIMUM(-12);
		private int val;

		Wind(int val) {
			this.val = val;
		}
	}

	/**
	 * Generates snow with default parameters.<br>
	 * You don't need to worry about calibrating the parameters.
	 * 
	 * @param wind
	 *            Use the enum inner class WeatherEffectUtil.Wind
	 */
	public static void generateSnow(ParticleEffectService engine, Wind wind) {
		engine.addWeatherLayerTimes(GameBase.$options().weatherEffectSnow, 50,
				.2f + .1f * Math.abs(wind.val), .07f * wind.val, 3,
				10 + 30 / wind.val);
	}

	/**
	 * Generates rain with default parameters.<br>
	 * You don't need to worry about calibrating the parameters.
	 * 
	 * @param wind
	 *            Use the enum inner class WeatherEffectUtil.Wind
	 */
	public static void generateRain(ParticleEffectService engine, Wind wind) {
		engine.addWeatherLayerTimes(
				wind.val > 0 ? GameBase.$options().weatherEffectRainNW
						: GameBase.$options().weatherEffectRainNE, 50,
				1f + .67f * Math.abs(wind.val), .33f * wind.val, 2,
				10 + 30 / wind.val);
	}

	/**
	 * Stops the layer with index 0.
	 * 
	 * @see WeatherEffectEngine.stopLayer(0)
	 */
	public static void decreaseEffect(ParticleEffectService engine) {
		engine.stopLayer(0);
	}

	/**
	 * Adds one layer with snow.
	 * 
	 * @param wind
	 *            Use the enum inner class WeatherEffectUtil.Wind
	 */
	public static void increaseSnow(ParticleEffectService engine, Wind wind) {
		engine.addWeatherLayer(GameBase.$options().weatherEffectSnow, 50,
				.2f + .1f * Math.abs(wind.val), .07f * wind.val);
	}

	/**
	 * Adds one layer with rain.
	 * 
	 * @param wind
	 *            Use the enum inner class WeatherEffectUtil.Wind
	 */
	public static void increaseRain(ParticleEffectService engine, Wind wind) {
		engine.addWeatherLayer(
				wind.val > 0 ? GameBase.$options().weatherEffectRainNW
						: GameBase.$options().weatherEffectRainNE, 50,
				1f + .67f * Math.abs(wind.val), .33f * wind.val);
	}

	/**
	 * Stops the effect by removing one layer, waiting 20 seconds, removing the
	 * next layer,... The effect decreases until no mor layer exists.
	 * 
	 * @see WeatherEffectEngine.stop()
	 * @return
	 */
	public static void stop(ParticleEffectService engine) {
		engine.stop();
	}
}
