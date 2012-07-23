/*
 * This script file contains callback functions used by
 * com.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menuService, menu) {
	if (keycode == Keys.ESCAPE || keycode == Keys.BACK || keycode == Keys.MENU) {
		return menuService.resumeLastState();
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menuService, menu) {
	i18nContainer = "engineMenuText";

	var skin = menuService.skinNormal;
	// The chained function top() changes the positioning of the childs
	// to the top of the window. Default is center
	// Just try removing it or change it to bottom() and see what happens ;)
	var w = menuService.createWindow(i18nText("changelangmenu.title"), 
				-1, -1, 260, 300, skin).top();

	var cancel = new ui.TextButton(i18nText("changelangmenu.cancel"), skin);
	cancel.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			return menuService.resumeLastState();
		}
	));
	w.row().fill().expandX().padBottom(10);
	w.add(cancel);

	var fList = files.internal($.options.i18nPath).list();
	for (var i = 0; i < fList.length; i++) {
		var f = fList[i];
		if (f.isDirectory() && !f.name().startsWith(".")) {
			var text = new java.util.Locale(f.name()).displayLanguage;
			var button = new ui.TextButton(text, skin);
			button.addListener(new ClickAdapter(
				 "$.setLanguageISO(\""+f.name()+"\");"
				+"$.serviceProvider.getService(\"menu\").resumeLastState();"
			));
			w.row().fill().expandX();
			w.add(button);
		}
	}

	menuService.addActor(w);
	if (desktopMode) menuService.focus(cancel);
}
