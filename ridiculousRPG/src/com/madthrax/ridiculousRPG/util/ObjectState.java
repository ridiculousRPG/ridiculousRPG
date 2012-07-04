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

package com.madthrax.ridiculousRPG.util;

import java.io.Serializable;

/**
 * This class represents the state of one section of the entire game.<br>
 * E.g. one section could be one map.<br>
 * This implementation is optimized for serialization-size and not for
 * performance. All methods are synchronized. The performance is not that
 * important in this class, because state transitions are really rare compared
 * with the frame rendering.<br>
 * ATTENTION: Don't waste space by using high index values.<br>
 * The sizes of the internally used arrays are directly connected to the highest
 * index value as follows:<br> {@code len = ((index+8) >> 3) << 3}
 * 
 * @author Alexander Baumgartner
 */
public class ObjectState implements Serializable {
	private static final long serialVersionUID = 1L;

	private int[] intVar;
	private boolean[] boolVar;
	private float[] floatVar;
	private String[] stringVar;
	private byte[][] rawBytesVar;
	private ObjectState[] childFragment;

	// Count to determine if the state has changed
	private transient int changeCount;

	// We don't want to copy the array for every new element
	// actually increment by 1<<3 = 8
	private static final int INC_SHIFT = 3; // 1<<1 = 2 1<<2 = 4 1<<3 = 8
	private static final int INC_BY = 1 << INC_SHIFT;

	/**
	 * Reads an integer variable
	 * 
	 * @param index
	 * @return
	 */
	public synchronized int getInt(int index) {
		return intVar != null && intVar.length > index ? intVar[index] : 0;
	}

	/**
	 * Stores an integer variable
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setInt(int index, int value) {
		int[] intVar = this.intVar;
		if (intVar == null) {
			if (value == 0)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			intVar = new int[newLen];
			this.intVar = intVar;
		}
		int len = intVar.length;
		if (len > index) {
			intVar[index] = value;
			// shrink
			if (value == 0 && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && intVar[newLen - 1] == 0)
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen < len) {
					if (newLen == 0) {
						this.intVar = null;
					} else {
						this.intVar = new int[newLen];
						System.arraycopy(intVar, 0, this.intVar, 0, newLen);
					}
				}
			}
		} else if (value != 0) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			intVar = new int[newLen];
			System.arraycopy(this.intVar, 0, intVar, 0, len);
			intVar[index] = value;
			this.intVar = intVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized int casInt(int index, int oldVal, int newVal) {
		int actVal = getInt(index);
		if (oldVal == actVal) {
			setInt(index, newVal);
		}
		return actVal;
	}

	/**
	 * Reads a boolean variable
	 * 
	 * @param index
	 * @return
	 */
	public synchronized boolean getBool(int index) {
		return boolVar != null && boolVar.length > index && boolVar[index];
	}

	/**
	 * Stores a boolean variable
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setBool(int index, boolean value) {
		boolean[] boolVar = this.boolVar;
		if (boolVar == null) {
			if (!value)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			boolVar = new boolean[newLen];
			this.boolVar = boolVar;
		}
		int len = boolVar.length;
		if (len > index) {
			boolVar[index] = value;
			// shrink
			if (!value && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && !boolVar[newLen - 1])
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen == 0) {
					this.boolVar = null;
				} else {
					this.boolVar = new boolean[newLen];
					System.arraycopy(boolVar, 0, this.boolVar, 0, newLen);
				}
			}
		} else if (value) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			boolVar = new boolean[newLen];
			System.arraycopy(this.boolVar, 0, boolVar, 0, len);
			boolVar[index] = value;
			this.boolVar = boolVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized boolean casBool(int index, boolean oldVal,
			boolean newVal) {
		boolean actVal = getBool(index);
		if (oldVal == actVal) {
			setBool(index, newVal);
		}
		return actVal;
	}

	/**
	 * Reads a float variable
	 * 
	 * @param index
	 * @return
	 */
	public synchronized float getFloat(int index) {
		return floatVar != null && floatVar.length > index ? floatVar[index]
				: 0f;
	}

	/**
	 * Stores a float variable
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setFloat(int index, float value) {
		float[] floatVar = this.floatVar;
		if (floatVar == null) {
			if (value == 0f)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			floatVar = new float[newLen];
			this.floatVar = floatVar;
		}
		int len = floatVar.length;
		if (len > index) {
			floatVar[index] = value;
			// shrink
			if (value == 0f && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && floatVar[newLen - 1] == 0f)
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen == 0) {
					this.floatVar = null;
				} else {
					this.floatVar = new float[newLen];
					System.arraycopy(floatVar, 0, this.floatVar, 0, newLen);
				}
			}
		} else if (value != 0f) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			floatVar = new float[newLen];
			System.arraycopy(this.floatVar, 0, floatVar, 0, len);
			floatVar[index] = value;
			this.floatVar = floatVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized float casFloat(int index, float oldVal, float newVal) {
		float actVal = getFloat(index);
		if (oldVal == actVal) {
			setFloat(index, newVal);
		}
		return actVal;
	}

	/**
	 * Reads a {@link String} variable
	 * 
	 * @param index
	 * @return
	 */
	public synchronized String getString(int index) {
		return stringVar != null && stringVar.length > index ? stringVar[index]
				: null;
	}

	/**
	 * Stores a {@link String} variable
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setString(int index, String value) {
		String[] stringVar = this.stringVar;
		if (stringVar == null) {
			if (value == null)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			stringVar = new String[newLen];
			this.stringVar = stringVar;
		}
		int len = stringVar.length;
		if (len > index) {
			stringVar[index] = value;
			// shrink
			if (value == null && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && stringVar[newLen - 1] == null)
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen == 0) {
					this.stringVar = null;
				} else {
					this.stringVar = new String[newLen];
					System.arraycopy(stringVar, 0, this.stringVar, 0, newLen);
				}
			}
		} else if (value != null) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			stringVar = new String[newLen];
			System.arraycopy(this.stringVar, 0, stringVar, 0, len);
			stringVar[index] = value;
			this.stringVar = stringVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized String casString(int index, String oldVal, String newVal) {
		String actVal = getString(index);
		if (oldVal == actVal) {
			setString(index, newVal);
		}
		return actVal;
	}

	/**
	 * Reads a {@link byte} array variable
	 * 
	 * @param index
	 * @return
	 */
	public synchronized byte[] getRawBytes(int index) {
		return rawBytesVar != null && rawBytesVar.length > index ? rawBytesVar[index]
				: null;
	}

	/**
	 * Stores a {@link byte} array variable
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setRawBytes(int index, byte[] value) {
		byte[][] bytesVar = this.rawBytesVar;
		if (bytesVar == null) {
			if (value == null)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			bytesVar = new byte[newLen][];
			this.rawBytesVar = bytesVar;
		}
		int len = bytesVar.length;
		if (len > index) {
			rawBytesVar[index] = value;
			// shrink
			if (value == null && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && bytesVar[newLen - 1] == null)
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen == 0) {
					this.rawBytesVar = null;
				} else {
					this.rawBytesVar = new byte[newLen][];
					System.arraycopy(bytesVar, 0, this.rawBytesVar, 0, newLen);
				}
			}
		} else if (value != null) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			bytesVar = new byte[newLen][];
			System.arraycopy(this.rawBytesVar, 0, bytesVar, 0, len);
			bytesVar[index] = value;
			this.rawBytesVar = bytesVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized byte[] casRawBytes(int index, byte[] oldVal,
			byte[] newVal) {
		byte[] actVal = getRawBytes(index);
		if (oldVal == actVal) {
			setRawBytes(index, newVal);
		}
		return actVal;
	}

	/**
	 * Reads a child by it's index or creates a new {@link ObjectState} if no
	 * child with the given index exists.
	 * 
	 * @param index
	 *            The index of the child
	 * @return a child's GameStateFragment
	 */
	public synchronized ObjectState getChild(int index) {
		ObjectState[] childVar = this.childFragment;
		ObjectState child = null;
		if (childVar.length > index) {
			child = childVar[index];
		}
		return child == null ? new ObjectState() : child;
	}

	/**
	 * Stores a child with the given index
	 * 
	 * @param index
	 * @param value
	 */
	public synchronized void setChild(int index, ObjectState value) {
		ObjectState[] childVar = this.childFragment;
		if (childVar == null) {
			if (value == null)
				return;
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			childVar = new ObjectState[newLen];
			this.childFragment = childVar;
		}
		int len = childVar.length;
		if (len > index) {
			childVar[index] = value;
			// shrink
			if (value == null && index >= len - INC_BY) {
				int newLen = len;
				while (newLen > 0 && childVar[newLen - 1] == null)
					newLen--; // remove trailing empty elements
				newLen = ((newLen + INC_BY) >> INC_SHIFT) << INC_SHIFT;
				if (newLen == 0) {
					this.childFragment = null;
				} else {
					this.childFragment = new ObjectState[newLen];
					System
							.arraycopy(childVar, 0, this.childFragment, 0,
									newLen);
				}
			}
		} else if (value != null) {
			int newLen = ((index + INC_BY) >> INC_SHIFT) << INC_SHIFT;
			childVar = new ObjectState[newLen];
			System.arraycopy(this.childFragment, 0, childVar, 0, len);
			childVar[index] = value;
			this.childFragment = childVar;
		}
		changeCount++;
	}

	/**
	 * Low performance software-implementation of compare and swap
	 * 
	 * @param index
	 * @param oldVal
	 * @param newVal
	 * @return the old stored value
	 */
	public synchronized ObjectState casChild(int index, ObjectState oldVal,
			ObjectState newVal) {
		ObjectState actVal = getChild(index);
		if (oldVal == actVal) {
			setChild(index, newVal);
		}
		return actVal;
	}

	public int getChangeCount() {
		return changeCount;
	}
}
