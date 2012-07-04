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

package com.madthrax.ridiculousRPG.event;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.animation.TileAnimation;
import com.madthrax.ridiculousRPG.event.handler.EventHandler;
import com.madthrax.ridiculousRPG.map.MapRenderRegion;
import com.madthrax.ridiculousRPG.map.tiled.TiledMapWithEvents;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;
import com.madthrax.ridiculousRPG.movement.input.Move4WayAdapter;
import com.madthrax.ridiculousRPG.util.BlockingBehavior;
import com.madthrax.ridiculousRPG.util.ColorSerializable;
import com.madthrax.ridiculousRPG.util.Direction;
import com.madthrax.ridiculousRPG.util.ObjectState;
import com.madthrax.ridiculousRPG.util.Speed;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * @author Alexander Baumgartner
 */
public class EventObject extends Movable implements Comparable<EventObject>,
		Disposable, Serializable {
	private static final long serialVersionUID = 1L;

	private static final float COLOR_WHITE_BITS = Color.WHITE.toFloatBits();

	private TileAnimation animation;
	private String texturePath;
	private String effectFrontPath;
	private String effectRearPath;
	private transient ParticleEffect effectFront;
	private transient ParticleEffect effectRear;
	private transient TextureRegionRef imageRef;
	private transient TextureRegion image;
	private Point2D.Float softMove = new Point2D.Float(0f, 0f);
	private EventHandler eventHandler;
	private Color color = new ColorSerializable(1f, 1f, 1f, 1f);
	private float colorFloatBits = color.toFloatBits();

	/**
	 * Set the property "id" in the Tiled editor to identify an event.<br>
	 * If "id" is not set, it will automatically be computed when the event is
	 * put onto a map.<br>
	 * The id is always unique for one map.<br>
	 * You can use the id to load and store the state of the event.
	 * 
	 * @see EventHandler#load(EventObject, ObjectState)
	 * @see EventHandler#store(EventObject, ObjectState, boolean)
	 */
	public int id = -1;
	public int gid = 0;

	public String name;
	public String type;
	public float z;
	public Rectangle drawBound = new Rectangle();
	public boolean visible = false;
	public boolean pushable = false;
	public boolean touchable = false;
	public boolean reactOnGlobalChange = false;

	public float rotation = 0f, scaleX = 1f, scaleY = 1f;
	/**
	 * If an event consumes input, it triggers touch and push events. The
	 * {@link EventHandler} from the other event is called.
	 */
	public boolean consumeInput = false;
	public BlockingBehavior blockingBehavior = BlockingBehavior.BUILDING_LOW;
	/**
	 * The outreach for pushing other events. Default = 10 pixel
	 * 
	 * @see {@link #reaches(EventObject)}
	 */
	public int outreach = 10;

	/**
	 * All actual collisions for this object are stored in this list
	 */
	public transient Array<EventObject> collision;
	/**
	 * Touching events which have just triggered a touch event
	 */
	public transient Array<EventObject> justTouching;
	/**
	 * All reachable objects which can be pushed at this time are stored in this
	 * list
	 */
	public transient Array<EventObject> reachable;
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public HashMap<String, String> properties = new HashMap<String, String>();

	public static final String EVENT_TYPE_PLAYER = "player";
	public static final String EVENT_TYPE_GLOBAL = "global";

	/**
	 * Creates an empty new event.
	 */
	public EventObject() {
		initTransient();
	}

	/**
	 * Creates a new event.<br>
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 */
	public EventObject(String name, String imagePath, int x, int y,
			MovementHandler moveHandler) {
		this(name, imagePath, x, y, moveHandler, BlockingBehavior.NONE,
				Speed.S07_NORMAL);
	}

	/**
	 * Creates a new event.<br>
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 */
	public EventObject(String name, final String imagePath, int x, int y,
			MovementHandler moveHandler, BlockingBehavior blockingBehaviour,
			Speed moveSpeed) {
		this(name, TextureRegionLoader.load(imagePath), x, y, moveHandler,
				blockingBehaviour, moveSpeed);
	}

	/**
	 * Creates a new event.<br>
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param region
	 *            This texture reference will automatically be unloaded
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 */
	public EventObject(String name, TextureRegionRef region, int x, int y,
			MovementHandler moveHandler) {
		this(name, region, x, y, moveHandler, BlockingBehavior.NONE,
				Speed.S07_NORMAL);
	}

	/**
	 * Creates a new event.<br>
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param region
	 *            This texture reference will automatically be unloaded
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 */
	public EventObject(String name, TextureRegionRef region, int x, int y,
			MovementHandler moveHandler, BlockingBehavior blockingBehaviour,
			Speed moveSpeed) {
		this();
		image = imageRef = region;
		visible = true;
		this.moveSpeed = moveSpeed;
		addX(x);
		addY(y);
		drawBound.width = image.getRegionWidth();
		drawBound.height = image.getRegionHeight();
		// we won't be to eager with touch computation
		touchBound.x += drawBound.width * .17f;
		touchBound.y += drawBound.height * .17f;
		touchBound.width = drawBound.width * .66f;
		touchBound.height = drawBound.height * .66f;
		this.setMoveHandler(moveHandler);
		this.blockingBehavior = blockingBehaviour;
	}

	/**
	 * Creates a new event.<br>
	 * For the undocumented parameters see
	 * {@link #setAnimationTexture(String, int, int, int, int, boolean)}
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 */
	public EventObject(String name, String animationPath,
			int animationTileWidth, int animationTileHeight, int anzCols,
			int anzRows, int x, int y) {
		this(name, animationPath, animationTileWidth, animationTileHeight,
				anzCols, anzRows, x, y, Move4WayAdapter.$());
	}

	/**
	 * Draws a particle effect in front of this event.
	 * 
	 * @param internalPath
	 *            Path to the particle effect.
	 */
	public ParticleEffect setEffectFront(String internalPath) {
		if (effectFront != null) {
			effectFront.dispose();
			effectFront = null;
		}
		if (internalPath != null) {
			FileHandle fh = Gdx.files.internal(internalPath);
			if (fh.exists()) {
				effectFrontPath = internalPath;
				effectFront = new ParticleEffect();
				effectFront.load(fh, fh.parent());
				effectFront.setPosition(drawBound.x + drawBound.width * .5f,
						drawBound.y);
				visible = true;
			}
		}
		return effectFront;
	}

	public void startEffectFront() {
		if (effectFront != null) {
			effectFront.start();
		}
	}

	/**
	 * Applies this effect two times. Once in front of this event and once
	 * behind this event.
	 * 
	 * @param internalPath
	 *            Path to the particle effect.
	 * @see #setEffectFront(String)
	 * @see #setEffectRear(String)
	 */
	public void setEffect(String internalPath) {
		setEffectFront(internalPath);
		setEffectRear(internalPath);
	}

	/**
	 * Draws a particle effect behind the event.
	 * 
	 * @param internalPath
	 *            Path to the particle effect.
	 */
	public ParticleEffect setEffectRear(String internalPath) {
		if (effectRear != null) {
			effectRear.dispose();
			effectRear = null;
		}
		if (internalPath != null) {
			FileHandle fh = Gdx.files.internal(internalPath);
			if (fh.exists()) {
				effectRearPath = internalPath;
				effectRear = new ParticleEffect();
				effectRear.load(fh, fh.parent());
				effectRear.setPosition(drawBound.x + drawBound.width * .5f,
						drawBound.y);
				visible = true;
			}
		}
		return effectRear;
	}

	public void startEffectRear() {
		if (effectRear != null) {
			effectRear.start();
		}
	}

	/**
	 * Creates a new event.<br>
	 * For the undocumented parameters see
	 * {@link #setAnimationTexture(String, int, int, int, int, boolean, boolean)}
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 * @param moveHandler
	 *            the starting {@link MovementHandler}
	 */
	public EventObject(String name, String animationPath,
			int animationTileWidth, int animationTileHeight, int anzCols,
			int anzRows, int x, int y, MovementHandler moveHandler) {
		this(name, animationPath, animationTileWidth, animationTileHeight,
				anzCols, anzRows, x, y, moveHandler, Speed.S07_NORMAL,
				Direction.N, BlockingBehavior.PASSES_LOW_BARRIER, true);
	}

	/**
	 * Creates a new event.<br>
	 * For the undocumented parameters see
	 * {@link #setAnimationTexture(String, int, int, int, int, boolean, boolean)}
	 * 
	 * @param name
	 *            the name of this event. Don't use the name as key. Names may
	 *            change and they don't need to be unique.
	 * @param x
	 *            the starting x position
	 * @param y
	 *            the starting y position
	 * @param moveHandler
	 *            the starting {@link MovementHandler}
	 * @param startDirection
	 *            the starting direction
	 * @param isAnimationCompressed
	 */
	public EventObject(String name, String animationPath,
			int animationTileWidth, int animationTileHeight, int anzCols,
			int anzRows, int x, int y, MovementHandler moveHandler,
			Speed moveSpeed, Direction startDirection,
			BlockingBehavior blockingBehaviour, boolean isAnimationCompressed) {
		this();
		addX(x);
		addY(y);
		z = .1f;
		setAnimationTexture(animationPath, animationTileWidth,
				animationTileHeight, anzCols, anzRows, true, false,
				isAnimationCompressed);
		this.image = animation.setAnimationPosition(startDirection);
		this.visible = true;
		this.moveSpeed = moveSpeed;
		this.name = name;
		this.setMoveHandler(moveHandler);
		this.blockingBehavior = blockingBehaviour;
	}

	/**
	 * With this constructor it's possible to simply instantiate an EventObject
	 * from an object on the tiled map.
	 * 
	 * @param object
	 *            One concrete tiled object
	 * @param layer
	 *            The layer on which the object is placed
	 * @param atlas
	 *            The atlas which contains all tiles as texture regions
	 * @param map
	 *            A reference to the entire tiled map
	 */
	public EventObject(TiledObject object, TiledObjectGroup layer,
			TileAtlas atlas, TiledMap map) {
		this();
		float mapHeight = map.height * map.tileHeight;
		name = object.name;
		type = object.type;
		if (object.gid > 0) {
			gid = object.gid;
			AtlasRegion region = (AtlasRegion) atlas.getRegion(object.gid);
			setImage(region);
			visible = true;
			addX(region.offsetX + object.x);
			addY(mapHeight + region.offsetY - object.y);
			drawBound.width = touchBound.width = region.getRegionWidth();
			drawBound.height = touchBound.height = region.getRegionHeight();
		} else {
			addX(object.x);
			addY(mapHeight - object.y - object.height);
			drawBound.width = touchBound.width = object.width;
			drawBound.height = touchBound.height = object.height;
		}
	}

	private void estimateTouchBound() {
		touchBound.x = drawBound.x + drawBound.width * .167f;
		touchBound.y = drawBound.y;
		touchBound.width = drawBound.width * .67f;
		if (drawBound.height > drawBound.width) {
			touchBound.height = drawBound.width * .5f;
		} else {
			touchBound.height = drawBound.height * .5f;
		}
	}

	public void addX(float value) {
		super.addX(value);
		setDrawbounds(drawBound.x + value, drawBound.y);
	}

	public void addY(float value) {
		super.addY(value);
		setDrawbounds(drawBound.x, drawBound.y + value);
	}

	/**
	 * Centers the touch bounds of this event. The draw bound is used for
	 * centering the touch bound.
	 */
	public void centerTouchbound() {
		touchBound.x = drawBound.x + (drawBound.width - touchBound.width) * .5f;
		touchBound.y = drawBound.y + (drawBound.height - touchBound.height)
				* .5f;
	}

	/**
	 * Centers the draw bounds of this event. The touch bound is used for
	 * centering the draw bound.
	 */
	public void centerDrawbound() {
		setDrawbounds(
				touchBound.x + (touchBound.width - drawBound.width) * .5f,
				touchBound.y + (touchBound.height - drawBound.height) * .5f);
	}

	private void setDrawbounds(float x, float y) {
		drawBound.x = x;
		drawBound.y = y;
		if (effectRear != null) {
			effectRear.setPosition(x + drawBound.width * .5f, y);
		}
		if (effectFront != null) {
			effectFront.setPosition(x + drawBound.width * .5f, y);
		}
	}

	/**
	 * Changes the direction of this {@link EventObject} to face the other one.<br>
	 * The touch bounds are used to compute the direction.
	 */
	public void lookAt(EventObject other) {
		if (animation == null)
			return;
		float x, y;
		if (getX() + getWidth() < other.getX()) { // East
			x = 1;
		} else if (getX() > other.getX() + other.getWidth()) { // West
			x = -1;
		} else { // unchanged
			x = 0;
		}
		if (getY() + getHeight() < other.getY()) { // North
			y = 1;
		} else if (getY() > other.getY() + other.getHeight()) { // South
			y = -1;
		} else { // unchanged
			if (x == 0)
				return;
			y = 0;
		}
		// set direction...
		animation.animate(0, 0, Direction.fromMovement(x, y), 0);
	}

	/**
	 * Checks if one EventObject touches the other respecting all softMoves!
	 */
	public boolean overlaps(EventObject other) {
		float x1 = getX();
		float y1 = getY();
		float x2 = other.getX();
		float y2 = other.getY();
		if (moves) {
			x1 += softMove.x;
			y1 += softMove.y;
		}
		if (other.moves) {
			x2 += other.softMove.x;
			y2 += other.softMove.y;
		}
		return x1 < x2 + other.getWidth() && x1 + getWidth() > x2
				&& y1 < y2 + other.getHeight() && y1 + getHeight() > y2;
	}

	/**
	 * Checks if one EventObject reaches the other respecting all softMoves!
	 * 
	 * @param other
	 * @return true if other is reachable
	 */
	public boolean reaches(EventObject other) {
		float x1 = getX();
		float y1 = getY();
		float x2 = other.getX();
		float y2 = other.getY();
		if (moves) {
			x1 += softMove.x;
			y1 += softMove.y;
		}
		if (other.moves) {
			x2 += other.softMove.x;
			y2 += other.softMove.y;
		}
		return x1 < x2 + other.getWidth() + outreach
				&& x1 + getWidth() + outreach > x2
				&& y1 < y2 + other.getHeight() + outreach
				&& y1 + getHeight() + outreach > y2;
	}

	public void setColor(Color color) {
		this.color.set(color);
		colorFloatBits = color.toFloatBits();
	}

	public Color getColor() {
		return color;
	}

	public void draw(SpriteBatch spriteBatch) {
		if (visible) {
			float eventColorBits = this.colorFloatBits;
			float gameColorBits = GameBase.$().getGameColorBits();
			if (gameColorBits != COLOR_WHITE_BITS) {
				if (eventColorBits != COLOR_WHITE_BITS) {
					Color c1 = this.color;
					Color c2 = GameBase.$().getGameColorTint();
					eventColorBits = Color.toFloatBits(c1.r * c2.r,
							c1.g * c2.g, c1.b * c2.b, c1.a * c2.a);
				} else {
					eventColorBits = gameColorBits;
				}
			}
			float x = drawBound.x;
			float y = drawBound.y;
			float h = drawBound.height;
			float w = drawBound.width;
			spriteBatch.setColor(eventColorBits);
			if (effectRear != null)
				effectRear.draw(spriteBatch);
			if (image != null)
				spriteBatch.draw(image, x, y, w * .5f, h * .5f, w, h, scaleX,
						scaleY, rotation);
			if (effectFront != null)
				effectFront.draw(spriteBatch);
			spriteBatch.setColor(gameColorBits);
		}
	}

	/**
	 * Compares an event with an other to determine the rendering order. This
	 * method is used by the MapRenderService.
	 * 
	 * @param o
	 *            other event
	 * @return rendering order: 1 if this event is in front of the other (it
	 *         will be rendered later and overwrites the other one).
	 */
	public int compareTo(EventObject o) {
		if (o.z == 0) {
			if (z == 0)
				return 0;
			return 1;
		} else if (drawBound.y - z > o.drawBound.y - o.z || z == 0) {
			return -1;
		} else if (drawBound.y - z < o.drawBound.y - o.z) {
			return 1;
		}
		return 0;
	}

	/**
	 * Compares an event with a MapRenderRegion to determine the rendering
	 * order. This method is used by the MapRenderService.
	 * 
	 * @param o
	 *            MapRenderRegion
	 * @return rendering order: 1 if this event is in front of the other (it
	 *         will be rendered later and overwrites the other one).
	 */
	public int compareTo(MapRenderRegion o) {
		if (o.z == 0) {
			if (z == 0)
				return 0;
			return 1;
		} else if (drawBound.y - z > o.yz || z == 0) {
			return -1;
		} else if (drawBound.y - z < o.yz) {
			return 1;
		}
		return 0;
	}

	/**
	 * The event will automatically be animated by using this movement method.
	 */
	@Override
	public synchronized float offerMove(Direction dir, float deltaTime) {
		float distance = moveSpeed.computeStretch(deltaTime);
		float x = dir.getDistanceX(distance);
		float y = dir.getDistanceY(distance);
		softMove.x = x;
		softMove.y = y;
		moves = true;
		if (animation != null) {
			this.image = animation.animate(x, y, dir, deltaTime);
		}
		return distance;
	}

	/**
	 * The event will NOT automatically be animated by using this movement
	 * method. You have to use one of the animate-methods if you want some
	 * animation.
	 */
	@Override
	public synchronized void offerMove(float x, float y) {
		softMove.x = x;
		softMove.y = y;
		moves = true;
	}

	@Override
	public synchronized boolean commitMove() {
		if (moves) {
			addX(softMove.x);
			addY(softMove.y);
			moves = false;
			return true;
		}
		return false;
	}

	/**
	 * Uses TileAnimation.animate(int, Speed) to set the image for this event.<br>
	 * 
	 * @see {@link TileAnimation#animate(int, Speed)}
	 */
	public void animate(float x, float y, float deltaTime) {
		if (animation != null) {
			this.image = animation.animate(x, y, Direction.fromMovement(x, y),
					deltaTime);
		}
	}

	/**
	 * Uses TileAnimation.animate(int, Speed) to set the image for this event.<br>
	 * 
	 * @see {@link TileAnimation#animate(int, Speed)}
	 */
	public void animate(int animationTextureRow, Speed speed, float deltaTime) {
		if (animation != null) {
			this.image = animation.animate(animationTextureRow, speed,
					deltaTime);
		}
	}

	/**
	 * Uses TileAnimation.stop() to set the image for this event.<br>
	 * 
	 * @see {@link TileAnimation#stop()}
	 */
	@Override
	public void stop() {
		if (animation != null)
			this.image = animation.stop();
	}

	/**
	 * Uses TileAnimation.stop() to set the image for this event.<br>
	 * 
	 * @see {@link TileAnimation#stop()}
	 */
	public void resetImage() {
		stop();
		if (imageRef != null)
			image = imageRef;
	}

	/**
	 * ATTENTION: The new animation texture must have exactly the same form as
	 * the old one!<br>
	 * Image size must be the same, tile (=character) size too,...<br>
	 * There must already exist an initialized animation.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 */
	public void changeAnimationTexture(String path) {
		if (animation == null) {
			GameBase.$error("EventObject.changeAnimationTexture",
					"Error changing animation for '" + this + "'",
					new IllegalStateException("You have to initialize the "
							+ "animation before changing the texture"));
		} else {
			this.image = animation.setAnimationTexture(path);
		}
	}

	/**
	 * Loads a new animation-texture for this event.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 * @param estimateTouchBound
	 *            true, if you want to recompute the touchBound from the new
	 *            tileWidth and tileHeight.
	 */
	public void setAnimationTexture(String path, int tileWidth, int tileHeight,
			int anzCols, int anzRows, boolean estimateTouchBound,
			boolean estimateDrawBound) {
		setAnimationTexture(path, tileWidth, tileHeight, anzCols, anzRows,
				estimateTouchBound, estimateDrawBound, false);
	}

	/**
	 * Loads a new animation-texture for this event.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 * @param estimateTouchBound
	 *            true, if you want to recompute the touchBound from the new
	 *            tileWidth and tileHeight.
	 * @param isCompressed
	 *            true if the animation should be uncompressed. (default=false)
	 *            <ul>
	 *            <li>If there is only one row, a compressed animation is
	 *            uncompressed by flipping this row to a second one.<br>
	 *            <li>If there are three rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            <li>If there are fife rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            The fourth row represents the south east direction and will be
	 *            flipped to the south west.<br>
	 *            And the fifth row represents the north east direction and will
	 *            be flipped to the north west.
	 *            </ul>
	 */
	public void setAnimationTexture(String path, int tileWidth, int tileHeight,
			int anzCols, int anzRows, boolean estimateTouchBound,
			boolean estimateDrawBound, boolean isCompressed) {
		if (animation == null) {
			animation = new TileAnimation(path, tileWidth, tileHeight, anzCols,
					anzRows, isCompressed);
			image = animation.getActualTextureRegion();
		} else {
			image = animation.setAnimationTexture(path, tileWidth, tileHeight,
					anzCols, anzRows, isCompressed);
		}
		this.drawBound.width = tileWidth;
		this.drawBound.height = tileHeight;
		if (estimateTouchBound) {
			estimateTouchBound();
		} else if (estimateDrawBound) {
			setDrawbounds(touchBound.x - (tileWidth - getWidth()) / 2,
					touchBound.y);
		}
	}

	/**
	 * Sets a animation for this event. Never use the same animation-object for
	 * two events because the animation will automatically be disposed with the
	 * event!<br>
	 * You have to dispose the old animation if you don't need it anymore
	 * (simply chain with .dispose()). The new animation will automatically be
	 * disposed.<br>
	 * Be careful when using this method!
	 * 
	 * @param animation
	 *            The animation used for animating this event
	 * @param estimateTouchBound
	 *            true, if you want to recompute the touchBound from the new
	 *            tileWidth and tileHeight.
	 * @return The old animation. If you don't need it any more you have to
	 *         dispose it!
	 */
	public TileAnimation setAnimation(TileAnimation animation,
			boolean estimateTouchBound, boolean estimateDrawBound) {
		TileAnimation old = this.animation;
		this.animation = animation;
		image = animation.getActualTextureRegion();
		int width = image.getRegionWidth();
		int height = image.getRegionHeight();
		drawBound.width = width;
		drawBound.height = height;
		if (estimateTouchBound) {
			estimateTouchBound();
		} else if (estimateDrawBound) {
			setDrawbounds(touchBound.x - (width - getWidth()) / 2, touchBound.y);
		}
		return old;
	}

	/**
	 * The animation-object will automatically be disposed with the event! Never
	 * use the same animation-object for two events.
	 * 
	 * @return The actual running animation. Never use this inside an other
	 *         event!
	 */
	public TileAnimation getAnimation() {
		return animation;
	}

	public EventHandler getEventHandler() {
		return eventHandler;
	}

	/**
	 * This texture reference will automatically be unloaded
	 * 
	 * @param texturePath
	 *            path to an png image
	 */
	public void setImage(String texturePath, boolean estimateTouchBound,
			boolean estimateDrawBound) {
		this.texturePath = texturePath;
		TextureRegionRef texture = TextureRegionLoader.load(texturePath);
		if (imageRef != null)
			imageRef.dispose();
		imageRef = texture;
		image = texture;
		drawBound.width = texture.getRegionWidth();
		drawBound.height = texture.getRegionHeight();
		if (estimateTouchBound) {
			estimateTouchBound();
		} else if (estimateDrawBound) {
			setDrawbounds(touchBound.x - (drawBound.width - getWidth()) / 2,
					touchBound.y);
		}
	}

	public void setEventHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void dispose() {
		if (effectFront != null) {
			effectFront.dispose();
			effectFront = null;
		}
		if (effectRear != null) {
			effectRear.dispose();
			effectRear = null;
		}
		if (animation != null) {
			animation.dispose();
			animation = null;
		}
		if (imageRef != null) {
			imageRef.dispose();
			imageRef = null;
		}
		if (eventHandler != null) {
			eventHandler.dispose();
			eventHandler = null;
		}
		visible = false;
		pushable = false;
		touchable = false;
		moves = false;
		image = null;
		color = null;
		drawBound = null;
		touchBound = null;
		collision = null;
		justTouching = null;
		reachable = null;
		properties = null;
		setMoveHandler(null);
	}

	public void clearCollision() {
		collision.clear();
		justTouching.clear();
		reachable.clear();
	}

	public void init() {
		if (eventHandler != null) {
			eventHandler.init();
			eventHandler.onLoad(this);
		}
	}

	public boolean isGlobalEvent() {
		return name != null
				&& (EVENT_TYPE_PLAYER.equalsIgnoreCase(type) || EVENT_TYPE_GLOBAL
						.equalsIgnoreCase(type));
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		initTransient();
		if (texturePath != null) {
			setImage(texturePath, false, false);
		}
		if (animation != null) {
			image = animation.getActualTextureRegion();
		}
		setEffectFront(effectFrontPath);
		setEffectRear(effectRearPath);
		init();
	}

	// default values for transient variables
	private void initTransient() {
		collision = new Array<EventObject>(false, 4);
		justTouching = new Array<EventObject>(false, 4);
		reachable = new Array<EventObject>(false, 4);
	}

	public void setImage(AtlasRegion region) {
		image = region;
	}

	public void computeParticleEffect(float deltaTime) {
		if (effectFront != null) {
			effectFront.update(deltaTime);
		}
		if (effectRear != null) {
			effectRear.update(deltaTime);
		}
	}

	@Override
	public String toString() {
		return "event '" + (name == null ? "id=" + id : name) + " (type="
				+ type + ")'";
	}
}
