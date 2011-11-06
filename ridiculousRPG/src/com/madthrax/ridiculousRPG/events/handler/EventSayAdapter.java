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
package com.madthrax.ridiculousRPG.events.handler;

import com.madthrax.ridiculousRPG.events.EventObject;

/**
 * This is a convenience class for events wich only want to say something
 * @author Alexander Baumgartner
 */
public class EventSayAdapter extends EventAdapter {
	public String sayOnPush;
	public String sayOnTouch;
	public boolean touchPerformed;

	public boolean push(EventObject self, EventObject pushedBy) {
		if (sayOnPush!=null) {
			//TODO: show textbox
			System.out.println(sayOnPush);
			return true;
		}
		return false;
	}
	public boolean touch(EventObject self, EventObject pushedBy) {
		if (!touchPerformed && sayOnTouch!=null) {
			//TODO: show textbox
			System.out.println(sayOnTouch);
			touchPerformed = true;
			return true;
		}
		return false;
	}
}
