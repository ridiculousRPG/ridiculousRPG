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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class MessagingService extends ActorsOnStageService implements
		Serializable {
	private static final long serialVersionUID = 1L;

	// Only the most important state informations will be serialized.
	private Rectangle boxPosition;
	private String title;
	private String callBackScript;
	private String callBackFunction = "drawMessageBox";
	private boolean allowNull = true;

	private transient Invocable scriptEngine;
	private transient TextureRegionRef face;
	private transient Array<Message> lines;
	private transient IntMap<PictureRef> pictures;
	private transient boolean dispose;
	private transient Object[] resultPointer;
	private transient boolean dirty;

	public static final int MARGIN = 5;

	public MessagingService() {
		callBackScript = GameBase.$options().messageCallBackScript;
		init();
	}

	private void init() {
		boxPosition = new Rectangle(0, 0, 0, 200);
		setAllowNull(allowNull);
		setFadeTime(.15f);
		lines = new Array<Message>();
		pictures = new IntMap<PictureRef>();
		resultPointer = new Object[] { null };
		try {
			setCallBackScript(callBackScript);
		} catch (ScriptException e) {
			GameBase.$error("MessagingService.init",
					"Error loading (compiling) callback script for messaging "
							+ "service. File: " + callBackScript, e);
		}
	}

	public void setCallBackScript(String scriptPath) throws ScriptException {
		callBackScript = scriptPath;
		scriptEngine = GameBase.$scriptFactory().obtainInvocable(
				GameBase.$scriptFactory().loadScript(scriptPath), scriptPath);
		((ScriptEngine) scriptEngine).put(ScriptEngine.FILENAME, scriptPath);
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
		setCloseOnAction(allowNull);
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
			try {
				if (scriptEngine != null)
					scriptEngine.invokeFunction(callBackFunction, this, title,
							face, lines, boxPosition, pictures.values());
			} catch (Exception e) {
				GameBase.$error("MessagingService." + callBackFunction,
						"Error processing message callback function "
								+ callBackFunction, e);
			}

			lines.clear();

			while (resultPointer[0] == null && getActors().size > 0 && !dispose)
				Thread.yield();
			if (!GameBase.$serviceProvider().releaseAttention(this)) {
				GameBase.$error("MessagingService.commit",
						"Failed to release attention",
						new IllegalStateException(
								"Oooops, couldn't release the attention. "
										+ "Something got terribly wrong!"));
				GameBase.$serviceProvider().forceTotalReset();
				clear();
			}
			fadeOutAllActors();
			setAllowNull(true);
			// sleep until box has disappeared
			do {
				Thread.yield();
			} while (getActors().size > 0 && !dispose);

			if (dirty)
				super.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
		return resultPointer[0];
	}

	public interface Message {
		public Actor getActor();

		public void setText(String text);

		public String getText();
	}

	@Override
	public void resize(int width, int height) {
		// TODO: if (!inMutex)
		if (getActors().size == 0) {
			super.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		} else {
			dirty = true;
		}
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

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
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

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
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

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	public static class PictureRef {
		TextureRegionRef textureRegion;
		int x, y;

		public PictureRef(int posX, int posY) {
			x = posX;
			y = posY;
		}

		public Image getImage() {
			Image img = new Image(textureRegion);
			img.setX(x);
			img.setY(y);
			return img;
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeFloat(super.getDisplayInfoTime());
		out.writeFloat(super.getFadeInfoTime());
		out.writeFloat(super.getFadeTime());
		out.writeBoolean(super.isCloseOnAction());
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		super.setDisplayInfoTime(in.readFloat());
		super.setFadeInfoTime(in.readFloat());
		super.setFadeTime(in.readFloat());
		super.setCloseOnAction(in.readBoolean());
		init();
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
