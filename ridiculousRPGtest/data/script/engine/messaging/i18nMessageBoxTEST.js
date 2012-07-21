/*
 * A test MessageMox which has a customized style and automatically
 * converts all texts to the users language using a special
 * message container
 */

/*
 * This script file contains the callback function used by
 * com.ridiculousRPG.ui.MessagingService.
 */

/**
 * Called if the the commit statement is performed (or autocommit).
 * See also data/global/messaging.js for defined shortcuts
 */
function drawMessageBox(msgService, title, face, lines, boxPosition, boxAutoSize, pictures) {
	// DROP PICTURES ONTO THE SCREEN
	while (pictures.hasNext()) {
		msgService.addActor(pictures.next().getImage());
	}

	// CREATE LAYOUT TABLE
	i18nContainer = "messageText";
	var skin = msgService.skinNormal;
	var t = new ui.Table().top();
	t.setBackground(msgService.createDrawable("data/image/customDesign/WindowBackground.png"));
	t.setWidth($.screen.width);
	t.setHeight(200);
	t.setY($.screen.height-200);

	// TITLE
	t.row().fillX().expandX().pad(5);
	var l = new ui.Label(i18nText(title), skin);
	l.setColor(0.7, 0.7, 0.7, 1);
	t.add(l).colspan(2);

	// FACE
	t.row().pad(5);
	if (face != null) {
		t.add(new ui.Image(face));
	} else {
		t.add(); // empty cell because of colspan=2
	}

	// CREATE MESSAGE-LINE TABLE
	var t2 = new ui.Table().top();
	for (var i = 0, n = lines.size; i < n; i++) {
		t2.row().fillX().expandX();
		var msgLine = lines.get(i);
		// CONVERT TEXT LANGUAGE I18N
		msgLine.text = i18nText(msgLine.text);
		t2.add(msgLine.getActor());
	}

	// CREATE SCROLLPANE
	var s = new ui.ScrollPane(t2, skin);
	s.setFadeScrollBars(false);
	t.add(s).fill().expand();

	// MAKE WINDOW VISIBLE AND FOCUS FIRST INPUT-FIELD ON DESKTOP
	msgService.addActor(t);
	if (desktopMode) msgService.focus(t2);
}
