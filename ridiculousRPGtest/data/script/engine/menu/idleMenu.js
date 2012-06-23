/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menuService, menu) {
	if (keycode == Keys.P) {
		return menuService.changeState(MENU_STATE_PAUSE);
	}
	if (keycode == Keys.ESCAPE || keycode == Keys.MENU) {
		$.writeScreenshot(); // take screen shot
		return menuService.changeState(MENU_STATE_GAME);
	}
	if ($.controlKeyPressed) {
		i18nContainer = "engineMenuText";
		if (keycode == Keys.S) {
			$.writeScreenshot(); // take screen shot
			if ($.quickSave()) {
				menuService.showInfoFocused(i18nText("idlemenu.quicksave.succeed"));
				return true;
			}
		}
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				menuService.showInfoFocused(i18nText("idlemenu.quickload.succeed"));
				return true;
			}
		}
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menuService, menu) {
	// no menu is shown in idle state
}
