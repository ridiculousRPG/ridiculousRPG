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

import java.util.Locale;

import com.badlogic.gdx.utils.Disposable;

/**
 * @author Alexander Baumgartner
 */
public class GameConfig implements Disposable {
	public Locale locale = Locale.getDefault();

	private static GameConfig INSTANCE = null;

	private GameConfig() {
		// TODO: load configuration from config-file and
		// use defaults if no config-file is found
	}

	public static GameConfig get() {
		if (INSTANCE == null)
			INSTANCE = new GameConfig();
		return INSTANCE;
	}

	public void dispose() {
	}
}
