package com.ridiculousRPG.video.vlc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Zipper {
	static public void unzip(File zipFile, File toDir)
			throws ZipException, IOException {
		ZipFile zip = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
		toDir.mkdir();
		byte data[] = new byte[2048];
		while (zipFileEntries.hasMoreElements()) {
			ZipEntry entry = zipFileEntries.nextElement();
			File destFile = new File(toDir, entry.getName());
			destFile.getParentFile().mkdirs();

			if (!entry.isDirectory()) {
				InputStream in = zip.getInputStream(entry);
				FileOutputStream out = new FileOutputStream(destFile);
				int currentByte;
				int len = data.length;
				while ((currentByte = in.read(data, 0, len)) != -1) {
					out.write(data, 0, currentByte);
				}
				out.close();
				in.close();
			}
		}
	}
}
