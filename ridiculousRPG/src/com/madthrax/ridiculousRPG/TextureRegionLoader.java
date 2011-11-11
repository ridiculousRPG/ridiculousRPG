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

package com.madthrax.ridiculousRPG;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * This class is used to load and cache textures. It automatically adds a
 * padding if the textures width or height isn't a power of two.<br>
 * <br>
 * The implementation should perform well. Don't hesitate to use it whenever you
 * need a texture.
 * 
 * @author Alexander Baumgartner
 */
public final class TextureRegionLoader {
	private TextureRegionLoader() {
	}// static container

	static HashMap<String, TextureCache> textureCache = new HashMap<String, TextureCache>(
			128);
	static HashMap<TextureCache, String> textureReverseCache = new HashMap<TextureCache, String>(
			128);
	static Pool<TextureRegionRef> textureRegionPool = new Pool<TextureRegionRef>(
			512, 8192) {
		@Override
		protected TextureRegionRef newObject() {
			return new TextureRegionRef();
		}
	};

	/**
	 * This method loads the Pixmap, adds some padding if it's not sized with
	 * powers of two, creates a {@link TextureCache} and caches the created
	 * Texture. Then it obtains a {@link TextureRegionRef} from the pool and
	 * returns the requested {@link TextureRegionRef}
	 * 
	 * @param internalPath
	 *            The path to the picture
	 * @param x
	 *            top left corner
	 * @param y
	 *            top left corner
	 * @param width
	 *            the regions width
	 * @param height
	 *            the regions height
	 * @return A TextureRegion matching the given parameters
	 */
	public static TextureRegionRef load(String internalPath, int x, int y,
			int width, int height) {
		return load(Gdx.files.internal(internalPath), x, y, width, height);
	}

	/**
	 * This method loads the Pixmap, adds some padding if it's not sized with
	 * powers of two, creates a {@link TextureCache} and caches the created
	 * Texture. Then it obtains a {@link TextureRegionRef} from the pool and
	 * returns the requested {@link TextureRegionRef}
	 * 
	 * @param filePath
	 *            The file to the picture
	 * @param x
	 *            top left corner
	 * @param y
	 *            top left corner
	 * @param width
	 *            the regions width
	 * @param height
	 *            the regions height
	 * @return A TextureRegion matching the given parameters
	 */
	public static TextureRegionRef load(FileHandle filePath, int x, int y,
			int width, int height) {
		return obtainCache(filePath).obtainRegion(x, y, width, height);
	}

	/**
	 * This method loads the Pixmap, adds some padding if it's not sized with
	 * powers of two, creates a {@link TextureCache} and caches the created
	 * Texture. Then it obtains a {@link TextureRegionRef} from the pool and
	 * returns the {@link TextureRegionRef}, which is cropped to the size of
	 * the Pixmap.
	 * 
	 * @param internalPath
	 *            The path to the picture
	 * @return A TextureRegion matching the given parameters
	 */
	public static TextureRegionRef load(String internalPath) {
		return load(Gdx.files.internal(internalPath));
	}

	/**
	 * This method loads the Pixmap, adds some padding if it's not sized with
	 * powers of two, creates a {@link TextureCache} and caches the created
	 * Texture. Then it obtains a {@link TextureRegionRef} from the pool and
	 * returns the {@link TextureRegionRef}, which is cropped to the size of
	 * the Pixmap.
	 * 
	 * @param filePath
	 *            The file to the picture
	 * @return A TextureRegion matching the given parameters
	 */
	public static TextureRegionRef load(FileHandle filePath) {
		return obtainCache(filePath).obtainRegion();
	}

	private static TextureCache obtainCache(FileHandle filePath) {
		String fileName = filePath.name();
		TextureCache tCache = textureCache.get(fileName);
		if (tCache == null) {
			Pixmap pm = new Pixmap(filePath);
			int width = pm.getWidth();
			int height = pm.getHeight();
			int safeWidth = MathUtils.nextPowerOfTwo(width);
			int safeHeight = MathUtils.nextPowerOfTwo(height);
			if (width != safeWidth || height != safeHeight) {
				tCache = new TextureCache(safeWidth, safeHeight, pm.getFormat());
				tCache.draw(pm, 0, 0);
			} else {
				tCache = new TextureCache(pm);
			}
			textureCache.put(fileName, tCache);
			textureReverseCache.put(tCache, fileName);
		}
		return tCache;
	}

	/**
	 * Use {@link TextureRegionLoader#load} if possible
	 * @author Alexander Baumgartner
	 */
	public static class TextureRegionRef extends TextureRegion implements
			Disposable {
		@Override
		public void dispose() {
			getTexture().dispose();
			textureRegionPool.free(this);
		}
	}

	/**
	 * Use {@link TextureRegionLoader#load} if possible
	 * @author Alexander Baumgartner
	 */
	public static class TextureCache extends Texture {
		// reference count
		private int count;
		// width of the pixmap
		private int width;
		// height of the pixmap
		private int height;

		/**
		 * Use {@link TextureRegionLoader#load} if possible
		 */
		public TextureCache(Pixmap pm) {
			super(pm);
			width = pm.getWidth();
			height = pm.getHeight();
		}

		/**
		 * Use {@link TextureRegionLoader#load} if possible
		 */
		public TextureCache(int width, int height, Format format) {
			super(width, height, format);
		}

		public TextureRegionRef obtainRegion() {
			count++;
			TextureRegionRef c = textureRegionPool.obtain();
			c.setTexture(this);
			c.setRegion(0, 0, width, height);
			return c;
		}

		public TextureRegionRef obtainRegion(int x, int y, int width,
				int height) {
			count++;
			TextureRegionRef c = textureRegionPool.obtain();
			c.setTexture(this);
			c.setRegion(x, y, width, height);
			return c;
		}

		public void draw(Pixmap pm, int x, int y) {
			super.draw(pm, x, y);
			width = pm.getWidth();
			height = pm.getHeight();
		}

		@Override
		public void dispose() {
			if (--count == 0) {
				textureCache.remove(textureReverseCache.remove(this));
				super.dispose();
			}
		}
	}
}
