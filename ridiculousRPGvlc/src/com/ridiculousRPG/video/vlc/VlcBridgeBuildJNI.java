package com.ridiculousRPG.video.vlc;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class VlcBridgeBuildJNI {
	public static void main(String[] args) throws Exception {
		new NativeCodeGenerator().generate("src", "bin", "jni");
		BuildConfig conf = new BuildConfig("vlcbridge", "target", "lib", "jni");

		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.linkerFlags += " ../../vlc/linux64/libvlc64.so";
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.linkerFlags += " ../../vlc/linux32/libvlc.so";
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.linkerFlags += " ../../vlc/windows64/libvlc64.dll";
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.linkerFlags += " ../../vlc/windows32/libvlc.dll";
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.linkerFlags += " ../../vlc/android32/libvlc.so";
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.linkerFlags += " ../../vlc/macosx32/libvlc.dylib";
		new AntScriptGenerator().generate(conf, lin64, lin32, win64, win32, android, mac);

		BuildExecutor.executeAnt("jni/build-linux64.xml", "");
		//BuildExecutor.executeAnt("jni/build.xml", "compile-natives");
	}
}
