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
var menu = new ridiculousRPG.ui.StandardMenuService();

var execScript = internalFile("data/scripts/engine/menu/titleMenu.js");
var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, true, true);
handler.background = ridiculousRPG.TextureRegionLoader.load("data/image/Title.png");
menu.putStateHandler(MENU_STATE_TITLE, handler);

var execScript = internalFile("data/scripts/engine/menu/gameoverMenu.js");
var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, true, true);
handler.background = ridiculousRPG.TextureRegionLoader.load("data/image/GameOver.png");
menu.putStateHandler(MENU_STATE_GAMEOVER, handler);

var execScript = internalFile("data/scripts/engine/menu/gameMenu.js");
var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true);
menu.putStateHandler(MENU_STATE_GAME, handler);

var execScript = internalFile("data/scripts/engine/menu/idleMenu.js");
var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, false, false, true);
menu.putStateHandler(MENU_STATE_IDLE, handler);

var execScript = internalFile("data/scripts/engine/menu/pauseMenu.js");
var handler = new ridiculousRPG.ui.MenuStateScriptAdapter(execScript, true, false, true);
menu.putStateHandler(MENU_STATE_PAUSE, handler);

$.serviceProvider.putService("menu", menu);
menu.changeState(MENU_STATE_TITLE);

//Add the MultimediaService to the service provider
$.serviceProvider.putService("video", new ridiculousRPG.video.MultimediaService());

// ONLY FOR TESTING!!! NEVER DO THIS IN YOUR GAME
// play a video on startup
$.serviceProvider.getService("video").play(internalFile("data/video/test.ogg"), $.screen, 1, true);
