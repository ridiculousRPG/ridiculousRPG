package com.madthrax.ridiculousRPG.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.tiled.TileSet;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.tiledmappacker.TiledMapPacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameConfig;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.Initializable;

public class MapPackerService extends GameServiceDefaultImpl implements Initializable {
	private boolean initialized = false;

	@Override
	public void init() {
		if (isInitialized() || !GameBase.isGameInitialized()) return;
		File mapDir = new File(GameConfig.get().mapDir);
		File packMapDir = new File(GameConfig.get().mapPackDir);
		boolean packMaps = false;
		if (!packMapDir.exists()) {
			packMapDir.mkdirs();
			packMaps = true;
		} else {
			try {
				BufferedReader checkUpdate = new BufferedReader(new FileReader(packMapDir+"check.txt"));
				File[] tmxFiles = mapDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".tmx");
					}
				});
				Arrays.sort(tmxFiles);
				for (File tmx : tmxFiles) {
					String line = checkUpdate.readLine();
					if (line==null || !line.startsWith(tmx.getName()) || !line.endsWith(String.valueOf(tmx.lastModified()))) {
						packMaps = true;
						break;
					}
					TiledMap tmxMap = TiledLoader.createMap(Gdx.files.absolute(tmx.getAbsolutePath()));
					for (TileSet tileSet : tmxMap.tileSets) {
						File img =new File(mapDir, tileSet.imageName);
						line = checkUpdate.readLine();
						if (line==null || !line.startsWith(img.getName()) || !line.endsWith(String.valueOf(img.lastModified()))) {
							packMaps = true;
							break;
						}
					}
				}
				checkUpdate.close();
			} catch (Exception packIt) {
				packMaps = true;
			}
		}
		if (packMaps) {
			try {
				Gdx.files.absolute(packMapDir.getAbsolutePath()).deleteDirectory();
				TexturePacker.Settings settings = new TexturePacker.Settings();
				settings.defaultFormat=Format.RGBA8888;
				settings.stripWhitespace=true;
				settings.incremental=true;
				settings.alias=true;
				new TiledMapPacker().processMap(mapDir, packMapDir, settings);

				// write info-file
				PrintWriter checkUpdate = new PrintWriter(packMapDir+"check.txt");
				File[] tmxFiles = mapDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".tmx");
					}
				});
				Arrays.sort(tmxFiles);
				for (File tmx : tmxFiles) {
					checkUpdate.println(tmx.getName()+" / "+tmx.lastModified());
					TiledMap tmxMap = TiledLoader.createMap(Gdx.files.absolute(tmx.getAbsolutePath()));
					for (TileSet tileSet : tmxMap.tileSets) {
						File img =new File(mapDir, tileSet.imageName);
						checkUpdate.println(img.getName()+" / "+img.lastModified());
					}
				}
				checkUpdate.close();
			} catch (IOException e) {}
		}

		initialized = true;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
	@Override
	public void dispose() {}
}
