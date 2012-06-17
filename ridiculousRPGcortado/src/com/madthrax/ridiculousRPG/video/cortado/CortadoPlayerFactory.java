package com.madthrax.ridiculousRPG.video.cortado;

import java.net.URL;

import com.badlogic.gdx.math.Rectangle;
import com.madthrax.ridiculousRPG.video.Videoplayer;
import com.madthrax.ridiculousRPG.video.VideoplayerFactory;

public class CortadoPlayerFactory implements VideoplayerFactory {
	private static final long serialVersionUID = 1L;

	@Override
	public Videoplayer createPlayer(URL url, Rectangle screenBounds,
			boolean projectToMap, boolean withAudio, boolean drawPlaceholder) {
		return new CortadoPlayerAppletWrapper(url, screenBounds, projectToMap,
				withAudio, drawPlaceholder);
	}
}
