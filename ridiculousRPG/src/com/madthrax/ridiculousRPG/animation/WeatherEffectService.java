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

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * A container for weather effects. You can add effect-layers into this
 * container. Every effect-layer is simulated by a texture. Use textures which
 * are powers of 2 for your effects.
 * 
 * @author Alexander Baumgartner
 */
public class WeatherEffectService extends GameServiceDefaultImpl implements
		Computable, Drawable, ResizeListener {
	private ArrayList<EffectLayer> renderLayers = new ArrayList<EffectLayer>();
	private EffectLayer[] addLayer = new EffectLayer[10];
	private EffectLayer[] removeLayer = new EffectLayer[10];
	private float[] addLayerWait = new float[10];
	private float[] removeLayerWait = new float[10];
	int removePointer = 0;
	int addPointer = 0;

	/**
	 * Creates a new container for weather effects.<br>
	 * Inside this container you can add effect-layers by calling the method
	 * addLayer(...).<br>
	 * Add a new layer of the same effect to increase the weather effect.
	 */
	public WeatherEffectService() {
	}

	/**
	 * Applies one effect various times to this container.<br>
	 * It gives you a more realistic effect if a layer is applied more times.<br>
	 * The effect will be increased softly by the given interval.<br>
	 * The layer will be inserted at the end of the layer-list and therefore
	 * it's rendered at the last time. So it draws in front of the other layers.
	 * 
	 * @param path
	 *            Path to the texture. Width and height should be powers of 2.
	 * @param pixelOverlap
	 *            For a smoother effect you can overlap the tiled effect-layer.<br>
	 *            Therefore the texture has to be less dense at the edges. (The
	 *            bound is defined by pixelOverlap)<br>
	 *            Say 0 if you don't know what to do. (Negative values are
	 *            allowed but not useful in most cases)
	 * @param effectSpeed
	 *            The speed of the effect. Only positive values > 0 are useful!<br>
	 *            For most situations the value should be between 0.1 and 5<br>
	 *            (Try 0.5 for snow and 3.0 for rain)
	 * @param windSpeed
	 *            The speed of the wind. Positive values generate wind from west
	 *            to east and negative values from east to west.<br>
	 *            For most situations the value should be between -2.5 and 2.5<br>
	 *            (Try 0.3 (-0.3) for snow and 1.0 for rain)
	 * @param times
	 *            How many times should this layer be applied?
	 * @param waitIntervall
	 *            The time (in seconds) to wait until adding the next layer.
	 *            Maybe 10 seconds could be a good choice.
	 * @see addLayer(path, effectSpeed, windSpeed, layerIndex)
	 */
	public void addWeatherLayerTimes(String path, int pixelOverlap,
			float effectSpeed, float windSpeed, int times, float waitIntervall) {
		int width = (int) GameBase.$().getPlane().width;
		int height = (int) GameBase.$().getPlane().height;
		if (times > 0) {
			renderLayers.add(new WeatherEffectLayer(path, pixelOverlap, width,
					height, effectSpeed, windSpeed, 0, false));
		}
		ensureAddCapacity(times - 1);
		for (int i = 1; i < times; i++, addPointer++) {
			addLayer[addPointer] = new WeatherEffectLayer(path, pixelOverlap,
					width, height, effectSpeed, windSpeed, 0, false);
			addLayerWait[addPointer] = waitIntervall;
		}
	}

	private void ensureAddCapacity(int increment) {
		int maxLen = addLayer.length;
		int newLen = addPointer + increment;
		if (newLen > maxLen) {
			newLen = Math.max(newLen, maxLen + 10);
			WeatherEffectLayer[] addLayer = new WeatherEffectLayer[newLen];
			float[] addLayerWait = new float[newLen];
			System.arraycopy(this.addLayer, 0, addLayer, 0, maxLen);
			System.arraycopy(this.addLayerWait, 0, addLayerWait, 0, maxLen);
		}
	}

	private void ensureRemoveCapacity(int increment) {
		int maxLen = removeLayer.length;
		int newLen = removePointer + increment;
		if (newLen > maxLen) {
			newLen = Math.max(newLen, maxLen + 10);
			WeatherEffectLayer[] removeLayer = new WeatherEffectLayer[newLen];
			float[] removeLayerWait = new float[newLen];
			System.arraycopy(this.removeLayer, 0, removeLayer, 0, maxLen);
			System.arraycopy(this.removeLayerWait, 0, removeLayerWait, 0,
					maxLen);
		}
	}

	/**
	 * Adds a new effect-layer to this container. The layer will be inserted at
	 * the end of the layer-list and therefore it's rendered at the last time.
	 * So it draws in front of the other layers.
	 * 
	 * @param path
	 *            Path to the texture. Width and height should be powers of 2.
	 * @param effectSpeed
	 *            The speed of the effect. Only positive values > 0 are useful!<br>
	 *            For most situations the value should be between 0.1 and 5<br>
	 *            (Try 0.5 for snow and 3.0 for rain)
	 * @param windSpeed
	 *            The speed of the wind. Positive values generate wind from west
	 *            to east and negative values from east to west.<br>
	 *            For most situations the value should be between -2.5 and 2.5<br>
	 *            (Try 0.3 (-0.3) for snow and 1.0 for rain)
	 * @see addLayer(path, effectSpeed, windSpeed, layerIndex)
	 */
	public WeatherEffectLayer addWeatherLayer(String path, float effectSpeed,
			float windSpeed) {
		return addWeatherLayer(path, 0, effectSpeed, windSpeed,
				Integer.MAX_VALUE);
	}

	/**
	 * Adds a new effect-layer to this container. The layer will be inserted at
	 * the end of the layer-list and therefore it's rendered at the last time.
	 * So it draws in front of the other layers.
	 * 
	 * @param path
	 *            Path to the texture. Width and height should be powers of 2.
	 * @param pixelOverlap
	 *            For a smoother effect you can overlap the tiled effect-layer.<br>
	 *            Therefore the texture has to be less dense at the edges. (The
	 *            bound is defined by pixelOverlap)<br>
	 *            Say 0 if you don't know what to do. (Negative values are
	 *            allowed but not useful in most cases)
	 * @param effectSpeed
	 *            The speed of the effect. Only positive values > 0 are useful!<br>
	 *            For most situations the value should be between 0.1 and 5<br>
	 *            (Try 0.5 for snow and 3.0 for rain)
	 * @param windSpeed
	 *            The speed of the wind. Positive values generate wind from west
	 *            to east and negative values from east to west.<br>
	 *            For most situations the value should be between -2.5 and 2.5<br>
	 *            (Try 0.3 (-0.3) for snow and 1.0 for rain)
	 * @see addLayer(path, effectSpeed, windSpeed, layerIndex)
	 */
	public WeatherEffectLayer addWeatherLayer(String path, int pixelOverlap,
			float effectSpeed, float windSpeed) {
		return addWeatherLayer(path, pixelOverlap, effectSpeed, windSpeed,
				Integer.MAX_VALUE);
	}

	/**
	 * Adds a new effect-layer to this container. The layer will be inserted at
	 * the specified position of the layer-list.<br>
	 * Note: The last layer in the list is rendered last and therefore it's
	 * drawn in front of the other layers.
	 * 
	 * @param path
	 *            Path to the texture. Width and height should be powers of 2.
	 * @param pixelOverlap
	 *            For a smoother effect you can overlap the tiled effect-layer.<br>
	 *            Therefore the texture has to be less dense at the edges. (The
	 *            bound is defined by pixelOverlap)<br>
	 *            Say 0 if you don't know what to do. (Negative values are
	 *            allowed but not useful in most cases)
	 * @param effectSpeed
	 *            The speed of the effect. Only positive values > 0 are useful!<br>
	 *            For most situations the value should be between 0.1 and 5<br>
	 *            (Try 0.5 for snow and 3.0 for rain)
	 * @param windSpeed
	 *            The speed of the wind. Positive values generate wind from west
	 *            to east and negative values from east to west.<br>
	 *            For most situations the value should be between -2.5 and 2.5<br>
	 *            (Try 0.3 (-0.3) for snow and 1.0 for rain)
	 * @param layerIndex
	 *            The position for this layer to be rendered (position in the
	 *            layer-list).<br>
	 *            Use Integer.MAX_VALUE if you want to append the layer at the
	 *            end of the list.
	 * @see addLayer(path, effectSpeed, windSpeed)
	 */
	private WeatherEffectLayer addWeatherLayer(String path, int pixelOverlap,
			float effectSpeed, float windSpeed, int layerIndex) {
		int width = (int) GameBase.$().getPlane().width;
		int height = (int) GameBase.$().getPlane().height;
		WeatherEffectLayer newLayer = new WeatherEffectLayer(path,
				pixelOverlap, width, height, effectSpeed, windSpeed, 0, false);
		if (renderLayers.size() > layerIndex) {
			renderLayers.add(layerIndex, newLayer);
		} else {
			renderLayers.add(newLayer);
		}
		return newLayer;
	}

	/**
	 * Stops one layer.<br>
	 * Note that the layer is not removed immediately. It takes some time while
	 * the last snow flake falls from the sky to the ground - like in real
	 * nature ;)
	 * 
	 * @see disposeLayer(layerIndex)
	 * @param layerIndex
	 */
	public void stopLayer(int layerIndex) {
		if (renderLayers.size() > layerIndex) {
			renderLayers.get(layerIndex).stop();
		}
	}

	/**
	 * The same as stop(5f);
	 * 
	 * @see stop(stopTime)
	 * @see dispose()
	 */
	public void stop() {
		stop(5f);
	}

	/**
	 * Stops this weather effect by decreasing the effect, layer by layer.<br>
	 * You can specify the seconds roughly used for stopping the entire effect.<br>
	 * Note that the given stopTime cannot be exact because none of the layers
	 * is stopped immediate. It takes some time while the last snow flake falls
	 * from the sky to the ground - like in real nature ;)
	 * 
	 * @param waitIntervall
	 *            The time (in seconds) to wait until stopping the next layer.
	 *            Maybe 5 seconds could be a good choice.
	 * @see dispose()
	 */
	public void stop(float waitIntervall) {
		int len = renderLayers.size();
		if (len > 0)
			renderLayers.get(len - 1).stop();
		ensureRemoveCapacity(len - 1);
		for (int i = 1; i < len; i++) {
			EffectLayer layer = renderLayers.get(i);
			if (layer.isStopRequested())
				continue;
			layer.setStopRequested(true);
			removeLayer[removePointer] = layer;
			removeLayerWait[removePointer] = waitIntervall;
			removePointer++;
		}
	}

	/**
	 * Computes the weather effect animation.
	 */
	@Override
	public void compute(float deltaTime, boolean actionKeyDown) {
		if (addPointer > 0) {
			addLayerWait[addPointer - 1] -= deltaTime;
			if (addLayerWait[addPointer - 1] <= 0) {
				addPointer--;
				renderLayers.add(addLayer[addPointer]);
				addLayer[addPointer] = null;
			}
		}
		if (removePointer > 0) {
			removeLayerWait[removePointer - 1] -= deltaTime;
			if (removeLayerWait[removePointer - 1] <= 0) {
				removePointer--;
				removeLayer[removePointer].stop();
				removeLayer[removePointer] = null;
			}
		}
		for (int i = 0, len = renderLayers.size(); i < len;) {
			EffectLayer layer = renderLayers.get(i);
			if (layer.isFinished()) {
				disposeLayer(i);
			} else {
				layer.compute(deltaTime, actionKeyDown);
				i++;
			}
		}
	}

	/**
	 * Draws the entire weather effect.
	 */
	@Override
	public void draw(SpriteBatch batch, Camera cam, boolean debug) {
		for (int i = 0, len = renderLayers.size(); i < len; i++) {
			renderLayers.get(i).draw(batch, cam, debug);
		}
	}

	/**
	 * Stops one layer immediately and unloads the texture.
	 * 
	 * @param layerIndex
	 * @see stopLayer(layerIndex)
	 */
	public void disposeLayer(int layerIndex) {
		if (renderLayers.size() > layerIndex) {
			renderLayers.remove(layerIndex).dispose();
		}
	}

	/**
	 * Stops the effect immediately and unloads all textures.
	 * 
	 * @see stop()
	 */

	public void dispose() {
		for (int i = 0, len = renderLayers.size(); i < len; i++) {
			renderLayers.get(i).dispose();
		}
		renderLayers.clear();
		while (addPointer > 0) {
			addPointer--;
			addLayer[addPointer].dispose();
			addLayer[addPointer] = null;
		}
		while (removePointer > 0) {
			removePointer--;
			removeLayer[removePointer] = null;
		}
	}

	@Override
	public void resize(int width, int height) {
		int pixelWidth = (int) GameBase.$().getPlane().width;
		int pixelHeight = (int) GameBase.$().getPlane().height;
		for (int i = 0, len = renderLayers.size(); i < len; i++) {
			renderLayers.get(i).resize(pixelWidth, pixelHeight);
		}
		for (int i = addPointer - 1; i > -1; i--) {
			addLayer[i].resize(pixelWidth, pixelHeight);
		}
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.projection;
	}
}
