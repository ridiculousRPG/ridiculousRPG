package com.ridiculousRPG.video;

import java.io.Serializable;
import java.net.URL;

import com.badlogic.gdx.math.Rectangle;

public interface VideoplayerFactory extends Serializable {
	/**
	 * Instantiates a new video player.
	 * 
	 * @param url
	 *            URL to video file
	 * @param screenBounds
	 *            The screen position, width and height
	 * @param projectToMap
	 *            Defines whether to project the video onto the map or onto the
	 *            screen coordinates
	 * @param withAudio
	 *            OPTIONAL: The player may not support this argument.<br>
	 *            If false, the audio channel will be disabled.
	 * @param drawPlaceholder
	 *            OPTIONAL: The player may not support this argument.<br>
	 *            If true and supported, the player should draw a placeholder
	 *            until the stream starts. If false, the player should not draw
	 *            anything before the method play() is called.
	 * @return An instance of a video player
	 */
	public Videoplayer createPlayer(URL url, Rectangle screenBounds,
			boolean projectToMap, boolean withAudio, boolean drawPlaceholder);
}
