// Define some common shortcuts
System = Packages.java.lang.System;
Thread = Packages.java.lang.Thread;
ridiculousRPG = Packages.com.madthrax.ridiculousRPG;
$ = ridiculousRPG.GameBase.$();
gdx = Packages.com.badlogic.gdx;
Keys = gdx.Input.Keys;
ui = gdx.scenes.scene2d.ui;
Rectangle = gdx.math.Rectangle;
Vector2 = gdx.math.Vector2;
WeatherEffectUtil = ridiculousRPG.animation.WeatherEffectUtil;
Speed = ridiculousRPG.event.Speed;
Color = gdx.graphics.Color;

// Convenience method to obtain internal files from pathname
function internalFile(pathName) {
	return gdx.Gdx.files.internal(pathName);
}
// Convenience method to obtain external files from pathname
function externalFile(pathName) {
	return gdx.Gdx.files.external(pathName);
}
