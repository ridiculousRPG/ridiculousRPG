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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * This class handles the GameServices. The services are executed in the same
 * order they are added. In the most cases you probably want to draw a map
 * before drawing something else.<br>
 * Every service of the same type(=class) can only be provided once!
 * 
 * @author Alexander Baumgartner
 */
public class GameServiceProvider {
	private Map<String, GameService> services = new HashMap<String, GameService>();
	private AtomicReference<GameService> hasAttention = new AtomicReference<GameService>();
	private int attentionCount = 0;
	private boolean freezeTheWorld = false;
	private boolean clearTheScreen = false;

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	private InputMultiplexer attentionInputMultiplexer = new InputMultiplexer();
	private Array<Computable> computables = new Array<Computable>();
	private Array<Drawable> drawables = new Array<Drawable>();
	private Array<ResizeListener> resizeListener = new Array<ResizeListener>();

	public GameServiceProvider() {
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	/**
	 * Returns a {@link GameService} by its name.
	 * 
	 * @param name
	 *            The name of services to return.
	 * @return The {@link GameService} or null if no service with the specified
	 *         name exists
	 * @see #putService(String, GameService)
	 */
	public GameService getService(String name) {
		return services.get(name);
	}

	/**
	 * A convenience method which checks if the {@link GameService} is from the
	 * specified type. If not it will return null.
	 * 
	 * @param clazz
	 *            The type of services to return.
	 * @param name
	 *            The name of services to return.
	 * @return The service from type clazz with the specified name or null if
	 *         there is no service with the specified name or the service is not
	 *         from the type
	 */
	@SuppressWarnings("unchecked")
	public <T extends GameService> T getService(Class<T> clazz, String name) {
		GameService s = services.get(name);
		return (T) (clazz.isInstance(s) ? s : null);
	}

	/**
	 * Returns all services which are from the specified type or extend from the
	 * specified type.
	 * 
	 * @param clazz
	 *            The type of services to return.
	 * @return An {@link Array} with all services from type clazz.
	 */
	@SuppressWarnings("unchecked")
	public <T extends GameService> Array<T> getServices(Class<T> clazz) {
		Array<T> ret = new Array<T>();
		for (GameService s : services.values()) {
			if (clazz.isInstance(s))
				ret.add((T) s);
		}
		return ret;
	}

	/**
	 * Adds or replaces a service. The service will be inserted before the
	 * referenceService.<br>
	 * If there exists already a service with the specified name, the old
	 * service will be removed and returned.<br>
	 * <br>
	 * If referenceService doesn't exist, the behavior will be exactly the same
	 * as {@link #putService(String, GameService)}. <br>
	 * <br>
	 * This method is NOT thread safe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks (if you
	 * don't need it anymore)!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param name
	 *            The name under which this service will be provided.
	 * @param service
	 *            The service to append to the {@link GameServiceProvider}.
	 * @return the old {@link GameService} which was stored under this name or
	 *         null if no service with this name existed.
	 * @see #putService(String, GameService)
	 * @see #putServiceAfter(String, GameService, String)
	 */
	public GameService putServiceBefore(String name, GameService service,
			String referenceService) {
		return putServiceRelativeTo(name, service, referenceService, 0);
	}

	/**
	 * Adds or replaces a service. The service will be inserted after the
	 * referenceService.<br>
	 * If there exists already a service with the specified name, the old
	 * service will be removed and returned.<br>
	 * <br>
	 * If referenceService doesn't exist, the behavior will be exactly the same
	 * as {@link #putService(String, GameService)}. <br>
	 * <br>
	 * This method is NOT thread safe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks (if you
	 * don't need it anymore)!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param name
	 *            The name under which this service will be provided.
	 * @param service
	 *            The service to append to the {@link GameServiceProvider}.
	 * @return the old {@link GameService} which was stored under this name or
	 *         null if no service with this name existed.
	 * @see #putService(String, GameService)
	 * @see #putServiceBefore(String, GameService, String)
	 */
	public GameService putServiceAfter(String name, GameService service,
			String referenceService) {
		return putServiceRelativeTo(name, service, referenceService, 1);
	}

	private GameService putServiceRelativeTo(String name, GameService service,
			String referenceService, int offset) {
		GameService old = putService(name, service);
		GameService ref = getService(referenceService);
		// change position
		if (ref != null && ref != service) {
			if (ref instanceof Drawable && service instanceof Drawable) {
				shiftPos(drawables, (Drawable) service, (Drawable) ref, offset);
			}
			if (ref instanceof Computable && service instanceof Computable) {
				shiftPos(computables, (Computable) service, (Computable) ref,
						offset);
			}
			if (ref instanceof ResizeListener
					&& service instanceof ResizeListener) {
				shiftPos(resizeListener, (ResizeListener) service,
						(ResizeListener) ref, offset);
			}
			if (ref instanceof InputProcessor
					&& service instanceof InputProcessor) {
				shiftPos(inputMultiplexer.getProcessors(),
						(InputProcessor) service, (InputProcessor) ref, offset);
			}
		}
		return old;
	}

	private <T> void shiftPos(Array<T> array, T service, T ref, int offset) {
		array.removeValue(service, true);
		array.insert(array.indexOf(ref, true) + offset, service);
	}

	/**
	 * Adds or replaces a service. The service will be added at the end if it
	 * doesn't exist already. This means that the service will be computed last
	 * and drawn first (All other services will overdraw this service).<br>
	 * If there exists already a service with the specified name, it will be
	 * replaced (the position will be the one of the old service). The old
	 * service will be removed and returned.<br>
	 * <br>
	 * This method is NOT thread safe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks (if you
	 * don't need it anymore)!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param name
	 *            The name under which this service will be provided.
	 * @param service
	 *            The service to append to the {@link GameServiceProvider}.
	 * @return the old {@link GameService} which was stored under this name or
	 *         null if no service with this name existed.
	 * @see #putServiceAfter(String, GameService, String)
	 * @see #putServiceBefore(String, GameService, String)
	 */
	public GameService putService(String name, GameService service) {
		GameService old = services.put(name, service);
		if (old instanceof Drawable) {
			int index = drawables.indexOf((Drawable) old, true);
			if (service instanceof Drawable)
				drawables.set(index, (Drawable) service);
			else
				drawables.removeIndex(index);
		} else if (service instanceof Drawable) {
			drawables.add((Drawable) service);
		}
		if (old instanceof Computable) {
			int index = computables.indexOf((Computable) old, true);
			if (service instanceof Computable)
				computables.set(index, (Computable) service);
			else
				computables.removeIndex(index);
		} else if (service instanceof Computable) {
			computables.add((Computable) service);
		}
		if (old instanceof ResizeListener) {
			int index = resizeListener.indexOf((ResizeListener) old, true);
			if (service instanceof ResizeListener)
				resizeListener.set(index, (ResizeListener) service);
			else
				resizeListener.removeIndex(index);
		} else if (service instanceof ResizeListener) {
			resizeListener.add((ResizeListener) service);
		}
		if (old instanceof InputProcessor) {
			int index = inputMultiplexer.getProcessors().indexOf(
					(InputProcessor) old, true);
			if (service instanceof InputProcessor)
				inputMultiplexer.getProcessors().set(index,
						(InputProcessor) service);
			else
				inputMultiplexer.getProcessors().removeIndex(index);
		} else if (service instanceof InputProcessor) {
			inputMultiplexer.getProcessors().add((InputProcessor) service);
		}
		return old;
	}

	/**
	 * Adds or replaces a service. The service will be added in front of all
	 * other services (at the head) if it doesn't exist already. This means that
	 * the service will be computed first and drawn last (This service will
	 * overdraw all the other services).<br>
	 * If there exists already a service with the specified name, it will be
	 * replaced (the position will be the one of the old service). The old
	 * service will be removed and returned.<br>
	 * <br>
	 * This method is NOT thread safe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks (if you
	 * don't need it anymore)!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param name
	 *            The name under which this service will be provided.
	 * @param service
	 *            The service to append to the {@link GameServiceProvider}.
	 * @return the old {@link GameService} which was stored under this name or
	 *         null if no service with this name existed.
	 * @see #putServiceAfter(String, GameService, String)
	 * @see #putServiceBefore(String, GameService, String)
	 */
	public GameService putServiceHead(String name, GameService service) {
		GameService old = services.put(name, service);
		if (old instanceof Drawable) {
			int index = drawables.indexOf((Drawable) old, true);
			if (service instanceof Drawable)
				drawables.set(index, (Drawable) service);
			else
				drawables.removeIndex(index);
		} else if (service instanceof Drawable) {
			drawables.insert(0, (Drawable) service);
		}
		if (old instanceof Computable) {
			int index = computables.indexOf((Computable) old, true);
			if (service instanceof Computable)
				computables.set(index, (Computable) service);
			else
				computables.removeIndex(index);
		} else if (service instanceof Computable) {
			computables.insert(0, (Computable) service);
		}
		if (old instanceof ResizeListener) {
			int index = resizeListener.indexOf((ResizeListener) old, true);
			if (service instanceof ResizeListener)
				resizeListener.set(index, (ResizeListener) service);
			else
				resizeListener.removeIndex(index);
		} else if (service instanceof ResizeListener) {
			resizeListener.insert(0, (ResizeListener) service);
		}
		if (old instanceof InputProcessor) {
			int index = inputMultiplexer.getProcessors().indexOf(
					(InputProcessor) old, true);
			if (service instanceof InputProcessor)
				inputMultiplexer.getProcessors().set(index,
						(InputProcessor) service);
			else
				inputMultiplexer.getProcessors().removeIndex(index);
		} else if (service instanceof InputProcessor) {
			inputMultiplexer.getProcessors()
					.insert(0, (InputProcessor) service);
		}
		return old;
	}

	/**
	 * This method is NOT thread safe.<br>
	 * Note: You have to dispose the old service to avoid memory leaks!<br>
	 * If you need to put services from different threads concurrently, you have
	 * to synchronize your code!
	 * 
	 * @param name
	 *            The name under which the service is provided.
	 * @return The removed service or null if no service with the specified name
	 *         exists.
	 */
	public GameService removeService(String name) {
		GameService old = services.remove(name);
		if (old == null)
			return null;
		if (old instanceof InputProcessor)
			inputMultiplexer.removeProcessor((InputProcessor) old);
		if (old instanceof Computable)
			computables.removeValue((Computable) old, true);
		if (old instanceof Drawable)
			drawables.removeValue((Drawable) old, true);
		if (old instanceof ResizeListener)
			resizeListener.removeValue((ResizeListener) old, true);
		return old;
	}

	/**
	 * A service is able to request the users attention for itself. All other
	 * {@link InputProcessor} are dead (except the essential ones) until the
	 * service releases its attention.<br>
	 * <br>
	 * This method is thread safe if it's used carefully. Don't request
	 * attention for the same {@link GameService} within different threads
	 * without querying the attention. (The attentionCount is not thread safe
	 * but the simple request and release operations are.)<br>
	 * <br>
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
		// we have already attention
		if (hasAttention.get() == service) {
			attentionCount++;
			checkFreeze(service, freezeTheWorld, clearTheScreen);
			return true;
		}
		// try to grab the attention
		boolean succeed = hasAttention.compareAndSet(null, service);
		if (succeed) {
			attentionCount++;
			attentionInputMultiplexer.clear();
			boolean found = false;
			for (InputProcessor in : inputMultiplexer.getProcessors()) {
				if (in == service) {
					attentionInputMultiplexer.addProcessor(in);
					found = true;
				} else if (((GameService) in).essential()) {
					attentionInputMultiplexer.addProcessor(in);
				}
			}
			if (!found && service instanceof InputProcessor) {
				attentionInputMultiplexer
						.addProcessor((InputProcessor) service);
			}
			checkFreeze(service, freezeTheWorld, clearTheScreen);
			Gdx.input.setInputProcessor(attentionInputMultiplexer);
		}
		return succeed;
	}

	private void checkFreeze(GameService service, boolean freezeTheWorld,
			boolean clearTheScreen) {
		if (freezeTheWorld) {
			if (!this.freezeTheWorld) {
				for (GameService s : services.values()) {
					if (s != service && !s.essential())
						s.freeze();
				}
				this.freezeTheWorld = freezeTheWorld;
			}
		} else if (this.freezeTheWorld) {
			this.freezeTheWorld = false;
			for (GameService s : services.values()) {
				if (s != service && !s.essential())
					s.unfreeze();
			}
		}
		this.clearTheScreen = clearTheScreen;
	}

	/**
	 * Release the attention. Checks if the service has attention<br>
	 * This method is thread safe if it's used carefully. Don't request
	 * attention for the same {@link GameService} within different threads
	 * without querying the attention. (The attentionCount is not thread safe
	 * but the simple request and release operations are.)
	 * 
	 * @param service
	 * @return true on succeed, otherwise false
	 */
	public boolean releaseAttention(GameService service) {
		if (attentionCount > 1 && service == hasAttention.get()) {
			attentionCount--;
			return true;
		}
		boolean succeed = hasAttention.compareAndSet(service, null);
		if (succeed) {
			attentionCount--;
			checkFreeze(service, false, false);
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
			for (int i = 0, len = computables.size; i < len; i++) {
				Computable c = computables.get(i);
				if (c == holdsAttention) {
					holdsAttention = null;
					c.compute(deltaTime, actionKeyPressed);
				} else if (((GameService) c).essential()) {
					c.compute(deltaTime, actionKeyPressed);
				}
			}
		} else {
			for (int i = 0, len = computables.size; i < len; i++) {
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
		try {
			drawAllInternal(debug);
		} catch (Exception e) {
			// after an exception while drawing, spriteBatch has an undefined
			// state,
			// that's why we throw it away and create a new one.
			e.printStackTrace();
			GameBase.$().rebuildSpriteBatch();
		}
	}

	void drawAllInternal(boolean debug) {
		// clear the screen
		Gdx.gl.glClearColor(GameBase.$().getBackgroundColor().r, GameBase.$()
				.getBackgroundColor().g, GameBase.$().getBackgroundColor().b,
				GameBase.$().getBackgroundColor().a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		float tintColorBits = GameBase.$().getGameColorBits();
		SpriteBatch spriteBatch = GameBase.$().getSpriteBatch();
		Camera camera = GameBase.$().getCamera();
		GameService holdsAttention = hasAttention.get();
		Matrix4 proj = null;
		if (clearTheScreen) {
			for (int i = drawables.size - 1; i > -1; i--) {
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
			for (int i = drawables.size - 1; i > -1; i--) {
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

	void resize(int width, int height) {
		for (int i = resizeListener.size - 1; i > -1; i--) {
			resizeListener.get(i).resize(width, height);
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

	protected void saveSerializableServices(ObjectOutputStream oOut) throws IOException {
		Map<String, GameService> serializeIt = new HashMap<String, GameService>();
		for (Map.Entry<String, GameService> es : services.entrySet()) {
			if (es.getValue() instanceof Serializable) {
				serializeIt.put(es.getKey(), es.getValue());
			}
		}
		oOut.writeObject(serializeIt);
	}
	protected void loadSerializableServices(ObjectInputStream oIn) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		Map<String, GameService> unserializeIt = (Map<String, GameService>) oIn.readObject();
		for (Map.Entry<String, GameService> es : unserializeIt.entrySet()) {
			putService(es.getKey(), es.getValue());
		}
	}
}
