package com.ridiculousRPG.video.vlc;

import java.io.IOException;

public class VlcBridgeTestMain {

	/**
	 * IMPORTANT: Before running this tests you need to execute
	 * {@link VlcBridgeBuildJNI} which generates all the needed JNI code.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		VlcBridgeLoadNatives.loadNatives();
		VlcBridge vlc = new VlcBridge();
		//vlc.setDebug(true);
		vlc.startBackgroundVlc();
	}
}
