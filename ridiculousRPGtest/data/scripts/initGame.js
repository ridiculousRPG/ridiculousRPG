/*
 * This script file is the starting point of the game.
 * See property INIT_SCRIPT in game.ini
 *
 * The default scripting engine is Mozilla Rhino.
 * See http://download.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 *
 * ATTENTION: Imports don't work across different scopes!
 */

// The very first thing on startup is loading the global scope.
// After this two step we can use all the convenience shortcuts and
// methods defined in global scope.
scriptFactory = Packages.com.madthrax.ridiculousRPG.GameBase.$scriptFactory();
scriptFactory.evalAllGlobalScripts("data/scripts/global", false);

//Allow toggling between debug and normal mode (Alt+D)
$.serviceProvider.putService("toggleDebug", new ridiculousRPG.misc.ToggleDebugModeService());
//Allow toggling between fullscreen mode and windowed mode (Alt+Enter)
$.serviceProvider.putService("toggleFullscreen", new ridiculousRPG.camera.CameraToggleFullscreenService());
//Add the DisplayFPSService to the service provider
$.serviceProvider.putService("displayFPS", new ridiculousRPG.ui.DisplayFPSService());
//Add the StandardMenuService to the service provider
$.serviceProvider.putService("menu", new ridiculousRPG.ui.StandardMenuService(internalFile("data/scripts/engine/handleMenu.js")));
//Add the MultimediaService to the service provider
$.serviceProvider.putService("video", new ridiculousRPG.video.MultimediaService());
$.serviceProvider.getService("video").play(internalFile("data/video/test.ogg"), $.screen, 1, true);
