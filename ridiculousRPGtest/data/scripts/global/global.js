/*
 * All script files in data/scripts/global/ are loaded
 * automatically in their natural order by the script initGame.js.
 * Split the file up before it becomes a monster!
 * 
 * The default scripting engine is Mozilla Rhino.
 * See http://download.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 * 
 * ATTENTION: Imports don't work across different scopes!
 */

// make java.lang.System referencable with System
System = Packages.java.lang.System;
// make shortcut for package com.madthrax.ridiculousRPG
ridiculousRPG = Packages.com.madthrax.ridiculousRPG;
// make shortcut for package com.badlogic.gdx
gdx = Packages.com.badlogic.gdx;
// make instantiated GameBase referencable with the Dollar sign $
$ = ridiculousRPG.GameBase.$();
// make shortcut for Keys
Keys = gdx.Input.Keys;
// make shortcut for gui creation
ui = gdx.scenes.scene2d.ui;

// Define the different menu states
MENU_STATE_TITLE = 1;
MENU_STATE_GAME = 2;
MENU_STATE_IDLE = 3;
MENU_STATE_PAUSE = 4;

// Convenience method to obtain internal files from pathname
function internalFile(pathName) {
	return gdx.Gdx.files.internal(pathName);
}
// Convenience method to obtain external files from pathname
function externalFile(pathName) {
	return gdx.Gdx.files.external(pathName);
}
