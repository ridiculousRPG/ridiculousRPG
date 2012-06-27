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
	var w = new ui.Window(i18nText("titlemenu.title"), skin);

	var start = new ui.TextButton(i18nText("titlemenu.new"), skin);
	start.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
			menuService.startNewGame();
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(start);

	var resume = new ui.TextButton(i18nText("titlemenu.quickload"), skin);
	resume.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("titlemenu.loadfailed"));
			}
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var load = new ui.TextButton(i18nText("titlemenu.load"), skin);
	load.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(load);


	var toggleFull = new ui.TextButton(
			$.isFullscreen() ? i18nText("titlemenu.windowed") : i18nText("titlemenu.fullscreen"), skin);
	toggleFull.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			$.toggleFullscreen();
		}
	));
	w.row().fill(true, true).expand(true, false);
	w.add(toggleFull);

	var exit = new ui.TextButton(i18nText("titlemenu.exit"), skin);
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
	menuService.focus(start);
}
