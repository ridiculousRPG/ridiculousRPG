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

import java.io.PrintStream;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class tests if the script engine works properly and may be used with
 * this game engine.<br>
 * You should see the following output:<br>
 * <code><pre> testOut1: 5
 * testOut2: -22
 * engine1: 5.0
 * engine2: 15.0
 * engine1: -5.0
 * engine2: 5.0 </pre></code>
 * 
 * Ladies and Gentlemen, start your engines ;)
 * 
 * @author Alexander Baumgartner
 */
public class TestScriptEngine {

	/**
	 * Defines the language to use. Change this if you want to test an other
	 * engine.
	 */
	public String scriptLanguage = "JavaScript";
	/**
	 * Maybe you have to change the test, if it doesn't match the syntax of your
	 * script language
	 */
	public String testScript1 = "function push(eventSelf, eventPushed) "
			+ "{ eventSelf.x = 5; eventPushed.setName('testOut2') }\n"
			+ "function touch(eventSelf, eventPushed) "
			+ "{ eventPushed.x = -22; eventSelf.setName('testOut1') }";
	/**
	 * Maybe you have to change the test, if it doesn't match the syntax of your
	 * script language
	 */
	public String testScript2 = "x=5; function f(y) {return x+y;}";
	/**
	 * Maybe you have to change the test, if it doesn't match the syntax of your
	 * script language
	 */
	public String testScript3 = "function g(y) {return f(y)-x;}";
	/**
	 * Maybe you have to change the test, if it doesn't match the syntax of your
	 * script language
	 */
	public String testScript4 = "function g(y) {return f(y)+x;}";

	/**
	 * The main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new TestScriptEngine().startEngineTests(System.out);
	}

	/**
	 * Before calling this method, maybe you want to change the
	 * {@link #scriptLanguage}<br>
	 * <br>
	 * You should see the following output:<br>
	 * <code><pre> testOut1: 5
	 * testOut2: -22
	 * engine1: 5.0
	 * engine2: 15.0
	 * engine1: -5.0
	 * engine2: 5.0 </pre></code>
	 * 
	 * @throws Exception
	 */
	public void startEngineTests(PrintStream out) throws Exception {
		simpleTest(out);
		scopeTest(out);
	}

	private void simpleTest(PrintStream out) throws ScriptException,
			NoSuchMethodException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(scriptLanguage);
		TestEventParm eventSelf = new TestEventParm();
		TestEventParm eventPushed = new TestEventParm();

		// The JavaScript code in a String
		engine.eval(testScript1);
		if (engine instanceof Invocable) {
			Invocable inv = (Invocable) engine;
			inv.invokeFunction("push", eventSelf, eventPushed);
			inv.invokeFunction("touch", eventSelf, eventPushed);
		} else {
			out.println("Sorry engine is not invokable :(");
		}

		// print result
		eventSelf.print(out);
		eventPushed.print(out);
	}

	private void scopeTest(PrintStream out) throws ScriptException,
			NoSuchMethodException {
		ScriptEngineManager manager = new ScriptEngineManager();

		// should be available in engine 1 and engine 2
		ScriptEngine global = manager.getEngineByName(scriptLanguage);
		global.eval(testScript2, manager.getBindings());

		ScriptEngine engine1 = manager.getEngineByName(scriptLanguage);
		engine1.eval(testScript3);
		ScriptEngine engine2 = manager.getEngineByName(scriptLanguage);
		engine2.eval(testScript4);

		if (engine1 instanceof Invocable) {
			Invocable inv = (Invocable) engine1;
			Invocable inv2 = (Invocable) engine2;
			out.println("engine1: " + inv.invokeFunction("g", 5));
			out.println("engine2: " + inv2.invokeFunction("g", 5));
			out.println("engine1: " + inv.invokeFunction("g", -5));
			out.println("engine2: " + inv2.invokeFunction("g", -5));
		} else {
			out.println("Sorry engine is not invokable :(");
		}
	}

	public static class TestEventParm {
		public int x;
		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public void print(PrintStream out) {
			out.println(name + ": " + x);
		}
	}
}
