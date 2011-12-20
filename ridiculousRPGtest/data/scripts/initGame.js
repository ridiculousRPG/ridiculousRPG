/*
 * This script file is the starting point of the game.
 * See property INIT_SCRIPT in game.ini
 *
 * Split the file up before it becomes a monster!
 *
 * The default scripting engine is Mozilla Rhino.
 * See http://download.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 *
 * ATTENTION: Imports don't work across different scopes!
 */

// The very first thing on startup is loading the global scope.
// After this two step we can use all the convenience shortcuts and
// methods defined as global scope.
$scriptFactory = Packages.com.madthrax.ridiculousRPG.GameBase.$scriptFactory();
$scriptFactory.evalAllGlobalScripts("data/scripts/global", false);

//Add the DisplayFPSService to the service provider
$serviceProvider.putService("fpsService", new ridiculousRPG.ui.DisplayFPSService());
//Add the StandardMenuService to the service provider
$serviceProvider.putService("menuService", new ridiculousRPG.ui.StandardMenuService());
//Add the ImageProjectionService to the service provider (used by StandardMenuService)
$serviceProvider.putService("imageProjectionService", ridiculousRPG.animation.ImageProjectionService.$background);
