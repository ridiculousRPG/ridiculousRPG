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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;

/**
 * Extends the {@link Window} class and offers a variable sized title box.<br>
 * Therefore three {@link NinePatch}s are used. One for the titlebox (
 * {@link WindowWithVariableTitleboxStyle#titleBackground}),<br>
 * a second one to align the title to the left side of the window (
 * {@link WindowWithVariableTitleboxStyle#titleBackgroundStretch})<br>
 * and the third one is used for the message body (
 * {@link WindowStyle#background}).
 * 
 * @see Window
 * @author Alexander Baumgartner
 */
public class WindowWithVariableTitlebox extends Window {
	private Rectangle titleBounds = new Rectangle();

	public WindowWithVariableTitlebox(Stage stage, Skin skin) {
		this("", stage, skin);
	}

	public WindowWithVariableTitlebox(String title, Stage stage, Skin skin) {
		this(title, stage, skin.getStyle(WindowWithVariableTitleboxStyle.class));
	}

	public WindowWithVariableTitlebox(String title, Stage stage,
			WindowWithVariableTitleboxStyle style) {
		super(title, stage, style);
	}

	public WindowWithVariableTitlebox(String title, Stage stage,
			WindowWithVariableTitleboxStyle style, int prefWidth,
			int prefHeight, String name) {
		super(title, stage, style, name);
		height(prefHeight);
		width(prefWidth);
	}

	@Override
	public void setStyle(WindowStyle style) {
		super.setStyle(style);

		calculateTitleBounds();
		invalidateHierarchy();
	}

	private void calculateTitleBounds() {
		WindowWithVariableTitleboxStyle style = (WindowWithVariableTitleboxStyle) getStyle();
		final NinePatch titleBackground = style.titleBackground;
		final Rectangle titleBounds = new Rectangle();
		final TextBounds textBounds = style.titleFont
				.getMultiLineBounds(getTitle());

		titleBounds.width = textBounds.width;
		titleBounds.x = titleBackground.getLeftWidth();
		titleBounds.height = titleBackground.getBottomHeight()
				+ titleBackground.getTopHeight() + textBounds.height;
		titleBounds.y = height - titleBounds.height;

		padTop((int) (titleBounds.height + style.background.getTopHeight()));
		this.titleBounds = titleBounds;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		WindowWithVariableTitleboxStyle style = (WindowWithVariableTitleboxStyle) getStyle();
		final NinePatch background = style.background;
		final NinePatch titleBackground = style.titleBackground;
		final NinePatch titleBackgroundStretch = style.titleBackgroundStretch;
		final BitmapFont titleFont = style.titleFont;
		final Color titleFontColor = style.titleFontColor;
		final float colorA = color.a * parentAlpha;

		validate();
		applyTransform(batch);
		calculateTitleBounds();
		batch.setColor(color.r, color.g, color.b, colorA);

		float titleWidth = titleBounds.width + titleBackground.getLeftWidth()
				+ titleBackground.getRightWidth();
		float width = Math.max(titleWidth
				+ titleBackgroundStretch.getLeftWidth()
				+ titleBackgroundStretch.getRightWidth(), this.width);
		titleBackground.draw(batch, 0, height - titleBounds.height, titleWidth,
				titleBounds.height);
		titleBackgroundStretch.draw(batch, titleWidth, height
				- titleBounds.height, width - titleWidth, titleBounds.height);
		background.draw(batch, 0, 0, width, height - titleBounds.height);

		titleFont.setColor(color.r * titleFontColor.r, color.g
				* titleFontColor.g, color.b * titleFontColor.b, colorA
				* titleFontColor.a);
		titleFont.drawMultiLine(batch, getTitle(), titleBackground
				.getLeftWidth(), height - titleBackground.getTopHeight());
		batch.flush();

		super.drawChildren(batch, parentAlpha);

		resetTransform(batch);
	}

	@Override
	public boolean touchDown(float x, float y, int pointer) {
		if (titleBounds.y > y) {
			super.touchDown(x, y, pointer);
		} else if (titleBounds.contains(x, y)) {
			super.touchDown(x, titleBounds.y + titleBounds.height, pointer);
		}
		return false;
	}

	/**
	 * Defines the style of a window with a variable sized title box,
	 * 
	 * @see {@link WindowWithVariableTitlebox}
	 */
	static public class WindowWithVariableTitleboxStyle extends WindowStyle {
		public NinePatch titleBackground;
		public NinePatch titleBackgroundStretch;

		public WindowWithVariableTitleboxStyle() {
		}

		public WindowWithVariableTitleboxStyle(BitmapFont titleFont,
				Color titleFontColor, NinePatch background,
				NinePatch titleBackground, NinePatch titleBackgroundStretch) {
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);
			this.background = background;
			this.titleBackground = titleBackground;
			this.titleBackgroundStretch = titleBackgroundStretch;
		}
	}
}
