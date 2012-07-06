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

import java.util.IdentityHashMap;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.utils.Pool;

/**
 * This Class is used for reusing free {@link BitmapFontCache} objects
 * 
 * @author Alexander Baumgartner
 */
public class BitmapFontCachePool {
	private IdentityHashMap<BitmapFont, Pool<BitmapFontCache>> pool = new IdentityHashMap<BitmapFont, Pool<BitmapFontCache>>();

	/**
	 * Returns an object from this pool. The object may be new or reused
	 * (previously {@link #free(Object) freed}).<br>
	 * You have to dispose the fonts by yourself.
	 * 
	 * @param font
	 * @return
	 */
	public BitmapFontCache obtain(final BitmapFont font) {
		Pool<BitmapFontCache> fontCache = pool.get(font);
		if (fontCache == null) {
			fontCache = new Pool<BitmapFontCache>(64, 256) {

				@Override
				protected BitmapFontCache newObject() {
					return new BitmapFontCache(font);
				}
			};
			pool.put(font, fontCache);
		}
		return fontCache.obtain();
	}

	public void free(BitmapFontCache cache) {
		pool.get(cache.getFont()).free(cache);
	}

	public void clear() {
		pool.clear();
	}
}
