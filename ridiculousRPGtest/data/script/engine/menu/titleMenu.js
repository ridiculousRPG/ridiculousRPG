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
	var w = menuService.createWindow(i18nText("titlemenu.title"), skin);
	var button;

	var start = new ui.TextButton(i18nText("titlemenu.new"), skin);
	start.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
			menuService.startNewGame();
		}
	));
	w.row().fill().expandX().padBottom(10);
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
	w.row().fill().expandX();
	w.add(resume);

	button = new ui.TextButton(i18nText("titlemenu.load"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_LOAD);
		}
	));
	w.row().fill().expandX().padBottom(10);
	w.add(button);

	// Only useful for desktop mode
	if (desktopMode) {
		button = new ui.TextButton(
				$.isFullscreen() ? i18nText("titlemenu.windowed") : i18nText("titlemenu.fullscreen"), skin);
		button.addListener(new ClickAdapter(
			function (actorEv, x, y) {
				$.toggleFullscreen();
			}
		));
		w.row().fill().expandX();
		w.add(button);

		button = new ui.TextButton(i18nText("titlemenu.defaultresolution"), skin);
		button.addListener(new ClickAdapter(
			function (actorEv, x, y) {
				$.restoreDefaultResolution();
			}
		));
		w.row().fill().expandX();
		w.add(button);
	}

	button = new ui.TextButton(i18nText("titlemenu.language"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_CHANGELANG);
		}
	));
	w.row().fill().expandX();
	w.add(button);

	var button = new ui.TextButton(i18nText("titlemenu.exit"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			$.exit();
		}
	));
	w.row().fill().expandX();
	w.add(button);

	menuService.addActor(w);
	if (desktopMode) menuService.focus(start);
}
