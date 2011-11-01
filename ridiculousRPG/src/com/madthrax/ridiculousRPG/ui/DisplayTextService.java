package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * Draws multilined text onto the screen. This class is optimized for performance.
 * Don't hesitate to use it.
 */
public abstract class DisplayTextService extends GameServiceDefaultImpl implements Drawable, Initializable {
	public enum Alignment {LEFT, BOTTOM, CENTER, RIGHT, TOP}

	/**
	 * A singleton instance for simply printing messages onto the screen.
	 */
	public static final DisplayTextService $screen = new DisplayTextService() {
		@Override
		public Matrix4 projectionMatrix(Camera camera) {
			return camera.view;
		}
	};
	/**
	 * A singleton instance for simply printing messages onto the map.
	 */
	public static final DisplayTextService $map = new DisplayTextService(){
		@Override
		public Matrix4 projectionMatrix(Camera camera) {
			return camera.projection;
		}
	};
	private BitmapFont font;
	private Array<Message> msgs = new Array<Message>(false, 64);

	/**
	 * Changing the default color will effect all messages drawn afterwards.
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @see {@link Color#toFloatBits()}
	 */
	public float defaultColor = Color.WHITE.toFloatBits();

	private Pool<Message> messagePool = new Pool<Message>(64, 2048) {
		@Override
		protected Message newObject() {
			return new Message();
		}
	};
	class Message {
		float x, y, color;
		CharSequence text;
		public void drawMultiLine(SpriteBatch spriteBatch) {
			font.setColor(color);
			font.draw(spriteBatch, text, x, y);
		}
	}

	protected DisplayTextService() {}

	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public void message(CharSequence text, float x, float y) {
		message(text, defaultColor, x, y);
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public void message(CharSequence text, Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		message(text, defaultColor, horizontalAlign, verticalAlign, padding);
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @see {@link Color#toFloatBits()}
	 */
	public void message(CharSequence text, float color, Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		if (!isInitialized()) return;
		TextBounds b = font.getMultiLineBounds(text);
		float x=padding, y=b.height+padding;

		if (horizontalAlign==Alignment.CENTER)
			x = (GameBase.screenWidth-b.width)*.5f;
		else if (horizontalAlign==Alignment.RIGHT)
			x = GameBase.screenWidth-b.width-padding;

		if (verticalAlign==Alignment.CENTER)
			y = (GameBase.screenHeight-b.height)*.5f;
		else if (verticalAlign==Alignment.TOP)
			y = GameBase.screenHeight-padding;
			
		msgs.add(createMsg(text, color, x, y));
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @see {@link Color#toFloatBits()}
	 */
	public void message(CharSequence text, float color, float x, float y) {
		if (!isInitialized()) return;
		msgs.add(createMsg(text, color, x, y));
	}
	private Message createMsg(CharSequence text, float color, float x, float y) {
		Message m = messagePool.obtain();
		m.text = text;
		m.x = x;
		m.y = y;
		m.color = color;
		return m;
	}
	/**
	 * Don't call this method yourself. It's called by the {@link GameServiceProvider}.
	 */
	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		Message m;
		for (int i = msgs.size-1; i>-1; i--) {
			messagePool.free(m = msgs.pop());
			m.drawMultiLine(spriteBatch);
		}
	}
	/**
	 * @param font
	 * The font will automatically be disposed by this service.
	 */
	public void setFont(BitmapFont font) {
		if (font!=null) font.dispose();
		this.font = font;
	}
	@Override
	public void init() {
		if (isInitialized() || !GameBase.isGameInitialized()) return;
		font = new BitmapFont();
	}
	@Override
	public boolean isInitialized() {
		return font!=null;
	}
	@Override
	public void dispose() {
		if (isInitialized()) font.dispose();
	}
}
