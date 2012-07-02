package com.madthrax.ridiculousRPG.i18n;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import com.badlogic.gdx.files.FileHandle;
import com.madthrax.ridiculousRPG.GameBase;

public class TextLoader {
	private HashMap<String, Properties> i18n = new HashMap<String, Properties>();
	private FileHandle fallbackDir;
	private FileHandle directory;
	private int filesInCacheSize;

	private static final String DEFAULT_EXTENSION = ".txt";
	private static final String NULL_STRING = "";

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
		Locale l = new Locale(directory.name());
		String checkExistence = l.getDisplayLanguage();
		if (checkExistence != null && checkExistence.length() > 0) {
			Locale.setDefault(l);
		}
		i18n.clear();
	}

	public synchronized String getText(String container, String key)
			throws IOException {
		if (container == null || key == null)
			return NULL_STRING;

		Properties p = i18n.get(container);
		if (p == null && !container.endsWith(DEFAULT_EXTENSION))
			p = i18n.get(container + DEFAULT_EXTENSION);

		if (p == null) {
			p = new Properties();
			load(container, fallbackDir, p);
			if (!fallbackDir.file().equals(directory.file()))
				load(container, directory, p);

			if (i18n.size() == filesInCacheSize) {
				i18n.clear();
			}
			i18n.put(container, p);
		}
		return p.getProperty(key, key);
	}

	private void load(String container, FileHandle langDir, Properties p)
			throws IOException {
		FileHandle fh = langDir.child(container);
		if (!fh.exists() && !container.endsWith(DEFAULT_EXTENSION)) {
			fh = langDir.child(container + DEFAULT_EXTENSION);
		}
		if (!fh.exists())
			return;
		Reader r = fh.reader(GameBase.$options().encoding);
		p.load(r);
		r.close();
	}
}
