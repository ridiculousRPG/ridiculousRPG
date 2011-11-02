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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Alexander Baumgartner
 */
public class GameConfig implements Disposable {
	public Skin uiSkin;
	public Locale locale = Locale.getDefault();
	public String mapDir = "data/map/";
	public String mapPackDir = "data/map/pack/";

	private String skinConf = "data/theme/default/uiskin.json";
	private String skinImg = "data/theme/default/uiskin.png";


	private static GameConfig INSTANCE = null;

	private GameConfig() {
		//TODO: load configuration from config-file and
		// use defaults if no config-file is found
		uiSkin = new Skin(Gdx.files.internal(skinConf), Gdx.files.internal(skinImg));
	}

	public static GameConfig get() {
		if (INSTANCE==null) INSTANCE = new GameConfig();
		return INSTANCE;
	}
	public void setSkin(String skinConf_Path, String skinPNG_Path) {
		skinConf = skinConf_Path;
		skinImg = skinPNG_Path;
		uiSkin.dispose();
		uiSkin = new Skin(Gdx.files.internal(skinConf), Gdx.files.internal(skinImg));
	}

	@Override
	public void dispose() {
		//TODO: store configuration to config-file
		uiSkin.dispose();
	}
}
