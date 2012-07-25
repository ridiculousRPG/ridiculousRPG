package com.ridiculousRPG.event;

import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.map.tiled.TiledMapWithEvents;
import com.ridiculousRPG.util.BlockingBehavior;

public class EllipseObject extends Ellipse2D.Float {
	private static final long serialVersionUID = 1L;

	private String name;
	public BlockingBehavior blockingBehavior = BlockingBehavior.BUILDING_LOW;
	/**
	 * This map holds the local event properties.<br>
	 * If you use a {@link TiledMapWithEvents}, all object-properties starting
	 * with the $ sign are considered local event properties.
	 */
	public Map<String, String> properties = new HashMap<String, String>();

	public EventHandler eventHandler;
	public boolean touchable;
	private Color color;
	public boolean visible;

	private static transient ShapeRenderer renderer;

	public EllipseObject(String name, float x, float y, float width,
			float height) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void init() {
		if (eventHandler != null) {
			eventHandler.init();
			eventHandler.onLoad();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	public void draw(boolean debug) {
		if (visible && color != null) {
			float x = this.x + width * .5f;
			float y = this.y + height * .5f;
			ShapeRenderer r = getRenderer();
			r.setColor(color);
			r.scale(width * .01f, height * .01f, 1);
			r.circle(x, y, 50);
		}
		if (debug) {
			float x = this.x + width * .5f;
			float y = this.y + height * .5f;
			ShapeRenderer r = getRenderer();
			r.setColor(blockingBehavior.color);
			r.scale(width * .01f, height * .01f, 1);
			if (visible && color != null) {
				r.circle(x + 1, y + 1, 50);
			} else {
				r.circle(x, y, 50);
			}
		}
	}

	public static void startPolygonBatch(Matrix4 projection) {
		getRenderer().setProjectionMatrix(projection);
		getRenderer().begin(ShapeType.Circle);
	}

	public static void endPolygonBatch() {
		getRenderer().end();
	}

	private static ShapeRenderer getRenderer() {
		if (renderer == null) {
			synchronized (EllipseObject.class) {
				if (renderer != null)
					return renderer;
				renderer = new ShapeRenderer();
			}
		}
		return renderer;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		if (color != null)
			visible = true;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		init();
	}

	@Override
	public String toString() {
		return "ellipse '" + name + " (center:" + x + ',' + y + " size:"
				+ width + ',' + height + ")'";
	}
}
