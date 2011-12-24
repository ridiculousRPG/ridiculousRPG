package com.madthrax.ridiculousRPG.ui;

import javax.script.Invocable;
import javax.script.ScriptException;

import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.GameBase;

public class MenuStateScriptAdapter implements MenuStateHandler {
	boolean freezeTheWorld;
	private Invocable scriptEngine;

	public MenuStateScriptAdapter(FileHandle callBackScript,
			boolean freezeTheWorld) throws ScriptException {
		this.freezeTheWorld = freezeTheWorld;
		this.scriptEngine = GameBase.$scriptFactory().obtainInvocable(
				callBackScript);
	}

	@Override
	public void createGui(MenuService menu) {
		try {
			scriptEngine.invokeFunction("createGui", menu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean processInput(int keycode, MenuService menu) {
		try {
			Object ret = scriptEngine.invokeFunction("processInput", keycode,
					menu);
			return (ret instanceof Boolean) && ((Boolean) ret);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
