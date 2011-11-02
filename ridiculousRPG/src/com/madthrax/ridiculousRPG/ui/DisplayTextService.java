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

package com.madthrax.ridiculousRPG.ui;

import java.util.IdentityHashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * Draws multilined text onto the screen. This class is optimized for performance.
 * Don't hesitate to use it.
 * @author Alexander Baumgartner
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
	private final IdentityHashMap<BitmapFontCache, Boolean> msgs = new IdentityHashMap<BitmapFontCache, Boolean>(64);
	private final BitmapFontCachePool fontCachePool = new BitmapFontCachePool();

	/**
	 * Changing the default color will effect all messages drawn afterwards.
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @see {@link Color#toFloatBits()}
	 */
	public float defaultColor = Color.WHITE.toFloatBits();

	protected DisplayTextService() {}

	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public BitmapFontCache addMessage(CharSequence text, float x, float y) {
		return addMessage(text, defaultColor, x, y, 0f, false);
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public BitmapFontCache addMessage(CharSequence text, Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		return addMessage(text, defaultColor, horizontalAlign, verticalAlign, padding);
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color, Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		return addMessage(text, color, horizontalAlign, verticalAlign, padding, 0f, false);
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @param wrapWidth If wrapWidth > 0 then the text will be wrapped at the specified bound.
	 * @param autoRemove to remove the message immediately after displaying it (displays it for only one frame).
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color, Alignment horizontalAlign, Alignment verticalAlign, float padding, float wrapWidth, boolean autoRemove) {
		if (!isInitialized()) return null;
		float x=padding, y=GameBase.screenHeight-padding;
		BitmapFontCache bfc = createMsg(text, color, 0f, 0f, wrapWidth);
		TextBounds b = bfc.getBounds();

		if (horizontalAlign==Alignment.CENTER)
			x = (GameBase.screenWidth-b.width)*.5f;
		else if (horizontalAlign==Alignment.RIGHT)
			x = GameBase.screenWidth-b.width-padding;

		if (verticalAlign==Alignment.CENTER)
			y = GameBase.screenHeight - (GameBase.screenHeight-b.height)*.5f;
		else if (verticalAlign==Alignment.BOTTOM)
			y = b.height+padding;

		if (wrapWidth==0f && projectionMatrix(GameBase.camera)==GameBase.camera.view) {
			if (x < 0f) x = 0f;
			if (y > GameBase.screenHeight) y = GameBase.screenHeight;
		}

		bfc.setPosition(x, y);
		msgs.put(bfc, Boolean.valueOf(autoRemove));
		return bfc;
	}
	/**
	 * Adds a message which will be drawn onto the screen
	 * (To increase the performance you should not compute the colors 
	 * floatbits at every iteration)
	 * @param wrapWidth If wrapWidth > 0 then the text will be wrapped at the specified bound.
	 * @param autoRemove to remove the message immediately after displaying it (displays it for only one frame).
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color, float x, float y, float wrapWidth, boolean autoRemove) {
		if (!isInitialized()) return null;
		BitmapFontCache bfc = createMsg(text, color, x, y, wrapWidth);
		msgs.put(bfc, Boolean.valueOf(autoRemove));
		return bfc;
	}
	public void removeMessage(BitmapFontCache msg) {
		fontCachePool.free(msg);
		msgs.remove(msg);
	}

	private BitmapFontCache createMsg(CharSequence text, float color, float x, float y, float wrapWidth) {
		BitmapFontCache bfc = fontCachePool.obtain(font);
		bfc.setColor(color);
		if (wrapWidth > 0f)
			bfc.setWrappedText(text, x, y, wrapWidth);
		else
			bfc.setMultiLineText(text, x, y);
		return bfc;
	}
	/**
	 * Don't call this method yourself. It's called by the {@link GameServiceProvider}.
	 */
	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		for (Entry<BitmapFontCache, Boolean> entry : msgs.entrySet()) {
			entry.getKey().draw(spriteBatch);
			if (entry.getValue().booleanValue()) removeMessage(entry.getKey());
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
		msgs.clear();
		if (isInitialized()) font.dispose();
	}
}
