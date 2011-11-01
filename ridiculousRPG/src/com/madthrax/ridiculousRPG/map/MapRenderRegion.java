package com.madthrax.ridiculousRPG.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MapRenderRegion implements Comparable<MapRenderRegion> {
	public float x,y,z,yz;
	public int width, height;
	private TextureRegion region;
	public MapRenderRegion(TextureRegion region, float x, float y, float z) {
		this.region = region;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yz = y-z;
		this.width = region.getRegionWidth();
		this.height = region.getRegionHeight();
	}
	@Override
	public int compareTo(MapRenderRegion o) {
		if (o.z==0) {
			if (z==0) return 0;
			return 1;
		} else if (yz > o.yz || z==0) {
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
