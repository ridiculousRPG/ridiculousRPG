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
package com.madthrax.ridiculousRPG.events.handler;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.events.EventObject;

/**
 * This class executes JavaScript on touch/push events.
 * Particularly a JavaScript function is generated and will be called
 * on every touch/push event. The function has 3 parameters:<br>
 * <ol>
 * <li>eventSelf = This {@link EventObject}</li>
 * <li>eventTrigger = The {@link EventObject} which touched/pushed this {@link EventObject}</li>
 * <li>$ = {@link GameBase#$()} - The game instance itself</li>
 * </ol> 
 * @author Alexander Baumgartner
 */
public class EventExecScriptAdapter extends EventAdapter {
	private boolean touch, push;
    private Invocable engine;
	private String[] onPush = new String[1];
	private String[] onTouch = new String[1];
    private static final ScriptEngineManager ENGINE_FACTORY = new ScriptEngineManager();

	public boolean push(EventObject eventSelf, EventObject eventTrigger) throws ScriptException {
		if (!push) return false;
		if (engine==null) initEngine();
		try {
			return (Boolean)engine.invokeFunction("push", eventSelf, eventTrigger, GameBase.$());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}
	public boolean touch(EventObject eventSelf, EventObject eventTrigger) throws ScriptException {
		if (!touch) return false;
		if (engine==null) initEngine();
		try {
			return (Boolean)engine.invokeFunction("touch", eventSelf, eventTrigger, GameBase.$());
		} catch (NoSuchMethodException e) {
			// this should never happen
			throw new AssertionError(e);
		}
	}
	private void initEngine() throws ScriptException {
		String pushScript = "function push(eventSelf, eventTrigger, $) { ";
		for (String codeLine : onPush) {
			pushScript += codeLine + ";\n";
		}
		onPush = null;
		pushScript += "return true; }\n";
		String touchScript = "function touch(eventSelf, eventTrigger, $) { ";
		for (String codeLine : onTouch) {
			pushScript += codeLine + ";\n";
		}
		onTouch = null;
		touchScript += "return true; }\n";
        ScriptEngine engine = ENGINE_FACTORY.getEngineByName("JavaScript");
        engine.eval(pushScript+touchScript);
        if (engine instanceof Invocable) {
            this.engine = (Invocable) engine;
        } else {
        	throw new ScriptException("Sorry engine is not invokable :(");
        }
	}
	public void execOnPush(String val, int index) {
		index++; // lowest possible index is -1
		if (index>=onPush.length) {
			onPush = increase(onPush, index);
		}
		onPush[index] = val;
		push=true;
	}
	public void execOnTouch(String val, int index) {
		index++; // lowest possible index is -1
		if (index>=onTouch.length) {
			onTouch = increase(onTouch, index);
		}
		onTouch[index] = val;
		touch=true;
	}
	private String[] increase(String[] oldArray, int index) {
		String[] newArray = new String[Math.max(oldArray.length+10, index+1)];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		return newArray;
	}
}
