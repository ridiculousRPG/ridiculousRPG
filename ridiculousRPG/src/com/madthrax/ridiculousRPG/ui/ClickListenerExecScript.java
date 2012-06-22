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

package com.madthrax.ridiculousRPG.ui;

import java.text.DateFormat;

import javax.script.ScriptException;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.madthrax.ridiculousRPG.GameBase;

/**
 * Executes the given script on click
 * 
 * @author Alexander Baumgartner
 */
public class ClickListenerExecScript implements ClickListener {
	private String scriptCode;

	public ClickListenerExecScript(String scriptCode) {
		this.scriptCode = scriptCode;
	}
	@Override
	public void click(Actor actor, float x, float y) {
		try {
			GameBase.$().eval(scriptCode);
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}
}
