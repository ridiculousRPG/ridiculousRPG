package com.madthrax.ridiculousRPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
import com.madthrax.ridiculousRPG.service.Initializable;

public class GameIconService extends GameServiceDefaultImpl implements Initializable {
	private boolean initialized = false;
	private Pixmap applIcon;

	@Override
	public void init() {
		if (isInitialized() || !GameBase.isGameInitialized()) return;
		FileHandle applIconFile = Gdx.files.internal("data/icon.png");
		if (applIconFile.exists()) {
			applIcon = new Pixmap(applIconFile);
			if (applIcon.getWidth()==applIcon.getHeight() &&
				MathUtils.isPowerOfTwo(applIcon.getWidth())) {
				try {
					Gdx.graphics.setIcon(new Pixmap[]{applIcon});
				} catch (Throwable notTooBad) {
					applIcon.dispose();
					applIcon = null;
				}
			} else {
				applIcon.dispose();
				applIcon = null;
			}
		}

		initialized = true;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
	@Override
	public void dispose() {
		if (applIcon!=null) applIcon.dispose();
	}
}
