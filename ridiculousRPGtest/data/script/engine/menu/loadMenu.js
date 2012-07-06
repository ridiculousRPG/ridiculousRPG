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
	var w = menuService.createWindow(i18nText("loadmenu.title"), 
			-1, -1, 700, 480, skin).top();

	// ADVANCED LIST GENERATION: listSaveFiles(int cols, int emptyTailRows, int minRows)
	// var files = $.listSaveFiles(2, 1, 10);
	var files = $.listSaveFiles();
	var button;

	var quickLoad = generateButton(i18nText("loadmenu.quickload"), files[1], skin, menu);
	quickLoad.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused(i18nText("loadmenu.loadfailed"));
			}
		}
	));
	w.row().fill(true, true).expand(true, false).padBottom(10);
	w.add(quickLoad).colspan(4);

	button = new ui.TextButton(i18nText("loadmenu.cancel"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			return menuService.resumeLastState();
		}
	));
	w.add(button).colspan(2);

	var failedText = i18nText("loadmenu.loadfailed");
	var buttonText = i18nText("loadmenu.load");
	/* Use this loop if you prefer a "normal" top down menu for your game
	for (var i = 2; i < files.length;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 0; j < 2; j++, i++) {
			button = generateButton(buttonText+" "+(i-1), files[i-1], skin, menu);
			button.addListener(new ClickAdapter(
				 "if ($.loadFile("+i+")) { "
	*/
	for (var i = files.length-1; i > 1;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 2; j >= 0; j-=2, i--) {
			var index = (i-j);
			button = generateButton(buttonText+" "+index, files[index], skin, menu);
			button.addListener(new ClickAdapter(
				 "if ($.loadFile("+index+")) { "
				+"	$.serviceProvider.getService(\"menu\").changeState(MENU_STATE_IDLE); "
				+"} else { "
				+"	$.serviceProvider.getService(\"menu\").showInfoFocused(\""+failedText+"\"); "
				+"} "
			));
			w.add(button);
		}
	}

	menuService.addActor(w);
	if (desktopMode) menuService.focus(quickLoad);
}

function generateButton(buttonText, zipFile, skin, menu) {
	if (zipFile==null) {
		// button.getLabel().setAlignment(ui.Align.LEFT);
		return new ui.TextButton(buttonText + " - " + i18nText("loadmenu.empty"), skin);
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
