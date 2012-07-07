package com.ridiculousRPG.event;

import java.util.HashMap;
import java.util.Map;

import com.ridiculousRPG.map.tiled.TiledMapWithEvents;

public class PolygonObject {
	public String name;
	public float[] verticesX;
	public float[] verticesY;
	public String[] execAtNodeScript;
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public Map<String, String> properties = new HashMap<String, String>();
}
