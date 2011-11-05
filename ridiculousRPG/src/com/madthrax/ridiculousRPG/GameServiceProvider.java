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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Matrix4;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.Initializable;

/**
 * This class handles the GameServices. The services are executed in the same
 * order they are added. In the most cases you probably want to draw a map
 * before drawing something else.<br>
 * Every service of the same type(=class) can only be provided once!
 * 
 * @author Alexander Baumgartner
 */
public class GameServiceProvider implements Initializable {
	private InputAdapter nullInputAdapter = new InputAdapter();
	private Map<Class<? extends GameService>, GameService> services = new HashMap<Class<? extends GameService>, GameService>();
	private AtomicReference<GameService> hasAttention = new AtomicReference<GameService>();
	private boolean freezeTheWorld = false;
	private boolean clearTheScreen = false;

	private ArrayList<Initializable> initializables = new ArrayList<Initializable>();
	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private ArrayList<Computable> computables = new ArrayList<Computable>();
	private ArrayList<Drawable> drawables = new ArrayList<Drawable>();

	public void init() {
		ArrayList<Initializable> initializables = this.initializables;
		this.initializables = null;
		Gdx.input.setInputProcessor(inputMultiplexer);
		for (Initializable service : initializables)
			service.init();
	}

	@SuppressWarnings("unchecked")
	public <T extends GameService> T getService(Class<T> serviceType) {
		return (T) services.get(serviceType);
	}

	/**
	 * Adds a service to the ServiceProvider.<br>
	 * This method is NOT threadsafe.<br>
	 * If there exists already a service of the same type(=class), the old
	 * service will be removed and returned.<br>
	 * Note: You have to dispose the old service to avoid memory leaks!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param service
	 * @return the old service or null
	 */
	public GameService putService(GameService service) {
		GameService old = services.put(service.getClass(), service);
		if (service instanceof Initializable
				&& !((Initializable) service).isInitialized()) {
			if (isInitialized()) {
				((Initializable) service).init();
			} else { // initialize later, wait for the game-globals to be
				// initialized
				initializables.add((Initializable) service);
			}
		}
		if (service instanceof Drawable) {
			if (old != null)
				drawables.remove(old);
			drawables.add((Drawable) service);
		}
		if (service instanceof Computable) {
			if (old != null)
				computables.remove(old);
			computables.add((Computable) service);
		}
		if (service instanceof InputProcessor) {
			if (old != null)
				inputMultiplexer.removeProcessor((InputProcessor) old);
			inputMultiplexer.addProcessor((InputProcessor) service);
		}
		return old;
	}

	/**
	 * This method is NOT threadsafe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param service
	 * @return the old service or null
	 */
	public GameService removeService(GameService service) {
		GameService old = services.remove(service.getClass());
		if (old instanceof InputProcessor)
			inputMultiplexer.removeProcessor((InputProcessor) old);
		if (old instanceof Computable)
			computables.remove(old);
		if (old instanceof Drawable)
			drawables.remove(old);
		return old;
	}

	/**
	 * A service is able to request the users attention for itself. All other
	 * {@link InputProcessor} are dead until the service releases its attention.<br>
	 * This method IS threadsafe.<br>
	 * If {@link #freezeTheWorld} is true and {@link #clearTheScreen} is false,
	 * all other GameServices will draw their frozen pictures - in a lot of
	 * cases this is probably what you want.<br>
	 * <br>
	 * It's also possible to request attention for a foreign GameService. There
	 * is no need to put a GameService to the GameServiceProvider only to get
	 * the attention and remove it immediately after releasing the attention.
	 * 
	 * @param service
	 *            The service which wants the users attention
	 * @param freezeTheWorld
	 *            All computations will be stopped except the own one.
	 * @param clearTheScreen
	 *            All drawings will be stopped except the own one. The screen is
	 *            automatically cleared at every iteration.<br>
	 * @return true on succeed, otherwise false
	 */
	public boolean requestAttention(GameService service,
			boolean freezeTheWorld, boolean clearTheScreen) {
		boolean succeed = hasAttention.compareAndSet(null, service);
		if (succeed) {
			if (service instanceof Initializable
					&& !((Initializable) service).isInitialized()) {
				((Initializable) service).init();
			}
			this.freezeTheWorld = freezeTheWorld;
			this.clearTheScreen = clearTheScreen;
			if (service instanceof InputProcessor) {
				Gdx.input.setInputProcessor((InputProcessor) service);
			} else {
				Gdx.input.setInputProcessor(nullInputAdapter);
			}
		}
		return succeed;
	}

	/**
	 * Release the attention. Checks if the service has attention<br>
	 * This method IS threadsafe.
	 * 
	 * @param service
	 * @return true on succeed, otherwise false
	 */
	public boolean releaseAttention(GameService service) {
		boolean succeed = hasAttention.compareAndSet(service, null);
		if (succeed) {
			freezeTheWorld = false;
			clearTheScreen = false;
			Gdx.input.setInputProcessor(inputMultiplexer);
		}
		return succeed;
	}

	public void dispose() {
		for (GameService service : services.values()) {
			service.dispose();
		}
		services.clear();
		inputMultiplexer.clear();
		computables.clear();
		drawables.clear();
		hasAttention = new AtomicReference<GameService>();
		freezeTheWorld = false;
		clearTheScreen = false;
	}

	void computeAll() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		// to avoid over-/underflows (e.g. when debugging) we simulate at least
		// 10 FPS
		if (deltaTime > .1f)
			deltaTime = .1f;
		boolean actionKeyPressed = GameBase.$().isActionKeyPressed();
		if (freezeTheWorld) {
			if (hasAttention.get() instanceof Computable)
				((Computable) hasAttention.get()).compute(deltaTime,
						actionKeyPressed);
		} else
			for (int i = 0; i < computables.size(); i++) {
				computables.get(i).compute(deltaTime, actionKeyPressed);
			}
	}

	void drawAll(boolean debug) {
		// clear the screen
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		float tintColorBits = GameBase.$().getGameColorBits();
		SpriteBatch spriteBatch = GameBase.$().getSpriteBatch();
		Camera camera = GameBase.$().getCamera();
		if (clearTheScreen) {
			if (hasAttention.get() instanceof Drawable) {
				Drawable draw = (Drawable) hasAttention.get();
				spriteBatch.setProjectionMatrix(draw.projectionMatrix(camera));
				spriteBatch.begin();
				spriteBatch.setColor(tintColorBits);
				draw.draw(spriteBatch, camera, debug);
				spriteBatch.end();
			}
		} else {
			Matrix4 old = null;
			for (int i = 0; i < drawables.size(); i++) {
				Drawable d = drawables.get(i);
				if (d.projectionMatrix(camera) != old) {
					old = d.projectionMatrix(camera);
					if (i != 0)
						spriteBatch.end();
					spriteBatch.setProjectionMatrix(old);
					spriteBatch.begin();
				}
				spriteBatch.setColor(tintColorBits);
				drawables.get(i).draw(spriteBatch, camera, debug);
			}
			if (drawables.size() > 0) {
				spriteBatch.end();
			}
		}
		if (debug) {
			spriteBatch.setProjectionMatrix(camera.projection);
			spriteBatch.begin();
			float x1 = Math.max(0f, camera.position.x);
			float y1 = Math.max(0f, camera.position.y);
			float x2 = x1
					+ Math.min(camera.viewportWidth, GameBase.$()
							.getPlaneWidth());
			float y2 = y1
					+ Math.min(camera.viewportHeight, GameBase.$()
							.getPlaneHeight());
			if (f == null)
				f = new BitmapFont();
			f.setColor(0f, 1f, 1f, 1f);
			String text = "( " + (int) x1 + " / " + (int) y1 + " )";
			f.draw(spriteBatch, text, x1, y1 + f.getLineHeight());
			text = "( " + (int) x2 + " / " + (int) y2 + " )";
			TextBounds b = f.getBounds(text);
			f.draw(spriteBatch, text, x2 - b.width, y2);
			spriteBatch.end();

			spriteBatch.setProjectionMatrix(camera.view);
			spriteBatch.begin();
			x1 = Gdx.input.getX();
			y1 = GameBase.$().getScreenHeight() - Gdx.input.getY();
			text = "( " + (int) x1 + " / " + (int) y1 + " ) Screen\n";
			x2 = camera.position.x + x1 * camera.viewportWidth
					/ GameBase.$().getScreenWidth();
			y2 = camera.position.y + y1 * camera.viewportHeight
					/ GameBase.$().getScreenHeight();
			text += "( " + (int) x2 + " / " + (int) y2 + " ) Camera";
			f.setColor(1f, 0f, 1f, 1f);
			b = f.getMultiLineBounds(text);
			f.drawMultiLine(spriteBatch, text, Math.max(Math.min(x1 + 10,
					GameBase.$().getScreenWidth() - b.width), 0f), Math.max(
					Math.min(y1, GameBase.$().getScreenHeight()), b.height));
			text = "Execution order of Computable services";
			for (Computable c : computables) {
				text += "\n        " + c.getClass().getName();
			}
			text += "\n\nExecution order of Drawable services";
			for (Drawable d : drawables) {
				text += "\n        " + d.getClass().getName();
			}
			if (hasAttention.get() != null) {
				text += "\n\n" + hasAttention.get().getClass().getName()
						+ " holds attention!";
			}
			f.setColor(1f, 1f, 0f, 1f);
			b = f.getMultiLineBounds(text);
			f
					.drawMultiLine(spriteBatch, text, (GameBase.$()
							.getScreenWidth() - b.width) * .5f, GameBase.$()
							.getScreenHeight()
							- (GameBase.$().getScreenHeight() - b.height) * .5f);
			spriteBatch.end();
		}
	}

	static BitmapFont f = null;

	public boolean isInitialized() {
		return initializables == null;
	}
}
