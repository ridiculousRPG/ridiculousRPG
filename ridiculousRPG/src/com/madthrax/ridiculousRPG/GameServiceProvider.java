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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
	private Map<Class<? extends GameService>, GameService> services = new HashMap<Class<? extends GameService>, GameService>();
	private AtomicReference<GameService> hasAttention = new AtomicReference<GameService>();
	private boolean freezeTheWorld = false;
	private boolean clearTheScreen = false;

	private List<Initializable> initializables = new ArrayList<Initializable>();
	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private InputMultiplexer attentionInputMultiplexer = new InputMultiplexer();
	private List<Computable> computables = new ArrayList<Computable>();
	private List<Drawable> drawables = new ArrayList<Drawable>();

	public void init() {
		List<Initializable> initializables = this.initializables;
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
			attentionInputMultiplexer.clear();
			for (InputProcessor in : inputMultiplexer.getProcessors()) {
				if (in == service) {
					attentionInputMultiplexer.addProcessor(in);
					service = null;
				} else if (((GameService)in).essential()) {
					attentionInputMultiplexer.addProcessor(in);
				}
			}
			if (service instanceof InputProcessor) {
				attentionInputMultiplexer.addProcessor((InputProcessor) service);
			}
			Gdx.input.setInputProcessor(attentionInputMultiplexer);
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

	/**
	 * If a {@link GameService} holds the attention, this one is returned.
	 * Otherwise null will be returned.
	 * 
	 * @return the service which holds the attention or null if no
	 *         {@link GameService} holds the attention
	 */
	public GameService queryAttention() {
		return hasAttention.get();
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
		boolean actionKeyPressed = GameBase.$().isActionKeyDown();
		GameService holdsAttention = hasAttention.get();
		if (freezeTheWorld) {
			for (int i = 0, len = computables.size(); i < len; i++) {
				Computable c = computables.get(i);
				if (c == holdsAttention) {
					holdsAttention = null;
					c.compute(deltaTime, actionKeyPressed);
				} else if (((GameService) c).essential()) {
					c.compute(deltaTime, actionKeyPressed);
				}
			}
		} else {
			for (int i = 0, len = computables.size(); i < len; i++) {
				Computable c = computables.get(i);
				if (c == holdsAttention) {
					holdsAttention = null;
				}
				c.compute(deltaTime, actionKeyPressed);
			}
		}
		if (holdsAttention instanceof Computable) {
			((Computable) holdsAttention).compute(deltaTime, actionKeyPressed);
		}
	}

	void drawAll(boolean debug) {
		// clear the screen
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		float tintColorBits = GameBase.$().getGameColorBits();
		SpriteBatch spriteBatch = GameBase.$().getSpriteBatch();
		Camera camera = GameBase.$().getCamera();
		GameService holdsAttention = hasAttention.get();
		Matrix4 proj = null;
		if (clearTheScreen) {
			for (int i = 0, len = drawables.size(); i < len; i++) {
				Drawable d = drawables.get(i);
				if (d == holdsAttention) {
					holdsAttention = null;
					proj = drawWithMatrix(debug, spriteBatch, camera, proj, d,
							tintColorBits);
				} else if (((GameService) d).essential()) {
					proj = drawWithMatrix(debug, spriteBatch, camera, proj, d,
							tintColorBits);
				}
			}
		} else {
			for (int i = 0, len = drawables.size(); i < len; i++) {
				Drawable d = drawables.get(i);
				if (d == holdsAttention) {
					holdsAttention = null;
				}
				proj = drawWithMatrix(debug, spriteBatch, camera, proj, d,
						tintColorBits);
			}
		}
		if (holdsAttention instanceof Drawable) {
			Drawable d = (Drawable) holdsAttention;
			proj = drawWithMatrix(debug, spriteBatch, camera, proj, d,
					tintColorBits);
		}
		if (proj != null) {
			spriteBatch.end();
		}
		if (debug) {
			DebugHelper.drawViewportCorners(spriteBatch, camera);
			DebugHelper.drawMousePosition(spriteBatch, camera);
			DebugHelper.drawServiceExecutionOrder(spriteBatch, camera,
					computables, drawables, hasAttention.get());
		}
	}

	private Matrix4 drawWithMatrix(boolean debug, SpriteBatch spriteBatch,
			Camera camera, Matrix4 old, Drawable d, float tintColorBits) {
		if (d.projectionMatrix(camera) != old) {
			if (old != null) {
				spriteBatch.end();
			}
			old = d.projectionMatrix(camera);
			spriteBatch.setProjectionMatrix(old);
			spriteBatch.begin();
		}
		spriteBatch.setColor(tintColorBits);
		d.draw(spriteBatch, camera, debug);
		return old;
	}

	public boolean isInitialized() {
		return initializables == null;
	}
}
