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

package com.madthrax.ridiculousRPG.movement.misc;

import java.util.ArrayList;
import java.util.List;

import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} allows to combine any other
 * {@link MovementHandler}s. It runs all moves in parallel.<br>
 * You may use this {@link MovementHandler} stand alone (without an event) if
 * all the nested {@link MovementHandler} are designed to run stand alone. For
 * example see {@link MoveFadeColorAdapter}.<br>
 * Use this {@link MovementHandler} to change the color, speed,... while moving
 * an event around. Don't combine two real moving {@link MovementHandler}.
 * 
 * @author Alexander Baumgartner
 */
public class ParallelMovesAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	// We never want to create new ArrayList instances
	private final List<MovementHandler> movements = new ArrayList<MovementHandler>(
			4);
	private final List<MovementHandler> resetMoves = new ArrayList<MovementHandler>(
			4);

	protected ParallelMovesAdapter(MovementHandler... parallelMoves) {
		for (MovementHandler move : parallelMoves)
			addMove(move);
	}

	/**
	 * This {@link MovementHandler} allows to combine any other
	 * {@link MovementHandler}s. It runs all moves in parallel.
	 * 
	 * @return MoveParallelMovesAdapter the movement adapter
	 */
	public static ParallelMovesAdapter $(MovementHandler... parallelMoves) {
		return new ParallelMovesAdapter(parallelMoves);
	}

	/**
	 * Adds a new {@link MovementHandler} to be executed in parallel with all
	 * other {@link MovementHandler}s.
	 */
	public void addMove(MovementHandler move) {
		movements.add(move);
		resetMoves.add(move);
	}

	@Override
	public void freeze() {
		List<MovementHandler> movements = this.movements;
		for (int i = 0, len = movements.size(); i < len; i++) {
			movements.get(i).freeze();
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		List<MovementHandler> movements = this.movements;
		for (int i = 0, len = movements.size(); i < len; i++) {
			movements.get(i).moveBlocked(event);
		}
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		List<MovementHandler> movements = this.movements;
		for (int i = 0, len = movements.size(); i < len; i++) {
			MovementHandler m = movements.get(i);
			m.tryMove(event, deltaTime);
			if (m.finished) {
				movements.remove(i);
				len--;
				i--;
			}
		}
	}

	@Override
	public void reset() {
		List<MovementHandler> movements = this.movements;
		List<MovementHandler> resetMoves = this.resetMoves;
		movements.clear();
		for (int i = 0, len = resetMoves.size(); i < len; i++) {
			resetMoves.get(i).reset();
			movements.add(resetMoves.get(i));
		}
	}
}
