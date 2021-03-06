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

package com.ridiculousRPG.map.tiled;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
import com.ridiculousRPG.DebugHelper;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.event.EllipseObject;
import com.ridiculousRPG.event.EventFactory;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.event.EventTriggerAsync;
import com.ridiculousRPG.event.PolygonObject;
import com.ridiculousRPG.event.EventObject.MoveTransformation;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.map.MapLoader;
import com.ridiculousRPG.map.MapRenderRegion;
import com.ridiculousRPG.map.MapWithEvents;
import com.ridiculousRPG.util.BlockingBehavior;
import com.ridiculousRPG.util.ExecWithGlContext;
import com.ridiculousRPG.util.IntSet;
import com.ridiculousRPG.util.ObjectState;

/**
 * This class represents a tiled map with events on this map.<br>
 * The events may move around on the map.<br>
 * Don't forget to dispose the map and all events if you don't need it anymore.
 * 
 * @author Alexander Baumgartner
 */
public class TiledMapWithEvents implements MapWithEvents<EventObject> {
	private static final long serialVersionUID = 1L;

	private static transient MapLoader<EventObject> mapLoader;
	private transient TileAtlas atlas;
	private transient int width, height;
	private transient int tileWidth, tileHeight;
	private String tmxPath;

	private int localIdCount = 0;
	private int globalIdCount = 1000000000;
	// TODO: NEEDS REFACTORING
	private IntSet intSet = new IntSet();

	// tiles
	private transient MapRenderRegion[] staticRegions;
	// events
	private List<EventObject> dynamicRegions = new ArrayList<EventObject>(50);
	// named events
	private Map<String, EventObject> namedRegions = new HashMap<String, EventObject>(
			30);
	// polygons
	private List<PolygonObject> polyList = new ArrayList<PolygonObject>(16);
	// named polygons
	private Map<String, PolygonObject> polyMap = new HashMap<String, PolygonObject>(
			16);
	// ellipses
	private List<EllipseObject> ellipseList = new ArrayList<EllipseObject>(16);
	// named ellipses
	private Map<String, EllipseObject> ellipseMap = new HashMap<String, EllipseObject>(
			16);

	private static transient EventTrigger eventTrigger;
	private static final HashMap<Integer, ObjectState> EMPTY_MAP = new HashMap<Integer, ObjectState>();

	TiledMapWithEvents(String tmxPath) throws ScriptException {
		TiledMap map = loadTileMap(tmxPath);

		loadStaticTiles(map);
		loadEvents(map);
	}

	private TiledMap loadTileMap(String tmxPath) {
		this.tmxPath = tmxPath;
		final FileHandle tmxFile = Gdx.files.internal(tmxPath);
		final TiledMap map = TiledLoader.createMap(tmxFile);
		tileWidth = map.tileWidth;
		tileHeight = map.tileHeight;
		width = map.width * tileWidth;
		height = map.height * tileHeight;
		new ExecWithGlContext() {
			@Override
			public void exec() {
				atlas = new TileAtlas(map, tmxFile.parent());
			}
		}.runWait();
		return map;
	}

	private void loadStaticTiles(TiledMap map) {
		int i, j, k;
		int len_i, len_j, len_k;
		int z;
		ArrayList<MapRenderRegion> alTmp = new ArrayList<MapRenderRegion>(1000);
		for (i = 0, len_i = map.layers.size(); i < len_i; i++) {
			TiledLayer l = map.layers.get(i);
			if (EventFactory.isSkip(l.properties))
				continue;
			int[][] layerTiles = l.tiles;
			int layer_z = EventFactory.getZIndex(l.properties);
			for (j = 0, len_j = layerTiles.length; j < len_j; j++) {
				int[] row = layerTiles[j];
				float rowY = (len_j - (j + 1)) * map.tileHeight;
				for (k = 0, len_k = row.length; k < len_k; k++) {
					int tile = row[k];
					if (tile > 0) {
						z = layer_z + EventFactory.getZIndex(map, tile);
						AtlasRegion region = (AtlasRegion) atlas
								.getRegion(tile);
						if (region == null) {
							System.out.println("TILE-REGION " + tile
									+ " IS NULL");
						} else {
							alTmp.add(new MapRenderRegion(region, k
									* map.tileWidth + region.offsetX, rowY
									+ region.offsetY, z));
						}
					}
				}
			}
		}
		Collections.sort(alTmp);
		staticRegions = alTmp.toArray(new MapRenderRegion[alTmp.size()]);
	}

	private void loadEvents(TiledMap map) throws ScriptException {
		// TODO: Transformation for isometric maps
		MoveTransformation mvTrans = new MoveTransformation() {
			private static final long serialVersionUID = 1L;

			@Override
			public void set(float srcX, float srcY, Point2D.Float target) {
				target.x = srcX;
				target.y = srcY;
			}
		};
		int i, j, len_i, len_j;
		Map<Integer, ObjectState> eventsById = loadStateFromFS();
		for (i = 0, len_i = map.objectGroups.size(); i < len_i; i++) {
			TiledObjectGroup group = map.objectGroups.get(i);
			if (EventFactory.isSkip(group.properties))
				continue;
			for (j = 0, len_j = group.objects.size(); j < len_j; j++) {
				TiledObject object = group.objects.get(j);
				if (EventFactory.isSkip(object.properties))
					continue;

				if (object.polygon != null) {
					createPolygon(map, group, object, object.polygon, true);
				} else if (object.polyline != null) {
					createPolygon(map, group, object, object.polyline, false);
					/*
					 * } else if (isEllipse) { createEllipse(map, group,
					 * object);
					 */
				} else {
					createEvent(map, atlas, group, object, mvTrans);
				}
			}
		}
		Map<String, EventObject> globalEv = GameBase.$().getGlobalEventsClone();
		// Initialize the events
		for (i = 0, len_i = dynamicRegions.size(); i < len_i; i++) {
			EventObject eventObj = dynamicRegions.get(i);
			if (eventObj.isGlobalEvent()) {
				EventObject globalObj = globalEv.remove(eventObj.name);
				if (globalObj == null) {
					eventObj.init();
					GameBase.$().getGlobalEvents().put(eventObj.name, eventObj);
					if (eventObj.isPlayerEvent())
						eventObj.consumesEvent = true;
				} else {
					globalObj.clearCollision();
					put(globalObj.name, globalObj).dispose();
				}
			} else {
				if (eventObj.eventHandler != null
						&& eventsById.containsKey(eventObj.id)) {
					eventObj.eventHandler.setState(eventsById.get(eventObj.id));
				}
				eventObj.init();
			}
		}
		// Initialize polygons
		for (i = 0, len_i = polyList.size(); i < len_i; i++) {
			polyList.get(i).init();
		}
		// Initialize ellipses
		for (i = 0, len_i = ellipseList.size(); i < len_i; i++) {
			ellipseList.get(i).init();
		}

		for (EventObject globalObj : globalEv.values()) {
			globalObj.clearCollision();
			put(globalObj.name, globalObj);
		}

		// insert half-planes around the map
		EventObject ev;
		ev = new EventObject(mvTrans);
		ev.blockingBehavior = BlockingBehavior.ALL;
		ev.setTouchBound(new Rectangle2D.Float(-1000f, -1000f, width + 2000f,
				1000f));
		put(null, ev);
		ev = new EventObject(mvTrans);
		ev.blockingBehavior = BlockingBehavior.ALL;
		ev.setTouchBound(new Rectangle2D.Float(-1000f, -1000f, 1000f,
				height + 2000f));
		put(null, ev);
		ev = new EventObject(mvTrans);
		ev.blockingBehavior = BlockingBehavior.ALL;
		ev.setTouchBound(new Rectangle2D.Float(-1000f, height, width + 2000f,
				1000f));
		put(null, ev);
		ev = new EventObject(mvTrans);
		ev.blockingBehavior = BlockingBehavior.ALL;
		ev.setTouchBound(new Rectangle2D.Float(width, -1000f, 1000f,
				height + 2000f));
		put(null, ev);
	}

	private void createEvent(TiledMap map, TileAtlas atlas2,
			TiledObjectGroup group, TiledObject object,
			MoveTransformation mvTrans) {
		EventObject ev = new EventObject(mvTrans);
		float mapHeight = map.height * map.tileHeight;
		ev.name = object.name;
		ev.setType(object.type);
		if (object.gid > 0) {
			ev.gid = object.gid;
			ev.z = EventFactory.getZIndex(map, object.gid);
			AtlasRegion region = (AtlasRegion) atlas.getRegion(object.gid);
			ev.setImage(region);
			ev.addX(region.offsetX + object.x);
			ev.addY(mapHeight + region.offsetY - object.y);
			ev.drawBound.width = ev.getTouchBound().width = region
					.getRegionWidth();
			ev.drawBound.height = ev.getTouchBound().height = region
					.getRegionHeight();
		} else {
			ev.addX(object.x);
			ev.addY(mapHeight - object.y - object.height);
			ev.drawBound.width = ev.getTouchBound().width = object.width;
			ev.drawBound.height = ev.getTouchBound().height = object.height;
		}
		EventFactory.parseProps(ev, group.properties);
		EventFactory.parseProps(ev, object.properties);
		if (ev.visible && ev.z == 0)
			ev.z = .1f;
		put(object.name, ev);
	}

	private void createPolygon(TiledMap map, TiledObjectGroup group,
			TiledObject object, String polygon, boolean loop) {
		String name = object.name;
		String[] sa = polygon.split(" ");
		int len = loop ? sa.length + 1 : sa.length;
		float[] verticesX = new float[len];
		float[] verticesY = new float[len];
		float x = object.x;
		float y = map.height * map.tileHeight - object.y;
		for (int i = sa.length - 1; i > -1; i--) {
			String point = sa[i];
			// don't use regex split for performance reasons
			int index = point.indexOf(',');
			verticesX[i] = x + Integer.parseInt(point.substring(0, index));
			verticesY[i] = y - Integer.parseInt(point.substring(index + 1));
		}
		if (loop) {
			verticesX[len - 1] = verticesX[0];
			verticesY[len - 1] = verticesY[0];
		}
		PolygonObject poly = new PolygonObject(name, verticesX, verticesY, loop);
		EventFactory.parseProps(poly, group.properties);
		EventFactory.parseProps(poly, object.properties);

		polyList.add(poly);

		if (name != null && name.length() != 0)
			polyMap.put(name, poly);
	}

	@Override
	public PolygonObject findPolygon(String polygonName) {
		if (polyMap == null)
			return null;
		return polyMap.get(polygonName);
	}

	private void createEllipse(TiledMap map, TiledObjectGroup group,
			TiledObject object) {
		String name = object.name;
		float x = object.x;
		float y = map.height * map.tileHeight - object.y;
		EllipseObject ell = new EllipseObject(name, x, y, width, height);
		EventFactory.parseProps(ell, group.properties);
		EventFactory.parseProps(ell, object.properties);

		ellipseList.add(ell);

		if (name != null && name.length() != 0)
			ellipseMap.put(name, ell);
	}

	@Override
	public EllipseObject findEllipse(String ellipseName) {
		if (ellipseMap == null)
			return null;
		return ellipseMap.get(ellipseName);
	}

	/**
	 * Loads the current state from the file system.<br>
	 * The path used for loading the state is determined by the method
	 * {@link #getExternalSavePath()}.
	 * 
	 * @see #getExternalSavePath()
	 * @see #saveStateToFS()
	 * @return A map with {@link EventObject#id} as key and the state
	 *         {@link ObjectState} as value
	 */
	@SuppressWarnings("unchecked")
	protected Map<Integer, ObjectState> loadStateFromFS() {
		FileHandle fh = getExternalSavePath();
		if (fh.exists()) {
			try {
				ObjectInputStream stateIn = new ObjectInputStream(fh.read());
				Map<Integer, ObjectState> stateMap = (Map<Integer, ObjectState>) stateIn
						.readObject();
				stateIn.close();
				return stateMap;
			} catch (Exception e) {
				GameBase.$error("TiledMap.loadState",
						"Could not load the map state from the file system", e);
			}
		}
		return EMPTY_MAP;
	}

	/**
	 * Saves the current state to the file system.<br>
	 * The path used for saving the state is determined by the method
	 * {@link #getExternalSavePath()}.
	 * 
	 * @see #getExternalSavePath()
	 * @see #loadStateFromFS()
	 */
	@Override
	public void saveStateToFS() {
		FileHandle fh = getExternalSavePath();
		try {
			HashMap<Integer, ObjectState> eventsById = new HashMap<Integer, ObjectState>(
					100);
			ObjectOutputStream oOut = new ObjectOutputStream(fh.write(false));
			for (EventObject event : getAllEvents()) {
				EventHandler handler = event.eventHandler;
				if (handler != null) {
					eventsById.put(event.id, handler.getActualState());
				}
			}
			oOut.writeObject(eventsById);
			oOut.close();
		} catch (IOException e) {
			GameBase.$error("TiledMap.saveState",
					"Could not save the map state onto the file system", e);
		}
	}

	public EventObject put(String name, EventObject event) {
		computeId(event);
		EventObject old = null;
		if (name != null && name.length() > 0) {
			old = namedRegions.put(name, event);
		}
		if (old != null) {
			dynamicRegions.set(dynamicRegions.indexOf(old), event);
		} else {
			dynamicRegions.add(event);
		}
		return old;
	}

	public void put(EventObject event) {
		computeId(event);
		dynamicRegions.add(event);
	}

	// TODO: NEEDS REFACTORING
	private void computeId(EventObject event) {
		int id = event.id;
		if (id == -1) {
			nextId(event);
		} else if (id < (event.isGlobalEvent() ? globalIdCount : localIdCount)) {
			for (int i = 0, len = dynamicRegions.size(); i < len; i++) {
				if (dynamicRegions.get(i).id == id) {
					nextId(dynamicRegions.get(i));
					return;
				}
			}
		} else {
			intSet.put(id);
		}
	}

	// TODO: NEEDS REFACTORING
	private void nextId(EventObject eventToSet) {
		if (eventToSet.isGlobalEvent()) {
			while (intSet.containsKey(globalIdCount))
				globalIdCount++;
			eventToSet.id = globalIdCount;
			globalIdCount++;
		} else {
			while (intSet.containsKey(localIdCount))
				localIdCount++;
			eventToSet.id = localIdCount;
			localIdCount++;
		}
	}

	public EventObject get(String name) {
		return namedRegions.get(name);
	}

	public List<EventObject> getAllEvents() {
		return dynamicRegions;
	}

	public EventObject remove(String name) {
		EventObject old = namedRegions.remove(name);
		if (old != null) {
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
	 * 
	 * @return
	 */
	public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Height of one tile
	 * 
	 * @return
	 */
	public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * Move all events on the map, animate events, compute reachable events,...
	 * 
	 * @param deltaTime
	 */
	public void compute(float deltaTime, boolean actionKeyDown) {
		if (eventTrigger == null) {
			// Uses a shared context to load textures in other thread
			eventTrigger = new EventTriggerAsync();
		}
		eventTrigger
				.compute(deltaTime, actionKeyDown, dynamicRegions, polyList);
	}

	// TODO: NEEDS REFACTORING
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		List<EventObject> dynamicRegions = this.dynamicRegions;
		Collections.sort(dynamicRegions);
		// Load pointers into register
		MapRenderRegion[] staticRegions = this.staticRegions;
		MapRenderRegion region;
		Rectangle2D.Float drawBound;
		// Load variables into register
		float camX1 = camera.position.x;
		float camX2 = camera.position.x + camera.viewportWidth;
		float camY1 = camera.position.y;
		float camY2 = camera.position.y + camera.viewportHeight;

		int i = 0;
		EventObject event = dynamicRegions.get(0); // is never empty
		int dynSize = dynamicRegions.size();
		// If there are performance problems:
		// USE SPRITECACHE TO RENDER STATIC TILES!!!!!
		// 1) Add only MapRenderRegions with z>0 to staticRegions
		// 2) Build a new 3-dim array with [row][col][layer] for all
		// MapRenderRegions with z==0
		// 3) compute firstRow, lastRow, firstCol, lastCol
		// 4) iterate over
		// [firstRow<row<lastRow][firstCol<col<lastCol][allLayers]
		// com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer is not usable
		// (Tested and felt really bad)
		for (int j = 0, statSize = staticRegions.length; j < statSize; j++) {
			region = staticRegions[j];
			float rX = region.x;
			float rY = region.y;
			if (rX < camX2 && rY < camY2 && rX + region.width > camX1
					&& rY + region.height > camY1) {
				while (dynSize > i && event.compareTo(region) == -1) {
					if (event.visible) {
						drawBound = event.drawBound;
						if (drawBound.x < camX2 && drawBound.y < camY2
								&& drawBound.x + drawBound.width > camX1
								&& drawBound.y + drawBound.height > camY1)
							event.draw(spriteBatch);
					}
					i++;
					if (dynSize > i)
						event = dynamicRegions.get(i);
				}
				region.draw(spriteBatch);
			}
		}
		while (dynSize > i) {
			event = dynamicRegions.get(i);
			if (event.visible) {
				drawBound = event.drawBound;
				if (drawBound.x < camX2 && drawBound.y < camY2
						&& drawBound.x + drawBound.width > camX1
						&& drawBound.y + drawBound.height > camY1)
					event.draw(spriteBatch);
			}
			i++;
		}

		// draw polygon objects
		spriteBatch.end();
		PolygonObject.startPolygonBatch(spriteBatch.getProjectionMatrix());
		for (int j = 0, len = polyList.size(); j < len; j++)
			polyList.get(j).draw(debug);
		PolygonObject.endPolygonBatch();

		if (debug) {
			DebugHelper.debugEvents(dynamicRegions);
			DebugHelper.debugPolygons(polyList);
		}
		spriteBatch.begin();
	}

	public void dispose() {
		dispose(false);
	}

	public void dispose(final boolean recycle) {
		if (GameBase.$().isGlContextThread()) {
			disposeInternal(recycle);
		} else {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					disposeInternal(recycle);
				}
			});
		}
	}

	private void disposeInternal(boolean recycle) {
		for (int i = 0, size = dynamicRegions.size(); i < size; i++) {
			EventObject event = dynamicRegions.get(i);
			if (!event.isGlobalEvent()) {
				event.dispose();
			}
		}
		if (atlas != null)
			atlas.dispose();
		staticRegions = null;
		dynamicRegions = null;
		namedRegions = null;
		if (!recycle) {
			if (eventTrigger != null) {
				eventTrigger.dispose();
				eventTrigger = null;
			}
			if (mapLoader != null) {
				mapLoader.dispose();
				mapLoader = null;
			}
		}
	}

	@Override
	public FileHandle getExternalSavePath() {
		return GameBase.$tmpPath().child(
				tmxPath.replaceFirst("(?i)\\.tmx$", "").replaceAll("\\W", "_")
						+ ".sav");
	}

	/**
	 * Obtain the map loader to load tiled maps
	 */
	public static MapLoader<EventObject> getMapLoader() {
		if (mapLoader == null) {
			// Uses a shared context to load textures in other thread
			mapLoader = new TiledMapLoaderAsync();
		}
		return mapLoader;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();

		TiledMap map = loadTileMap(tmxPath);
		loadStaticTiles(map);
		for (EventObject ev : dynamicRegions) {
			if (ev.gid > 0)
				ev.setImage((AtlasRegion) atlas.getRegion(ev.gid));
		}
	}

}
