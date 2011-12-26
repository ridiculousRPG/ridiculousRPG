/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

importPackage(Packages.com.badlogic.gdx.scenes.scene2d.ui);

ServiceState = Packages.com.madthrax.ridiculousRPG.ui.StandardMenuService.ServiceState;
Keys = Packages.com.badlogic.gdx.Input.Keys;

// The following 6 states are defined by StandardMenuService.ServiceState
// TITLE_SCREEN, IDLE, PAUSED, GAME_MENU1, GAME_MENU2, GAME_MENU3

/**
 * Called if the MenuService is in idle state, which means, that
 * the game is running and no menu is actually shown to the user.
 */
function processInputIdle(menu, keycode) {
	if (keycode == Keys.P) {
		return menu.changeState(ServiceState.PAUSED);
	}
	if (keycode == Keys.ESCAPE) {
		return menu.changeState(ServiceState.GAME_MENU1);
	}
	return false;
}

/**
 * Called if the pause menu is actually shown to the user.
 */
function processInputPaused(menu, keycode) {
	if (keycode == Keys.P) {
		return changeState(ServiceState.IDLE);
	}
	return false;
}

/**
 * Called if the title screen is actually shown to the user.
 */
function processInputTitleScreen(menu, keycode) {
	if (keycode == Keys.ESCAPE) {
		$.exit();
		return true;
	}
	return false;
}

/**
 * Called if the ingame menu #1 is actually shown to the user.
 */
function processInputGameMenu1(menu, keycode) {
	if (keycode == Keys.ESCAPE) {
		return menu.changeState(ServiceState.IDLE);
	}
	return false;
}

/**
 * Called if the ingame menu #2 is actually shown to the user.
 */
function processInputGameMenu2(menu, keycode) {
	if (keycode == Keys.ESCAPE) {
		return menu.changeState(ServiceState.IDLE);
	}
	return false;
}

/**
 * Called if the ingame menu #3 is actually shown to the user.
 */
function processInputGameMenu3(menu, keycode) {
	if (keycode == Keys.ESCAPE) {
		return menu.changeState(ServiceState.IDLE);
	}
	return false;
}

