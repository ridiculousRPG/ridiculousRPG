package com.madthrax.ridiculousRPG.animations;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;

public class BoundedImage {
	private TextureRegionRef image;
	private Rectangle bounds;
	private Vector2 scale;
	private Vector2 origin;
	private float rotation;
	private boolean scroll;
	private Rectangle scrollReference;

	/**
	 * Scales the image to fit into the bounds
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds) {
		this(image, bounds, 0f);
	}

	/**
	 * Scales the image to fit into the bounds
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param rotation
	 *            rotates the image clockwise by the given amount
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds, float rotation) {
		this(image, bounds, rotation, new Vector2(0f, 0f));
	}

	/**
	 * Scales the image to fit into the bounds
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param rotation
	 *            rotates the image clockwise by the given amount
	 * @param origin
	 *            the relative origin from the center of this image Origin (0,0)
	 *            is the center
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds,
			float rotation, Vector2 origin) {
		this(image, bounds, false, null, rotation, origin, new Vector2(1f, 1f));
	}

	/**
	 * The image will not be scaled, but scrolls inside the bounds.<br>
	 * Scrolling is performed when scrollReference.x or scrollReference.y
	 * changes. The scroll speed/amount is computed relatively from
	 * bounds.width/scrollReference.width and
	 * bounds.height/scrollReference.height.
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param scrollReference
	 *            the bounds which are used to compute/perform scrolling.<br>
	 *            If null GameBase.$().getPlane() will be used.
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds,
			Rectangle scrollReference) {
		this(image, bounds, scrollReference, 0f);
	}

	/**
	 * The image will not be scaled, but scrolls inside the bounds.<br>
	 * Scrolling is performed when scrollReference.x or scrollReference.y
	 * changes. The scroll speed/amount is computed relatively from
	 * bounds.width/scrollReference.width and
	 * bounds.height/scrollReference.height.
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param scrollReference
	 *            the bounds which are used to compute/perform scrolling.<br>
	 *            If null GameBase.$().getPlane() will be used.
	 * @param rotation
	 *            rotates the image clockwise by the given amount
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds,
			Rectangle scrollReference, float rotation) {
		this(image, bounds, scrollReference, rotation, new Vector2(1f, 1f));
	}

	/**
	 * The image will be scaled by the specified relative scale.x, scale.y
	 * values (1 = no scaling) and scrolls inside the bounds if it's to large to
	 * fit. If the (scaled) image is smaller than bounds, it will be scaled to
	 * fill the bounds.<br>
	 * Scrolling is performed when scrollReference.x or scrollReference.y
	 * changes. The scroll speed/amount is computed relatively from
	 * bounds.width/scrollReference.width and
	 * bounds.height/scrollReference.height.
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param scrollReference
	 *            the bounds which are used to compute/perform scrolling.<br>
	 *            If null GameBase.$().getPlane() will be used.
	 * @param rotation
	 *            rotates the image clockwise by the given amount
	 * @param scale
	 *            minimum scale value for x and y
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds,
			Rectangle scrollReference, float rotation, Vector2 scale) {
		this(image, bounds, scrollReference, rotation, new Vector2(0f, 0f),
				scale);
	}

	/**
	 * The image will be scaled by the specified relative scale.x, scale.y
	 * values (1 = no scaling) and scrolls inside the bounds if it's to large to
	 * fit. If the (scaled) image is smaller than bounds, it will be scaled to
	 * fill the bounds.<br>
	 * Scrolling is performed when scrollReference.x or scrollReference.y
	 * changes. The scroll speed/amount is computed relatively from
	 * bounds.width/scrollReference.width and
	 * bounds.height/scrollReference.height.
	 * 
	 * @param image
	 *            the image to draw
	 * @param bounds
	 *            the bounds for drawing the image
	 * @param scrollReference
	 *            the bounds which are used to compute/perform scrolling.<br>
	 *            If null GameBase.$().getPlane() will be used.
	 * @param rotation
	 *            rotates the image clockwise by the given amount
	 * @param origin
	 *            the relative origin from the center of this image Origin (0,0)
	 *            is the center
	 * @param scale
	 *            minimum scale value for x and y
	 */
	public BoundedImage(TextureRegionRef image, Rectangle bounds,
			Rectangle scrollReference, float rotation, Vector2 origin,
			Vector2 scale) {
		this(image, bounds, true, scrollReference, rotation, origin, scale);
	}

	private BoundedImage(TextureRegionRef image, Rectangle bounds,
			boolean scroll, Rectangle scrollReference, float rotation,
			Vector2 origin, Vector2 scale) {
		this.image = image;
		this.bounds = bounds;
		this.scroll = scroll;
		this.scrollReference = scrollReference;
		this.rotation = rotation;
		this.origin = origin;
		this.scale = scale;
		if (scroll && scrollReference == null) {
			scrollReference = GameBase.$().getPlane();
		}
	}

	public void draw(SpriteBatch spriteBatch) {
		float x = bounds.x;
		float y = bounds.y;
		float w = bounds.width;
		float h = bounds.height;
		if (scroll) {
			// spriteBatch.draw(image.getTexture(), x, y, origin.x, origin.x, w, h, scale.x, scale.y, rotation, srcX, srcY, srcWidth, srcHeight, false, false);
			// TODO: scroll in bounds relative depending on scrollReference
		} else {
			spriteBatch.draw(image, x, y, w * .5f + origin.x, h * .5f
					+ origin.y, w, h, scale.x, scale.y, rotation);
		}
	}

	public Vector2 getScale() {
		return scale;
	}

	public Vector2 getOrigin() {
		return origin;
	}

	public Rectangle getScrollReference() {
		return scrollReference;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
}
