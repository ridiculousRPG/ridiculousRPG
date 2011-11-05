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

package com.madthrax.ridiculousRPG.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * @author Alexander Baumgartner
 */
public class MapRenderRegion implements Comparable<MapRenderRegion> {
	public float x, y, z, yz;
	public int width, height;
	private TextureRegion region;

	public MapRenderRegion(TextureRegion region, float x, float y, float z) {
		this.region = region;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yz = y - z;
		this.width = region.getRegionWidth();
		this.height = region.getRegionHeight();
	}

	public int compareTo(MapRenderRegion o) {
		if (o.z == 0) {
			if (z == 0)
				return 0;
			return 1;
		} else if (yz > o.yz || z == 0) {
			return -1;
		} else if (yz < o.yz) {
			return 1;
		}
		return 0;
	}

	public void draw(SpriteBatch spriteBatch) {
		spriteBatch.draw(region, x, y, width, height);
	}
}
