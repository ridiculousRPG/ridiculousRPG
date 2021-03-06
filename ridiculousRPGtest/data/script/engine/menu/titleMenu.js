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
	var w = menuService.createWindow(i18nText("titlemenu.title"), skin);
	var button;

	if (files.internal("data/image/Background.png").exists()) {
		// Apply the background image
		var img = menu.createImage("data/image/Background.png", menuService.width, menuService.height);
		// Use the following scaling to keep aspect ratio on resize:
		// img.setScaling(Scaling.fit);
		menuService.addActor(img);
	}

	if (files.internal("data/image/TitleBottom.png").exists()) {
		var img = menu.createImage("data/image/TitleBottom.png");
		if (menuService.width < img.width)
			img.width = menuService.width;
		img.setScaling(Scaling.fit);
		menuService.center(img);
		img.setY(15);
		menuService.addActor(img);
	}

	if (files.internal("data/image/TitleTop.png").exists()) {
		var img = menu.createImage("data/image/TitleTop.png");
		if (menuService.width < img.width)
			img.width = menuService.width;
		img.setScaling(Scaling.fit);
		menuService.center(img);
		img.setY(menuService.height - img.height - 25);
		menuService.addActor(img);
	}

	// create particle effect
	if (files.internal("data/effect/particle/greenMixed.effect").exists()) {
		var props = [
		    "effectFront", "data/effect/particle/greenMixed.effect"
		]
		var particleEffect = new ridiculousRPG.event.EventActor(50, 200, props);
		menuService.addActor(particleEffect);
	}
	if (files.internal("data/effect/particle/greenStars3.effect").exists()) {
		var props = [
	 	    "effectFront", "data/effect/particle/greenStars3.effect"
	 	]
	 	var particleEffect = new ridiculousRPG.event.EventActor(menuService.width - 100, menuService.height - 50, props);
	 	menuService.addActor(particleEffect);
	}

	var start = new ui.TextButton(i18nText("titlemenu.new"), skin);
	start.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.startNewGame();
			menuService.changeState(MENU_STATE_IDLE);
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
	w.row().fill().expandX().padBottom(10);
	w.add(button);

	var button = new ui.TextButton(i18nText("titlemenu.exit"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			$.exit();
		}
	));
	w.row().fill().expandX();
	w.add(button);

	/* REMOVE THIS MENUPOINT WHEN THE DESIGN FOR THE GAMEOVER SCREEN IS DONE*/
	var toTitle = new ui.TextButton("TEST GAMEOVER SCREEN", skin);
	toTitle.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_GAMEOVER);
		}
	));
	w.row().fill().expandX().padTop(20);
	w.add(toTitle);

	menuService.addActor(w);
	if (desktopMode) menuService.focus(start);
}
