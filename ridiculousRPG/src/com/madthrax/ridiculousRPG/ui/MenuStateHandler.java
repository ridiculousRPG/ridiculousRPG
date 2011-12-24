package com.madthrax.ridiculousRPG.ui;

public interface MenuStateHandler {
	public boolean processInput(int keycode, MenuService menu);
	public void createGui(MenuService menu);
}
