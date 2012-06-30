/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
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
	var width = 260;
	var height = 300;

	var skin = menuService.skinNormal;
	var w = new ui.Window(i18nText("changelangmenu.title"), skin);

	var cancel = new ui.TextButton(i18nText("changelangmenu.cancel"), skin);
	cancel.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			return menuService.resumeLastState();
		}
	));
	w.row().fill(true, true).expand(true, false).padBottom(10);
	w.add(cancel);

	var files = internalFile($.options.i18nPath).list();
	for (var i = 0; i < files.length; i++) {
		var f = files[i];
		if (f.isDirectory() && !f.name().startsWith(".")) {
			var text = new java.util.Locale(f.name()).displayLanguage;
			var button = new ui.TextButton(text, skin);
			button.addListener(new ClickAdapter(
				 "$.setLanguageISO(\""+f.name()+"\");"
				+"$.serviceProvider.getService(\"menu\").resumeLastState();"
			));
			w.row().fill(true, true).expand(true, false);
			w.add(button);
		}
	}

	var scroll = new ui.ScrollPane(w.top(), skin);
	scroll.setFadeScrollBars(false);
	scroll.width = Math.min(width, $.screen.width);
	scroll.height = Math.min(height, $.screen.height);
	menuService.center(scroll);
	menuService.addGUIcomponent(scroll);
	if (desktopMode) menuService.focus(cancel);
}
