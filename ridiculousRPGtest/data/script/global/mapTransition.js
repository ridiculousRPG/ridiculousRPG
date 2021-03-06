/*
 * Transition script for tiled maps
 */
function mapTransition(mapPath, playerX, playerY, stopWeatherEffect, speed) {
	var mapLoader = ridiculousRPG.map.tiled.TiledMapWithEvents.mapLoader;
	mapLoader.startLoadMap(mapPath);
	var mapService = $.serviceProvider.getService("map");
	var trackService = $.serviceProvider.getService("cameraTrack");
	if (speed != null) {
		// Fade out
		if (mapService.map != null) {
			fadeColor(MoveFadeColorAdapter(speed, Color.BLACK, true));
		} else {
			$.gameColorTint = Color.BLACK;
		}
	}

	// Stop the weather effect
	if (stopWeatherEffect) {
		var weatherService = $.serviceProvider.getService("weather");
		if (weatherService != null) {
			weatherService.dispose();
		}
	}
	// Switch to new map
	var oldMap = mapService.loadMap(mapLoader.endLoadMap());
	setPlayerPosition(playerX, playerY, trackService, oldMap != null);

	// Store old map state and dispose the object
	if (oldMap != null) {
		mapLoader.storeMapState(oldMap, true);
	}
	if (speed != null) {
		// Fade in
		fadeColor(MoveFadeColorAdapter(speed, Color.WHITE, true));
	}
}
function fadeColor(fadeAdapter) {
	var nanoTimeOld = System.nanoTime();
	while (!fadeAdapter.finished) {
		Thread.yield(); // let other threads do their work
		var nanoTimeNew = System.nanoTime();
		fadeAdapter.tryMove(null, (nanoTimeNew - nanoTimeOld) * 1e-9, null);
		nanoTimeOld = nanoTimeNew;
	}
}
function setPlayerPosition(playerX, playerY, trackService, movePlayer) {
	var globEv = $.globalEvents.values().toArray();
	for ( var i = 0; i < globEv.length; i++) {
		var ev = globEv[i];
		if (ev.isPlayerEvent()) {
			if (movePlayer)
				ev.forceMoveTo(playerX, playerY);
			if (trackService != null && trackService.trackObj == null)
				trackService.setTrackObj(ev, true);
		}
	}
}