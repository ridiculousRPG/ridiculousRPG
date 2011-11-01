package com.madthrax.ridiculousRPG.events;

import com.badlogic.gdx.graphics.TextureDict;
import com.badlogic.gdx.graphics.TextureRef;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;


public class Faceset implements Disposable {
	private TextureRef faceset;
	private TextureRegion[][] faces; // [row][col]

	public Faceset(String path, int faceWidth, int faceHeight, int anzRows, int anzCols) {
		setFaceset(path, faceWidth, faceHeight, anzRows, anzCols);
	}

	public void setFaceset(String path, int faceWidth, int faceHeight, int anzRows, int anzCols) {
		if (faceset!=null) faceset.unload();
		faceset = TextureDict.loadTexture(path, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		TextureRegion region = new TextureRegion(faceset.get(), 0, 0, faceWidth*anzCols, faceHeight*anzRows);
		faces = region.split(faceWidth, faceHeight);
	}
	public TextureRegion getFace(int row, int col) {
		return faces[row][col];
	}
	@Override
	public void dispose() {
		if (faceset!=null) faceset.unload();
	}
}
