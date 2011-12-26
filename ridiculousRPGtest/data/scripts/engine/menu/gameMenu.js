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
		return menu.changeState(MENU_STATE_IDLE);
	}
	return false;
}
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Game menu", skin);
	menu.addGUIcomponent(w);

	var resume = new ui.TextButton("Resume (Esc)", skin);
	resume.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_IDLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var bag = new ui.TextButton("Open bag", skin);
	bag.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.showInfoFocused("Bag is not implemented yet.\n"
					+ "This is an early alpha release!");
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(bag);

	var save = new ui.TextButton("Save", skin);
	save.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.showInfoFocused("Save is not implemented yet.\n"
					+ "This is an early alpha release!");
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(save);

	var load = new ui.TextButton("Load", skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.showInfoFocused("Load is not implemented yet.\n"
					+ "This is an early alpha release!");
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

	var exit = new ui.TextButton("Exit game", skin);
	exit.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.exit();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	w.height = menu.height;
	menu.addActor(w);
	menu.focus(resume);
}
