package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.ui.utils.ScissorStack;

/**
 * Extends the {@link Window} class and offers a variable
 * sized title box.<br>
 * Therefore three {@link NinePatch}s are used. One for the
 * titlebox ({@link WindowWithVariableTitleboxStyle#titleBackground}),<br>
 * a second one to align the title to the left side of the window
 * ({@link WindowWithVariableTitleboxStyle#titleBackgroundStretch})<br>
 * and the third one is used for the message body ({@link WindowStyle#background}).
 * @see Window
 */
public class WindowWithVariableTitlebox extends Window {

	public WindowWithVariableTitlebox(Stage stage, Skin skin) {
		this("", stage, skin);
	}
	public WindowWithVariableTitlebox(String title, Stage stage, Skin skin) {
		this(title, stage, skin.getStyle(WindowWithVariableTitleboxStyle.class));
	}
	public WindowWithVariableTitlebox(String title, Stage stage, WindowWithVariableTitleboxStyle style) {
		this(null, title, stage, style, 150, 150);
	}
	public WindowWithVariableTitlebox(String name, String title, Stage stage, WindowWithVariableTitleboxStyle style, int prefWidth, int prefHeight) {
		super(name, title, stage, style, prefWidth, prefHeight);
	}

	@Override
	public void setStyle (WindowStyle style) {
		this.style = style;
		setBackground(style.background);

		computeTitleBounds((WindowWithVariableTitleboxStyle) style);

		invalidateHierarchy();
	}

	private void calculateBoundsAndScissors (Matrix4 transform) {
		final NinePatch background = style.background;
		final Rectangle widgetBounds = this.widgetBounds;

		widgetBounds.x = background.getLeftWidth();
		widgetBounds.y = background.getBottomHeight();
		widgetBounds.width = width - background.getLeftWidth() - background.getRightWidth();
		widgetBounds.height = height - background.getTopHeight() - background.getBottomHeight();

		computeTitleBounds((WindowWithVariableTitleboxStyle) style);

		widgetBounds.height -= titleBounds.height;

		ScissorStack.calculateScissors(stage.getCamera(), transform, widgetBounds, scissors);
	}
	private void computeTitleBounds(WindowWithVariableTitleboxStyle style) {
		final BitmapFont titleFont = style.titleFont;
		final NinePatch titleBackground = style.titleBackground;
		final Rectangle titleBounds = this.titleBounds;

		textBounds.set(titleFont.getMultiLineBounds(title));

		titleBounds.width = textBounds.width;
		titleBounds.x = titleBackground.getLeftWidth();
		titleBounds.height = titleBackground.getBottomHeight() + titleBackground.getTopHeight() + textBounds.height;
		titleBounds.y = height - titleBounds.height;

		padTop((int)(titleBounds.height + style.background.getTopHeight()));
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		WindowWithVariableTitleboxStyle style = (WindowWithVariableTitleboxStyle) this.style;
		final NinePatch background = style.background;
		final NinePatch titleBackground = style.titleBackground;
		final NinePatch titleBackgroundStretch = style.titleBackgroundStretch;
		final BitmapFont titleFont = style.titleFont;
		final Color titleFontColor = style.titleFontColor;
		final float colorA = color.a * parentAlpha;

		validate();
		applyTransform(batch);
		calculateBoundsAndScissors(batch.getTransformMatrix());
		batch.setColor(color.r, color.g, color.b, colorA);

		float titleWidth = titleBounds.width+titleBackground.getLeftWidth()+titleBackground.getRightWidth();
		float width = Math.max(titleWidth+titleBackgroundStretch.getLeftWidth()+titleBackgroundStretch.getRightWidth(), this.width);
		titleBackground.draw(batch, 0, height-titleBounds.height, titleWidth, titleBounds.height);
		titleBackgroundStretch.draw(batch, titleWidth, height-titleBounds.height, width-titleWidth, titleBounds.height);
		background.draw(batch, 0, 0, width, height-titleBounds.height);

		titleFont.setColor(color.r*titleFontColor.r, color.g*titleFontColor.g, color.b*titleFontColor.b, colorA*titleFontColor.a);
		titleFont.drawMultiLine(batch, title, titleBackground.getLeftWidth(), height-titleBackground.getTopHeight());
		batch.flush();

		if (ScissorStack.pushScissors(scissors)) {
			super.drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		resetTransform(batch);
	}
	/** 
	 * Defines the style of a window with a variable sized title box,
	 * @see {@link WindowWithVariableTitlebox}
	 */
	static public class WindowWithVariableTitleboxStyle extends WindowStyle {
		public NinePatch titleBackground;
		public NinePatch titleBackgroundStretch;

		public WindowWithVariableTitleboxStyle () {}

		public WindowWithVariableTitleboxStyle (BitmapFont titleFont, Color titleFontColor, NinePatch background, NinePatch titleBackground, NinePatch titleBackgroundStretch) {
			this.titleFont = titleFont;
			this.titleFontColor.set(titleFontColor);
			this.background = background;
			this.titleBackground = titleBackground;
			this.titleBackgroundStretch = titleBackgroundStretch;
		}
	}

}
