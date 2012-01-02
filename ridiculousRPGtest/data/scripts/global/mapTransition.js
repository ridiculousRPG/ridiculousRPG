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
			fadeColor(MoveFadeColorAdapter.$(speed, Color.BLACK, true));
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
	var player = findPlayer(mapService, trackService);
	if (oldMap != null) {
		if (player != null)
			player.forceMoveTo(playerX, playerY);
		oldMap.dispose(true);
		//TODO: save old map state
	}
	if (trackService != null)
		trackService.setTrackObj(player, true);

	if (speed != null) {
		// Fade in
		fadeColor(MoveFadeColorAdapter.$(speed, Color.WHITE, true));
	}
}
function fadeColor(fadeAdapter) {
	var nanoTimeOld = System.nanoTime();
	while(!fadeAdapter.finished) {
		Thread.yield(); // let other threads do their work
		var nanoTimeNew = System.nanoTime();
		fadeAdapter.tryMove(null, (nanoTimeNew-nanoTimeOld) * 1e-9 );
		nanoTimeOld = nanoTimeNew;
	}
}
function findPlayer(mapService, trackService) {
	if (trackService==null || trackService.trackObj==null) {
		return mapService.map.get("player");
	}
	return trackService.trackObj;
}