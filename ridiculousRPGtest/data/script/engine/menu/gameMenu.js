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
	var w = menuService.createWindow(i18nText("gamemenu.title"), 
			0, -1, 180, 0, skin);

	var resume = new ui.TextButton(i18nText("gamemenu.resume"), skin);
	resume.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
		}
	));
	w.row().fill().expandX();
	w.add(resume);

	var bag = new ui.TextButton(i18nText("gamemenu.openbag"), skin);
	bag.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.showInfoFocused("Bag is not implemented yet.");
		}
	));
	w.row().fill().expandX().padBottom(10);
	w.add(bag);

	var quickload = new ui.TextButton(i18nText("gamemenu.quickload"), skin);
	quickload.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("gamemenu.loadfailed"));
			}
		}
	));
	w.row().fill().expandX();
	w.add(quickload);

	var quicksave = new ui.TextButton(i18nText("gamemenu.quicksave"), skin);
	quicksave.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickSave()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("gamemenu.savefailed"));
			}
		}
	));
	w.row().fill().expandX();
	w.add(quicksave);

	var load = new ui.TextButton(i18nText("gamemenu.load"), skin);
	load.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	));
	w.row().fill().expandX();
	w.add(load);

	var save = new ui.TextButton(i18nText("gamemenu.save"), skin);
	save.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_SAVE);
		}
	));
	w.row().fill().expandX().padBottom(10);
	w.add(save);

	var toTitle = new ui.TextButton(i18nText("gamemenu.return"), skin);
	toTitle.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_TITLE);
		}
	));
	w.row().fill().expandX();
	w.add(toTitle);

	var exit = new ui.TextButton(i18nText("gamemenu.exit"), skin);
	exit.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			$.exit();
		}
	));
	w.row().fill().expandX();
	w.add(exit);

	var toTitle = new ui.TextButton("TEST GAMEOVER", skin);
	toTitle.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_GAMEOVER);
		}
	));
	w.row().fill().expandX().padTop(50);
	w.add(toTitle);

	menuService.addActor(w);
	if (desktopMode) menuService.focus(resume);
}
