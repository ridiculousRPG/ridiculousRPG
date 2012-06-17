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
	if ($.controlKeyPressed) {
		if (keycode == Keys.S) {
			if ($.quickSave()) {
				menu.showInfoFocused("Quicksave successful (Ctrl+S)");
				return true;
			}
		}
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				menu.showInfoFocused("Quickload performed (Ctrl+L)");
				return true;
			}
		}
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	// no menu is shown in idle state
}
