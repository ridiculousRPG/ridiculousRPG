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

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.esotericsoftware.tablelayout.Cell;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class MessagingService extends ActorsOnStageService {

	private Rectangle boxPosition;
	private TextureRegionRef face;
	private String title;
	private float displayInfoTime = 2f;
	private Array<Message> lines = new Array<Message>();
	private IntMap<PictureRef> pictures = new IntMap<PictureRef>();
	private boolean allowNull;
	private boolean dispose;
	private Object[] resultPointer = new Object[] { null };
	private static final int FACE_MARGIN = 8;

	public MessagingService() {
		boxPosition = new Rectangle(0, 0, 0, 200);
		setAllowNull(true);
		setFadeTime(.15f);
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
		setCloseOnAction(allowNull);
	}

	public void addGUIcomponent(Object component) {
		if (component instanceof Actor)
			addActor((Actor) component);
	}

	public void center(Object obj) {
		if (obj instanceof Actor) {
			Actor actor = (Actor) obj;
			actor.setX((int) (centerX() - actor.getWidth() * .5f));
			actor.setY((int) (centerY() - actor.getHeight() * .5f));
		}
	}

	public void focus(Object guiElement) {
		if (guiElement instanceof Actor) {
			super.focus((Actor) guiElement);
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.ESCAPE) {
			if (allowNull)
				fadeOutAllActors();
			return true;
		}
		return super.keyDown(keycode);
	}

	/*
	 * box(x,y,width,height) - set preferred position, width and height for this
	 * conversations message box. (Default = 0,0,screen.width,250)
	 * face("filename",x,y,width,height) - set face for conversation.
	 * (automatically performs a commit if some text is outstanding)
	 * say("Line of text") - simply some text choice("item 1", 1) - one choice
	 * with the integer to return on click
	 * input("default value",maximum,numberInput) - text or number input. If
	 * numberInput is true, only numbers are allowed. Maximum specifies the
	 * maximum text length or the maximum value for number input. commit() -
	 * prints the message box and waits for the result. returns the result (or
	 * NULL if no result)
	 */

	// info("Text", "title") - an info box with title, which will disappear
	// automatically

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
			w.addAction(Actions.sequence(Actions.fadeIn(getFadeTime()), 
					Actions.delay(displayInfoTime, Actions.fadeOut(getFadeTime())),
					Actions.removeActor()));
			w.add(info);

			w.pack();
			center(w);
			addActor(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void info(String info) {
		showInfoNormal(info);
	}

	public void box(float x, float y, float width, float height) {
		boxPosition.x = x;
		boxPosition.y = y;
		boxPosition.width = width;
		boxPosition.height = height;
	}

	public Object face(String internalPath, int x, int y, int width, int height) {
		Object result = commit();
		TextureRegionRef tRef;
		if (internalPath == null) {
			tRef = null;
		} else if (x == 0 && y == 0 && width == 0 && height == 0) {
			tRef = TextureRegionLoader.load(internalPath);
		} else {
			tRef = TextureRegionLoader.load(internalPath, x, y, width, height);
		}
		if (face != null)
			face.dispose();
		face = tRef;
		return result;
	}

	public void putPicture(String internalPath, int x, int y, int width,
			int height, int posX, int posY, int posZkey) {
		if (internalPath == null) {
			removePicture(posZkey);
		} else {
			PictureRef pRef = new PictureRef(posX, posY);
			if (x == 0 && y == 0 && width == 0 && height == 0) {
				pRef.textureRegion = TextureRegionLoader.load(internalPath);
			} else {
				pRef.textureRegion = TextureRegionLoader.load(internalPath, x,
						y, width, height);
			}
			pRef = pictures.put(posZkey, pRef);
			if (pRef != null)
				pRef.textureRegion.dispose();
		}
	}

	public void removePicture(int posZkey) {
		if (posZkey == -1) {
			for (PictureRef pic : pictures.values()) {
				pic.textureRegion.dispose();
			}
			pictures.clear();
		} else {
			PictureRef pRef = pictures.remove(posZkey);
			if (pRef != null)
				pRef.textureRegion.dispose();
		}
	}

	public void title(String title) {
		this.title = title;
	}

	public void say(String text) {
		lines.add(new MessageText(text));
	}

	public void choice(String text, int value) {
		setAllowNull(false);
		lines.add(new MessageChoice(text, value));
	}

	public void input(String text, int maximum, boolean numeric,
			boolean password) {
		setAllowNull(false);
		lines.add(new MessageInput(text, maximum, numeric, password));
	}

	public Object commit() {
		if (dispose)
			return null;
		if (lines.size > 0
				&& GameBase.$serviceProvider().requestAttention(this, false,
						false)) {
			// TODO: An other box is showing up. At the time we have already a
			// problem if we reached this state because the new generated box
			// has already disposed the textures used by the old one, which is
			// currently showed at the screen.
			// We should extract data class with a dispose method and at every
			// commit a new instance of the data class should be assigned for
			// the user. At the end of this function the entire data class with
			// all the elements should be disposed.
			while (getActors().size > 0) {
				if (dispose) {
					GameBase.$serviceProvider().releaseAttention(this);
					return null;
				}
				Thread.yield();
			}

			resultPointer[0] = null;
			drawWindow();

			lines.clear();

			while (resultPointer[0] == null && getActors().size > 0
					&& !dispose)
				Thread.yield();
			if (!GameBase.$serviceProvider().releaseAttention(this)) {
				throw new RuntimeException(
						"Something got terribly wrong! This should never happen!");
			}
			fadeOutAllActors();
			setAllowNull(true);
			// sleep until box has disappeared
			do {
				Thread.yield();
			} while (getActors().size > 0 && !dispose);
			if (face != null) {
				face.dispose();
				face = null;
			}
			removePicture(-1);
		}
		return resultPointer[0];
	}

	private void drawWindow() {
		try {
			for (PictureRef pic : pictures.values()) {
				Image p = new Image(pic.textureRegion);
				p.setX(pic.x);
				p.setY(pic.y);
				addActor(p);
			}

			final Window w = new Window(getSkinNormal());

			if (title != null) {
				w.setTitle(title);
			}
			w.getColor().a = .1f;
			w.align(Align.top);
			w.addAction(Actions.fadeIn(getFadeTime()));
			int paddingLeft = Math.max(FACE_MARGIN,
					(int) (w.getStyle().background.getLeftWidth() + .5f));
			int textPadding = face == null ? 0 : face.getRegionWidth()
					+ FACE_MARGIN + paddingLeft
					- (int) (w.getStyle().background.getLeftWidth() + .5f);
			for (Message line : lines) {
				Cell<?> c = w.row();
				if (textPadding > 0) {
					c.padLeft(textPadding);
				}
				c.fill(true, false).expand(true, false);
				w.add(line.getActor());
			}
			computeWindowPos(w);
			w.pack();
			computeWindowPos(w);
			addActor(w);
			focus(w);

			if (face != null) {
				Image f = new Image(face);
				f.setX(w.getX() + paddingLeft);
				int paddingBottom = Math
						.max(FACE_MARGIN, (int) (w.getStyle().background
								.getBottomHeight() + .5f));
				int centerFace = (int) ((w.getHeight()
						- w.getStyle().background.getTopHeight()
						- w.getStyle().background.getBottomHeight() - face
						.getRegionHeight()) * .5f);
				f.setY(w.getY() + Math.max(paddingBottom, centerFace));
				addActor(f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void computeWindowPos(final Window w) {
		w.setX(boxPosition.x);
		w.setY(boxPosition.y);
		if (boxPosition.width == 0) {
			// width defined by margin x
			w.setWidth(GameBase.$().getScreen().width - 2 * w.getX());
		} else if (boxPosition.width < 0) {
			// bind box at the top edge of the screen
			if (w.getWidth() < -boxPosition.width) {
				// set preferred width
				w.setWidth(-boxPosition.width);
			}
			w.setX(GameBase.$().getScreen().width - w.getWidth() - w.getX());
		} else if (w.getWidth() < boxPosition.width) {
			// set preferred width
			w.setWidth(boxPosition.width);
		}
		if (boxPosition.height == 0) {
			// height defined by margin y
			w.setHeight(GameBase.$().getScreen().height - 2 * w.getY());
		} else if (boxPosition.height < 0) {
			// bind box at the right edge of the screen
			if (w.getHeight() < -boxPosition.height) {
				// set preferred height
				w.setHeight(-boxPosition.height);
			}
			w.setY(GameBase.$().getScreen().height - w.getHeight() - w.getY());
		} else if (w.getHeight() < boxPosition.height) {
			// set preferred height
			w.setHeight(boxPosition.height);
		}
		if (boxPosition.x < 0) {
			// center horizontal
			w.setX((int) (centerX() - w.getWidth() * .5f));
		}
		if (boxPosition.y < 0) {
			// center vertical
			w.setY((int) (centerY() - w.getHeight() * .5f));
		}
	}

	public void setDisplayInfoTime(float displayInfoTime) {
		this.displayInfoTime = displayInfoTime;
	}

	public float getDisplayInfoTime() {
		return displayInfoTime;
	}

	public interface Message {
		public Actor getActor();
	}

	public class MessageInput implements Message {
		private String text;
		private int maximum;
		private boolean numeric;
		private boolean password;

		public MessageInput(String text, int maximum, boolean numeric,
				boolean password) {
			if (numeric && text != null) {
				text = text.replaceAll("\\D+", "");
			}
			if (maximum < 0) {
				maximum = numeric ? 5 : 30;
			}
			this.text = text;
			this.maximum = maximum;
			this.numeric = numeric;
			this.password = password;
		}

		@Override
		public Actor getActor() {
			TextField tf = new TextField(text, getSkinNormal());
			if (password)
				tf.setPasswordMode(true);
			if (numeric) {
				tf.setTextFieldFilter(new TextFieldFilter() {
					@Override
					public boolean acceptChar(TextField textField, char key) {
						int len = 0;
						String text = textField.getText();
						if (text != null)
							len = text.length();
						return len < maximum && Character.isDigit(key);
					}
				});
			} else {
				tf.setTextFieldFilter(new TextFieldFilter() {
					@Override
					public boolean acceptChar(TextField textField, char key) {
						int len = 0;
						String text = textField.getText();
						if (text != null)
							len = text.length();
						return len < maximum;
					}
				});
			}
			tf.setTextFieldListener(new TextFieldListener() {
				@Override
				public void keyTyped(TextField textField, char key) {
					String text = textField.getText();
					if (text != null && text.trim().length() > 0
							&& (key == '\r' || key == '\n' || key == '\t')) {
						if (numeric) {
							resultPointer[0] = new Integer(text.trim());
						} else {
							resultPointer[0] = text.trim();
						}
					}
				}
			});
			return tf;
		}
	}

	public class MessageChoice implements Message {
		private String text;
		private int value;

		public MessageChoice(String text, int value) {
			this.text = text;
			this.value = value;
		}

		@Override
		public Actor getActor() {
			TextButton tb = new TextButton(text, getSkinNormal());
			tb.addListener(new ClickListener() {
				@Override
				public void clicked(ActorEvent actorEv, float x, float y) {
					resultPointer[0] = value;
				}
			});
			return tb;
		}

	}

	public class MessageText implements Message {
		private String text;

		public MessageText(String text) {
			this.text = text;
		}

		@Override
		public Actor getActor() {
			Label l = new Label(text, getSkinNormal());
			l.setWrap(true);
			return l;
		}
	}

	static class PictureRef {
		TextureRegionRef textureRegion;
		int x, y;

		public PictureRef(int posX, int posY) {
			x = posX;
			y = posY;
		}
	}

	@Override
	public void dispose() {
		dispose = true;
		super.dispose();
		if (face != null)
			face.dispose();
		removePicture(-1);
	}
}
