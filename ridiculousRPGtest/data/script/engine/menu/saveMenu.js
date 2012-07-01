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

	var skin = menuService.skinNormal;
	var w = menuService.createWindow(i18nText("savemenu.title"), 
			-1, -1, 700, 480, skin).top();

	// ADVANCED LIST GENERATION: listSaveFiles(int cols, int emptyTailRows, int minRows)
	// var files = $.listSaveFiles(2, 1, 10);
	var files = $.listSaveFiles();
	var button;

	var quickSave = generateButton(i18nText("savemenu.quicksave"), files[0], skin, menu);
	quickSave.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			if ($.quickSave()) {
				menuService.resumeLastState();
			} else {
				menuService.showInfoFocused(i18nText("savemenu.savefailed"));
			}
		}
	));
	w.row().fill(true, true).expand(true, false).padBottom(10);
	w.add(quickSave).colspan(4);

	button = new ui.TextButton(i18nText("savemenu.cancel"), skin);
	button.addListener(new ClickAdapter(
		function (actorEv, x, y) {
			return menuService.resumeLastState();
		}
	));
	w.add(button).colspan(2);

	var failedText = i18nText("savemenu.savefailed");
	var buttonText = i18nText("savemenu.save");
	/* Use this loop if you prefer a "normal" top down menu for your game
	for (var i = 1; i < files.length;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 0; j < 2; j++, i++) {
			button = generateButton("Save "+i, files[i], skin, menu);
			button.addListener(new ClickAdapter(
				 "if ($.saveFile("+i+")) { "
	*/
	for (var i = files.length-1; i > 0;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 2; j >= 0; j-=2, i--) {
			var index = (i+1-j);
			button = generateButton(buttonText+" "+index, files[index], skin, menu);
			button.addListener(new ClickAdapter(
				 "if ($.saveFile("+index+")) { "
				+"	$.serviceProvider.getService(\"menu\").resumeLastState(); "
				+"} else { "
				+"	$.serviceProvider.getService(\"menu\").showInfoFocused(\""+failedText+"\"); "
				+"} "
			));
			w.add(button);
		}
	}

	menuService.addActor(w);
	if (desktopMode) menuService.focus(quickSave);
}

function generateButton(buttonText, zipFile, skin, menu) {
	if (zipFile==null) {
		// button.getLabel().setAlignment(ui.Align.LEFT);
		return new ui.TextButton(buttonText + " - " + i18nText("savemenu.empty"), skin);
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
