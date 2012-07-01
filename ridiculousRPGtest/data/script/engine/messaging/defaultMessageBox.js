/*
 * This script file contains the callback function used by
 * com.madthrax.ridiculousRPG.ui.MessagingService.
 */

/**
 * Called if the the commit statement is performed (or autocommit).
 * See also data/global/messaging.js for defined shortcuts
 */
function drawMessageBox(msgService, title, face, lines, boxPosition, pictures) {
	while (pictures.hasNext()) {
		msgService.addActor(pictures.next().getImage());
	}

	// Last parameter is the skin. It defaults to skinNormal
	var w = msgService.createWindow(title, boxPosition, false, null).top();
	w.row().left().pad(5).padTop(8);

	if (face != null) {
		w.add(new ui.Image(face)).top();
	}

	var t = new ui.Table().top();
	for (var i = 0, n = lines.size; i < n; i++) {
		t.row().fillX().expandX();
		t.add(lines.get(i).getActor());
	}
	w.add(t).fill().expand();

	msgService.addActor(w);
	if (desktopMode) msgService.focus(w);
}
