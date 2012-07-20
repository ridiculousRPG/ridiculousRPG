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

package com.ridiculousRPG.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Alexander Baumgartner
 */
public class ColorSerializable extends Color implements Serializable {
	private static final long serialVersionUID = 1L;

	public ColorSerializable() {
		super();
	}

	public ColorSerializable(Color color) {
		super(color);
	}

	public ColorSerializable(float r, float g, float b, float a) {
		super(r, g, b, a);
	}

	public static final ColorSerializable wrap(Color c) {
		return c instanceof ColorSerializable ? (ColorSerializable) c
				: new ColorSerializable(c);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeFloat(a);
		out.writeFloat(b);
		out.writeFloat(g);
		out.writeFloat(r);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		a = in.readFloat();
		b = in.readFloat();
		g = in.readFloat();
		r = in.readFloat();
	}
}