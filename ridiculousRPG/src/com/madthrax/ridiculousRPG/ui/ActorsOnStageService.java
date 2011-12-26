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

package com.madthrax.ridiculousRPG.ui;

import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;

/**
 * @author Alexander Baumgartner
 */
public class ActorsOnStageService extends Stage implements GameService,
		Drawable, Computable {
	private Skin skinNormal, skinFocused;
	private boolean closeOnAction;
	private boolean releaseAttention;

	private boolean awaitingKeyUp;
	private Actor focusedActor = null;

	public ActorsOnStageService() {
		super(GameBase.$().getScreen().width, GameBase.$().getScreen().height,
				true, GameBase.$().getSpriteBatch());
		skinNormal = new Skin(Gdx.files
				.internal(GameBase.$options().uiSkinNormalConfig), Gdx.files
				.internal(GameBase.$options().uiSkinNormalImage));
		skinFocused = new Skin(Gdx.files
				.internal(GameBase.$options().uiSkinFocusConfig), Gdx.files
				.internal(GameBase.$options().uiSkinFocusImage));
	}

	/**
	 * @return the normal skin which is used for all objects except the one
	 *         which holds the keyboard focus
	 */
	public Skin getSkinNormal() {
		return skinNormal;
	}

	/**
	 * @param skinNormal
	 *            normal skin which is used for all objects except the one which
	 *            holds the keyboard focus
	 */
	public void setSkinNormal(Skin skinNormal) {
		this.skinNormal = skinNormal;
	}

	/**
	 * @return the skin which is used for the objects which holds the keyboard
	 *         focus
	 */
	public Skin getSkinFocused() {
		return skinFocused;
	}

	/**
	 * @param skinFocused
	 *            the skin which is used for the objects which holds the
	 *            keyboard focus
	 */
	public void setSkinFocused(Skin skinFocused) {
		this.skinFocused = skinFocused;
	}

	/**
	 * @return true if all windows will be closed when pressing the action key
	 */
	public boolean isCloseOnAction() {
		return closeOnAction;
	}

	/**
	 * @param closeOnAction
	 *            true if all windows should be closed when pressing the action
	 *            key
	 */
	public void setCloseOnAction(boolean closeOnAction) {
		this.closeOnAction = closeOnAction;
	}

	public void compute(float deltaTime, boolean actionKeyDown) {
		act(deltaTime);
	}

	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		getCamera().update();
		super.root.draw(spriteBatch, 1f);
	}

	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}

	@Override
	public boolean keyDown(int keycode) {
		// unfocus if actor is removed
		if (focusedActor != null
				&& !ActorFocusUtil.isActorOnStage(focusedActor, root)) {
			setKeyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		// consume tab key down
		if (keycode == Keys.TAB) {
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
					|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
				return ActorFocusUtil.focusPrev(focusedActor, root, false,
						false, this)
						|| ActorFocusUtil.focusLastChild(root, this);
			}
			return ActorFocusUtil.focusNext(focusedActor, root, false, false,
					this)
					|| ActorFocusUtil.focusFirstChild(root, this);
		}
		// alowed childs to consume key down
		boolean consumed = super.keyDown(keycode);
		if (!consumed && !awaitingKeyUp) {
			switch (keycode) {
			case Keys.SPACE:
			case Keys.ENTER:
				return (awaitingKeyUp = actionKeyPressed(true));
			case Keys.ESCAPE:
				if (focusedActor != null) {
					setKeyboardFocus(null);
				}
				return false;
			case Keys.UP:
				return ActorFocusUtil.focusPrev(focusedActor, root, true,
						false, this);
			case Keys.DOWN:
				return ActorFocusUtil.focusNext(focusedActor, root, true,
						false, this);
			case Keys.LEFT:
				return ActorFocusUtil.focusPrev(focusedActor, root, false,
						true, this);
			case Keys.RIGHT:
				return ActorFocusUtil.focusNext(focusedActor, root, false,
						true, this);
			}
		}
		return consumed;
	}

	@Override
	public boolean keyUp(int keycode) {
		// unfocus if actor is removed
		if (focusedActor != null
				&& !ActorFocusUtil.isActorOnStage(focusedActor, root)) {
			setKeyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		return checkFocusChanged(keyUpIntern(keycode));
	}

	public void focus(Actor actor) {
		ActorFocusUtil.focus(actor, false, this);
		checkFocusChanged(false);
	}

	private boolean checkFocusChanged(boolean consumed) {
		if (focusedActor != getKeyboardFocus()) {
			if (focusedActor != null)
				changeSkin(focusedActor, skinNormal);
			focusedActor = getKeyboardFocus();
			if (focusedActor != null)
				changeSkin(focusedActor, skinFocused);
		}
		return consumed;
	}

	public static void changeSkin(Actor actor, Skin newSikn) {
		try {
			Class<?> c = ActorFocusUtil.styleGetter(actor.getClass())
					.getReturnType();
			Method m = ActorFocusUtil.styleSetter(actor.getClass(), c);
			if (m != null)
				m.invoke(actor, newSikn.getStyle(c));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean keyUpIntern(int keycode) {
		if (keycode == Keys.TAB) {
			return false;
		} else if (awaitingKeyUp) {
			switch (keycode) {
			case Keys.SPACE:
			case Keys.ENTER:
				awaitingKeyUp = false;
				actionKeyPressed(false);
				return true;
			}
		}
		return super.keyUp(keycode);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean consumed = super.touchDown(x, y, pointer, button);
		if (!consumed && !awaitingKeyUp
				&& (pointer == 1 || button == Buttons.RIGHT)
				&& focusedActor == null) {
			return (awaitingKeyUp = actionKeyPressed(true));
		}
		return consumed;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return checkFocusChanged(touchUpIntern(x, y, pointer, button));
	}

	private boolean touchUpIntern(int x, int y, int pointer, int button) {
		if (awaitingKeyUp && (pointer == 1 || button == Buttons.RIGHT)) {
			awaitingKeyUp = false;
			actionKeyPressed(false);
			return true;
		}
		return super.touchUp(x, y, pointer, button);
	}

	private boolean actionKeyPressed(boolean down) {
		Actor a = focusedActor;
		if (a == null) {
			if (closeOnAction)
				clear();
		} else {
			// unfocus if actor is removed
			if (!ActorFocusUtil.isActorOnStage(a, root)) {
				setKeyboardFocus(null);
				focusedActor = null;
				awaitingKeyUp = false;
				return actionKeyPressed(down);
			}
			// simulate touch event
			if (down) {
				if (GameBase.$serviceProvider().queryAttention() != this) {
					if (!GameBase.$serviceProvider().requestAttention(this,
							false, false)) {
						return false;
					}
					releaseAttention = true;
				}
				a.touchDown(a.width / 2, a.height / 2, 0);
				if (a.parent != null)
					setKeyboardFocus(a);
				return true;
			} else {
				if (releaseAttention) {
					if (!GameBase.$serviceProvider().releaseAttention(this)) {
						return false;
					}
					releaseAttention = false;
				}
				a.touchUp(a.width / 2, a.height / 2, 0);
				return true;
			}
		}
		return false;
	}

	public void freeze() {
	}

	public void unfreeze() {
	}

	public boolean essential() {
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		skinNormal.dispose();
	}
}
