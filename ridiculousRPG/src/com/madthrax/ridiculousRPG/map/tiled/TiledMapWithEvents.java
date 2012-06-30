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

package com.madthrax.ridiculousRPG.map.tiled;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.DebugHelper;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.animation.TileAnimation;
import com.madthrax.ridiculousRPG.event.EventObject;
import com.madthrax.ridiculousRPG.event.EventTrigger;
import com.madthrax.ridiculousRPG.event.EventTriggerAsync;
import com.madthrax.ridiculousRPG.event.handler.EventExecScriptAdapter;
import com.madthrax.ridiculousRPG.event.handler.EventHandler;
import com.madthrax.ridiculousRPG.map.MapLoader;
import com.madthrax.ridiculousRPG.map.MapRenderRegion;
import com.madthrax.ridiculousRPG.map.MapWithEvents;
import com.madthrax.ridiculousRPG.movement.MovementHandler;
import com.madthrax.ridiculousRPG.util.BlockingBehavior;
import com.madthrax.ridiculousRPG.util.ExecuteInMainThread;
import com.madthrax.ridiculousRPG.util.IntSet;
import com.madthrax.ridiculousRPG.util.ObjectState;
import com.madthrax.ridiculousRPG.util.Speed;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

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
	private IntSet intSet = new IntSet();

	// tiles
	private transient MapRenderRegion[] staticRegions;
	// events
	private List<EventObject> dynamicRegions = new ArrayList<EventObject>(50);
	// named events
	private Map<String, EventObject> namedRegions = new HashMap<String, EventObject>(
			30);

	private static transient EventTrigger eventTrigger;

	private static final char EVENT_CUSTOM_PROP_KZ = '$';
	// the key is translated to lower case -> we are case insensitive
	private static final String EVENT_PROP_ID = "id";
	private static final String EVENT_PROP_DISPLAY = "display";
	private static final String EVENT_PROP_HEIGHT = "height";
	private static final String EVENT_PROP_OUTREACH = "outreach";
	private static final String EVENT_PROP_ROTATION = "rotation";
	private static final String EVENT_PROP_SCALEX = "scalex";
	private static final String EVENT_PROP_SCALEY = "scaley";
	private static final String EVENT_PROP_IMAGE = "image";
	private static final String EVENT_PROP_EFFECTFRONT = "effectfront";
	private static final String EVENT_PROP_EFFECTREAR = "effectrear";
	private static final String EVENT_PROP_CENTERIMAGE = "centerimage";
	private static final String EVENT_PROP_BLOCKING = "blocking";
	private static final String EVENT_PROP_MOVEHANDLER = "movehandler";
	private static final String EVENT_PROP_SPEED = "speed";
	private static final String EVENT_PROP_ANIMATION = "animation";
	private static final String EVENT_PROP_ESTIMATETOUCHBOUNDS = "estimatetouchbounds";
	private static final String EVENT_PROP_HANDLER = "eventhandler";
	// the following properties can not be mixed with an eventhandler
	// which doesn't extend the EventExecScriptAdapter
	private static final String EVENT_PROP_ONPUSH = "onpush";
	private static final String EVENT_PROP_ONTOUCH = "ontouch";
	private static final String EVENT_PROP_ONTIMER = "ontimer";
	private static final String EVENT_PROP_ONCUSTOMEVENT = "oncustomevent";
	private static final String EVENT_PROP_ONLOAD = "onload";

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
		if (GameBase.$().isGlContextThread()) {
			atlas = new TileAtlas(map, tmxFile.parent());
		} else {
			new ExecuteInMainThread() {
				@Override
				public void exec() {
					atlas = new TileAtlas(map, tmxFile.parent());
				}
			}.runWait();
		}
		return map;
	}

	private void loadStaticTiles(TiledMap map) {
		int i;
		int j;
		int k;
		int len_i;
		int len_j;
		int len_k;
		int z;
		ArrayList<MapRenderRegion> alTmp = new ArrayList<MapRenderRegion>(1000);
		for (i = 0, len_i = map.layers.size(); i < len_i; i++) {
			TiledLayer l = map.layers.get(i);
			if ("false".equals(l.properties.get(EVENT_PROP_DISPLAY)))
				continue;
			int[][] layerTiles = l.tiles;
			String prop = l.properties.get(EVENT_PROP_HEIGHT);
			int layer_z = 0;
			if (prop != null && prop.length() > 0)
				try {
					layer_z = Integer.parseInt(prop);
				} catch (NumberFormatException e) {
				}
			for (j = 0, len_j = layerTiles.length; j < len_j; j++) {
				int[] row = layerTiles[j];
				float rowY = (len_j - (j + 1)) * map.tileHeight;
				for (k = 0, len_k = row.length; k < len_k; k++) {
					int tile = row[k];
					if (tile > 0) {
						z = layer_z;
						prop = map.getTileProperty(tile, EVENT_PROP_HEIGHT);
						if (prop != null && prop.length() > 0)
							try {
								z += Integer.parseInt(prop);
							} catch (NumberFormatException e) {
							}
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
		int i;
		int j;
		int len_i;
		int len_j;
		String prop;
		EventObject ev;
		Map<Integer, ObjectState> eventsById = loadStateFromFS();
		for (i = 0, len_i = map.objectGroups.size(); i < len_i; i++) {
			TiledObjectGroup group = map.objectGroups.get(i);
			if ("false".equals(group.properties.get(EVENT_PROP_DISPLAY)))
				continue;
			for (j = 0, len_j = group.objects.size(); j < len_j; j++) {
				TiledObject object = group.objects.get(j);
				if ("false".equals(object.properties.get(EVENT_PROP_DISPLAY)))
					continue;
				ev = new EventObject(object, group, atlas, map);
				if (object.gid > 0) {
					prop = map.getTileProperty(object.gid, EVENT_PROP_HEIGHT);
					if (prop != null && prop.length() > 0) {
						ev.z += Integer.parseInt(prop);
					}
				}
				parseProperties(ev, group.properties);
				parseProperties(ev, object.properties);
				put(object.name, ev);
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
					if (EventObject.EVENT_TYPE_PLAYER
							.equalsIgnoreCase(eventObj.type))
						eventObj.consumeInput = true;
				} else {
					globalObj.clearCollision();
					put(globalObj.name, globalObj).dispose();
				}
			} else {
				if (eventObj.getEventHandler() != null
						&& eventsById.containsKey(eventObj.id)) {
					eventObj.getEventHandler().setState(
							eventsById.get(eventObj.id));
				}
				eventObj.init();
			}
		}
		for (EventObject globalObj : globalEv.values()) {
			globalObj.clearCollision();
			put(globalObj.name, globalObj);
		}

		// insert half-planes around the map
		put(null, ev = new EventObject());
		ev.setTouchBound(new Rectangle(-1000f, -1000f, width + 2000f, 1000f));
		put(null, ev = new EventObject());
		ev.setTouchBound(new Rectangle(-1000f, -1000f, 1000f, height + 2000f));
		put(null, ev = new EventObject());
		ev.setTouchBound(new Rectangle(-1000f, height, width + 2000f, 1000f));
		put(null, ev = new EventObject());
		ev.setTouchBound(new Rectangle(width, -1000f, 1000f, height + 2000f));
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
				e.printStackTrace();
			}
		}
		return new HashMap<Integer, ObjectState>();
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
				EventHandler handler = event.getEventHandler();
				if (handler != null) {
					eventsById.put(event.id, handler.getActualState());
				}
			}
			oOut.writeObject(eventsById);
			oOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to parse the object properties input.
	 * 
	 * @param ev
	 * @param props
	 */
	protected void parseProperties(EventObject ev, HashMap<String, String> props) {
		for (Entry<String, String> entry : props.entrySet()) {
			String key = entry.getKey().trim();
			// the libgdx homemade XmlReader of course is buggy :/
			String val = entry.getValue().replace("&quot;", "\"").replace(
					"&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
					.trim();
			if (key.length() == 0 || val.length() == 0)
				continue;
			if (key.charAt(0) == EVENT_CUSTOM_PROP_KZ) {
				ev.properties.put(key, val);
			} else {
				parseSingleProperty(ev, key, val, props);
			}
		}
	}

	private void parseSingleProperty(EventObject ev, String key, String val,
			HashMap<String, String> props) {
		// let's be case insensitive
		key = key.toLowerCase();
		try {
			if (EVENT_PROP_ID.equals(key)) {
				ev.id = Integer.parseInt(val);
			} else if (EVENT_PROP_HEIGHT.equals(key)) {
				ev.z += Integer.parseInt(val);
			} else if (EVENT_PROP_BLOCKING.equals(key)) {
				ev.blockingBehavior = BlockingBehavior.parse(val);
			} else if (EVENT_PROP_SPEED.equals(key)) {
				ev.setMoveSpeed(Speed.parse(val));
			} else if (EVENT_PROP_MOVEHANDLER.equals(key)) {
				Object evHandler = GameBase.$().eval(val);
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends MovementHandler> clazz = (Class<? extends MovementHandler>) evHandler;
					evHandler = clazz.getMethod("$").invoke(null);
				}
				if (evHandler instanceof MovementHandler) {
					ev.setMoveHandler((MovementHandler) evHandler);
				}
			} else if (EVENT_PROP_OUTREACH.equals(key)) {
				ev.outreach = Integer.parseInt(val);
			} else if (EVENT_PROP_ROTATION.equals(key)) {
				ev.rotation = Float.parseFloat(val);
			} else if (EVENT_PROP_SCALEX.equals(key)) {
				ev.scaleX = Float.parseFloat(val);
			} else if (EVENT_PROP_SCALEY.equals(key)) {
				ev.scaleY = Float.parseFloat(val);
			} else if (EVENT_PROP_IMAGE.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					boolean estimateTouch = "true".equalsIgnoreCase(props
							.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
					ev.setImage(val, estimateTouch, !estimateTouch);
					initVisibleEvent(ev, props);
				}
			} else if (EVENT_PROP_EFFECTFRONT.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					ev.setEffectFront(val);
					if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
						ev.z = .1f;
					}
				}
			} else if (EVENT_PROP_EFFECTREAR.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					ev.setEffectRear(val);
					if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
						ev.z = .1f;
					}
				}
			} else if (EVENT_PROP_ANIMATION.equals(key)) {
				FileHandle fh = Gdx.files.internal(val);
				if (fh.exists()) {
					TextureRegionRef t = TextureRegionLoader.load(val);
					TileAnimation anim = new TileAnimation(val, t
							.getRegionWidth() / 4, t.getRegionHeight() / 4, 4,
							4);
					t.dispose();
					boolean estimateTouch = "true".equalsIgnoreCase(props
							.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
					ev.setAnimation(anim, estimateTouch, !estimateTouch);
					initVisibleEvent(ev, props);
				} else {
					Object result = GameBase.$().eval(val);
					if (result instanceof TileAnimation) {
						boolean estimateTouch = "true".equalsIgnoreCase(props
								.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
						ev.setAnimation((TileAnimation) result, estimateTouch,
								!estimateTouch);
						initVisibleEvent(ev, props);
					}
				}
			} else if (EVENT_PROP_HANDLER.equals(key)) {
				Object evHandler = GameBase.$().eval(val);
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends EventHandler> clazz = (Class<? extends EventHandler>) evHandler;
					evHandler = clazz.newInstance();
				}

				// merge both event handler
				if (evHandler instanceof EventExecScriptAdapter
						&& ev.getEventHandler() instanceof EventExecScriptAdapter) {
					((EventExecScriptAdapter) evHandler)
							.merge((EventExecScriptAdapter) ev
									.getEventHandler());
				} else if (evHandler instanceof EventHandler) {
					ev.setEventHandler((EventHandler) evHandler);
				}
			} else if (key.startsWith(EVENT_PROP_ONPUSH)) {
				ev.pushable = true;
				if (ev.getEventHandler() == null) {
					ev.setEventHandler(new EventExecScriptAdapter());
				}
				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
					String index = key.substring(EVENT_PROP_ONPUSH.length())
							.trim();
					((EventExecScriptAdapter) ev.getEventHandler()).execOnPush(
							val, index.length() == 0 ? -1 : Integer
									.parseInt(index));
				}
			} else if (key.startsWith(EVENT_PROP_ONTOUCH)) {
				ev.touchable = true;
				if (ev.getEventHandler() == null) {
					ev.setEventHandler(new EventExecScriptAdapter());
				}
				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
					String index = key.substring(EVENT_PROP_ONTOUCH.length())
							.trim();
					((EventExecScriptAdapter) ev.getEventHandler())
							.execOnTouch(val, index.length() == 0 ? -1
									: Integer.parseInt(index));
				}
			} else if (key.startsWith(EVENT_PROP_ONTIMER)) {
				if (ev.getEventHandler() == null) {
					ev.setEventHandler(new EventExecScriptAdapter());
				}
				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
					String index = key.substring(EVENT_PROP_ONTIMER.length())
							.trim();
					((EventExecScriptAdapter) ev.getEventHandler())
							.execOnTimer(val, index.length() == 0 ? -1
									: Integer.parseInt(index));
				}
			} else if (key.startsWith(EVENT_PROP_ONCUSTOMEVENT)) {
				if (ev.getEventHandler() == null) {
					ev.setEventHandler(new EventExecScriptAdapter());
				}
				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
					String index = key.substring(
							EVENT_PROP_ONCUSTOMEVENT.length()).trim();
					((EventExecScriptAdapter) ev.getEventHandler())
							.execOnCustomTrigger(val, index.length() == 0 ? -1
									: Integer.parseInt(index));
				}
			} else if (key.startsWith(EVENT_PROP_ONLOAD)) {
				if (ev.getEventHandler() == null) {
					ev.setEventHandler(new EventExecScriptAdapter());
				}
				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
					String index = key.substring(EVENT_PROP_ONLOAD.length())
							.trim();
					((EventExecScriptAdapter) ev.getEventHandler()).execOnLoad(
							val, index.length() == 0 ? -1 : Integer
									.parseInt(index));
				}
			}
		} catch (Exception e) {
			// Maybe it would be better to display the error
			e.printStackTrace();
		}
	}

	private void initVisibleEvent(EventObject ev, HashMap<String, String> props) {
		ev.visible = true;
		if ("true".equalsIgnoreCase(props.get(EVENT_PROP_CENTERIMAGE)))
			ev.centerDrawbound();
		if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
			ev.z = .1f;
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
		eventTrigger.compute(deltaTime, actionKeyDown, dynamicRegions);
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
		float camX2 = camera.position.x + camera.viewportWidth;
		float camY1 = camera.position.y;
		float camY2 = camera.position.y + camera.viewportHeight;
		float rX, rY;

		int i = 0;
		EventObject event = dynamicRegions.get(0); // is never empty
		int dynSize = dynamicRegions.size();
		// If there are performance problems:
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
			rX = region.x;
			rY = region.y;
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
		if (debug) {
			spriteBatch.end();
			DebugHelper.debugEvents(dynamicRegions);
			spriteBatch.begin();
		}
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
