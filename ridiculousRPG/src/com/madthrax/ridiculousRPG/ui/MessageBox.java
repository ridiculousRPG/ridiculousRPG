package com.madthrax.ridiculousRPG.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameConfig;
import com.madthrax.ridiculousRPG.camera.CameraSimpleOrtho2D;
import com.madthrax.ridiculousRPG.messaging.style.EventMessageStyle;
import com.madthrax.ridiculousRPG.messaging.style.TextBoxStyle;

public class MessageBox {
	/**
	 * Displays a text by the given id and uses String.format
	 * to fill and format the output with the given arguments.
	 * @param spriteBatch
	 * The SpriteBatch used for drawing the message
	 * @param cam
	 * The camera view
	 * @param textId
	 * The id of this text (internationalized)
	 * @param textArgs
	 * Arguments to use with String.format
	 */
	public static void textBoxCentered(SpriteBatch spriteBatch, int textId, Object... textArgs) {
		String text = TextLoader.loadText(textId, GameConfig.get().locale, textArgs);
		TextBoxStyle textBox = GameConfig.get().uiSkin.getStyle("default", TextBoxStyle.class);
		BitmapFont font = textBox.textFont;
		font.setColor(textBox.textColor);
		TextBounds bounds = font.getWrappedBounds(text, GameBase.screenWidth-textBox.textBox.getLeftWidth()-textBox.textBox.getRightWidth());
		float x = (GameBase.screenWidth-bounds.width)/2;
		float y = (GameBase.screenHeight+bounds.height)/2;
		textBox.textBox.draw(spriteBatch, x-textBox.textBox.getLeftWidth(), y-bounds.height-textBox.textBox.getBottomHeight(), bounds.width+textBox.textBox.getLeftWidth()+textBox.textBox.getRightWidth(), bounds.height+textBox.textBox.getBottomHeight()+textBox.textBox.getTopHeight());
		font.drawWrapped(spriteBatch, text, x, y, GameBase.screenWidth-textBox.textBox.getLeftWidth()-textBox.textBox.getRightWidth());
	}
	public static void eventMessage(SpriteBatch spriteBatch, int textId, Object... textArgs) {
		eventMessage(spriteBatch, textId, null, null, textArgs);
	}
	public static void eventMessage(SpriteBatch spriteBatch, int textId, String name, Object... textArgs) {
		eventMessage(spriteBatch, textId, name, null, textArgs);
	}
	public static void eventMessage(SpriteBatch spriteBatch, int textId, String name, TextureRegion face, Object... textArgs) {
		String text = TextLoader.loadText(textId, GameConfig.get().locale, textArgs);
		if (name==null) {
			TextBoxStyle textBox = GameConfig.get().uiSkin.getStyle("default", TextBoxStyle.class);
			BitmapFont font = textBox.textFont;
			font.setColor(textBox.textColor);

			float minHeight = 80f;
			float messageDrawMaxWidth = GameBase.screenWidth-textBox.textBox.getLeftWidth()-textBox.textBox.getRightWidth();
			TextBounds bounds = font.getWrappedBounds(text, messageDrawMaxWidth);

			float messageHeight = Math.max(bounds.height+textBox.textBox.getBottomHeight()+textBox.textBox.getTopHeight(), minHeight);
			textBox.textBox.draw(spriteBatch, 0, 0, GameBase.screenWidth, messageHeight);
			font.drawWrapped(spriteBatch, text, textBox.textBox.getLeftWidth(), messageHeight-textBox.textBox.getTopHeight(), messageDrawMaxWidth);
		} else {
			EventMessageStyle textBox = GameConfig.get().uiSkin.getStyle("default", EventMessageStyle.class);
			BitmapFont nameFont = textBox.eventNameFont;
			nameFont.setColor(textBox.eventNameColor);
			BitmapFont messageFont = textBox.messageFont;
			messageFont.setColor(textBox.messageColor);

			float nameDrawMaxWidth = 280;
			TextBounds bounds = nameFont.getWrappedBounds(name, nameDrawMaxWidth);
			float nameWidth = bounds.width + textBox.eventNameBox.getLeftWidth() + textBox.eventNameBox.getRightWidth();
			float nameHeight = bounds.height + textBox.eventNameBox.getBottomHeight() + textBox.eventNameBox.getTopHeight();

			float messageOffset = 0f;
			float minHeight = 80f;
			if (face!=null) {
				minHeight = Math.max(minHeight, face.getRegionHeight());
				messageOffset = face.getRegionWidth()+10;
			}

			float messageDrawMaxWidth = GameBase.screenWidth-textBox.messageBox.getLeftWidth()-textBox.messageBox.getRightWidth()-messageOffset;
			bounds = messageFont.getWrappedBounds(text, messageDrawMaxWidth);
			float messageHeight = Math.max(bounds.height+textBox.messageBox.getBottomHeight()+textBox.messageBox.getTopHeight(), minHeight);
			float messageWidth = GameBase.screenWidth;

			textBox.messageBox.draw(spriteBatch, 0, 0, messageWidth, messageHeight);
			textBox.eventNameBox.draw(spriteBatch, 0, messageHeight, nameWidth, nameHeight);
			textBox.eventNameStretch.draw(spriteBatch, nameWidth, messageHeight, messageWidth-nameWidth, nameHeight);
			if (face!=null) {
				spriteBatch.draw(face, textBox.messageBox.getLeftWidth(), textBox.messageBox.getBottomHeight() + (bounds.height-face.getRegionHeight())*.5f);
			}
			messageFont.drawWrapped(spriteBatch, text, textBox.messageBox.getLeftWidth()+messageOffset, messageHeight - textBox.messageBox.getTopHeight(), messageDrawMaxWidth);
			nameFont.drawWrapped(spriteBatch, name, textBox.eventNameBox.getLeftWidth(), messageHeight+nameHeight - textBox.eventNameBox.getTopHeight(), nameDrawMaxWidth);
		}
	}
	public static void eventButton(SpriteBatch spriteBatch, CameraSimpleOrtho2D cam, int textId, String name, TextureRegion face, Object... textArgs) {
		String text = TextLoader.loadText(textId, GameConfig.get().locale, textArgs);
		//if (name==null) {
			Button b = new Button(text, GameConfig.get().uiSkin.getStyle("default", ButtonStyle.class));
			b.x = 20;
			b.y=20;
			b.draw(spriteBatch, 1);
			TextBoxStyle textBox = GameConfig.get().uiSkin.getStyle("default", TextBoxStyle.class);
			BitmapFont font = textBox.textFont;
			font.setColor(textBox.textColor);

			float minHeight = 80f;
			float messageDrawMaxWidth = GameBase.screenWidth-textBox.textBox.getLeftWidth()-textBox.textBox.getRightWidth();
			TextBounds bounds = font.getWrappedBounds(text, messageDrawMaxWidth);

			float messageHeight = Math.max(bounds.height+textBox.textBox.getBottomHeight()+textBox.textBox.getTopHeight(), minHeight);
			textBox.textBox.draw(spriteBatch, 0, 0, GameBase.screenWidth, messageHeight);
			//font.drawWrapped(spriteBatch, text, textBox.textBox.getLeftWidth(), messageHeight-textBox.textBox.getTopHeight(), messageDrawMaxWidth);
		/*} else {
			EventMessageStyle textBox = GameConfig.get().uiSkin.getStyle("default", EventMessageStyle.class);
			BitmapFont nameFont = textBox.eventNameFont;
			nameFont.setColor(textBox.eventNameColor);
			BitmapFont messageFont = textBox.messageFont;
			messageFont.setColor(textBox.messageColor);

			float nameDrawMaxWidth = 280;
			TextBounds bounds = nameFont.getWrappedBounds(name, nameDrawMaxWidth);
			float nameWidth = bounds.width + textBox.eventNameBox.getLeftWidth() + textBox.eventNameBox.getRightWidth();
			float nameHeight = bounds.height + textBox.eventNameBox.getBottomHeight() + textBox.eventNameBox.getTopHeight();

			float messageOffset = 0f;
			float minHeight = 80f;
			if (face!=null) {
				minHeight = Math.max(minHeight, face.getRegionHeight());
				messageOffset = face.getRegionWidth()+10;
			}

			float messageDrawMaxWidth = GameBase.screenWidth-textBox.messageBox.getLeftWidth()-textBox.messageBox.getRightWidth()-messageOffset;
			bounds = messageFont.getWrappedBounds(text, messageDrawMaxWidth);
			float messageHeight = Math.max(bounds.height+textBox.messageBox.getBottomHeight()+textBox.messageBox.getTopHeight(), minHeight);
			float messageWidth = GameBase.screenWidth;

			textBox.messageBox.draw(spriteBatch, 0, 0, messageWidth, messageHeight);
			textBox.eventNameBox.draw(spriteBatch, 0, messageHeight, nameWidth, nameHeight);
			textBox.eventNameStretch.draw(spriteBatch, nameWidth, messageHeight, messageWidth-nameWidth, nameHeight);
			if (face!=null) {
				spriteBatch.draw(face, textBox.messageBox.getLeftWidth(), textBox.messageBox.getBottomHeight() + (bounds.height-face.getRegionHeight())*.5f);
			}
			messageFont.drawWrapped(spriteBatch, text, textBox.messageBox.getLeftWidth()+messageOffset, messageHeight - textBox.messageBox.getTopHeight(), messageDrawMaxWidth);
			nameFont.drawWrapped(spriteBatch, name, textBox.eventNameBox.getLeftWidth(), messageHeight+nameHeight - textBox.eventNameBox.getTopHeight(), nameDrawMaxWidth);
		}*/
	}
}
