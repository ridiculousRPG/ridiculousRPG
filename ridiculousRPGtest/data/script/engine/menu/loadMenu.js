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
	var width = Math.min(600, $.screen.width);
	var height = Math.min(320, $.screen.height);
	var scrollBarWidth = 14;

	var skin = menuService.skinNormal;
	var w = new ui.Window("Load menu", skin);
	var files = $.listSaveFiles();
	// ADVANCED LIST GENERATION: listSaveFiles(int cols, int emptyTailRows, int minRows)
	// var files = $.listSaveFiles(2, 1, 10);
	var button;

	var quickLoad = generateButton("Quick Load", files[0], skin, menu);
	quickLoad.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menuService.changeState(MENU_STATE_IDLE);
			} else {
				menuService.showInfoFocused("Load failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quickLoad).colspan(4);

	button = new ui.TextButton("Cancel (Esc)", skin);
	button.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			return menuService.resumeLastState();
		}
	};
	w.add(button).colspan(2);

	/* Use this loop if you prefer a "normal" top down menu for your game
	for (var i = 1; i < files.length;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 0; j < 2; j++, i++) {
			button = generateButton("Load "+i, files[i], skin, menu);
			button.clickListener = new ridiculousRPG.ui.ClickListenerExecScript(
				 "if ($.loadFile("+i+")) { "
	*/
	for (var i = files.length-1; i > 0;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 2; j >= 0; j-=2, i--) {
			var index = (i+1-j);
			button = generateButton("Load "+index, files[index], skin, menu);
			button.clickListener = new ridiculousRPG.ui.ClickListenerExecScript(
				 "if ($.loadFile("+index+")) { "
				+"	$.serviceProvider.getService(\"menu\").changeState(MENU_STATE_IDLE); "
				+"} else { "
				+"	$.serviceProvider.getService(\"menu\").showInfoFocused(\"Load failed!\"); "
				+"} "
			);
			w.add(button);
		}
	}

	var t = new ui.tablelayout.Table();
	w.setMovable(false);
	w.setModal(true);
	w.width(width-scrollBarWidth);
	w.height(height);
	t.align(ui.Align.LEFT);
	t.add(w);
	var scroll = new ui.ScrollPane(t, skin);
	scroll.width = width;
	scroll.height = height;
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
