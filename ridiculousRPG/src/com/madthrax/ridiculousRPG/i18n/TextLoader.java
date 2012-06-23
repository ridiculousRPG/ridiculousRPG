package com.madthrax.ridiculousRPG.i18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.GameBase;

public class TextLoader {
	private HashMap<String, Properties> i18n = new HashMap<String, Properties>();
	private FileHandle fallbackDir;
	private FileHandle directory;
	private int filesInCacheSize;

	private static final String DEFAULT_EXTENSION = ".txt";

	public TextLoader(FileHandle directory, int filesInCacheSize) {
		this.fallbackDir = directory;
		this.directory = directory;
		this.filesInCacheSize = filesInCacheSize;
	}

	public FileHandle getDirectory() {
		return directory;
	}

	public synchronized void setDirectory(FileHandle directory) {
		this.directory = directory;
		i18n.clear();
	}

	public synchronized String getText(String container, String key)
			throws IOException {

		Properties p = i18n.get(container);
		if (p == null && !container.endsWith(DEFAULT_EXTENSION))
			p = i18n.get(container + DEFAULT_EXTENSION);

		if (p == null) {
			FileHandle fh = directory.child(container);
			if (!fh.exists()) {
				fh = fallbackDir.child(container);
			}
			if (!fh.exists() && !container.endsWith(DEFAULT_EXTENSION)) {
				fh = directory.child(container + DEFAULT_EXTENSION);
				if (!fh.exists()) {
					fh = fallbackDir.child(container + DEFAULT_EXTENSION);
				}
			}

			if (!fh.exists())
				return null;
			if (i18n.size() == filesInCacheSize) {
				i18n.clear();
			}
			p = new Properties();
			p.load(fh.reader(GameBase.$options().encoding));
			i18n.put(container, p);
		}
		return p.getProperty(key, key);
	}
}
