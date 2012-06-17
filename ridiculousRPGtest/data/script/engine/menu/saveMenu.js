/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menu) {
	if (keycode == Keys.ESCAPE) {
		return menu.resumeLastState();
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Save menu", skin);
	var files = $.listSaveFiles();
	var button;

	var quickSave = new ui.TextButton("Quick Save - " + generateText(files[0]), skin);
	quickSave.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			if ($.quickSave()) {
				menu.resumeLastState();
			} else {
				menu.showInfoFocused("Save failed!");
			}
		}
	};
	w.row().fill(true, true).expand(true, false).colspan(2);
	w.add(quickSave);

	for (var i = 1; i < files.length;) {
		w.row().fill(true, true).expand(true, false);
		for (var j = 0; j < 2; j++, i++) {
			button = new ui.TextButton("Save "+i+" - " + generateText(files[i]), skin);
			button.clickListener = new ridiculousRPG.ui.ClickListenerExecScript(
					"var menu = $.serviceProvider.getService(\"menu\"); "
					+"if ($.saveFile("+i+")) { "
					+"	menu.resumeLastState(); "
					+"} else { "
					+"	menu.showInfoFocused(\"Save failed!\"); "
					+"} "
				);
			w.add(button);
		}
	}

	
	var t = new ui.tablelayout.Table();
	w.setMovable(false);
	w.setModal(true);
	w.width(586);
	w.height(420);
	t.align(ui.Align.LEFT);
	t.add(w);
	var scroll = new ui.ScrollPane(t, skin);
	scroll.width = 600;
	scroll.height = 420;
	menu.center(scroll);
	menu.addGUIcomponent(scroll);
	menu.focus(quickSave);
}

function generateText(fileHandle) {
	return fileHandle==null ? "EMPTY"
			: java.text.DateFormat.getDateTimeInstance().format(
					new java.util.Date(fileHandle.lastModified()));
}
