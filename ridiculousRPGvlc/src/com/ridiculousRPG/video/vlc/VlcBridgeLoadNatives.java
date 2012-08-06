package com.ridiculousRPG.video.vlc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.zip.ZipException;

import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class VlcBridgeLoadNatives {
	public static void loadVlcNatives(File libDir, boolean debug)
			throws IOException {
		File osDependendDir = new File(libDir, OS_DEPENDEND_PATH_NAME());
		File vlcNativesJar = new File(osDependendDir, "vlc-natives.jar");
		if (vlcNativesJar.exists()) {
			if (debug)
				System.out.println("loading libraries from archiv "
						+ vlcNativesJar.getPath());
			loadFromArchiv(vlcNativesJar);
		} else if (debug) {
			System.out.println("archiv " + vlcNativesJar.getPath()
					+ " not found - skipping");
		}
		if (osDependendDir.exists()) {
			if (debug)
				System.out.println("loading libraries from directory "
						+ osDependendDir.getPath());
			loadFromDir(osDependendDir);
		} else if (debug) {
			System.out.println("directory " + osDependendDir.getPath()
					+ " not found - skipping");
		}
	}

	public static void loadFromArchiv(File archivName) throws ZipException,
			IOException {
		final File libvlcTmpDir = new File(
				System.getProperty("java.io.tmpdir"), archivName.getName()
						.replaceAll("\\W", "_"));
		if (!libvlcTmpDir.exists()) {
			Zipper.unzip(archivName, libvlcTmpDir);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					delDir(libvlcTmpDir);
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
		loadFromDir(libvlcTmpDir);
	}

	public static void loadFromDir(final File libvlcTmpDir) {
		File[] libList = libvlcTmpDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile()
						&& !pathname.getName().endsWith(".jar");
			}
		});
		for (int i = 0, n = libList.length; i < n; i++) {
			try {
				System.load(libList[i].getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String OS_DEPENDEND_PATH_NAME() {
		boolean isWindows = System.getProperty("os.name").contains("Windows");
		boolean isLinux = System.getProperty("os.name").contains("Linux");
		boolean isMac = System.getProperty("os.name").contains("Mac");
		boolean isAndroid = false;
		boolean is64Bit = "amd64".equals(System.getProperty("os.arch"));
		String vm = System.getProperty("java.vm.name");
		if (vm != null && vm.contains("Dalvik")) {
			isAndroid = true;
			isWindows = false;
			isLinux = false;
			isMac = false;
			is64Bit = false;
		}
		String osName;
		if (isWindows) {
			osName = TargetOs.Windows.toString().toLowerCase();
		} else if (isLinux) {
			osName = TargetOs.Linux.toString().toLowerCase();
		} else if (isMac) {
			osName = TargetOs.MacOsX.toString().toLowerCase();
		} else if (isAndroid) {
			osName = TargetOs.Android.toString().toLowerCase();
		} else {
			throw new IllegalStateException(
					"Operating system not supported, sorry!");
		}
		return osName + (is64Bit ? "64" : "32");
	}
}
