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
// make instantiated GameBase referencable with the Dollar sign $
$ = ridiculousRPG.GameBase.$();
