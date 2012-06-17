/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menu) {
	if (keycode == Keys.ESCAPE || keycode == Keys.BACK || keycode == Keys.MENU) {
		return menu.resumeLastState();
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Load menu", skin);
	var files = $.listSaveFiles();
	var button;

	var quickLoad = new ui.TextButton("Quick Load - " + generateText(files[0]), skin);
	quickLoad.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickLoad()) {
				menu.changeState(MENU_STATE_IDLE);
			} else {
				menu.showInfoFocused("Load failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(quickLoad).colspan(4);

	button = new ui.TextButton("Cancel (Esc)", skin);
	button.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			return menu.resumeLastState();
		}
	};
	w.add(button).colspan(2);

	for (var i = 1; i < files.length;) {
		w.row().fill(true, true).expand(true, false).colspan(3);
		for (var j = 0; j < 2; j++, i++) {
			button = new ui.TextButton("Load "+i+" - " + generateText(files[i]), skin);
			// button.getLabel().setAlignment(ui.Align.LEFT);
			button.clickListener = new ridiculousRPG.ui.ClickListenerExecScript(
					"var menu = $.serviceProvider.getService(\"menu\"); "
					+"if ($.loadFile("+i+")) { "
					+"	menu.changeState(MENU_STATE_IDLE); "
					+"} else { "
					+"	menu.showInfoFocused(\"Load failed!\"); "
					+"} "
				);
			w.add(button);
		}
	}

	var t = new ui.tablelayout.Table();
	w.setMovable(false);
	w.setModal(true);
	w.width(586);
	w.height(320);
	t.align(ui.Align.LEFT);
	t.add(w);
	var scroll = new ui.ScrollPane(t, skin);
	scroll.width = 600;
	scroll.height = 320;
	menu.center(scroll);
	menu.addGUIcomponent(scroll);
	menu.focus(quickLoad);
}

function generateText(fileHandle) {
	return fileHandle==null ? "EMPTY"
			: java.text.DateFormat.getDateTimeInstance().format(
					new java.util.Date(fileHandle.lastModified()));
}
