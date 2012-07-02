/*
 * Copyright 2011 Alexander Baumgartner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.madthrax.ridiculousRPG.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * @author Alexander Baumgartner
 */
public class Zipper {
	static final int BUFFER = 4096;

	/**
	 * Unzips one zip-file into the specified directory
	 * 
	 * @param fromFile
	 * @param toDirectory
	 * @throws ZipException
	 * @throws IOException
	 */
	public static void unzip(FileHandle fromFile, FileHandle toDirectory)
			throws ZipException, IOException {
		ZipFile zipfile = new ZipFile(fromFile.file());
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			InputStream in = zipfile.getInputStream(entry);
			toDirectory.child(entry.getName()).write(in, false);
			in.close();
		}
		zipfile.close();
	}

	/**
	 * Packs all files from the specified directory into one zip-file
	 * 
	 * @param fromDirectory
	 * @param toFile
	 * @throws IOException
	 */
	public static void zip(FileHandle fromDirectory, FileHandle toFile)
			throws IOException {
		byte data[] = new byte[BUFFER];
		ZipOutputStream out = new ZipOutputStream(toFile.write(false));
		for (FileHandle file : fromDirectory.list()) {
			out.putNextEntry(new ZipEntry(file.name()));
			InputStream in = file.read(BUFFER);
			int count;
			while ((count = in.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			in.close();
		}
		out.close();
	}

	public static byte[] extractFiledata(FileHandle zipContainer,
			String fileName) throws ZipException, IOException {
		ZipFile z = new ZipFile(zipContainer.file());
		ZipEntry e = z.getEntry(fileName);
		if (e == null)
			return null;
		InputStream in = z.getInputStream(e);
		ByteArrayOutputStream b = new ByteArrayOutputStream(BUFFER * 32);
		byte[] data = new byte[BUFFER];
		int len;
		while ((len = in.read(data)) != -1) {
			b.write(data, 0, len);
		}
		in.close();
		z.close();
		return b.toByteArray();
	}

	public static TextureRegionRef extractCIM(FileHandle zipFile,
			String cimFile, boolean flipX, boolean flipY) {
		try {
			byte[] buf = Zipper.extractFiledata(zipFile, cimFile);
			if (buf == null)
				return null;
			Pixmap pix = PixmapIO.readCIM(new InputStreamFileHandle(
					new ByteArrayInputStream(buf)));
			buf = null;
			TextureRegionRef tRef = TextureRegionLoader.obtainEmptyRegion(pix
					.getWidth(), pix.getHeight(), pix.getFormat());
			tRef.draw(pix);
			tRef.flip(flipX, flipY);
			pix.dispose();
			return tRef;
		} catch (Exception e) {
			GameBase.$error("Zipper.extractCIM",
					"Failed to extract texture from compressed file", e);
			return null;
		}
	}
}