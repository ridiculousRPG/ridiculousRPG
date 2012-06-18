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

package com.madthrax.ridiculousRPG.util;

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
import com.madthrax.ridiculousRPG.GameBase;

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
	 * returns the {@link TextureRegionRef}, which is cropped to the size of the
	 * Pixmap.
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
	 * returns the {@link TextureRegionRef}, which is cropped to the size of the
	 * Pixmap.
	 * 
	 * @param filePath
	 *            The file to the picture
	 * @return A TextureRegion matching the given parameters
	 */
	public static TextureRegionRef load(FileHandle filePath) {
		return obtainCache(filePath).obtainRegion();
	}

	/**
	 * Obtains a texture region for drawing {@link Pixmap}s on it.<br>
	 * This method creates a texture region with an underlying texture (which is
	 * sized with the next powers of two) for drawing.
	 * 
	 * @param width
	 *            the width of the texture region
	 * @param height
	 *            the height of the texture region
	 * @param format
	 *            the format for drawing {@link Pixmap}s onto this TextureRegion
	 * @return A texture region with the specified width and height for drawing
	 *         on it.
	 */
	public static TextureRegionRef obtainEmptyRegion(int width, int height,
			final Format format) {
		final int safeWidth = MathUtils.nextPowerOfTwo(width);
		final int safeHeight = MathUtils.nextPowerOfTwo(height);
		TextureCache tCache;
		if (GameBase.$().isGlContextThread()) {
			tCache = new TextureCache(safeWidth, safeHeight, format);
		} else {
			final TextureCacheContainer tCC = new TextureCacheContainer();
			new ExecuteInMainThread() {
				@Override
				public void exec() {
					tCC.tCache = new TextureCache(safeWidth, safeHeight, format);
				}
			}.runWait();
			tCache = tCC.tCache;
		}
		return tCache.obtainRegion(0, 0, width, height);
	}

	private static TextureCache obtainCache(FileHandle filePath) {
		String fileName = filePath.path();
		TextureCache tCache = textureCache.get(fileName);
		if (tCache == null) {
			final Pixmap pm = new Pixmap(filePath);
			final int width = pm.getWidth();
			final int height = pm.getHeight();
			final int safeWidth = MathUtils.nextPowerOfTwo(width);
			final int safeHeight = MathUtils.nextPowerOfTwo(height);
			if (GameBase.$().isGlContextThread()) {
				if (width != safeWidth || height != safeHeight) {
					tCache = new TextureCache(safeWidth, safeHeight, pm
							.getFormat());
					tCache.setPixmap(pm, true);
				} else {
					tCache = new TextureCache(pm, true);
				}
			} else {
				final TextureCacheContainer tCC = new TextureCacheContainer();
				new ExecuteInMainThread() {
					@Override
					public void exec() {
						if (width != safeWidth || height != safeHeight) {
							tCC.tCache = new TextureCache(safeWidth,
									safeHeight, pm.getFormat());
							tCC.tCache.setPixmap(pm, true);
						} else {
							tCC.tCache = new TextureCache(pm, true);
						}
					}
				}.runWait();
				tCache = tCC.tCache;
			}
			textureCache.put(fileName, tCache);
			textureReverseCache.put(tCache, fileName);
		}
		return tCache;
	}

	/**
	 * Use {@link TextureRegionLoader#load} or
	 * {@link TextureRegionLoader#obtainEmptyRegion(int, int, Format)} to obtain
	 * a texture region.
	 * 
	 * @author Alexander Baumgartner
	 */
	public static class TextureRegionRef extends TextureRegion implements
			Disposable {
		/**
		 * Use {@link TextureRegionLoader#load} or
		 * {@link TextureRegionLoader#obtainEmptyRegion(int, int, Format)} to
		 * obtain a texture region.
		 */
		protected TextureRegionRef() {
		}

		/**
		 * Draws the {@link Pixmap} at position (0,0)
		 * 
		 * @param pm
		 *            The {@link Pixmap} to draw.
		 */
		public void draw(Pixmap pm) {
			getTexture().draw(pm, 0, 0);
		}

		/**
		 * Draws the {@link Pixmap} at position (x,y)
		 * 
		 * @param pm
		 *            The {@link Pixmap} to draw.
		 */
		public void draw(Pixmap pm, int x, int y) {
			getTexture().draw(pm, x, y);
		}

		@Override
		public void dispose() {
			getTexture().dispose();
			textureRegionPool.free(this);
		}
	}

	private static class TextureCacheContainer {
		TextureCache tCache;
	}

	/**
	 * Use {@link TextureRegionLoader#load} if possible
	 * 
	 * @author Alexander Baumgartner
	 */
	protected static class TextureCache extends Texture {
		// reference count
		private int count;
		// width of the pixmap
		private int width;
		// height of the pixmap
		private int height;
		// The pixmap can automatically be disposed
		private Pixmap pixmap;

		/**
		 * Use {@link TextureRegionLoader#load} if possible
		 */
		protected TextureCache(Pixmap pm, boolean autoDisposePixmap) {
			super(pm);
			width = pm.getWidth();
			height = pm.getHeight();
			if (autoDisposePixmap)
				pixmap = pm;
		}

		/**
		 * Sets the specified {@link Pixmap} for this {@link TextureCache}. This
		 * method shouldn't be used outside of the implementation, because it
		 * effects all {@link TextureRegionRef}s which are already instantiaded
		 * from this {@link TextureCache}.
		 * 
		 * @param pm
		 * @param autoDisposePixmap
		 */
		protected void setPixmap(Pixmap pm, boolean autoDisposePixmap) {
			draw(pm, 0, 0);
			if (autoDisposePixmap) {
				if (pixmap != null)
					pixmap.dispose();
				pixmap = pm;
			}
		}

		/**
		 * Use {@link TextureRegionLoader#load} if possible
		 */
		protected TextureCache(int width, int height, Format format) {
			super(width, height, format);
		}

		/**
		 * Returns the entire region, which is sized by the loaded
		 * {@link Pixmap}'s size. Normally this should be the entire picture
		 * represented by this {@link TextureCache}
		 * 
		 * @return
		 */
		public TextureRegionRef obtainRegion() {
			return obtainRegion(0, 0, width, height);
		}

		/**
		 * Retuns the specified region of this texture.
		 * 
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 * @return
		 */
		public TextureRegionRef obtainRegion(int x, int y, int width, int height) {
			count++;
			TextureRegionRef c = textureRegionPool.obtain();
			c.setTexture(this);
			c.setRegion(x, y, width, height);
			return c;
		}

		@Override
		public void draw(Pixmap pm, int x, int y) {
			super.draw(pm, x, y);
			width = pm.getWidth();
			height = pm.getHeight();
		}

		@Override
		public void dispose() {
			count--;
			if (count == 0) {
				textureCache.remove(textureReverseCache.remove(this));
				if (GameBase.$().isGlContextThread()) {
					super.dispose();
					if (pixmap != null)
						pixmap.dispose();
				} else {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							TextureCache.super.dispose();
							if (pixmap != null)
								pixmap.dispose();
						}
					});
				}
				pixmap = null;
			}
		}
	}
}
