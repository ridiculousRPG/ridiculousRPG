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

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Remove;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
import com.madthrax.ridiculousRPG.service.ResizeListener;

/**
 * This class provides a customizable standard menu for the game.<br>
 * 
 * @author Alexander Baumgartner
 */
public class MessagingService extends ActorsOnStageService implements
		ResizeListener {

	private Rectangle boxPosition = new Rectangle();
	private TextureRegionRef face = null;
	private String title = null;
	private Array<Message> lines = new Array<Message>();
	private Array<MessageChoice> choices = new Array<MessageChoice>();
	private Array<MessageInput> inputs = new Array<MessageInput>();

	public void addGUIcomponent(Object component) {
		if (component instanceof Actor)
			addActor((Actor) component);
	}

	public void center(Object obj) {
		if (obj instanceof Actor) {
			Actor actor = (Actor) obj;
			actor.x = (int) (centerX() - actor.width * .5f);
			actor.y = (int) (centerY() - actor.height * .5f);
		}
	}

	public void focus(Object guiElement) {
		if (guiElement instanceof Actor) {
			super.focus((Actor) guiElement);
		}
	}

	public void resize(int width, int height) {
		// TODO: resize
	}

	public float getHeight() {
		return height;
	}

	public float getWidth() {
		return width;
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

			w.touchable = false;
			w.color.a = .1f;
			w.action(Sequence.$(FadeIn.$(.3f), Delay.$(FadeOut.$(.3f), 2f),
					Remove.$()));
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

	public Object face() {
		return commit();
	}

	public void title(String title) {
		this.title = title;
	}

	public void say(String text) {
		MessageText msgText = new MessageText(text);
		lines.add(msgText);
	}

	public void choice() {
	}

	public void input() {
	}

	public Object commit() {
		try {
			final Window w = new Window(getSkinNormal());

			if (title != null) {
				w.setTitle(title);
			}
			w.touchable = false;
			w.color.a = .1f;
			w.action(Sequence.$(FadeIn.$(.3f), Delay.$(FadeOut.$(.3f), 2f),
					Remove.$()));
			for (Message line : lines) {
				w.row().fill(true, false).expand(true, false);
				w.add(line.getActor());
			}
			w.x = boxPosition.x;
			w.y = boxPosition.y;
			if (boxPosition.width == 0) {
				// width defined by margin x
				w.width = GameBase.$().getScreen().width - 2 * w.x;
			} else if (boxPosition.width < 0) {
				// bind box at the top edge of the screen
				if (w.width < -boxPosition.width) {
					// set preferred width
					w.width = -boxPosition.width;
				}
				w.x = GameBase.$().getScreen().width - w.width - w.x;
			} else if (w.width < boxPosition.width) {
				// set preferred width
				w.width = boxPosition.width;
			}
			if (boxPosition.height == 0) {
				// height defined by margin y
				w.height = GameBase.$().getScreen().height - 2 * w.y;
			} else if (boxPosition.height < 0) {
				// bind box at the right edge of the screen
				if (w.height < -boxPosition.height) {
					// set preferred height
					w.height = -boxPosition.height;
				}
				w.y = GameBase.$().getScreen().height - w.height - w.y;
			} else if (w.height < boxPosition.height) {
				// set preferred height
				w.height = boxPosition.height;
			}
			if (boxPosition.x < 0) {
				// center horizontal
				w.x = (int) (centerX() - w.width * .5f);
			}
			if (boxPosition.y < 0) {
				// center vertical
				w.y = (int) (centerY() - w.height * .5f);
			}
			addActor(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		lines.clear();
		choices.clear();
		inputs.clear();
		return null;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public interface Message {
		public Actor getActor();
	}

	public class MessageInput implements Message {

		@Override
		public Actor getActor() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class MessageChoice implements Message {

		@Override
		public Actor getActor() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class MessageText implements Message {
		private String text;

		public MessageText(String text) {
			this.text = text;
		}

		@Override
		public Actor getActor() {
			return new Label(text, getSkinNormal());
		}
	}
}
