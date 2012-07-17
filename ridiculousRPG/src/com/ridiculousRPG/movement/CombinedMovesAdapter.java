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

package com.ridiculousRPG.movement;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.ridiculousRPG.movement.auto.MoveSetXYAdapter;
import com.ridiculousRPG.movement.misc.MoveFadeColorAdapter;

/**
 * This {@link MovementHandler} allows to combine any other
 * {@link MovementHandler}s. It runs the combined move as one sequence and
 * allows looping the sequence or parts of the sequence.<br>
 * If looping is disabled the switch {@link MovementHandler#finished} is set to
 * true after the last move.<br>
 * You may use this {@link MovementHandler} stand alone (without an event) if
 * all the nested {@link MovementHandler} are designed to run stand alone. For
 * example see {@link MoveFadeColorAdapter}.
 * 
 * @author Alexander Baumgartner
 */
public class CombinedMovesAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	private Deque<MoveSegment> movementQueue = new ArrayDeque<MoveSegment>(8);
	private List<MoveSegment> resetMoves = new ArrayList<MoveSegment>(8);
	private MoveSegment lastMove;
	private boolean loop, resetEventPosition, initialized;

	public static final FinishPool MOVE_FINISH_POOL = new FinishPool();
	public static final SecondsPool MOVE_SECONDS_POOL = new SecondsPool();
	public static final RandomSecPool MOVE_RANDOM_POOL = new RandomSecPool();

	public static final Pool<MoveSetXYAdapter> MOVE_SET_XY = new Pool<MoveSetXYAdapter>() {
		@Override
		protected MoveSetXYAdapter newObject() {
			return new MoveSetXYAdapter(0, 0);
		}
	};

	/**
	 * This {@link MovementHandler} allows to combine any other
	 * {@link MovementHandler}s (even itself). It allows looping the sequence.<br>
	 * If looping is disabled the switch {@link MovementHandler#finished} is set
	 * to true after the last move.
	 * 
	 * @param loop
	 *            Restart at first move after finishing all moves.
	 * @param resetEventPosition
	 *            After finishing a loop-cycle the event's position will be
	 *            reset to the start position.
	 * @return MoveCombinedMovesAdapter the movement adapter
	 */
	public CombinedMovesAdapter(boolean loop, boolean resetEventPosition) {
		this.loop = loop;
		this.resetEventPosition = resetEventPosition;
	}

	/**
	 * The move will be executed until it's finished.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public MoveSegment addMoveToExecute(MovementHandler move) {
		return addMoveForTimes(move, 1);
	}

	/**
	 * The move will be executed until it's finished for the specified times.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public MoveSegment addMoveForTimes(MovementHandler move, int times) {
		return addMoveSegment(MOVE_FINISH_POOL.obtain(move, times));
	}

	/**
	 * The move will be executed for the given amount of seconds. Fractions are
	 * allowed.<br>
	 * If the move finishes before the time runs out, it will idle in the
	 * {@link MovementHandler#finished} state until the time is over.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public MoveSegment addMoveForSeconds(MovementHandler move, float execSeconds) {
		return addMoveSegment(MOVE_SECONDS_POOL.obtain(move, execSeconds));
	}

	/**
	 * The move will be executed for a random time between one and five seconds.<br>
	 * 1 &lt;= randomTime &lt; 5 &nbsp; &nbsp; &nbsp; &nbsp; (all fractions are
	 * included)<br>
	 * If the move finishes before the time runs out, it will idle in the
	 * {@link MovementHandler#finished} state until the time is over.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public MoveSegment addMoveForRandomPeriod(MovementHandler move) {
		return addMoveForRandomBounded(move, 1, 5);
	}

	/**
	 * The move will be executed for a random time between minSeconds and
	 * maxSeconds seconds.<br>
	 * minSeconds &lt;= randomTime &lt; maxSeconds &nbsp; &nbsp; &nbsp; &nbsp;
	 * (all fractions are included)<br>
	 * If the move finishes before the time runs out, it will idle in the
	 * {@link MovementHandler#finished} state until the time is over.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public MoveSegment addMoveForRandomBounded(MovementHandler move,
			float minSeconds, float maxSeconds) {
		return addMoveSegment(MOVE_RANDOM_POOL.obtain(move, minSeconds,
				maxSeconds));
	}

	/**
	 * With this method you can add an already instantiated MoveSegment. It also
	 * allows you to build custom MoveSegments and append them to the execution
	 * chain.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public synchronized void execMoveOnce(MoveSegment segmentToAdd) {
		movementQueue.offerFirst(segmentToAdd.forceRemove().returnToPool());
	}

	/**
	 * With this method you can add an already instantiated MoveSegment. It also
	 * allows you to build custom MoveSegments and append them to the execution
	 * chain.<br>
	 * (Tip: You can directly concatenate {@link MoveSegment#forceRemove()} with
	 * this method-call, if you want to execute the move only once)
	 */
	public synchronized MoveSegment addMoveSegment(MoveSegment segmentToAdd) {
		movementQueue.offer(segmentToAdd);
		resetMoves.add(segmentToAdd);
		return segmentToAdd;
	}

	@Override
	public void freeze() {
		if (lastMove != null) {
			lastMove.freeze();
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		if (lastMove != null) {
			lastMove.moveBlocked(event);
		}
	}

	@Override
	public void tryMove(Movable event, float deltaTime) {
		tryMove(event, deltaTime, 3);
	}

	private void tryMove(Movable event, float deltaTime, int recurse) {
		if (movementQueue.isEmpty()) {
			if (event != null)
				event.stop();
			lastMove = null;
			finished = true;
		} else {
			if (!initialized) {
				initialized = true;
				if (resetEventPosition && event != null) {
					MoveSetXYAdapter setXY = MOVE_SET_XY.obtain();
					setXY.other.setX(event.getX());
					setXY.other.setY(event.getY());
					addMoveToExecute(setXY);
				}
			}
			lastMove = movementQueue.peek();
			if (lastMove.softMoveSegment(event, deltaTime)) {
				nextSegment();
				if (recurse > 0)
					tryMove(event, deltaTime, recurse - 1);
			}
		}
	}

	/**
	 * Jump to next segment within this move sequence
	 * 
	 * @return this for chaining (.nextSegment().nextSegment())
	 */
	public synchronized CombinedMovesAdapter nextSegment() {
		MoveSegment curr = movementQueue.poll();
		if (curr != null && loop && !curr.forceRemove) {
			curr.reset();
			movementQueue.offer(curr);
		} else if (curr.returnToPool) {
			free(curr);
		}
		return this;
	}

	/**
	 * Returns the segment to the cache pool for reusing.
	 * 
	 * @param curr
	 *            Segment to free for reuse.
	 */
	public void free(MoveSegment curr) {
		curr.delegate.free();
		curr.delegate = null;
		curr.returnToPool = false;
		if (curr instanceof MoveSegmentFinished)
			MOVE_FINISH_POOL.free((MoveSegmentFinished) curr);
		else if (curr instanceof MoveSegmentSeconds)
			MOVE_SECONDS_POOL.free((MoveSegmentSeconds) curr);
		else if (curr instanceof MoveSegmentRandomSec)
			MOVE_RANDOM_POOL.free((MoveSegmentRandomSec) curr);
	}

	@Override
	public synchronized void reset() {
		super.reset();
		movementQueue.clear();
		for (int i = 0, len = resetMoves.size(); i < len; i++) {
			MoveSegment ms = resetMoves.get(i);
			ms.reset();
			movementQueue.add(ms);
		}
	}

	/**
	 * Empties this move sequence and frees all resources.
	 */
	public synchronized void clear() {
		for (int i = 0, len = resetMoves.size(); i < len; i++) {
			free(resetMoves.get(i));
		}
		resetMoves.clear();
		movementQueue.clear();
	}

	/**
	 * Abstract class for all possible PathSegment.
	 */
	public static abstract class MoveSegment implements Serializable, Poolable {
		private static final long serialVersionUID = 1L;

		/**
		 * The executed {@link MovementHandler} for this {@link MoveSegment}.<br>
		 * It makes sense to set this by the implementations constructor
		 */
		public MovementHandler delegate;
		public boolean returnToPool;
		private boolean forceRemove;

		/**
		 * Remove this {@link MoveSegment} from the loop after it's move
		 * finished.<br>
		 * When you build up a combined move loop you can use this method for
		 * initializing other moves.
		 */
		public MoveSegment forceRemove() {
			forceRemove = true;
			return this;
		}

		public MoveSegment returnToPool() {
			returnToPool = true;
			return this;
		}

		/**
		 * This method is called if the game is in idle state. E.g. it's paused
		 * or the main-menu is open...<br>
		 * (default implementation delegates to the {@link MovementHandler})
		 */
		protected void freeze() {
			if (delegate != null)
				delegate.freeze();
		}

		/**
		 * This method is called if the move couldn't be performed. The move has
		 * been canceled instead of committed.<br>
		 * (default implementation delegates to the {@link MovementHandler})
		 * 
		 * @param event
		 */
		protected void moveBlocked(Movable event) {
			if (delegate != null)
				delegate.moveBlocked(event);
		}

		/**
		 * This method is called periodically from the
		 * {@link CombinedMovesAdapter} until it returns true to indicate that
		 * this {@link MoveSegment} has finished.<br>
		 * 
		 * @param deltaTime
		 * @return true if the move has finished
		 */
		protected abstract boolean softMoveSegment(Movable event,
				float deltaTime);

		/**
		 * This method is called after the move has finished. It should restore
		 * the start-state of this segment to provide looping the segment.
		 */
		public void reset() {
			if (delegate != null)
				delegate.reset();
		}
	}

	public static class MoveSegmentSeconds extends MoveSegment {
		private static final long serialVersionUID = 1L;

		public float seconds;
		private float secondsCount;

		private MoveSegmentSeconds() {
		}

		@Override
		protected boolean softMoveSegment(Movable event, float deltaTime) {
			if (delegate != null)
				delegate.tryMove(event, deltaTime);
			secondsCount += deltaTime;
			return secondsCount >= seconds;
		}

		@Override
		public void reset() {
			super.reset();
			secondsCount = 0f;
		}
	}

	public static class MoveSegmentFinished extends MoveSegment {
		private static final long serialVersionUID = 1L;

		public int times;
		private int count;

		private MoveSegmentFinished() {
		}

		@Override
		protected boolean softMoveSegment(Movable event, float deltaTime) {
			if (delegate != null) {
				delegate.tryMove(event, deltaTime);
				if (delegate.finished) {
					count++;
					if (count < times) {
						super.reset();
					} else {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void reset() {
			super.reset();
			count = 0;
		}
	}

	public static class MoveSegmentRandomSec extends MoveSegmentSeconds {
		private static final long serialVersionUID = 1L;

		public float minSeconds, maxSeconds;

		private MoveSegmentRandomSec() {
		}

		protected void randomizeSeconds() {
			seconds = minSeconds + (float) Math.random()
					* (maxSeconds - minSeconds);
		}

		@Override
		public void reset() {
			super.reset();
			randomizeSeconds();
		}
	}

	public static class FinishPool extends Pool<MoveSegmentFinished> {
		@Override
		protected MoveSegmentFinished newObject() {
			return new MoveSegmentFinished();
		}

		public MoveSegmentFinished obtain(MovementHandler move, int times) {
			MoveSegmentFinished segment = MOVE_FINISH_POOL.obtain();
			segment.delegate = move;
			segment.times = times;
			return segment;
		}
	}

	public static class SecondsPool extends Pool<MoveSegmentSeconds> {
		@Override
		protected MoveSegmentSeconds newObject() {
			return new MoveSegmentSeconds();
		}

		public MoveSegmentSeconds obtain(MovementHandler move, float seconds) {
			MoveSegmentSeconds segment = obtain();
			segment.delegate = move;
			segment.seconds = seconds;
			return segment;
		}
	}

	public static class RandomSecPool extends Pool<MoveSegmentRandomSec> {
		@Override
		protected MoveSegmentRandomSec newObject() {
			return new MoveSegmentRandomSec();
		}

		public MoveSegmentRandomSec obtain(MovementHandler move,
				float minSeconds, float maxSeconds) {
			MoveSegmentRandomSec segment = obtain();
			segment.delegate = move;
			segment.minSeconds = minSeconds;
			segment.maxSeconds = maxSeconds;
			segment.randomizeSeconds();
			return segment;
		}
	}
}
