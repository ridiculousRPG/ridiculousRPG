package com.madthrax.ridiculousRPG.ui;

public interface MenuService {
	public MenuStateHandler getStateHandler(int state);
	public boolean changeState(MenuStateHandler newState);
	public float width();
	public float height();
	public void center(Object guiElement);
	public void showInfoNormal(String info);
	public void showInfoFocused(String info);
}
