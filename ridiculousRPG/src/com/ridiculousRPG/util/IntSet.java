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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;

/**
 * @author Alexander Baumgartner
 */
//TODO: DROP THIS JUNK
@Deprecated
public class IntSet implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Object DUMMY = new Object();
	private transient IntMap<Object> map = new IntMap<Object>();

	private void writeObject(ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();

		// Write out size
		s.writeInt(map.size);

		// Write out all elements in the proper order.
		for (Keys i = map.keys(); i.hasNext;)
			s.writeInt(i.next());
	}

	private void readObject(ObjectInputStream s) throws java.io.IOException,
			ClassNotFoundException {
		s.defaultReadObject();

		// Read in size
		int size = s.readInt();
		map = new IntMap<Object>((size * 4) / 3);

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			map.put(s.readInt(), DUMMY);
	}

	public void put(int id) {
		map.put(id, DUMMY);
	}

	public boolean containsKey(int id) {
		return map.containsKey(id);
	}
}
