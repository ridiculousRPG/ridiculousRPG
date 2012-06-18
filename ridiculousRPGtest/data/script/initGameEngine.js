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
scriptFactory.evalAllGlobalScripts("data/script/global", false);

//It's highly recommended to comment this line out for production release.
new ridiculousRPG.map.OnChangeMapPacker("work/map", "data/map").packOnChange();
//Set background from pitch black to dark gray
$.backgroundColor = new gdx.graphics.Color(0x16/0xFF, 0x16/0xFF, 0x16/0xFF, 1);

//Allow user to zoom in / out
$.serviceProvider.putService("cameraZoom", new ridiculousRPG.camera.CameraZoomService());
//Add the CameraTrackMovableService to the service provider
$.serviceProvider.putService("cameraTrack", new ridiculousRPG.camera.CameraTrackMovableService());
//Allow toggling between fullscreen mode and windowed mode (Alt+Enter)
$.serviceProvider.putService("toggleFullscreen", new ridiculousRPG.camera.CameraToggleFullscreenService());
//Allow toggling between debug and normal mode (Alt+D)
$.serviceProvider.putService("toggleDebug", new ridiculousRPG.misc.ToggleDebugModeService());
//Add the DisplayFPSService to the service provider
$.serviceProvider.putService("displayFPS", new ridiculousRPG.ui.DisplayFPSService());
//Add the StandardMenuService to the service provider
$.serviceProvider.putService("menu", new ridiculousRPG.ui.StandardMenuService());
//Add the StandardMenuService to the service provider
$.serviceProvider.putService("messaging", new ridiculousRPG.ui.MessagingService());
//Add the MapRenderService to the service provider
$.serviceProvider.putService("weather", new ridiculousRPG.animation.ParticleEffectService());
//Add the MultimediaService to the service provider
$.serviceProvider.putService("map", new ridiculousRPG.map.MapRenderService());
//Add the MultimediaService to the service provider
$.serviceProvider.putService("video", new ridiculousRPG.video.MultimediaService(new ridiculousRPG.video.cortado.CortadoPlayerFactory()));

initMenu($.serviceProvider.getService("menu"));

function initMenu(menu) {
	var execScript = internalFile("data/script/engine/menu/titleMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, true, true, false, false);
	handler.background = ridiculousRPG.util.TextureRegionLoader.load("data/image/Title.png");
	menu.putStateHandler(MENU_STATE_TITLE, handler);

	var execScript = internalFile("data/script/engine/menu/gameoverMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, true, true, false, false);
	handler.background = ridiculousRPG.util.TextureRegionLoader.load("data/image/GameOver.png");
	menu.putStateHandler(MENU_STATE_GAMEOVER, handler);

	var execScript = internalFile("data/script/engine/menu/gameMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true, true, false);
	menu.putStateHandler(MENU_STATE_GAME, handler);

	var execScript = internalFile("data/script/engine/menu/idleMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, false, false, true, false, true);
	menu.putStateHandler(MENU_STATE_IDLE, handler);

	var execScript = internalFile("data/script/engine/menu/pauseMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true, true, true);
	menu.putStateHandler(MENU_STATE_PAUSE, handler);

	var execScript = internalFile("data/script/engine/menu/loadMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true, true, true);
	menu.putStateHandler(MENU_STATE_LOAD, handler);

	var execScript = internalFile("data/script/engine/menu/saveMenu.js");
	var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true, true, true);
	menu.putStateHandler(MENU_STATE_SAVE, handler);

	menu.setStartNewGameScript("data/script/game/startNewGame.js");
	menu.changeState(MENU_STATE_TITLE);
}
