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

package com.ridiculousRPG;

import java.util.SortedMap;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * This class loads global scripts and generates new script engines.<br>
 * The currently running {@link ScriptEngine} is exposed by the variable
 * $scriptEngine.
 * 
 * @author Alexander Baumgartner
 */
public class ScriptFactory {
	private final ScriptEngineManager ENGINE_FACTORY = new ScriptEngineManager();
	private String scriptLanguage;
	private String[] scriptFileExtension;
	private static final String TEMPLATE_LINE_MARK = "#codeLine#";

	/**
	 * @see #evalAllGlobalScripts(FileHandle, boolean)
	 */
	public int evalAllGlobalScripts(String path, boolean recurse) {
		return evalAllGlobalScripts(Gdx.files.internal(path), recurse);
	}

	/**
	 * This method evaluates all scripts inside an specified directory using the
	 * global {@link ScriptEngine} and global bindings.
	 * 
	 * @param path
	 *            The path to a specified script file or a directory containing
	 *            script files.
	 * @param recurse
	 *            Specifies if scripts in subdirectories should be evaluated.
	 * @return The count of files which has been evaluated
	 */
	public int evalAllGlobalScripts(FileHandle path, boolean recurse) {
		ScriptEngine scriptEngine = ENGINE_FACTORY
				.getEngineByName(getScriptLanguage());
		scriptEngine.put("$scriptEngine", scriptEngine);
		return evalAllScripts(scriptEngine, path, recurse, ENGINE_FACTORY
				.getBindings());
	}

	/**
	 * This method adds a global variable.
	 */
	public void putGlobalVar(String name, Object value) {
		ENGINE_FACTORY.getBindings().put(name, value);
	}

	/**
	 * @see #evalAllScripts(ScriptEngine, FileHandle, boolean)
	 */
	public int evalAllScripts(ScriptEngine engine, String path, boolean recurse) {
		return evalAllScripts(engine, Gdx.files.internal(path), recurse);
	}

	/**
	 * This method evaluates all scripts inside an specified directory using the
	 * specified {@link ScriptEngine}.
	 * 
	 * @param engine
	 *            The {@link ScriptEngine} to use for evaluating the files.
	 * @param path
	 *            The path to a specified script file or a directory containing
	 *            script files.
	 * @param recurse
	 *            Specifies if scripts in subdirectories should be evaluated.
	 * @return The count of files which has been evaluated
	 */
	public int evalAllScripts(ScriptEngine engine, FileHandle path,
			boolean recurse) {
		return evalAllScripts(engine, path, recurse, null);
	}

	/**
	 * @see #evalAllScripts(ScriptEngine, FileHandle, boolean, Bindings)
	 */
	public int evalAllScripts(ScriptEngine engine, String path,
			boolean recurse, Bindings bindings) {
		return evalAllScripts(engine, Gdx.files.internal(path), recurse,
				bindings);
	}

	/**
	 * This method evaluates all scripts inside an specified directory using the
	 * specified {@link ScriptEngine} with the specified bindings.
	 * 
	 * @param engine
	 *            The {@link ScriptEngine} to use for evaluating the files.
	 * @param path
	 *            The path to a specified script file or a directory containing
	 *            script files.
	 * @param recurse
	 *            Specifies if scripts in subdirectories should be evaluated.
	 * @param bindings
	 *            The bindings object to use for evaluating the scripts. See
	 *            also {@link ScriptEngine#getBindings(int)}.
	 * @return The count of files which has been evaluated
	 */
	public int evalAllScripts(ScriptEngine engine, FileHandle path,
			boolean recurse, Bindings bindings) {
		try {
			engine.getBindings(ScriptContext.ENGINE_SCOPE).put(
					ScriptEngine.FILENAME, path.path());
			if (path.isDirectory()) {
				int count = 0;
				for (FileHandle child : path.list()) {
					if ((recurse && child.isDirectory())
							|| hasAllowedSuffix(child)) {
						count += evalAllScripts(engine, child, recurse,
								bindings);
					}
				}
				return count;
			} else if (bindings == null) {
				engine.eval(path.readString(GameBase.$options().encoding));
				return 1;
			} else {
				bindings.put(ScriptEngine.FILENAME, path.path());
				engine.eval(path.readString(GameBase.$options().encoding),
						bindings);
				bindings.remove(ScriptEngine.FILENAME);
				return 1;
			}
		} catch (ScriptException e) {
			GameBase.$error("ScriptFactory.evalAllScripts",
					"Problem evaluating script file " + path.path(), e);
			return 0;
		} finally {
			engine.getBindings(ScriptContext.ENGINE_SCOPE).remove(
					ScriptEngine.FILENAME);
		}
	}

	/**
	 * Determines if the file has an suffix which matches the list of allowed
	 * suffixes for this {@link ScriptFactory}. The comparison is case
	 * insensitive.
	 * 
	 * @param file
	 *            The {@link FileHandle} to check
	 * @return true if the file extension matches one of
	 *         {@link #getAllowedSuffix()}
	 */
	public boolean hasAllowedSuffix(FileHandle file) {
		String nameLC = file.name().toLowerCase();
		for (String allowedSuffix : getScriptFileExtension()) {
			if (nameLC.endsWith(allowedSuffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method loads a script from the specified path or returns the String
	 * unchanged, if it's not a valid path.
	 * 
	 * @param scriptCodeOrPath
	 * @return The script code
	 */
	public String loadScript(String scriptCodeOrPath) {
		FileHandle fh = null;
		try {
			fh = Gdx.files.internal(scriptCodeOrPath);
			if (fh.exists()) {
				return fh.readString(GameBase.$options().encoding);
			}
		} catch (Exception e) {
			GameBase.$error("ScriptFactory.loadScript",
					"Failed to load script '" + fh + "'", e);
		}
		return scriptCodeOrPath;
	}

	/**
	 * Creates a new script engine or reuses an other script engine. It's
	 * guaranteed, that the local script context is a new one and that the
	 * global script context is shared over all engines.<br>
	 * The actual {@link ScriptEngine} is exposed by the variable $scriptEngine.
	 * 
	 * @return A new script engine
	 */
	public ScriptEngine obtainEngine() {
		ScriptEngine scriptEngine = ENGINE_FACTORY
				.getEngineByName(getScriptLanguage());
		scriptEngine.put("$scriptEngine", scriptEngine);
		return scriptEngine;
	}

	/**
	 * This method merges the given code lines into the script function
	 * template.
	 * 
	 * @return A {@link String} with the generated script function
	 */
	public String prepareScriptFunction(SortedMap<Integer, String> codeLines,
			String functionTemplate) {
		String[] sa = functionTemplate.split(TEMPLATE_LINE_MARK);
		// estimate length
		StringBuilder script = new StringBuilder(functionTemplate.length()
				+ sa[1].length() * codeLines.size() + 100);
		script.append(sa[0]);
		boolean lineSeparator = false;
		for (String line : codeLines.values()) {
			if (lineSeparator) {
				script.append(sa[1]);
			} else {
				lineSeparator = true;
			}
			script.append(line);
		}
		script.append(sa[2]);
		return script.toString();
	}

	public Invocable obtainInvocable(FileHandle scriptFileToLoad)
			throws ScriptException {
		return obtainInvocable(scriptFileToLoad
				.readString(GameBase.$options().encoding), scriptFileToLoad
				.path());
	}

	public ScriptEngine obtainEngine(FileHandle scriptFileToLoad)
			throws ScriptException {
		return obtainEngine(scriptFileToLoad
				.readString(GameBase.$options().encoding), scriptFileToLoad
				.path());
	}

	/**
	 * Returns an invokable script engine with the compiled source code
	 * 
	 * @param scriptToLoad
	 *            The script to load
	 * @param nameInfoForErrorLog
	 *            Optional parameter to provide informations about the error
	 *            location
	 * @return An invokable script engine
	 * @throws ScriptException
	 *             If the scripting language doesn't support the
	 *             {@link Invocable} feature
	 */
	public Invocable obtainInvocable(CharSequence scriptToLoad,
			String nameInfoForErrorLog) throws ScriptException {
		ScriptEngine engine = obtainEngine(scriptToLoad, nameInfoForErrorLog);
		if (engine instanceof Invocable) {
			return (Invocable) engine;
		} else {
			throw new ScriptException("Sorry engine is not invokable :(");
		}
	}

	public ScriptEngine obtainEngine(CharSequence scriptToLoad,
			String nameInfoForErrorLog) throws ScriptException {
		ScriptEngine engine = obtainEngine();
		engine.put(ScriptEngine.FILENAME, nameInfoForErrorLog);
		engine.eval(scriptToLoad.toString());
		return engine;
	}

	public String getScriptLanguage() {
		return scriptLanguage;
	}

	public void setScriptLanguage(String scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	public void setScriptFileExtension(String[] scriptFileExtension) {
		for (int i = 0; i < scriptFileExtension.length; i++) {
			scriptFileExtension[i] = scriptFileExtension[i].toLowerCase();
			if (scriptFileExtension[i].length() == 0)
				scriptFileExtension[i] = "_NULL_";
		}
		this.scriptFileExtension = scriptFileExtension;
	}

	public String[] getScriptFileExtension() {
		return scriptFileExtension;
	}

	public void clearGlobalState() {
		ENGINE_FACTORY.getBindings().clear();
	}
}
