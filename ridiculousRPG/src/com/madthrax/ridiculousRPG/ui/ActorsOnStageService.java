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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * @author Alexander Baumgartner
 */
public class ActorsOnStageService extends Stage implements GameService,
		Drawable, Computable, ResizeListener {
	private Skin skinNormal, skinFocused;
	private boolean closeOnAction;
	private boolean releaseAttention;

	private boolean awaitingKeyUp;
	private Actor focusedActor = null;

	private Vector2 tmp = new Vector2();
	private float fadeTime;
	private float fadeInfoTime = .3f;
	private float displayInfoTime = 2f;

	private static final int SCROLL_PIXEL_AMOUNT = 64;

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

	public synchronized void resize(int width, int height) {
		setViewport(width, height, false);
	}

	@Override
	public synchronized void addActor(Actor actor) {
		if (actor instanceof ScrollWindow) {
			actor = ((ScrollWindow) actor).obtainRoot();
			setScrollFocus(actor);
		}
		if (fadeTime > 0f) {
			actor.getColor().a = .1f;
			actor.addAction(Actions.fadeIn(fadeTime));
		}
		super.addActor(actor);
	}

	public Window createWindow(String title, float x, float y, float w,
			float h, Skin skin) {
		return createWindow(title, new Rectangle(x, y, w, h), false, skin);
	}

	public Window createWindow(String title, Skin skin) {
		return createWindow(title, new Rectangle(-1, -1, 0, 0), true, skin);
	}

	public Window createWindow(String title, Rectangle boxPosition,
			boolean autoSize, Skin skin) {
		return this.new ScrollWindow(title, boxPosition, autoSize, skin);
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

	/**
	 * Sets the fading time which is used if the actors are removed by
	 * {@link #closeOnAction}. This value is only useful if
	 * {@link #closeOnAction} is set to true.
	 * 
	 * @param fadeTime
	 *            the fading time when close is requested
	 */
	public void setFadeTime(float fadeTime) {
		this.fadeTime = fadeTime;
	}

	public float getFadeTime() {
		return this.fadeTime;
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	// @Override
	public synchronized void removeActor(Actor actor) {
		actor.clearActions();
		actor.remove();
	}

	public synchronized void compute(float deltaTime, boolean actionKeyDown) {
		act(deltaTime);
	}

	public synchronized void draw(SpriteBatch spriteBatch, Camera camera,
			boolean debug) {
		try {
			// draw onto OUR spriteBatch!!!
			if (getRoot().getChildren().size == 1
					&& getRoot().getChildren().get(0) instanceof ScrollPane) {
				((ScrollPane) getRoot().getChildren().get(0)).getChildren()
						.get(0).setY(0);
			}
			getRoot().draw(spriteBatch, 1f);
			if (debug) {
				debugTableLayout(getActors());
			}
		} catch (RuntimeException e) {
			clear();
			throw e;
		}
	}

	private void debugTableLayout(Array<Actor> actors) {
		if (actors == null)
			return;
		for (int i = 0, n = actors.size; i < n; i++) {
			Actor a = actors.get(i);
			if (a instanceof Table) {
				Table tbl = (Table) a;
				if (tbl.getDebug() != Debug.all) {
					tbl.debug(Debug.all);
					tbl.layout();
				}
			}
			if (a instanceof Group) {
				debugTableLayout(((Group) a).getChildren());
			}
		}
		Table.drawDebug(this);
	}

	public Matrix4 projectionMatrix(Camera camera) {
		if (getActors().size > 0)
			getCamera().update();
		return getCamera().combined;
	}

	@Override
	public boolean scrolled(int amount) {
		if (getScrollFocus() instanceof ScrollPane) {
			ScrollPane s = (ScrollPane) getScrollFocus();
			s.setScrollY(s.getScrollY() + SCROLL_PIXEL_AMOUNT * amount);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		// unfocus if actor is removed
		if (focusedActor != null
				&& !ActorFocusUtil.isActorOnStage(focusedActor, getRoot())) {
			setKeyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		// consume tab key down
		if (keycode == Keys.TAB) {
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
					|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
				return checkScroll(ActorFocusUtil.focusPrev(focusedActor,
						getRoot(), false, false, this)
						|| ActorFocusUtil.focusLastChild(getRoot(), this));
			}
			return checkScroll(ActorFocusUtil.focusNext(focusedActor,
					getRoot(), false, false, this)
					|| ActorFocusUtil.focusFirstChild(getRoot(), this));
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
				return checkScroll(ActorFocusUtil.focusPrev(focusedActor,
						getRoot(), true, false, this));
			case Keys.DOWN:
				return checkScroll(ActorFocusUtil.focusNext(focusedActor,
						getRoot(), true, false, this));
			case Keys.LEFT:
				return checkScroll(ActorFocusUtil.focusPrev(focusedActor,
						getRoot(), false, true, this));
			case Keys.RIGHT:
				return checkScroll(ActorFocusUtil.focusNext(focusedActor,
						getRoot(), false, true, this));
			}
		}
		return consumed;
	}

	private boolean checkScroll(boolean focusChanged) {
		if (focusChanged) {
			Actor actor = getKeyboardFocus();
			float x = 0f;
			float y = 0f;
			for (Actor a = actor; a != null; a = a.getParent()) {
				if (a.getParent() instanceof ScrollPane) {
					((ScrollPane) a.getParent()).scrollTo(x, y, actor
							.getWidth(), actor.getHeight());
					return true;
				}
				x += a.getX();
				y += a.getY();
			}
		}
		return focusChanged;
	}

	@Override
	public boolean keyUp(int keycode) {
		// unfocus if actor is removed
		if (focusedActor != null
				&& !ActorFocusUtil.isActorOnStage(focusedActor, getRoot())) {
			setKeyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		return checkFocusChanged(keyUpIntern(keycode));
	}

	public void focus(Actor actor) {
		checkScroll(ActorFocusUtil.focus(actor, false, this));
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
		if (a == null || a instanceof Window) {
			if (closeOnAction && getActors().size > 0) {
				fadeOutAllActors();
				return true;
			}
		} else {
			// unfocus if actor is removed
			if (!ActorFocusUtil.isActorOnStage(a, getRoot())) {
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
				computeTouchPoint(a, tmp);
				super.touchDown((int) tmp.x, (int) tmp.y, 0, Buttons.LEFT);
				if (a.getParent() != null)
					setKeyboardFocus(a);
				return true;
			} else {
				if (releaseAttention) {
					if (!GameBase.$serviceProvider().releaseAttention(this)) {
						return false;
					}
					releaseAttention = false;
				}
				computeTouchPoint(a, tmp);
				super.touchUp((int) tmp.x, (int) tmp.y, 0, Buttons.LEFT);
				return true;
			}
		}
		return false;
	}

	private void computeTouchPoint(Actor a, Vector2 point) {
		point.set(a.getWidth() * .5f, a.getHeight() * .5f);
		a.localToStageCoordinates(point);
		point.set(point.x * Gdx.graphics.getWidth() / getWidth(),
				(getHeight() - point.y) * Gdx.graphics.getHeight()
						/ getHeight());
	}

	public synchronized void fadeOutAllActors() {
		if (fadeTime > 0) {
			Array<Actor> aa = getActors();
			ActorFocusUtil.disableRecursive(aa);
			for (int i = aa.size - 1; i > -1; i--) {
				Actor a = aa.get(i);
				a.getColor().a -= .1f;
				a.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions
						.removeActor()));
			}
		} else {
			clear();
		}
	}

	public void center(Actor actor) {
		actor.setX((int) ((getWidth() - actor.getWidth()) * .5f));
		actor.setY((int) ((getHeight() - actor.getHeight()) * .5f));
	}

	public int centerX() {
		return (int) (getWidth() * .5f);
	}

	public int centerY() {
		return (int) (getHeight() * .5f);
	}

	public void freeze() {
	}

	public void unfreeze() {
	}

	public boolean essential() {
		return false;
	}

	@Override
	public synchronized void dispose() {
		try {
			clear();
			super.dispose();
			skinNormal.dispose();
			skinFocused.dispose();
		} catch (Exception ignored) {
		}
	}

	public void showInfoNormal(String info) {
		showInfo(getSkinNormal(), info);
	}

	public void showInfoFocused(String info) {
		showInfo(getSkinFocused(), info);
	}

	private void showInfo(final Skin skin, String info) {
		try {
			final Window w = new Window(skin);

			w.setTouchable(false);
			w.getColor().a = .1f;
			w.addAction(Actions.sequence(Actions.fadeIn(fadeInfoTime), Actions
					.delay(displayInfoTime, Actions.fadeOut(fadeInfoTime)),
					Actions.removeActor()));
			w.add(info);

			w.pack();
			center(w);
			super.addActor(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDisplayInfoTime(float displayInfoTime) {
		this.displayInfoTime = displayInfoTime;
	}

	public float getDisplayInfoTime() {
		return displayInfoTime;
	}

	public void setFadeInfoTime(float fadeInfoTime) {
		this.fadeInfoTime = fadeInfoTime;
	}

	public float getFadeInfoTime() {
		return fadeInfoTime;
	}

	/**
	 * This widget combines a ScrollPane with a Window and is able to position
	 * itself with the given parameters (boxPosition, autoSize)
	 * 
	 * @author Alexander Baumgartner
	 */
	public class ScrollWindow extends Window {
		private boolean autoSize;
		private Rectangle boxPosition;
		private ScrollPane scroll;
		private Skin skin;

		public ScrollWindow(Rectangle boxPosition, boolean autoSize, Skin skin) {
			this("", boxPosition, autoSize, skin);
		}

		public ScrollWindow(String title, Rectangle boxPosition,
				boolean autoSize, Skin skin) {
			super(title == null ? "" : title, skin == null ? skinNormal : skin);
			setMovable(true);
			setModal(true);
			getCaptureListeners().clear();
			getListeners().clear();
			addListener(new ActorListener() {
				final Vector2 dragOffset = new Vector2();
				boolean dragging;

				public boolean touchDown(ActorEvent event, float x, float y,
						int pointer, int button) {
					dragging = pointer == 0
							&& getHeight() - y <= getPadTop().height(
									ScrollWindow.this) && y < getHeight()
							&& x > 0 && x < getWidth();
					if (dragging) {
						event.stop();
						dragOffset.set(x, y);
						return true;
					}
					return false;
				}

				public void touchDragged(ActorEvent event, float x, float y,
						int pointer) {
					if (dragging)
						scroll.translate(x - dragOffset.x, y - dragOffset.y);
				}
			});
			this.autoSize = autoSize;
			this.boxPosition = boxPosition;
			this.skin = skin == null ? skinNormal : skin;
		}

		public ScrollPane obtainRoot() {
			Rectangle boxPosition = this.boxPosition;
			if (boxPosition == null)
				boxPosition = new Rectangle();
			float stageW = ActorsOnStageService.this.getWidth();
			float stageH = ActorsOnStageService.this.getHeight();

			if (autoSize) {
				pack();
				boxPosition.width = getWidth();
				boxPosition.height = getHeight();
			} else {
				if (boxPosition.width == 0) {
					// width defined by margin x
					boxPosition.width = stageW - 2 * boxPosition.x;
				} else if (boxPosition.width < 0) {
					// bind box at the right edge of the screen
					boxPosition.x = stageW - boxPosition.width - boxPosition.x;
				}

				if (boxPosition.height == 0) {
					// height defined by margin y
					boxPosition.height = stageH - 2 * boxPosition.y;
				} else if (boxPosition.height < 0) {
					// bind box at the top edge of the screen
					boxPosition.y = stageH - boxPosition.height - boxPosition.y;
				}
			}
			ScrollPane s = new ScrollPane(skin);

			if (boxPosition.x < 0 || boxPosition.x + boxPosition.width > stageW) {
				// center horizontal
				if (boxPosition.width > stageW) {
					boxPosition.width = stageW;
					boxPosition.x = 0;
					boxPosition.height += s.getStyle().hScrollKnob
							.getMinHeight();
				} else {
					boxPosition.x = centerX() - boxPosition.width * 0.5f;
				}
			}

			if (boxPosition.y < 0
					|| boxPosition.y + boxPosition.height > stageH) {
				// center vertical
				if (boxPosition.height > stageH) {
					boxPosition.height = stageH;
					boxPosition.y = 0;
					if (boxPosition.x + boxPosition.width
							+ s.getStyle().vScrollKnob.getMinWidth() < stageW)
						boxPosition.width += s.getStyle().vScrollKnob
								.getMinWidth();
				} else {
					boxPosition.y = centerY() - boxPosition.height * 0.5f;
				}
			}

			s.setFadeScrollBars(false);
			s.setWidth(boxPosition.width);
			s.setHeight(boxPosition.height);
			s.setX((int) boxPosition.x);
			s.setY((int) boxPosition.y);
			s.setWidget(this);

			scroll = s;
			return s;
		}
	}
}
