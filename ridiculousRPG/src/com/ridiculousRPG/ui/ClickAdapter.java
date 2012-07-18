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

package com.ridiculousRPG.ui;

import javax.script.ScriptException;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.ridiculousRPG.GameBase;

/**
 * Executes the given script or closure-event on click.<br>
 * This implementation is jdk6-rhino friendly ;)
 * 
 * @author Alexander Baumgartner
 */
public class ClickAdapter extends
		com.badlogic.gdx.scenes.scene2d.utils.ClickListener {

	private String scriptCode;
	private ClickListener listener;

	public ClickAdapter(String scriptCode) {
		this.scriptCode = scriptCode;
	}

	public ClickAdapter(ClickListener listener) {
		this.listener = listener;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		try {
			if (listener != null)
				listener.clicked(event, x, y);
			else if (scriptCode != null)
				GameBase.$().eval(scriptCode);
		} catch (ScriptException e) {
			GameBase.$error("ClickAdapter.clicked",
					"Error processing callback function for '"
							+ event.getListenerActor() + "'", e);
		}
	}

	public interface ClickListener {
		public void clicked(InputEvent event, float x, float y);
	}
}
