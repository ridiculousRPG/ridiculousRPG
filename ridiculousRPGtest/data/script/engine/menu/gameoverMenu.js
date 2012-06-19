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
	if ($.controlKeyPressed) {
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				return menu.changeState(MENU_STATE_IDLE);;
			}
		}
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Game over", skin);

	var quickload = new ui.TextButton("Quick load (Ctrl+L)", skin);
	quickload.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menu.changeState(MENU_STATE_IDLE);
			} else {
				menu.showInfoFocused("Load failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quickload);

	var load = new ui.TextButton("Load", skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_LOAD);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(load);


	var toTitle = new ui.TextButton("Return to title", skin);
	toTitle.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_TITLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(toTitle);

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
	menu.focus(resume);
}
