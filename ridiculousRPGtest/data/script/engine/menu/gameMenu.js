/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menuService, menuHandler) {
	if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
		return menuService.changeState(MENU_STATE_IDLE);
	}
	if ($.controlKeyPressed) {
		if (keycode == Keys.S) {
			if ($.quickSave()) {
				menuService.showInfoFocused("Quicksave successful (Ctrl+S)");
				return menuService.changeState(MENU_STATE_IDLE);;
			}
		}
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				menuService.showInfoFocused("Quickload performed (Ctrl+L)");
				return menuService.changeState(MENU_STATE_IDLE);;
			}
		}
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menuService, menuHandler) {
	var skin = menuService.skinNormal;
	var w = new ui.Window("Game menu", skin);

	var resume = new ui.TextButton("Resume (Esc)", skin);
	resume.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var bag = new ui.TextButton("Open bag", skin);
	bag.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.showInfoFocused("Bag is not implemented yet.\n"
					+ "This is an early alpha release!");
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(bag);

	var quickload = new ui.TextButton("Quick load (Ctrl+L)", skin);
	quickload.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused("Load failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quickload);

	var quicksave = new ui.TextButton("Quick save (Ctrl+S)", skin);
	quicksave.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickSave()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused("Save failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quicksave);

	var load = new ui.TextButton("Load", skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(load);

	var save = new ui.TextButton("Save", skin);
	save.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_SAVE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(save);

	var toTitle = new ui.TextButton("Return to title", skin);
	toTitle.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_TITLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(toTitle);

	var exit = new ui.TextButton("Exit game", skin);
	exit.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.exit();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	w.height = menuService.getHeight();
	menuService.addGUIcomponent(w);
	menuService.focus(resume);
}
