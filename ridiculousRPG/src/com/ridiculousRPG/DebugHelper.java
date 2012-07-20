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

package com.ridiculousRPG;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.ridiculousRPG.event.EventObject;
import com.ridiculousRPG.event.PolygonObject;
import com.ridiculousRPG.service.Computable;
import com.ridiculousRPG.service.Drawable;
import com.ridiculousRPG.service.GameService;
import com.ridiculousRPG.ui.DisplayPlainTextService;
import com.ridiculousRPG.ui.DisplayPlainTextService.Alignment;

/**
 * This class offers some debug functions.
 * 
 * @author Alexander Baumgartner
 */
public final class DebugHelper {
	private static ShapeRenderer debugRenderer;
	private static DisplayPlainTextService textMapDebugger;
	private static DisplayPlainTextService textViewDebugger;
	private static final float colorServiceDebug = new Color(1f, 1f, 0f, .5f)
			.toFloatBits();

	private DebugHelper() {
	} // static container

	public static void drawServiceExecutionOrder(SpriteBatch spriteBatch,
			Camera camera, Array<Computable> computables,
			Array<Drawable> drawables, GameService holdsAttention) {

		String text = "";
		if (holdsAttention != null) {
			text += holdsAttention.getClass().getName()
					+ " holds attention!\n\n";
		}

		text += "Execution order of Computable services";
		for (Computable c : computables) {
			text += "\n        " + c.getClass().getName();
		}
		text += "\n\nExecution order of Drawable services";
		for (Drawable d : drawables) {
			text += "\n        " + d.getClass().getName();
		}

		getTextViewDebugger().addMessage(text, colorServiceDebug,
				Alignment.CENTER, Alignment.CENTER, 0f,
				GameBase.$().getScreen().width, true);
	}

	public static void drawMousePosition(SpriteBatch spriteBatch, Camera camera) {
		float x1 = Gdx.input.getX();
		float y1 = GameBase.$().getScreen().height - Gdx.input.getY();

		String text = "( " + (int) x1 + " / " + (int) y1 + " ) Screen\n";
		float x2 = camera.position.x + x1 * camera.viewportWidth
				/ GameBase.$().getScreen().width;
		float y2 = camera.position.y + y1 * camera.viewportHeight
				/ GameBase.$().getScreen().height;
		text += "( " + (int) x2 + " / " + (int) y2 + " ) Camera\n";
		float x3 = GameBase.$().getPlane().width - x2;
		float y3 = GameBase.$().getPlane().height - y2;
		text += "( " + (int) x3 + " / " + (int) y3 + " ) Origin top right";

		TextBounds b = getTextViewDebugger().getFont().getMultiLineBounds(text);
		float x = Math.max(Math.min(x1 + 10, GameBase.$().getScreen().width
				- b.width), 0f);
		float y = Math.max(Math.min(y1, GameBase.$().getScreen().height),
				b.height);

		getTextViewDebugger().addMessage(text, Color.MAGENTA.toFloatBits(), x,
				y, 0f, true);
	}

	public static void drawViewportCorners(SpriteBatch spriteBatch,
			Camera camera) {
		float x = Math.max(0f, camera.position.x);
		float y = Math.max(0f, camera.position.y);
		String text = "( " + (int) x + " / " + (int) y + " )";
		getTextMapDebugger().addMessage(text, Color.CYAN.toFloatBits(), x,
				y + getTextMapDebugger().getFont().getLineHeight(), 0f, true);

		x += Math.min(camera.viewportWidth, GameBase.$().getPlane().width);
		y += Math.min(camera.viewportHeight, GameBase.$().getPlane().height);
		text = "( " + (int) x + " / " + (int) y + " )";
		TextBounds b = getTextViewDebugger().getFont().getMultiLineBounds(text);
		getTextMapDebugger().addMessage(text, Color.CYAN.toFloatBits(),
				x - b.width, y, 0f, true);
	}

	public static void debugEvents(List<EventObject> dynamicRegions) {
		if (debugRenderer == null)
			debugRenderer = new ShapeRenderer();
		debugRenderer.setProjectionMatrix(GameBase.$().getCamera().projection);
		debugRenderer.begin(ShapeType.Rectangle);
		for (EventObject ev : dynamicRegions) {
			if (ev.visible) {
				debugRenderer.setColor(.7f, .7f, .7f, 1f);
				debugRenderer.rect(ev.drawBound.x, ev.drawBound.y,
						ev.drawBound.width, ev.drawBound.height);
			}
			debugRenderer.setColor(ev.blockingBehavior.color);
			debugRenderer.rect(ev.getX(), ev.getY(),
					Math.max(1, ev.getWidth()), Math.max(1, ev.getHeight()));
			if (ev.name != null) {
				getTextMapDebugger().addMessage(ev.name,
						ev.blockingBehavior.color.toFloatBits(),
						ev.drawBound.x + 2f,
						ev.drawBound.y + ev.drawBound.height - 2, 0f, true);
			}
		}
		debugRenderer.end();
	}

	public static void debugRectangle(Color color, Matrix4 transform,
			Rectangle... rects) {
		if (debugRenderer == null)
			debugRenderer = new ShapeRenderer();
		debugRenderer.setProjectionMatrix(GameBase.$().getCamera().projection);
		if (transform != null)
			debugRenderer.setTransformMatrix(transform);
		debugRenderer.begin(ShapeType.Rectangle);
		debugRenderer.setColor(color);
		for (Rectangle r : rects) {
			debugRenderer.rect(r.x, r.y, r.width, r.height);
		}
		if (transform != null)
			debugRenderer.identity();
		debugRenderer.end();
	}

	public static void debugPolygons(List<PolygonObject> polyList) {
		for (PolygonObject poly : polyList) {
			for (int i = poly.vertexX.length - 1; i >= 0; i--) {
				float x = poly.vertexX[i];
				Color c = poly.getColor();
				if (c == null)
					c = poly.blockingBehavior.color;
				if (poly.loop && i == 0)
					x -= 20;
				getTextMapDebugger().addMessage("#" + i, c.toFloatBits(), x,
						poly.vertexY[i], 0f, true);
			}
		}
	}

	public static DisplayPlainTextService getTextMapDebugger() {
		if (textMapDebugger == null) {
			textMapDebugger = new DisplayPlainTextService() {
				@Override
				public Matrix4 projectionMatrix(Camera camera) {
					return camera.projection;
				}
			};
			GameBase.$serviceProvider().putServiceHead("textMapDebugger",
					textMapDebugger);
		}
		return textMapDebugger;
	}

	public static DisplayPlainTextService getTextViewDebugger() {
		if (textViewDebugger == null) {
			textViewDebugger = new DisplayPlainTextService() {
				@Override
				public Matrix4 projectionMatrix(Camera camera) {
					return camera.view;
				}
			};
			GameBase.$serviceProvider().putServiceHead("textViewDebugger",
					textViewDebugger);
		}
		return textViewDebugger;
	}

	public static void clear() {
		if (textMapDebugger != null) {
			textMapDebugger.dispose();
			textMapDebugger = null;
		}
		if (textViewDebugger != null) {
			textViewDebugger.dispose();
			textViewDebugger = null;
		}
	}
}
