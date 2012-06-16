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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;

/**
 * Draws multilined text onto the screen. This class is optimized for
 * performance. Don't hesitate to use it.
 * 
 * @author Alexander Baumgartner
 */
public abstract class DisplayPlainTextService extends GameServiceDefaultImpl
		implements Drawable {

	public enum Alignment {
		LEFT, BOTTOM, CENTER, RIGHT, TOP
	}

	private BitmapFont font;
	private final Array<BitmapFontCache> msgDisplay = new Array<BitmapFontCache>(
			false, 32);
	private final Array<BitmapFontCache> msgDisplayOnce = new Array<BitmapFontCache>(
			false, 64);
	private final BitmapFontCachePool fontCachePool = new BitmapFontCachePool();
	private float defaultColor = Color.WHITE.toFloatBits();

	protected DisplayPlainTextService() {
		font = new BitmapFont();
	}

	/**
	 * @return the actual default colors float bits
	 * @see {@link Color#toFloatBits()}
	 */
	public float getDefaultColor() {
		return defaultColor;
	}

	/**
	 * Changing the default color will effect all messages drawn afterwards. (To
	 * increase the performance you should not compute the colors float bits at
	 * every iteration)
	 * 
	 * @see {@link Color#toFloatBits()}
	 * @param defaultColor
	 *            the float bits representing the new default color
	 */
	public void setDefaultColor(float defaultColor) {
		this.defaultColor = defaultColor;
	}

	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public BitmapFontCache addMessage(CharSequence text, float x, float y) {
		return addMessage(text, defaultColor, x, y, 0f, false);
	}

	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public BitmapFontCache addMessage(CharSequence text,
			Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		return addMessage(text, defaultColor, horizontalAlign, verticalAlign,
				padding);
	}

	/**
	 * Adds a message which will be drawn onto the screen
	 */
	public BitmapFontCache addMessage(CharSequence text,
			Alignment horizontalAlign, Alignment verticalAlign,
			boolean forceRemove) {
		return addMessage(text, defaultColor, horizontalAlign, verticalAlign,
				0f, 0f, forceRemove);
	}

	/**
	 * Adds a message which will be drawn onto the screen (To increase the
	 * performance you should not compute the colors floatbits at every
	 * iteration)
	 * 
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color,
			Alignment horizontalAlign, Alignment verticalAlign, float padding) {
		return addMessage(text, color, horizontalAlign, verticalAlign, padding,
				0f, false);
	}

	/**
	 * Adds a message which will be drawn onto the screen (To increase the
	 * performance you should not compute the colors floatbits at every
	 * iteration)
	 * 
	 * @param wrapWidth
	 *            If wrapWidth > 0 then the text will be wrapped at the
	 *            specified bound.
	 * @param forceRemove
	 *            to remove the message immediately after displaying it
	 *            (displays it for only one frame).
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color,
			Alignment horizontalAlign, Alignment verticalAlign, float padding,
			float wrapWidth, boolean forceRemove) {
		float x = padding, y = GameBase.$().getScreen().height - padding;
		BitmapFontCache bfc = createMsg(text, color, 0f, 0f, wrapWidth);
		TextBounds b = bfc.getBounds();

		if (horizontalAlign == Alignment.CENTER)
			x = (GameBase.$().getScreen().width - b.width) * .5f;
		else if (horizontalAlign == Alignment.RIGHT)
			x = GameBase.$().getScreen().width - b.width - padding;

		if (verticalAlign == Alignment.CENTER)
			y = GameBase.$().getScreen().height
					- (GameBase.$().getScreen().height - b.height) * .5f;
		else if (verticalAlign == Alignment.BOTTOM)
			y = b.height + padding;

		if (wrapWidth == 0f
				&& projectionMatrix(GameBase.$().getCamera()) == GameBase.$()
						.getCamera().view) {
			if (x < 0f)
				x = 0f;
			if (y > GameBase.$().getScreen().height)
				y = GameBase.$().getScreen().height;
		}

		bfc.setPosition(x, y);
		if (forceRemove)
			msgDisplayOnce.add(bfc);
		else
			msgDisplay.add(bfc);
		return bfc;
	}

	/**
	 * Adds a message which will be drawn onto the screen (To increase the
	 * performance you should not compute the colors floatbits at every
	 * iteration)
	 * 
	 * @param wrapWidth
	 *            If wrapWidth > 0 then the text will be wrapped at the
	 *            specified bound.
	 * @param forceRemove
	 *            to remove the message immediately after displaying it
	 *            (displays it for only one frame).
	 * @see {@link Color#toFloatBits()}
	 */
	public BitmapFontCache addMessage(CharSequence text, float color, float x,
			float y, float wrapWidth, boolean forceRemove) {
		BitmapFontCache bfc = createMsg(text, color, x, y, wrapWidth);
		if (forceRemove)
			msgDisplayOnce.add(bfc);
		else
			msgDisplay.add(bfc);
		return bfc;
	}

	public void removeMessage(BitmapFontCache msg) {
		fontCachePool.free(msg);
		msgDisplay.removeValue(msg, true);
	}

	private BitmapFontCache createMsg(CharSequence text, float color, float x,
			float y, float wrapWidth) {
		BitmapFontCache bfc = fontCachePool.obtain(font);
		bfc.setColor(color);
		if (wrapWidth > 0f)
			bfc.setWrappedText(text, x, y, wrapWidth);
		else
			bfc.setMultiLineText(text, x, y);
		return bfc;
	}

	/**
	 * Don't call this method yourself. It's called by the
	 * {@link GameServiceProvider}.
	 */
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		for (int i = msgDisplay.size - 1; i > -1; i--)
			msgDisplay.get(i).draw(spriteBatch);
		for (int i = msgDisplayOnce.size - 1; i > -1; i--)
			msgDisplayOnce.get(i).draw(spriteBatch);
		msgDisplayOnce.clear();
	}

	/**
	 * @param font
	 *            The font will automatically be disposed by this service.
	 */
	public void setFont(BitmapFont font) {
		if (font != null)
			font.dispose();
		this.font = font;
	}

	/**
	 * The {@link DisplayPlainTextService} (an per default also it's successors)
	 * is essential and will always be drawn. No matter if an other
	 * {@link GameService} has frozen the world.
	 * 
	 * @return true
	 * @see GameService#essential()
	 */
	@Override
	public boolean essential() {
		return true;
	}

	public void dispose() {
		msgDisplay.clear();
		msgDisplayOnce.clear();
		fontCachePool.clear();
		if (font != null)
			font.dispose();
	}
}
