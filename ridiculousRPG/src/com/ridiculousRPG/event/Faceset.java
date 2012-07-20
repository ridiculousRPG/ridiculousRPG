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

package com.ridiculousRPG.event;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.util.TextureRegionLoader;
import com.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * @author Alexander Baumgartner
 */
public class Faceset implements Disposable {
	private TextureRegionRef faceset;
	private TextureRegion[][] faces; // [row][col]

	public Faceset(String path, int faceWidth, int faceHeight, int anzRows,
			int anzCols) {
		setFaceset(path, faceWidth, faceHeight, anzRows, anzCols);
	}

	public void setFaceset(String path, int faceWidth, int faceHeight,
			int anzRows, int anzCols) {
		if (faceset != null)
			faceset.dispose();
		faceset = TextureRegionLoader.load(path, 0, 0, faceWidth * anzCols,
				faceHeight * anzRows);
		faces = faceset.split(faceWidth, faceHeight);
	}

	public TextureRegion getFace(int row, int col) {
		return faces[row][col];
	}

	public void dispose() {
		if (faceset != null)
			faceset.dispose();
	}
}
