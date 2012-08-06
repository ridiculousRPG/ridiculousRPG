package com.ridiculousRPG.video.vlc;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class VlcBridgeBuildJNI {
	public static void main(String[] args) throws Exception {
		// generate native code
		new NativeCodeGenerator().generate("src", "bin", "jni");

		// generate build scripts
		BuildTarget target = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		target.compilerPrefix = "";
		target.linkerFlags += " ../../../lib/linux64/libvlc64.so";
		BuildConfig conf = new BuildConfig("vlcbridge", "target", "lib", "jni");
		// TODO: generate build scripts for other OS
		new AntScriptGenerator().generate(conf, target);

		// exec build.xml
		BuildExecutor.executeAnt("jni/build.xml", "");
	}
}
