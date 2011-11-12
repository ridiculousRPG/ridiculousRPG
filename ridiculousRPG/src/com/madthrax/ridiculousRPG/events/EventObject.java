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

package com.madthrax.ridiculousRPG.events;

import java.awt.geom.Point2D;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
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
import com.madthrax.ridiculousRPG.ObjectState;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
import com.madthrax.ridiculousRPG.animations.TileAnimation;
import com.madthrax.ridiculousRPG.events.handler.EventHandler;
import com.madthrax.ridiculousRPG.map.MapRenderRegion;
import com.madthrax.ridiculousRPG.map.TiledMapWithEvents;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;
import com.madthrax.ridiculousRPG.movement.input.Move4WayAdapter;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * @author Alexander Baumgartner
 */
public class EventObject extends Movable implements Comparable<EventObject>,
		Initializable, Disposable {
	private static final float COLOR_WHITE_BITS = Color.WHITE.toFloatBits();

	private TileAnimation animation;
	private TextureRegionRef imageRef;
	private TextureRegion image;
	private Point2D.Float softMove = new Point2D.Float(0f, 0f);
	private EventHandler eventHandler;
	private Color color = new Color(1f, 1f, 1f, 1f);
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
	public String name;
	public String type;
	public float z;
	public Rectangle drawBound = new Rectangle();
	public boolean visible = false;
	public boolean pushable = false;
	public boolean touchable = false;
	public boolean initialized = false;
	public float rotation = 0f, scaleX = 1f, scaleY = 1f;
	/**
	 * If an event consumes input, it triggers touch and push events. The
	 * {@link EventHandler} from the other event is called.
	 */
	public boolean consumeInput = false;
	public BlockingBehaviour blockingBehaviour = BlockingBehaviour.BUILDING_LOW;
	/**
	 * The outreach for pushing other events. Default = 10 pixel
	 * 
	 * @see {@link #reaches(EventObject)}
	 */
	public int outreach = 10;

	/**
	 * All actual collisions for this object are stored in this list
	 */
	public Array<EventObject> collision = new Array<EventObject>(false, 4);
	/**
	 * Touching events which have just triggered a touch event
	 */
	public Array<EventObject> justTouching = new Array<EventObject>(false, 4);
	/**
	 * All reachable objects which can be pushed at this time are stored in this
	 * list
	 */
	public Array<EventObject> reachable = new Array<EventObject>(false, 4);
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public HashMap<String, String> properties = new HashMap<String, String>();

	/**
	 * Creates an empty new event.
	 */
	public EventObject() {
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
		this(name, imagePath, x, y, moveHandler, BlockingBehaviour.NONE,
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
			MovementHandler moveHandler, BlockingBehaviour blockingBehaviour,
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
		this(name, region, x, y, moveHandler, BlockingBehaviour.NONE,
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
			MovementHandler moveHandler, BlockingBehaviour blockingBehaviour,
			Speed moveSpeed) {
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
		this.blockingBehaviour = blockingBehaviour;
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
				Direction.N, BlockingBehaviour.PASSES_LOW_BARRIER, true);
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
			BlockingBehaviour blockingBehaviour, boolean isAnimationCompressed) {
		addX(x);
		addY(y);
		z = .1f;
		setAnimationTexture(animationPath, animationTileWidth,
				animationTileHeight, anzCols, anzRows, true,
				isAnimationCompressed);
		this.image = animation.setAnimationPosition(startDirection);
		this.visible = true;
		this.moveSpeed = moveSpeed;
		this.name = name;
		this.setMoveHandler(moveHandler);
		this.blockingBehaviour = blockingBehaviour;
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
		float mapHeight = map.height * map.tileHeight;
		name = object.name;
		type = object.type;
		if (object.gid > 0) {
			AtlasRegion region = (AtlasRegion) atlas.getRegion(object.gid);
			image = region;
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

	private void addX(float value) {
		touchBound.x += value;
		drawBound.x += value;
	}

	private void addY(float value) {
		touchBound.y += value;
		drawBound.y += value;
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
		drawBound.x = touchBound.x + (touchBound.width - drawBound.width) * .5f;
		drawBound.y = touchBound.y + (touchBound.height - drawBound.height)
				* .5f;
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
		if (visible && image != null) {
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
			spriteBatch.draw(image, x, y, w * .5f, h * .5f, w, h, scaleX,
					scaleY, rotation);
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
	public float offerMove(Direction dir, float deltaTime) {
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
	public void offerMove(float x, float y) {
		softMove.x = x;
		softMove.y = y;
		moves = true;
	}

	@Override
	public boolean commitMove() {
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
		if (animation == null)
			throw new IllegalStateException(
					"You have to initialize the animation before changing the texture");
		this.image = animation.setAnimationTexture(path);
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
			int anzCols, int anzRows, boolean estimateTouchBound) {
		setAnimationTexture(path, tileWidth, tileHeight, anzCols, anzRows,
				estimateTouchBound, false);
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
			boolean isCompressed) {
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
		if (estimateTouchBound)
			estimateTouchBound();
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
			boolean estimateTouchBound) {
		TileAnimation old = this.animation;
		this.animation = animation;
		image = animation.getActualTextureRegion();
		int width = image.getRegionWidth();
		int height = image.getRegionHeight();
		drawBound.width = width;
		drawBound.height = height;
		if (estimateTouchBound) {
			estimateTouchBound();
		} else {
			drawBound.x = touchBound.x - (width - getWidth()) / 2;
			drawBound.y = touchBound.y;
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
	 * @param tRef
	 */
	public void setImage(TextureRegionRef texture) {
		if (imageRef != null)
			imageRef.dispose();
		imageRef = texture;
		image = texture;
	}

	public void setEventHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void dispose() {
		if (animation != null) {
			animation.dispose();
			animation = null;
		}
		if (imageRef != null) {
			imageRef.dispose();
			imageRef = null;
		}
		visible = false;
		pushable = false;
		touchable = false;
		moves = false;
		image = null;
		setMoveHandler(null);
	}

	public void init() {
		if (eventHandler instanceof Initializable) {
			((Initializable) eventHandler).init();
		}
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}
}
