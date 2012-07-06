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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ridiculousRPG.GameBase;

/**
 * Executes the given script or closure-event on change.<br>
 * This implementation is jdk6-rhino friendly ;)
 * 
 * @author Alexander Baumgartner
 */
public class ChangeAdapter extends
		com.badlogic.gdx.scenes.scene2d.utils.ChangeListener {
	private String scriptCode;
	private ChangeListener listener;

	public ChangeAdapter(String scriptCode) {
		this.scriptCode = scriptCode;
	}

	public ChangeAdapter(ChangeListener listener) {
		this.listener = listener;
	}

	@Override
	public void changed(ChangeEvent event, Actor actor) {
		try {
			if (listener != null)
				listener.changed(event, actor);
			else if (scriptCode != null)
				GameBase.$().eval(scriptCode);
		} catch (ScriptException e) {
			GameBase.$error("ChangeAdapter.changed",
					"Error processing callback function for '"
							+ event.getListenerActor() + "'", e);
		}
	}

	public interface ChangeListener {
		public void changed(ChangeEvent event, Actor actor);
	}
}
