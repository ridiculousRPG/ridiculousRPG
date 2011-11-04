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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.madthrax.ridiculousRPG.events.BlockingBehaviour;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.events.TriggerEventHandler;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.ui.DisplayTextService;

// THIS CLASS IS OPTIMIZED FOR PERFORMANCE NOT FOR READABILITY!
// SOMETIMES THE CODE COULD LOOK A LITTLE STRANGE (NOT PRETTY JAVA-STYLED)
/**
 * This class represents a tiled map with events on this map.<br>
 * The events may move around on the map.<br>
 * Don't forget to dispose the map and all events if you don't need it anymore.
 * @author Alexander Baumgartner
 */
public class TiledMapWithEvents implements MapWithEvents<EventObject> {
	private TileAtlas atlas;
	private int width, height;
	private int tileWidth, tileHeight;

	private int idCount = -1;
	private IntMap<Object> usedIds = new IntMap<Object>();
	private static final Object DUMMY = new Object();
	private static ShapeRenderer debugRenderer;

	// tiles
	private MapRenderRegion[] staticRegions;
	// events
	private List<EventObject> dynamicRegions = new ArrayList<EventObject>(50);
	// named events
	private Map<String, EventObject> namedRegions = new HashMap<String, EventObject>(30);

	private Computable triggerEventHandler;

	private static final char EVENT_CUSTOM_PROP_KZ = '$';
	private static final String EVENT_PROP_ID = "ID";

	/**
	 * Creates a new map with the specified events from a tmx file.<br>
	 * tmx files can be created by using the Tiled editor.
	 * @param tmxFile
	 */
	public TiledMapWithEvents(FileHandle tmxFile) {
		TiledMap map = TiledLoader.createMap(tmxFile);
		tileWidth = map.tileWidth;
		tileHeight = map.tileHeight;
		width = map.width * tileWidth;
		height = map.height * tileHeight;
		atlas = new TileAtlas(map, tmxFile.parent());
		int i,j,k,len_i,len_j,len_k,layer_z,z;
		int[][] layerTiles;
		int[] row;
		String prop;
		ArrayList<MapRenderRegion> alTmp = new ArrayList<MapRenderRegion>(1000);
		for (i=0, len_i=map.layers.size(); i<len_i; i++) {
			layerTiles = map.layers.get(i).tiles;
			prop = map.layers.get(i).properties.get("height");
			layer_z = 0;
			if (prop!=null && prop.length()>0) try {
				layer_z = Integer.parseInt(prop);
			} catch (NumberFormatException e) {}
			for (j=0, len_j=layerTiles.length; j<len_j; j++) {
				row = layerTiles[j];
				float rowY = (len_j-(j+1))*map.tileHeight;
				for (k=0, len_k=row.length; k<len_k; k++) {
					int tile = row[k];
					if (tile>0){
						z = layer_z;
						prop = map.getTileProperty(tile, "height");
						if (prop!=null && prop.length()>0) try {
							z += Integer.parseInt(prop);
						} catch (NumberFormatException e) {}
						AtlasRegion region = (AtlasRegion) atlas.getRegion(tile);
						alTmp.add(new MapRenderRegion(region, k*map.tileWidth+region.offsetX, rowY+region.offsetY, z));
					}
				}
			}
		}
		Collections.sort(alTmp);
		staticRegions = alTmp.toArray(new MapRenderRegion[alTmp.size()]);
		for (i=0, len_i=map.objectGroups.size(); i<len_i; i++) {
			TiledObjectGroup group = map.objectGroups.get(i);
			for (j=0, len_j=group.objects.size(); j<len_j; j++) {
				TiledObject object = group.objects.get(j);
				EventObject ev = new EventObject(object, group, atlas, map);
				parseProperties(ev, object.properties);
				put(object.name, ev);
			}
		}
		// insert half-planes around the map
		EventObject ev;
		put(null, ev = new EventObject());
		ev.touchBound = new Rectangle(-1000f, -1000f, width+2000f, 1000f);
		put(null, ev = new EventObject());
		ev.touchBound = new Rectangle(-1000f, -1000f, 1000f, height+2000f);
		put(null, ev = new EventObject());
		ev.touchBound = new Rectangle(-1000f, height, width+2000f, 1000f);
		put(null, ev = new EventObject());
		ev.touchBound = new Rectangle(width, -1000f, 1000f, height+2000f);

		triggerEventHandler = new TriggerEventHandler(dynamicRegions);
	}
	/**
	 * Method to parse the object properties input.
	 * @param ev
	 * @param props
	 */
	protected void parseProperties(EventObject ev, HashMap<String, String> props) {
		//TODO: Define EventHandler, animation, height, ... as Tiled properties
		for (Entry<String, String> entry : props.entrySet()) {
			String key = entry.getKey();
			if (key.length()==0) continue;
			String val = entry.getValue();
			if (entry.getKey().charAt(0)==EVENT_CUSTOM_PROP_KZ) {
				ev.properties.put(key, val);
			} else if (EVENT_PROP_ID.equals(key)) {
				try {
					ev.id = Integer.parseInt(val);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public EventObject put(String name, EventObject event) {
		computeId(event);
		EventObject old = null;
		if (name!=null && name.length()>0) {
			old = namedRegions.put(name, event);
			if (old!=null) {
				dynamicRegions.remove(old);
			}
		}
		dynamicRegions.add(event);
		return old;
	}
	public void put(EventObject event) {
		computeId(event);
		dynamicRegions.add(event);
	}
	private void computeId(EventObject event) {
		int id = event.id;
		if (id==-1) {
			event.id = nextId();
		} else if (id<idCount) {
			for (int i = 0, len = dynamicRegions.size(); i < len; i++) {
				if (dynamicRegions.get(i).id == id) {
					dynamicRegions.get(i).id = nextId();
					return;
				}
			}
		} else {
			usedIds.put(id, DUMMY);
		}
	}
	private int nextId() {
		int idCount = this.idCount+1;
		while (usedIds.containsKey(idCount)) {
			usedIds.remove(idCount);
			idCount++;
		}
		this.idCount = idCount;
		return idCount;
	}
	public EventObject get(String name) {
		return namedRegions.get(name);
	}
	public List<EventObject> getAllEvents() {
		return dynamicRegions;
	}
	public EventObject remove(String name) {
		EventObject old = namedRegions.remove(name);
		if (old!=null) {
			dynamicRegions.remove(old);
		}
		return old;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	/**
	 * Width of one tile
	 * @return
	 */
	public int getTileWidth() {
		return tileWidth;
	}
	/**
	 * Height of one tile
	 * @return
	 */
	public int getTileHeight() {
		return tileHeight;
	}
	/**
	 * Move all events on the map, animate events, compute reachable events,...
	 * @param deltaTime
	 */
	public void compute(float deltaTime, boolean actionKeyPressed) {
		triggerEventHandler.compute(deltaTime, actionKeyPressed);
	}
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		List<EventObject> dynamicRegions = this.dynamicRegions;
		Collections.sort(dynamicRegions);
		// Load pointers into register
		MapRenderRegion[] staticRegions = this.staticRegions;
		MapRenderRegion region;
		Rectangle drawBound;
		// Load variables into register
		float camX1 = camera.position.x;
		float camX2 = camera.position.x+camera.viewportWidth;
		float camY1 = camera.position.y;
		float camY2 = camera.position.y+camera.viewportHeight;
		float rX, rY;

		int i = 0;
		EventObject event = dynamicRegions.get(0); // is never empty
		int dynSize = dynamicRegions.size();
		// If there are performance problems:
		// 1) Add only MapRenderRegions with z>0 to staticRegions
		// 2) Build a new 3-dim array with [row][col][layer] for all MapRenderRegions with z==0
		// 3) compute firstRow, lastRow, firstCol, lastCol
		// 4) iterate over [firstRow<row<lastRow][firstCol<col<lastCol][allLayers]
		// com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer is not usable (Tested and felt really bad)
		for (int j=0, statSize=staticRegions.length; j < statSize; j++) {
			region = staticRegions[j];
			rX = region.x;
			rY = region.y;
			if (rX < camX2 &&
				rY < camY2 &&
				rX+region.width > camX1 &&
				rY+region.height > camY1) {
				while(dynSize>i && event.compareTo(region)==-1) {
					if (event.visible) {
						drawBound = event.drawBound;
						if (drawBound.x < camX2 && 
							drawBound.y < camY2 && 
							drawBound.x + drawBound.width > camX1 && 
							drawBound.y + drawBound.height > camY1)
							event.draw(spriteBatch);
					}
					event = dynamicRegions.get(++i);
				}
				region.draw(spriteBatch);
			}
		}
		while(dynSize>i) {
			event = dynamicRegions.get(i);
			if (event.visible) {
				drawBound = event.drawBound;
				if (drawBound.x < camX2 && 
					drawBound.y < camY2 && 
					drawBound.x + drawBound.width > camX1 && 
					drawBound.y + drawBound.height > camY1)
				event.draw(spriteBatch);
			}
			i++;
		}
		if (debug) {
			spriteBatch.end();
			if (debugRenderer==null) debugRenderer = new ShapeRenderer();
			debugRenderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
			debugRenderer.begin(ShapeType.Rectangle);
			for (EventObject ev : dynamicRegions) {
				if (ev.visible) {
					debugRenderer.setColor(.7f, .7f, .7f, 1f);
					debugRenderer.rect(ev.drawBound.x, ev.drawBound.y, ev.drawBound.width, ev.drawBound.height);
				}
				if (!ev.blockingBehaviour.blocks(BlockingBehaviour.PASSES_NO_BARRIER)) {
					debugRenderer.setColor(0f, 1f, 0f, 1f);
				} else if (!ev.blockingBehaviour.blocks(BlockingBehaviour.PASSES_ALL_BARRIERS)) {
					debugRenderer.setColor(1f, 1f, 0f, 1f);
				} else {
					debugRenderer.setColor(1f, 0f, 0f, 1f);
				}
				debugRenderer.rect(ev.getX(), ev.getY(), ev.getWidth(), ev.getHeight());
				if (ev.name!=null)
					DisplayTextService.$map.addMessage(ev.name, DisplayTextService.$map.defaultColor, ev.drawBound.x+2f, ev.drawBound.y+ev.drawBound.height-2, 0f, true);
			}
			debugRenderer.end();
			spriteBatch.begin();
		}
	}
	public void dispose() {
		if (triggerEventHandler instanceof Disposable) {
			((Disposable) triggerEventHandler).dispose();
		}
		if (atlas!=null) atlas.dispose();
		staticRegions = null;
		dynamicRegions = null;
		namedRegions = null;
	}
	public void dispose(boolean disposeAllEvents) {
		if (disposeAllEvents) {
			for (int i=0, size=dynamicRegions.size(); i<size; i++) {
				dynamicRegions.get(i).dispose();
			}
		}
		dispose();
	}
}
