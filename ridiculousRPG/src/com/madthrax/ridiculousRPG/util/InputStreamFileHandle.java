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

import java.io.InputStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

/**
 * @author Alexander Baumgartner
 */
public class InputStreamFileHandle extends FileHandle {
	private InputStream in;

	public InputStreamFileHandle(InputStream in) {
		this.in = in;
		type = FileType.Internal;
	}

	public InputStream read() {
		return in;
	}
}
