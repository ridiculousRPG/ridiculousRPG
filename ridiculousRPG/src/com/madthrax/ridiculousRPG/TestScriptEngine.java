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

/**
 * This class tests if the Mozilla Rhino based script engine works on your platform.<br>
 * You should see the following output:<br>
 * <code><pre> madthrax: 5
 * ridiculous: -22</pre></code>
 * @author Alexander Baumgartner
 */
public class TestScriptEngine {
	public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        TestEventParm eventSelf = new TestEventParm();
        TestEventParm eventPushed = new TestEventParm();

        // The JavaScript code in a String
        String script = "function push(eventSelf, eventPushed) { eventSelf.x = 5; eventPushed.setName('ridiculous') }\n" +
        		"function touch(eventSelf, eventPushed) { eventPushed.x = -22; eventSelf.setName('madthrax') }";
        engine.eval(script);
        if (engine instanceof Invocable) {
            Invocable inv = (Invocable) engine;
            inv.invokeFunction("push", eventSelf, eventPushed);
            inv.invokeFunction("touch", eventSelf, eventPushed);
        } else {
        	System.out.println("Sorry engine is not invokable :(");
        }

        // print result
		eventSelf.print();
		eventPushed.print();
	}
	public static class TestEventParm {
		public int x;
		private String name;
		public void setName(String name) {
			this.name = name;
		}
		public void print() {
			System.out.println(name+": "+x);
		}
	}
}
