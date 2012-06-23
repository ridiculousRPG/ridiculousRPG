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
		i18nContainer = "engineMenuText";
		if (keycode == Keys.S) {
			if ($.quickSave()) {
				menuService.showInfoFocused(i18nText("gamemenu.quicksave.succeed"));
				return menuService.changeState(MENU_STATE_IDLE);;
			}
		}
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				menuService.showInfoFocused(i18nText("gamemenu.quickload.succeed"));
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
	i18nContainer = "engineMenuText";
	var skin = menuService.skinNormal;
	var w = new ui.Window(i18nText("gamemenu.title"), skin);

	var resume = new ui.TextButton(i18nText("gamemenu.resume"), skin);
	resume.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var bag = new ui.TextButton(i18nText("gamemenu.openbag"), skin);
	bag.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.showInfoFocused("Bag is not implemented yet.\n"
					+ "This is an early alpha release!");
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(bag);

	var quickload = new ui.TextButton(i18nText("gamemenu.quickload"), skin);
	quickload.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("gamemenu.loadfailed"));
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quickload);

	var quicksave = new ui.TextButton(i18nText("gamemenu.quicksave"), skin);
	quicksave.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickSave()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("gamemenu.savefailed"));
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quicksave);

	var load = new ui.TextButton(i18nText("gamemenu.load"), skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(load);

	var save = new ui.TextButton(i18nText("gamemenu.save"), skin);
	save.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_SAVE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(save);

	var toTitle = new ui.TextButton(i18nText("gamemenu.return"), skin);
	toTitle.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menuService.changeState(MENU_STATE_TITLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(toTitle);

	var exit = new ui.TextButton(i18nText("gamemenu.exit"), skin);
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
