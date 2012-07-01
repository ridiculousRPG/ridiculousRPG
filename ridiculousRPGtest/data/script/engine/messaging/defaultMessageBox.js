/*
 * This script file contains the callback function used by
 * com.madthrax.ridiculousRPG.ui.MessagingService.
 */

/**
 * Called if the the commit statement is performed (or autocommit).
 * See also data/global/messaging.js for defined shortcuts
 */
function drawMessageBox(msg, title, face, lines, boxPosition, pictures) {
	while (pictures.hasNext()) {
		msg.addActor(pictures.next().getImage());
	}

	var w = new ui.Window(msg.getSkinNormal());
	if (title != null) {
		w.setTitle(title);
	}
	w.top();
	w.row().left().pad(5);

	if (face != null) {
		w.add(new ui.Image(face)).top();
	}

	var t = new ui.Table();
	t.top();
	for (var i = 0, n = lines.size; i < n; i++) {
		t.row().fill(true, false).expand(true, false);
		t.add(lines.get(i).getActor());
	}
	w.add(t).fill().expand();

	var scroll = new ui.ScrollPane(w, msg.getSkinNormal());
	scroll.setFadeScrollBars(false);
	computeWindowPos(w, scroll, boxPosition, msg.centerX(), msg.centerY());
	msg.addActor(scroll);
	if (desktopMode) msg.focus(w);
}

function computeWindowPos(w, s, boxPosition, centerX, centerY) {
	w.pack();
	s.setWidth(w.getWidth());
	s.setHeight(w.getHeight());
	s.setX(boxPosition.x);
	s.setY(boxPosition.y);

	if (boxPosition.width == 0) {
		// width defined by margin x
		s.setWidth($.screen.width - 2 * boxPosition.x);
	} else if (boxPosition.width < 0) {
		// bind box at the top edge of the screen
		if (s.getWidth() < -boxPosition.width) {
			// set preferred width
			s.setWidth(-boxPosition.width);
		}
		s.setX($.screen.width - s.getWidth() - s.getX());
	} else if (s.getWidth() < boxPosition.width) {
		// set preferred width
		s.setWidth(boxPosition.width);
	}

	if (boxPosition.height == 0) {
		// height defined by margin y
		s.setHeight($.screen.height - 2 * boxPosition.y);
	} else if (boxPosition.height < 0) {
		// bind box at the right edge of the screen
		if (s.getHeight() < -boxPosition.height) {
			// set preferred height
			s.setHeight(-boxPosition.height);
		}
		s.setY($.screen.height - s.getHeight() - s.getY());
	} else if (s.getHeight() < boxPosition.height) {
		// set preferred height
		s.setHeight(boxPosition.height);
	}

	if (s.getX() < 0 || s.getX() + s.getWidth() > $.screen.width) {
		// center horizontal
		if (s.getWidth() > $.screen.width)
			s.setWidth($.screen.width);
		s.setX(centerX - s.getWidth() * 0.5);
	}

	if (s.getY() < 0 || s.getY() + s.getHeight() > $.screen.height) {
		if (s.getHeight() > $.screen.height)
			s.setHeight($.screen.height);
		// center vertical
		s.setY(centerY - s.getHeight() * 0.5);
	}
}
