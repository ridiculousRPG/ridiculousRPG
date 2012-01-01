function mapTransition(mapPath, speed) {
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

	// Switch to new map
	var oldMap = mapService.loadMap(mapLoader.endLoadMap());
	if (trackService != null)
		trackService.setTrackObj(mapService.map.get("player"), true);
	if (oldMap != null) {
		oldMap.dispose();
		//TODO: save old map state
	}

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