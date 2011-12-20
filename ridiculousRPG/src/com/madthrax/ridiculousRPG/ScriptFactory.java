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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SortedIntList;
import com.badlogic.gdx.utils.SortedIntList.Node;

/**
 * This class loads global scripts and generates new script engines
 * 
 * @author Alexander Baumgartner
 */
public class ScriptFactory {
	private final ScriptEngineManager ENGINE_FACTORY = new ScriptEngineManager();

	public void init(String initScript) {
		ScriptEngine engine = ENGINE_FACTORY
				.getEngineByName(getScriptLanguage());
		try {
			FileHandle[] files = Gdx.files.internal("data/scripts/global/")
					.list();
			for (FileHandle scriptFile : files) {
				engine.eval(scriptFile.readString("UTF-8"), ENGINE_FACTORY
						.getBindings());
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method loads a script from the specified path or returns the String
	 * unchanged, if it's not a valid path.
	 * 
	 * @param scriptCodeOrPath
	 * @return The script code
	 */
	public String loadScript(String scriptCodeOrPath) {
		try {
			FileHandle fh = Gdx.files.internal(scriptCodeOrPath);
			if (fh.exists()) {
				return fh.readString("UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scriptCodeOrPath;
	}

	/**
	 * Creates a new script engine or reuses an other script engine. It's
	 * guaranteed, that the local script context is a new one and that the
	 * global script context is shared over all engines.
	 * 
	 * @return A new script engine
	 */
	public ScriptEngine obtainEngine() {
		return ENGINE_FACTORY.getEngineByName(getScriptLanguage());
	}

	/**
	 * Override this method if you want an other script language.<br>
	 * The script engine has to support invocation by implementing the interface
	 * {@link Invocable}<br>
	 * Maybe you also need to override
	 * {@link #createFunction(SortedIntList, String, String)}
	 * 
	 * @return The script language used by this script interpreter.
	 */
	public String getScriptLanguage() {
		return "JavaScript";
	}

	/**
	 * Override this method if you want to generate a function for an other
	 * script language.<br>
	 * Don't forget to override {@link #getScriptLanguage()} too.<br>
	 * 
	 * @param codeLines
	 *            The lines of script code sorted by the line number.
	 * @param fncName
	 *            The name of the script function.
	 * @param returnTrue
	 *            True if the function should return true per default. If an
	 *            event occurred, returning true means that this event has been
	 *            consumed by the function. In a lot of cases it makes sense to
	 *            return true.
	 * @param fncParam
	 *            All specified parameters.
	 * @return A {@link String} with the generated script function
	 */
	public String createScriptFunction(SortedIntList<String> codeLines,
			String fncName, boolean returnTrue, String... fncParam) {
		StringBuilder script = new StringBuilder();
		script.append("\nfunction ").append(fncName).append('(');
		for (int i = 0, len = fncParam.length; i < len; i++) {
			if (i > 0)
				script.append(',');
			script.append(fncParam[i]);
		}
		script.append(") {");
		for (Node<String> line : codeLines) {
			script.append('\n').append(line.value).append(';');
		}
		// let gc do it's work
		codeLines.clear();
		if (returnTrue)
			script.append("\nreturn true;");
		script.append("\n}");
		return script.toString();
	}
}
