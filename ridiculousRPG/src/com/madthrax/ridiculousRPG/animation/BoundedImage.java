package com.madthrax.ridiculousRPG.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;

public class BoundedImage implements Disposable {
	private TextureRegionRef image;
	private Rectangle bounds;
	private Vector2 scale;
	private Vector2 origin;
	private float rotation;
	private boolean scroll;
	private Rectangle scrollReference;

	/**
	 * Scales the image to fill the screen
	 * 
	 * @param image
	 *            the image to draw
	 */
	public BoundedImage(TextureRegionRef image) {
		this(image, GameBase.$().getScreen());
	}

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
			int x2 = image.getRegionX();
			int y2 = image.getRegionY();
			float w2 = image.getRegionWidth() * scale.x;
			float h2 = image.getRegionHeight() * scale.y;
			if (w2 > w) {
				if (scrollReference.x > 0) {
					// 0 - 1
					float relScroll = x
							/ (scrollReference.width - GameBase.$().getCamera().viewportWidth);
					// scrollX
					x2 += (w2 - w) * relScroll;
				}
			}
			if (h2 > h) {
				if (scrollReference.y > 0) {
					// 0 - 1
					float relScroll = y
							/ (scrollReference.height - GameBase.$()
									.getCamera().viewportHeight);
					// scrollY
					y2 += (h2 - h) * relScroll;
				}
			}
			spriteBatch.draw(image.getTexture(), x, y, image.getRegionWidth()
					* .5f + origin.x, image.getRegionHeight() * .5f + origin.y,
					w, h, 1f, 1f, rotation, x2, y2, (int) (w / scale.x),
					(int) (h / scale.y), false, false);
		} else {
			spriteBatch.draw(image, x, y, image.getRegionWidth() * .5f
					+ origin.x, image.getRegionHeight() * .5f + origin.y, w, h,
					scale.x, scale.y, rotation);
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

	/**
	 * Disposes the underlying {@link TextureRegionRef}. You can either dispose
	 * the {@link BoundedImage} <b>OR</b> the underlying
	 * {@link TextureRegionRef}. <b>NEVER</b> call <b>both</b>, this would
	 * confuse the reference counting!
	 */
	@Override
	public void dispose() {
		image.dispose();
	}
}
