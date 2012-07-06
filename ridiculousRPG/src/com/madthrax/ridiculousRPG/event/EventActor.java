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

package com.madthrax.ridiculousRPG.event;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.madthrax.ridiculousRPG.event.handler.EventHandler;

/**
 * Wrapper class to wrap an {@link EventObject} into an {@link Actor}.
 * 
 * @see Actor
 * @see EventObject
 * @author Alexander Baumgartner
 */
// TODO: Maybe: Movable extends com.badlogic.gdx.scenes.scene2d.Actor
// and drop this wrapper
public class EventActor extends Actor {
	private EventObject event = new EventObject();
	private boolean initialized;

	public EventActor(float x, float y, String[] props) {
		this(x, y, 20, 20, toMap(props));
	}

	private static Map<String, String> toMap(String[] props) {
		HashMap<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i < props.length - 1; i += 2) {
			ret.put(props[i], props[i + 1]);
		}
		return ret;
	}

	public EventActor(float x, float y, float width, float height,
			Map<String, String> properties) {
		setVisible(event.visible);
		setTouchable(event.touchable);
		addListener(new ClickListener() {
			@Override
			public void clicked(ActorEvent event, float x, float y) {
				EventObject ev = EventActor.this.event;
				EventHandler handler = ev.getEventHandler();
				if (handler != null)
					handler.onPush(ev, ev);
			}

			@Override
			public void enter(ActorEvent event, float x, float y, int pointer,
					Actor fromActor) {
				EventObject ev = EventActor.this.event;
				EventHandler handler = ev.getEventHandler();
				if (handler != null)
					handler.onTouch(ev, ev);
			}

		});
		setBounds(x, y, width, height);
		EventFactory.parseProperties(event, properties);
		// refresh all values for the Actor class
		super.setBounds(event.getX(), event.getY(), event.getWidth(), event
				.getHeight());
		super.setScale(event.scaleX, event.scaleY);
		super.setColor(event.getColor());
		super.setRotation(event.rotation);
		super.setTouchable(event.touchable);
		super.setVisible(event.visible);
		super.setZIndex((int) event.z);
	}

	@Override
	public void act(float delta) {
		if (!initialized) {
			initialized = true;
			event.init();
		}
		event.computeParticleEffect(delta);
		event.getMoveHandler().tryMove(event, delta);
		event.commitMove();
		if (event.getEventHandler() != null)
			event.getEventHandler().onTimer(event, delta);
		super.act(delta);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		event.draw(batch);
	}

	@Override
	protected void setStage(Stage stage) {
		super.setStage(stage);
		// Actor has been removed from the stage
		if (stage == null) {
			event.dispose();
			event = null;
		}
	}

	@Override
	public void setColor(Color color) {
		super.setColor(color);
		event.setColor(color);
	}

	@Override
	public void setColor(float r, float g, float b, float a) {
		super.setColor(r, g, b, a);
		event.getColor().set(r, g, b, a);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);
		event.getTouchBound().height = height;
		event.drawBound.height = height;
	}

	@Override
	public void setPosition(float x, float y) {
		setX(x);
		setY(y);
	}

	@Override
	public void setSize(float width, float height) {
		setHeight(height);
		setWidth(width);
	}

	@Override
	public void setTouchable(boolean touchable) {
		super.setTouchable(touchable);
		event.touchable = touchable;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		event.visible = visible;
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width);
		event.getTouchBound().width = width;
		event.drawBound.width = width;
	}

	@Override
	public void setX(float x) {
		super.setX(x);
		event.setX(x);
		event.drawBound.x = x;
	}

	@Override
	public void setY(float y) {
		super.setY(y);
		event.setY(y);
		event.drawBound.y = y;
	}

	@Override
	public void setZIndex(int index) {
		super.setZIndex(index);
		event.z = index;
	}

	@Override
	public void rotate(float amount) {
		event.rotation += amount;
		super.rotate(amount);
	}

	@Override
	public void scale(float scaleX, float scaleY) {
		event.scaleX += scaleX;
		event.scaleY += scaleY;
		super.scale(scaleX, scaleY);
	}

	@Override
	public void scale(float scale) {
		scale(scale, scale);
	}

	@Override
	public void setBounds(float x, float y, float width, float height) {
		setPosition(x, y);
		setSize(width, height);
	}

	@Override
	public void setRotation(float rotation) {
		event.rotation = rotation;
		super.setRotation(rotation);
	}

	@Override
	public void setScale(float scale) {
		event.scaleX = scale;
		event.scaleY = scale;
		super.setScale(scale);
	}

	@Override
	public void setScaleX(float scaleX) {
		event.scaleX = scaleX;
		super.setScaleX(scaleX);
	}

	@Override
	public void setScaleY(float scaleY) {
		event.scaleY = scaleY;
		super.setScaleY(scaleY);
	}

	@Override
	public void size(float width, float height) {
		setWidth(width + getWidth());
		setHeight(height + getHeight());
	}

	@Override
	public void size(float size) {
		size(size, size);
	}

	@Override
	public void translate(float x, float y) {
		super.translate(x, y);
		event.translate(x, y);
	}

}
