// Define some common shortcuts
System = Packages.java.lang.System;
Thread = Packages.java.lang.Thread;
ridiculousRPG = Packages.com.madthrax.ridiculousRPG;
$ = ridiculousRPG.GameBase.$();
gdx = Packages.com.badlogic.gdx;
Keys = gdx.Input.Keys;
ui = gdx.scenes.scene2d.ui;
ClickAdapter = com.ridiculousRPG.ui.ClickAdapter;
ChangeAdapter = com.ridiculousRPG.ui.ChangeAdapter;
Rectangle = gdx.math.Rectangle;
Vector2 = gdx.math.Vector2;
WeatherEffectUtil = ridiculousRPG.animation.WeatherEffectUtil;
Speed = ridiculousRPG.util.Speed;
Direction = ridiculousRPG.util.Direction;
Color = gdx.graphics.Color;
desktopMode = gdx.Gdx.app.type == gdx.Application.ApplicationType.Desktop;

// Convenience method to obtain internal files from pathname
function internalFile(pathName) {
	return gdx.Gdx.files.internal(pathName);
}
// Convenience method to obtain external files from pathname
function externalFile(pathName) {
	return gdx.Gdx.files.external(pathName);
}

i18nContainer="UNDEFINED";
// Returns a text from the given container and key
function i18nText(container, key) {
	if (key==null) {
		key = container;
		container = i18nContainer;
	}
	return $.getText(container, key);
}