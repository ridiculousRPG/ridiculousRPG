/*
 * All script files in data/scripts/global/
 * are loaded automatically in their natural order.
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
StandardMenuService = Packages.com.madthrax.ridiculousRPG.ui.StandardMenuService;
// make com.madthrax.ridiculousRPG.animations.WeatherEffectUtil referencable with WeatherEffectUtil
WeatherEffectUtil = Packages.com.madthrax.ridiculousRPG.animations.WeatherEffectUtil;
Wind = WeatherEffectUtil.Wind;
// shortcut to obtain a service
function $service(serviceType) {
	return $.$service(serviceType);
}
