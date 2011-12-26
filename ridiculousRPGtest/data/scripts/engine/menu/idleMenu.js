/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menu) {
	if (keycode == Keys.P) {
		return menu.changeState(MENU_STATE_PAUSE);
	}
	if (keycode == Keys.ESCAPE) {
		return menu.changeState(MENU_STATE_GAME);
	}
	return false;
}
function createGui(menu) {
	// no menu is shown in idle state
}
