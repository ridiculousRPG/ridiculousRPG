// Define some common shortcuts
System = Packages.java.lang.System;
rR = ridiculousRPG = Packages.com.ridiculousRPG;
gdx = Packages.com.badlogic.gdx;
ui = gdx.scenes.scene2d.ui;
files = gdx.Gdx.files;
Keys = gdx.Input.Keys;
ClickAdapter = ui.ClickAdapter = rR.ui.ClickAdapter;
ChangeAdapter = ui.ChangeAdapter = rR.ui.ChangeAdapter;
Rectangle = gdx.math.Rectangle;
Vector2 = gdx.math.Vector2;
WeatherEffectUtil = rR.animation.WeatherEffectUtil;
Speed = rR.util.Speed;
Direction = rR.util.Direction;
Color = gdx.graphics.Color;
Scaling = gdx.utils.Scaling;
Align = gdx.scenes.scene2d.utils.Align;

desktopMode = gdx.Gdx.app.type == gdx.Application.ApplicationType.Desktop;
$ = rR.GameBase.$();

i18nContainer="UNDEFINED";
// Returns a text from the given container and key
function i18nText(container, key) {
	if (key==null) {
		key = container;
		container = i18nContainer;
	}
	return $.getText(container, key);
}