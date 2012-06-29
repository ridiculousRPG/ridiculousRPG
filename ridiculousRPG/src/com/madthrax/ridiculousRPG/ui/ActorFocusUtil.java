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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;

/**
 * This class offers some static methods to change the keyboard focused actor.<br>
 * It allows to focus the next/previous actor on the stage.
 * 
 * @author Alexander Baumgartner
 */
public final class ActorFocusUtil {
	private ActorFocusUtil() {
	} // static container

	private static Vector2 tmpPoint = new Vector2(0f, 0f);

	public static boolean focusPrev(Actor focused, Actor root, boolean up,
			boolean left, Stage stage) {
		if (focused == null)
			return focus(root, true, stage);
		return focusPrevIntern(focused, up, left, stage);
	}

	private static boolean focusPrevIntern(Actor focused, boolean up,
			boolean left, Stage stage) {
		if (focused.getParent() == null)
			return false;
		Array<Actor> allActors = focused.getParent().getChildren();

		return tryFocusPrevChild(focused, up, left, stage, allActors)
				|| focusPrevIntern(focused.getParent(), up, left, stage);
	}

	private static boolean tryFocusPrevChild(Actor focused, boolean up,
			boolean left, Stage stage, Array<Actor> allActors) {
		Vector2 tmpPoint = ActorFocusUtil.tmpPoint;
		focused.stageToLocalCoordinates(tmpPoint.set(focused.getX(), focused.getY()));
		float fX1 = focused.getX() - tmpPoint.x;
		float fY1 = focused.getY() - tmpPoint.y;
		float fX2 = fX1 + focused.getWidth() * .5f;
		float fY2 = fY1 + focused.getHeight() * .5f;
		for (int i = allActors.size - 1; i > -1; i--) {
			Actor a = allActors.get(i);
			if (a == focused)
				continue;
			a.stageToLocalCoordinates(tmpPoint.set(a.getX(), a.getY()));
			float aX1 = a.getX() - tmpPoint.x;
			float aY1 = a.getY() - tmpPoint.y;
			float aX2 = aX1 + a.getWidth() * .5f;
			float aY2 = aY1 + a.getHeight() * .5f;
			if (isFocusable(a)) {
				if (up) {
					if (aY1 >= fY2 && aX2 > fX1 && aX1 < fX2)
						return focus(a, true, stage);
				} else if (left) {
					if (aX2 <= fX1 && aY1 < fY2 && aY2 > fY1)
						return focus(a, true, stage);
				} else if (aY1 >= fY2 || (aX2 <= fX1 && aY1 < fY2 && aY2 > fY1))
					return focus(a, true, stage);
			} else if (a instanceof WidgetGroup) {
				if (tryFocusPrevChild(focused, up, left, stage,
						((WidgetGroup) a).getChildren())) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean focusNext(Actor focused, Actor root, boolean down,
			boolean right, Stage stage) {
		if (focused == null)
			return focus(root, false, stage);
		return focusNextIntern(focused, down, right, stage);
	}

	private static boolean focusNextIntern(Actor focused, boolean down,
			boolean right, Stage stage) {
		if (focused.getParent() == null)
			return false;
		Array<Actor> allActors = focused.getParent().getChildren();

		return tryFocusNextChild(focused, down, right, stage, allActors)
				|| focusNextIntern(focused.getParent(), down, right, stage);
	}

	private static boolean tryFocusNextChild(Actor focused, boolean down,
			boolean right, Stage stage, Array<Actor> allActors) {
		Vector2 tmpPoint = ActorFocusUtil.tmpPoint;
		focused.stageToLocalCoordinates(tmpPoint.set(focused.getX(), focused.getY()));
		float fX1 = focused.getX() - tmpPoint.x;
		float fY1 = focused.getY() - tmpPoint.y;
		float fX2 = fX1 + focused.getWidth() * .5f;
		float fY2 = fY1 + focused.getHeight() * .5f;
		for (int i = 0, len = allActors.size; i < len; i++) {
			Actor a = allActors.get(i);
			if (a == focused)
				continue;
			a.stageToLocalCoordinates(tmpPoint.set(a.getX(), a.getY()));
			float aX1 = a.getX() - tmpPoint.x;
			float aY1 = a.getY() - tmpPoint.y;
			float aX2 = aX1 + a.getWidth() * .5f;
			float aY2 = aY1 + a.getHeight() * .5f;
			if (isFocusable(a)) {
				if (down) {
					if (aY2 <= fY1 && aX2 > fX1 && aX1 < fX2)
						return focus(a, false, stage);
				} else if (right) {
					if (aX1 >= fX2 && aY1 < fY2 && aY2 > fY1)
						return focus(a, false, stage);
				} else if (aY2 <= fY1 || (aX1 >= fX2 && aY1 < fY2 && aY2 > fY1))
					return focus(a, false, stage);
			} else if (a instanceof WidgetGroup) {
				if (tryFocusNextChild(focused, down, right, stage,
						((WidgetGroup) a).getChildren())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean focusFirstChild(Group actorGrp, Stage stage) {
		Array<Actor> allActors = actorGrp.getChildren();
		for (int i = 0, len = allActors.size; i < len; i++) {
			Actor a = allActors.get(i);
			if (isFocusable(a))
				return focus(a, false, stage);
			if (a instanceof Group && focusFirstChild((Group) a, stage))
				return true;
		}
		return false;
	}

	public static boolean focusLastChild(Group actorGrp, Stage stage) {
		Array<Actor> allActors = actorGrp.getChildren();
		for (int i = allActors.size- 1; i > -1; i--) {
			Actor a = allActors.get(i);
			if (isFocusable(a))
				return focus(a, true, stage);
			if (a instanceof Group && focusLastChild((Group) a, stage))
				return true;
		}
		return false;
	}

	/**
	 * Sets the keyboard focus to the given actor. If the child is a group,
	 * either the first or the last child of the group gets the focus.
	 * 
	 * @param actor
	 *            The actor to focus
	 * @param focusLastIfGroup
	 *            If true and actor is a group the last child of the group gets
	 *            the focus.
	 * @param stage
	 * @return true if succeeded, false if the actor is not focusable
	 */
	public static boolean focus(Actor actor, boolean focusLastIfGroup,
			Stage stage) {
		// focus child
		if (actor instanceof Group) {
			if (focusLastIfGroup) {
				if (focusLastChild((Group) actor, stage))
					return true;
			} else {
				if (focusFirstChild((Group) actor, stage))
					return true;
			}
		}
		// focus self
		if (actor == null || actor.getParent() == null)
			return false;
		stage.setKeyboardFocus(actor);
		return true;
	}

	public static boolean isActorOnStage(Actor actor, Group root) {
		if (actor == null)
			return false;
		for (Group bottom = actor.getParent(); bottom != null; bottom = (actor = bottom).getParent()) {
			if (!isActorInList(actor, bottom.getChildren()))
				return false;
		}
		return actor == root;
	}

	private static boolean isActorInList(Actor actor, Array<Actor> actors) {
		int i = actors.size;
		while (i > 0) {
			if (actors.get(--i) == actor)
				return true;
		}
		return false;
	}

	private static boolean isFocusable(Actor a) {
		return a.isTouchable() && a.isVisible() && styleGetter(a.getClass()) != null
				&& !(a instanceof Label) && !(a instanceof ScrollPane)
				&& !(a instanceof Image) && !(a instanceof Window);
	}

	public static Method styleGetter(Class<? extends Actor> actorClass) {
		try {
			return actorClass.getMethod("getStyle");
		} catch (Exception e) {
			return null;
		}
	}

	public static Method styleSetter(Class<? extends Actor> actorClass,
			Class<?> styleClass) {
		if (styleClass == null)
			return null;
		try {
			return actorClass.getMethod("setStyle", styleClass);
		} catch (Exception e) {
			return styleSetter(actorClass, styleClass.getSuperclass());
		}
	}
}
