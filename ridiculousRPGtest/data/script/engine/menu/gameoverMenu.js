/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menuService, menu) {
	if (keycode == Keys.ESCAPE) {
		$.exit();
		return true;
	}
	if ($.controlKeyPressed) {
		if (keycode == Keys.L) {
			if ($.quickLoad()) {
				return menuService.changeState(MENU_STATE_IDLE);;
			}
		}
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menuService, menu) {
	i18nContainer = "engineMenuText";
	var skin = menuService.skinNormal;
	var w = new ui.Window(i18nText("gameovermenu.title"), skin);

	var quickload = new ui.TextButton(i18nText("gameovermenu.quickload"), skin);
	quickload.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("gameovermenu.loadfailed"));
			}
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(quickload);

	var load = new ui.TextButton(i18nText("gameovermenu.load"), skin);
	load.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(load);


	var toTitle = new ui.TextButton(i18nText("gameovermenu.return"), skin);
	toTitle.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_TITLE);
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(toTitle);

	var exit = new ui.TextButton(i18nText("gameovermenu.exit"), skin);
	exit.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			$.exit();
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	menuService.center(w);
	menuService.addGUIcomponent(w);
	if (desktopMode) menuService.focus(resume);
}
