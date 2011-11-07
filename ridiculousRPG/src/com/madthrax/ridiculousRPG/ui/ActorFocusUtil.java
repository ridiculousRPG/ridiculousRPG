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
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

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
			boolean left) {
		if (focused == null)
			return focus(root, true);
		return focusPrevIntern(focused, up, left);
	}

	private static boolean focusPrevIntern(Actor focused, boolean up,
			boolean left) {
		if (focused.parent == null)
			return false;

		Vector2 tmpPoint = ActorFocusUtil.tmpPoint;
		focused.toLocalCoordinates(tmpPoint.set(focused.x, focused.y));
		float fX1 = focused.x - tmpPoint.x;
		float fY1 = focused.y - tmpPoint.y;
		float fX2 = fX1 + focused.width * .5f;
		float fY2 = fY1 + focused.height * .5f;
		List<Actor> allActors = focused.parent.getActors();
		for (int i = allActors.size() - 1; i > -1; i--) {
			Actor a = allActors.get(i);
			if (a == focused)
				continue;
			a.toLocalCoordinates(tmpPoint.set(a.x, a.y));
			float aX1 = a.x - tmpPoint.x;
			float aY1 = a.y - tmpPoint.y;
			float aX2 = aX1 + a.width * .5f;
			float aY2 = aY1 + a.height * .5f;
			if (isFocusable(a)) {
				if (up) {
					if (aY1 >= fY2 && aX2 > fX1 && aX1 < fX2)
						return focus(a, true);
				} else if (left) {
					if (aX2 <= fX1 && aY1 < fY2 && aY2 > fY1)
						return focus(a, true);
				} else if (aY1 >= fY2 || (aX2 <= fX1 && aY1 < fY2 && aY2 > fY1))
					return focus(a, true);
			}
		}

		return focusPrevIntern(focused.parent, up, left);
	}

	public static boolean focusNext(Actor focused, Actor root, boolean down,
			boolean right) {
		if (focused == null)
			return focus(root, false);
		return focusNextIntern(focused, down, right);
	}

	private static boolean focusNextIntern(Actor focused, boolean down,
			boolean right) {
		if (focused.parent == null)
			return false;

		Vector2 tmpPoint = ActorFocusUtil.tmpPoint;
		focused.toLocalCoordinates(tmpPoint.set(focused.x, focused.y));
		float fX1 = focused.x - tmpPoint.x;
		float fY1 = focused.y - tmpPoint.y;
		float fX2 = fX1 + focused.width * .5f;
		float fY2 = fY1 + focused.height * .5f;
		List<Actor> allActors = focused.parent.getActors();
		for (int i = 0, len = allActors.size(); i < len; i++) {
			Actor a = allActors.get(i);
			if (a == focused)
				continue;
			a.toLocalCoordinates(tmpPoint.set(a.x, a.y));
			float aX1 = a.x - tmpPoint.x;
			float aY1 = a.y - tmpPoint.y;
			float aX2 = aX1 + a.width * .5f;
			float aY2 = aY1 + a.height * .5f;
			if (isFocusable(a)) {
				if (down) {
					if (aY2 <= fY1 && aX2 > fX1 && aX1 < fX2)
						return focus(a, false);
				} else if (right) {
					if (aX1 >= fX2 && aY1 < fY2 && aY2 > fY1)
						return focus(a, false);
				} else if (aY2 <= fY1 || (aX1 >= fX2 && aY1 < fY2 && aY2 > fY1))
					return focus(a, false);
			}
		}

		return focusNextIntern(focused.parent, down, right);
	}

	public static boolean focusFirstChild(Group actorGrp) {
		List<Actor> allActors = actorGrp.getActors();
		for (int i = 0, len = allActors.size(); i < len; i++) {
			Actor a = allActors.get(i);
			if (isFocusable(a))
				return focus(a, false);
		}
		return false;
	}

	public static boolean focusLastChild(Group actorGrp) {
		List<Actor> allActors = actorGrp.getActors();
		for (int i = allActors.size() - 1; i > -1; i--) {
			Actor a = allActors.get(i);
			if (isFocusable(a))
				return focus(a, true);
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
	 * @return true if succeeded, false if the actor is not focusable
	 */
	public static boolean focus(Actor actor, boolean focusLastIfGroup) {
		// focus child
		if (actor instanceof Group) {
			if (focusLastIfGroup) {
				if (focusLastChild((Group) actor))
					return true;
			} else {
				if (focusFirstChild((Group) actor))
					return true;
			}
		}
		// focus self
		if (actor == null || actor.parent == null)
			return false;
		actor.parent.keyboardFocus(actor);
		return true;
	}

	public static boolean isActorOnStage(Actor actor, Group root) {
		if (actor == null)
			return false;
		for (Group bottom = actor.parent; bottom != null; bottom = (actor = bottom).parent) {
			if (!isActorInList(actor, bottom.getActors()))
				return false;
		}
		return actor == root;
	}

	private static boolean isActorInList(Actor actor, List<Actor> actors) {
		int i = actors.size();
		while (i > 0) {
			if (actors.get(--i) == actor)
				return true;
		}
		return false;
	}

	private static boolean isFocusable(Actor a) {
		return a.touchable && a.visible && styleGetter(a.getClass()) != null;
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
