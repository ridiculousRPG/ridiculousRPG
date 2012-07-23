/*
 * This script file contains callback functions used by
 * com.ridiculousRPG.ui.StandardMenuService.
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
	var w = menuService.createWindow(i18nText("gameovermenu.title"), skin);

	if (files.internal("data/image/Background.png").exists()) {
		// Apply the background image
		var img = menu.createImage("data/image/Background.png", menuService.width, menuService.height);
		// Use the following scaling to keep aspect ratio on resize:
		// img.setScaling(Scaling.fit);
		menuService.addActor(img);
	}

	if (files.internal("data/image/GameOverBottom.png").exists()) {
		var img = menu.createImage("data/image/GameOverBottom.png");
		img.setPosition(20, 20);
		menuService.addActor(img);
	}

	// create particle effect
	if (files.internal("data/effect/particle/bloodDrops.effect").exists()) {
		var props = [
		    "effectFront", "data/effect/particle/bloodDrops.effect"
		]
	 	var particleEffect = new ridiculousRPG.event.EventActor(menuService.width - 150, menuService.height, props);
		menuService.addActor(particleEffect);
	}

	if (files.internal("data/image/GameOverTop.png").exists()) {
		var img = menu.createImage("data/image/GameOverTop.png");
		if (menuService.width < img.width)
			img.width = menuService.width;
		img.setScaling(Scaling.fit);
		menuService.center(img);
		img.setY(menuService.height - img.height - 25);
		menuService.addActor(img);
	}

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

	menuService.addActor(w);
	if (desktopMode) menuService.focus(quickload);
}
