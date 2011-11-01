/**
 * Bugfix setStyle for Checkbox
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
