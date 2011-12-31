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

import java.util.SortedMap;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SortedIntList;

/**
 * This class loads global scripts and generates new script engines.<br>
 * The currently running {@link ScriptEngine} is exposed by the variable
 * $scriptEngine.
 * 
 * @author Alexander Baumgartner
 */
public class ScriptFactory {
	private final ScriptEngineManager ENGINE_FACTORY = new ScriptEngineManager();

	public void init(String initScript) {
		evalAllScripts(obtainEngine(), GameBase.$options().initScript, false);
	}

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
				engine.eval(path.readString(GameBase.$options().encoding),
						bindings);
				return 1;
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
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
		String suffix = file.extension();
		for (String allowed : getAllowedSuffix()) {
			if (suffix.equalsIgnoreCase(allowed)) {
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
		try {
			FileHandle fh = Gdx.files.internal(scriptCodeOrPath);
			if (fh.exists()) {
				return fh.readString(GameBase.$options().encoding);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * Override this method if you want an other script language.<br>
	 * The script engine has to support invocation by implementing the interface
	 * {@link Invocable}<br>
	 * Maybe you also need to override {@link #getAllowedSuffix()}
	 * {@link #createFunction(SortedIntList, String, String)}
	 * 
	 * @return The script language used by this script interpreter.
	 */
	public String getScriptLanguage() {
		return "JavaScript";
	}

	/**
	 * Override this method if you want an other script language.<br>
	 * The script engine has to support invocation by implementing the interface
	 * {@link Invocable}<br>
	 * Maybe you also need to override {@link #getScriptLanguage()}
	 * {@link #createFunction(SortedIntList, String, String)}
	 * 
	 * @return An array of allowed file suffixes. This factory allows
	 *         &quot;js&quot; and &quot;jscript&quot;.
	 */
	public String[] getAllowedSuffix() {
		return new String[] { "js", "jscript" };
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
	 * @param returnTrueFalseNone
	 *            True if the function should return true per default. If an
	 *            event occurred, returning true means that this event has been
	 *            consumed by the function. In a lot of cases it makes sense to
	 *            return true.<br>
	 *            False if the function should return false per default.<br>
	 *            Null if the function should return nothing per default.
	 * @param fncParam
	 *            Specifies the parameters for the function.
	 * @return A {@link String} with the generated script function
	 */
	public String createScriptFunction(SortedMap<Integer, String> codeLines,
			String fncName, Boolean returnTrueFalseNone, String... fncParam) {
		StringBuilder script = new StringBuilder();
		script.append("\nfunction ").append(fncName).append('(');
		for (int i = 0, len = fncParam.length; i < len; i++) {
			if (i > 0)
				script.append(',');
			script.append(fncParam[i]);
		}
		script.append(") {");
		for (String line : codeLines.values()) {
			script.append('\n').append(line).append(';');
		}
		if (returnTrueFalseNone != null) {
			if (returnTrueFalseNone)
				script.append("\nreturn true;");
			else
				script.append("\nreturn false;");
		}
		script.append("\n}");
		return script.toString();
	}

	public Invocable obtainInvocable(FileHandle scriptFileToLoad)
			throws ScriptException {
		return obtainInvocable(scriptFileToLoad
				.readString(GameBase.$options().encoding));
	}

	public ScriptEngine obtainEngine(FileHandle scriptFileToLoad)
			throws ScriptException {
		return obtainEngine(scriptFileToLoad
				.readString(GameBase.$options().encoding));
	}

	public Invocable obtainInvocable(CharSequence scriptToLoad)
			throws ScriptException {
		ScriptEngine engine = obtainEngine(scriptToLoad);
		if (engine instanceof Invocable) {
			return (Invocable) engine;
		} else {
			throw new ScriptException("Sorry engine is not invokable :(");
		}
	}

	public ScriptEngine obtainEngine(CharSequence scriptToLoad)
			throws ScriptException {
		ScriptEngine engine = obtainEngine();
		engine.eval(scriptToLoad.toString());
		return engine;
	}
}
