/*
 * This script file will be executed when the "New Game" button
 * is pressed by the user, or an other script decides that a new
 * game should be started.
 * Usually you want to load a startup map and delete the auto-save
 * state.
 */

// Clear temporary directory
$.clearTmpFiles();
// Play 3 seconds from the test card
$.serviceProvider.getService("video").play(internalFile("data/video/test.ogg"), false, 3, false);
// Display startup map
mapTransition("data/map/001dinerOutside.tmx", 0, 0, true, null);
