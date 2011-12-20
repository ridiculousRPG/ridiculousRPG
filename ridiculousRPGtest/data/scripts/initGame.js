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

// make java.lang.System referencable with System
System = Packages.java.lang.System;
// make instantiated GameBase referencable with the Dollar sign $  
$ = Packages.com.madthrax.ridiculousRPG.GameBase.$();
