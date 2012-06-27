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
	var width = 700;
	var height = 480;

	var skin = menuService.skinNormal;
	var w = new ui.Window(i18nText("loadmenu.title"), skin);
	var files = $.listSaveFiles();
	// ADVANCED LIST GENERATION: listSaveFiles(int cols, int emptyTailRows, int minRows)
	// var files = $.listSaveFiles(2, 1, 10);
	var button;

	var quickLoad = generateButton(i18nText("loadmenu.quickload"), files[0], skin, menu);
	quickLoad.addListener(new ui.ClickListener() {
		clicked: function (actorEv, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("loadmenu.loadfailed"));
			}
		}
	});
	w.row().fill(true, true).expand(true, false);
	w.add(quickLoad).colspan(4);

	button = new ui.TextButton(i18nText("loadmenu.cancel"), skin);
	button.addListener(new ui.ClickListener() {
		clicked: function (actorEv, x, y) {
			return menuService.resumeLastState();
		}
	});
	w.add(button).colspan(2);

	var failedText = i18nText("loadmenu.loadfailed");
	var buttonText = i18nText("loadmenu.load");
	/* Use this loop if you prefer a "normal" top down menu for your game
	for (var i = 1; i < files.length;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 0; j < 2; j++, i++) {
			button = generateButton("Load "+i, files[i], skin, menu);
			button.addListener(new ridiculousRPG.ui.ClickListenerExecScript(
				 "if ($.loadFile("+i+")) { "
	*/
	for (var i = files.length-1; i > 0;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 2; j >= 0; j-=2, i--) {
			var index = (i+1-j);
			button = generateButton(buttonText+" "+index, files[index], skin, menu);
			button.addListener(new ridiculousRPG.ui.ClickListenerExecScript(
				 "if ($.loadFile("+index+")) { "
				+"	$.serviceProvider.getService(\"menu\").changeState(MENU_STATE_IDLE); "
				+"} else { "
				+"	$.serviceProvider.getService(\"menu\").showInfoFocused(\""+failedText+"\"); "
				+"} "
			));
			w.add(button);
		}
	}

	w.setMovable(false);
	w.setModal(true);
	w.width(width);
	w.height(height);
	var scroll = new ui.ScrollPane(w, skin);
	scroll.width = Math.min(width, $.screen.width);
	scroll.height = Math.min(height, $.screen.height);
	menuService.center(scroll);
	menuService.addGUIcomponent(scroll);
	menuService.focus(quickLoad);
}

function generateButton(buttonText, zipFile, skin, menu) {
	if (zipFile==null) {
		// button.getLabel().setAlignment(ui.Align.LEFT);
		return new ui.TextButton(buttonText + " - EMPTY", skin);
	}
	var DF = java.text.DateFormat;
	var dateText = DF.getDateTimeInstance(DF.MEDIUM, DF.SHORT).format(
					new java.util.Date(zipFile.lastModified()));
	var button = new ui.Button(skin);
	var tRef = ridiculousRPG.util.Zipper.extractCIM(zipFile,
			$.screenThumbnailName, false, true);
	if (tRef != null) {
		button.add(menu.createImage(tRef, true)).padRight(5);
	}
	button.add(buttonText + " - " + dateText).expand().fill();
	return button;
}
