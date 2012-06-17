/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menu) {
	if (keycode == Keys.ESCAPE) {
		$.exit();
		return true;
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Start menu", skin);

	var start = new ui.TextButton("Start new game", skin);
	start.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_IDLE);
			menu.startNewGame();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(start);

	var resume = new ui.TextButton("Quick load", skin);
	resume.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menu.changeState(MENU_STATE_IDLE);
			} else {
				menu.showInfoFocused("Load failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var load = new ui.TextButton("Load", skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_LOAD);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(load);


	var toggleFull = new ui.TextButton(
			$.isFullscreen() ? "Window mode" : "Fullscreen mode", skin);
	toggleFull.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.toggleFullscreen();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(toggleFull);

	var exit = new ui.TextButton("Exit game (Esc)", skin);
	exit.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.exit();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	menu.center(w);
	menu.addGUIcomponent(w);
	menu.focus(start);
}
