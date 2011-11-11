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

package com.madthrax.ridiculousRPG.service;

/**
 * @author Alexander Baumgartner
 */
public abstract class GameServiceDefaultImpl implements GameService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.madthrax.ridiculousRPG.service.GameService#essential()
	 */
	public void freeze() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.madthrax.ridiculousRPG.service.GameService#essential()
	 */
	public void unfreeze() {
	}

	/**
	 * The default implementation is not essential
	 * 
	 * @return false
	 * @see GameService#essential()
	 */
	public boolean essential() {
		return false;
	}

}
