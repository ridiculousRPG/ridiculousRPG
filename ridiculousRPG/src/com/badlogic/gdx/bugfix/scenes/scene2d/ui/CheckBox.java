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

/**
 * Bugfix setStyle for Checkbox
 * @author Alexander Baumgartner
 */
package com.badlogic.gdx.bugfix.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CheckBox extends com.badlogic.gdx.scenes.scene2d.ui.CheckBox {
	public CheckBox(String text, Skin skin) {
		super(text, skin);
	}
	@Override
	public void setStyle(ButtonStyle style) {
		super.setStyle(style);
		if (image != null && style instanceof CheckBoxStyle) {
			image.setRegion(isChecked() ? ((CheckBoxStyle)style).checkboxOn : ((CheckBoxStyle)style).checkboxOff);
		}
	}
}
