package com.ridiculousRPG.video.vlc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;

public class VlcBridgeLoadNatives {
	public static void loadNatives() throws IOException {
		// TODO: linux32, win32, win64, mac
		String osDependendPath = "lib/linux64/vlc-natives.jar";
		String osDependendLibSuffix = ".so";
		// END _TODO

		final File libvlcDir = new File(System.getProperty("java.io.tmpdir")
				+ "/libvlc_so/");
		if (!libvlcDir.exists()) {
			UnZipper.extractFolder(new File(osDependendPath), libvlcDir);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					delDir(libvlcDir);
				}

				public void delDir(File dir) {
					File[] files = dir.listFiles();
					for (int i = files.length - 1; i >= 0; i--) {
						if (files[i].isDirectory())
							delDir(files[i]);
						else
							files[i].delete();
					}
					dir.delete();
				}
			});
		}
		final String needFinalModifier = osDependendLibSuffix;
		File[] libList = libvlcDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(needFinalModifier);
			}
		});
		for (File lib : libList) {
			System.load(lib.getAbsolutePath());
		}
		new JniGenSharedLibraryLoader("lib/vlcbridge-natives.jar")
				.load("vlcbridge");
	}
}
