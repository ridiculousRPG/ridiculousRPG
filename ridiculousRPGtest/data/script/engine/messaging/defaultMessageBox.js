/*
 * This script file contains the callback function used by
 * com.ridiculousRPG.ui.MessagingService.
 */

/**
 * Called if the the commit statement is performed (or autocommit).
 * See also data/global/messaging.js for defined shortcuts
 */
function drawMessageBox(msgService, title, face, lines, boxPosition, pictures) {
	// DROP PICTURES ONTO THE SCREEN
	while (pictures.hasNext()) {
		msgService.addActor(pictures.next().getImage());
	}

	// CREATE LAYOUT WINDOW WITH AUTOMATIC SCROLLBARS AS NEEDED
	// Last parameter is the skin. It defaults to skinNormal
	var w = msgService.createWindow(title, boxPosition, false, null).top();
	w.row().pad(5);

	// FACE
	if (face != null) {
		w.add(new ui.Image(face));
	}

	// CREATE MESSAGE-LINE TABLE
	var t = new ui.Table().top();
	for (var i = 0, n = lines.size; i < n; i++) {
		t.row().fillX().expandX();
		t.add(lines.get(i).getActor());
	}
	w.add(t).fill().expand();

	// MAKE WINDOW VISIBLE AND FOCUS FIRST INPUT-FIELD ON DESKTOP
	msgService.addActor(w);
	if (desktopMode) msgService.focus(w);
}
