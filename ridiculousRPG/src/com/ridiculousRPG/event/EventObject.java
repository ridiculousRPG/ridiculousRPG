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

package com.ridiculousRPG.event;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.animation.TileAnimation;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.map.MapRenderRegion;
import com.ridiculousRPG.map.tiled.TiledMapWithEvents;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.util.BlockingBehavior;
import com.ridiculousRPG.util.ColorSerializable;
import com.ridiculousRPG.util.Direction;
import com.ridiculousRPG.util.ObjectState;
import com.ridiculousRPG.util.Speed;
import com.ridiculousRPG.util.TextureRegionLoader;
import com.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * @author Alexander Baumgartner
 */
public class EventObject extends Movable implements Comparable<EventObject>,
		Disposable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final float COLOR_WHITE_BITS = Color.WHITE.toFloatBits();

	private EventType type;
	private TileAnimation animation;
	private String texturePath;
	private String effectFrontPath;
	private String effectRearPath;
	private transient ParticleEffect effectFront;
	private transient ParticleEffect effectRear;
	private transient TextureRegionRef imageRef;
	private transient TextureRegion image;
	private final Point2D.Float softMove = new Point2D.Float();
	private final Rectangle2D.Float softMoveBounds = new Rectangle2D.Float();
	private MoveTransformation mvTransform;
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
	public float z;
	public Rectangle2D.Float drawBound = new Rectangle2D.Float();
	public boolean visible = false;
	public boolean pushable = false;
	public boolean touchable = false;
	public EventHandler eventHandler;

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
	public transient Array<EventHandler> collision;
	/**
	 * Touching events which have just triggered a touch event
	 */
	public transient Array<EventHandler> justTouching;
	/**
	 * All reachable objects which can be pushed at this time are stored in this
	 * list
	 */
	public transient Array<EventHandler> reachable;
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Creates an empty new event.
	 * 
	 * @param moveTrans
	 *            The transformation of this move.
	 */
	public EventObject(MoveTransformation moveTrans) {
		initTransient();
		mvTransform = moveTrans;
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

	public void setType(String type) {
		if (type == null) {
			setType(EventType.LOCAL);
		} else {
			type = type.trim();
			if (type.length() == 0) {
				setType(EventType.LOCAL);
			} else {
				try {
					setType(EventType.valueOf(type.toUpperCase()));
				} catch (Exception e) {
					setType(EventType.LOCAL);
					GameBase.$info("EventObject.setType",
							"Could not find type " + type
									+ " - fallback to LOCAL", e);
				}
			}
		}
	}

	public void setType(EventType type) {
		this.type = type;
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
		if (animation != null && visible && other.visible)
			animation.animate(other.getX() - getX(), other.getY() - getY(), 0f);
	}

	/**
	 * Checks if one EventObject touches the other respecting all softMoves!
	 */
	public boolean intersects(EventObject other) {
		float x1, y1, x2, y2;
		if (moves) {
			x1 = this.softMoveBounds.x;
			y1 = this.softMoveBounds.y;
		} else {
			x1 = this.touchBound.x;
			y1 = this.touchBound.y;
		}
		if (other.moves) {
			x2 = other.softMoveBounds.x;
			y2 = other.softMoveBounds.y;
		} else {
			x2 = other.touchBound.x;
			y2 = other.touchBound.y;
		}
		return x1 < x2 + other.getWidth() && x1 + getWidth() > x2
				&& y1 < y2 + other.getHeight() && y1 + getHeight() > y2;
	}

	/**
	 * Checks if this EventObject touches the given polygon!
	 */
	public boolean intersects(PolygonObject other) {
		Rectangle2D.Float rect;
		if (moves)
			rect = this.softMoveBounds;
		else
			rect = this.touchBound;

		int endIDX = other.vertexX.length - 1;
		float x1, y1, x2, y2;
		x2 = other.vertexX[endIDX];
		y2 = other.vertexY[endIDX];
		for (int i = endIDX - 1; i >= 0; i--) {
			x1 = other.vertexX[i];
			y1 = other.vertexY[i];
			if (rect.intersectsLine(x1, y1, x2, y2))
				return true;
			x2 = x1;
			y2 = y1;
		}
		return false;
	}

	/**
	 * Checks if one EventObject reaches the other respecting all softMoves!
	 * 
	 * @param other
	 * @return true if other is reachable
	 */
	public boolean reaches(EventObject other) {
		float x1, y1, x2, y2;
		if (moves) {
			x1 = this.softMoveBounds.x;
			y1 = this.softMoveBounds.y;
		} else {
			x1 = this.touchBound.x;
			y1 = this.touchBound.y;
		}
		if (other.moves) {
			x2 = other.softMoveBounds.x;
			y2 = other.softMoveBounds.y;
		} else {
			x2 = other.touchBound.x;
			y2 = other.touchBound.y;
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
			boolean trans = offsetX != 0 || offsetY != 0;
			if (trans)
				spriteBatch.setTransformMatrix(spriteBatch.getTransformMatrix()
						.translate(offsetX, offsetY, 0));
			if (effectRear != null)
				effectRear.draw(spriteBatch);
			if (image != null)
				spriteBatch.draw(image, x, y, w * .5f, h * .5f, w, h, scaleX,
						scaleY, rotation);
			if (effectFront != null)
				effectFront.draw(spriteBatch);
			if (trans)
				spriteBatch.setTransformMatrix(spriteBatch.getTransformMatrix()
						.translate(-offsetX, -offsetY, 0));
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
		mvTransform.set(x, y, softMove);
		softMoveBounds.setRect(getX() + softMove.x, getY() + softMove.y,
				getWidth(), getHeight());
		moves = true;
		if (animation != null) {
			this.image = animation.animate(x, y, dir, deltaTime);
		}
		return distance;
	}

	/**
	 * The event will NOT automatically be animated by using this movement
	 * method. You have to use one of the animate-methods if you want some
	 * animation.<br>
	 * 
	 * @see #animate(float, float, float)
	 */
	@Override
	public synchronized void offerMove(float x, float y) {
		mvTransform.set(x, y, softMove);
		softMoveBounds.setRect(getX() + softMove.x, getY() + softMove.y,
				getWidth(), getHeight());
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
	 * Animates this event if an animation is applied to this event.
	 * 
	 * @see {@link TileAnimation#animate(float, float, float)}
	 */
	public void animate(float x, float y, float deltaTime) {
		if (animation != null) {
			this.image = animation.animate(x, y, deltaTime);
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
	 * Executes a jump movement by the given amount into the actual direction.
	 */
	public void jump(float amount) {
		Direction dir;
		if (animation != null)
			dir = animation.lastDir;
		else
			dir = Direction.values()[0];
		jump(dir.getDistanceX(amount), dir.getDistanceY(amount));
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
		visible = true;
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
		visible = true;
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
		super.init();
		if (eventHandler != null) {
			eventHandler.init();
			eventHandler.onLoad();
		}
	}

	public boolean isGlobalEvent() {
		return name != null
				&& (EventType.PLAYER == type || EventType.GLOBAL == type);
	}

	public boolean isPlayerEvent() {
		return EventType.PLAYER == type;
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
		collision = new Array<EventHandler>(false, 4);
		justTouching = new Array<EventHandler>(false, 4);
		reachable = new Array<EventHandler>(false, 4);
	}

	public void setImage(AtlasRegion region) {
		image = region;
		visible = true;
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

	public static abstract class MoveTransformation implements Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * transforms srcX and srcY and applies them to target.x target.y
		 * 
		 * @param srcX
		 * @param srcY
		 * @param target
		 */
		public abstract void set(float srcX, float srcY, Point2D.Float target);
	}
}
