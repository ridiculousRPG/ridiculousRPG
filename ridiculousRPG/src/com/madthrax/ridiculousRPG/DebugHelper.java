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

package com.madthrax.ridiculousRPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;

/**
 * This class offers some debug functions.
 * 
 * @author Alexander Baumgartner
 */
public final class DebugHelper {
	private DebugHelper() {
	} // static container

	private static BitmapFont f = null;

	public static void drawServiceExecutionOrder(SpriteBatch spriteBatch,
			Camera camera, Array<Computable> computables,
			Array<Drawable> drawables, GameService holdsAttention) {
		spriteBatch.setProjectionMatrix(camera.view);
		spriteBatch.begin();
		String text = "Execution order of Computable services";
		for (Computable c : computables) {
			text += "\n        " + c.getClass().getName();
		}
		text += "\n\nExecution order of Drawable services";
		for (Drawable d : drawables) {
			text += "\n        " + d.getClass().getName();
		}
		if (holdsAttention != null) {
			text += "\n\n" + holdsAttention.getClass().getName()
					+ " holds attention!";
		}
		f.setColor(1f, 1f, 0f, 1f);
		TextBounds b = f.getMultiLineBounds(text);
		f.drawMultiLine(spriteBatch, text,
				(GameBase.$().getScreen().width - b.width) * .5f, GameBase.$()
						.getScreen().height
						- (GameBase.$().getScreen().height - b.height) * .5f);
		spriteBatch.end();
	}

	public static void drawMousePosition(SpriteBatch spriteBatch, Camera camera) {
		spriteBatch.setProjectionMatrix(camera.view);
		spriteBatch.begin();
		float x1 = Gdx.input.getX();
		float y1 = GameBase.$().getScreen().height - Gdx.input.getY();
		String text = "( " + (int) x1 + " / " + (int) y1 + " ) Screen\n";
		float x2 = camera.position.x + x1 * camera.viewportWidth
				/ GameBase.$().getScreen().width;
		float y2 = camera.position.y + y1 * camera.viewportHeight
				/ GameBase.$().getScreen().height;
		text += "( " + (int) x2 + " / " + (int) y2 + " ) Camera";
		f.setColor(1f, 0f, 1f, 1f);
		TextBounds b = f.getMultiLineBounds(text);
		f.drawMultiLine(spriteBatch, text, Math.max(Math.min(x1 + 10, GameBase
				.$().getScreen().width
				- b.width), 0f), Math.max(Math.min(y1, GameBase.$()
				.getScreen().height), b.height));
		spriteBatch.end();
	}

	public static void drawViewportCorners(SpriteBatch spriteBatch,
			Camera camera) {
		spriteBatch.setProjectionMatrix(camera.projection);
		spriteBatch.begin();
		float x1 = Math.max(0f, camera.position.x);
		float y1 = Math.max(0f, camera.position.y);
		float x2 = x1
				+ Math.min(camera.viewportWidth, GameBase.$().getPlane().width);
		float y2 = y1
				+ Math.min(camera.viewportHeight,
						GameBase.$().getPlane().height);
		if (f == null)
			f = new BitmapFont();
		f.setColor(0f, 1f, 1f, 1f);
		String text = "( " + (int) x1 + " / " + (int) y1 + " )";
		f.draw(spriteBatch, text, x1, y1 + f.getLineHeight());
		text = "( " + (int) x2 + " / " + (int) y2 + " )";
		TextBounds b = f.getBounds(text);
		f.draw(spriteBatch, text, x2 - b.width, y2);
		spriteBatch.end();
	}
}
