package com.madthrax.ridiculousRPG;

import com.madthrax.ridiculousRPG.service.GameService;

public class GestureDetectorService extends
		com.badlogic.gdx.input.GestureDetector implements GameService {

	public GestureDetectorService(GestureListener listener) {
		super(listener);
	}

	@Override
	public boolean essential() {
		return true;
	}

	@Override
	public void freeze() {
	}

	@Override
	public void unfreeze() {
	}

	@Override
	public void dispose() {
	}
}
