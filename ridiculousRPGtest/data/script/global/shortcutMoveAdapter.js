/**
 * Shortcuts for MovementHandler
 * @see com.ridiculousRPG.movement.**
 */
CombinedMovesAdapter = com.ridiculousRPG.movement.CombinedMovesAdapter;
ParallelMovesAdapter = com.ridiculousRPG.movement.ParallelMovesAdapter;

MoveArcAdapter = com.ridiculousRPG.movement.auto.MoveArcAdapter;
MoveDistanceAdapter = com.ridiculousRPG.movement.auto.MoveDistanceAdapter;
MoveEllipseAdapter = com.ridiculousRPG.movement.auto.MoveEllipseAdapter;
MoveMagneticAdapter = com.ridiculousRPG.movement.auto.MoveMagneticAdapter;
MoveRandomAdapter = com.ridiculousRPG.movement.auto.MoveRandomAdapter;
MoveRectangleAdapter = com.ridiculousRPG.movement.auto.MoveRectangleAdapter;
MoveSetXYAdapter = com.ridiculousRPG.movement.auto.MoveSetXYAdapter;
MoveTracerAdapter = com.ridiculousRPG.movement.auto.MoveTracerAdapter;
MovePolygonAdapter = com.ridiculousRPG.movement.auto.MovePolygonAdapter;

Move2WayNSAdapter = com.ridiculousRPG.movement.input.Move2WayNSAdapter;
Move2WayWEAdapter = com.ridiculousRPG.movement.input.Move2WayWEAdapter;
Move4WayAdapter = com.ridiculousRPG.movement.input.Move4WayAdapter;
Move8WayAdapter = com.ridiculousRPG.movement.input.Move8WayAdapter;

MoveAnimateEventAdapter = com.ridiculousRPG.movement.misc.MoveAnimateEventAdapter;
MoveChangeSpeedAdapter = com.ridiculousRPG.movement.misc.MoveChangeSpeedAdapter;
MoveExecScriptAdapter = com.ridiculousRPG.movement.misc.MoveExecScriptAdapter;
MoveFadeColorAdapter = com.ridiculousRPG.movement.misc.MoveFadeColorAdapter;
MoveNullAdapter = com.ridiculousRPG.movement.misc.MoveNullAdapter;
MoveRotateEventAdapter = com.ridiculousRPG.movement.misc.MoveRotateEventAdapter;

function moveTimes(move, times) {
	if (times==null) times = 1;
	return CombinedMovesAdapter.MOVE_FINISH_POOL.obtain(move, times);
}

function moveSeconds(move, seconds) {
	if (seconds==null) seconds = 3;
	return CombinedMovesAdapter.MOVE_SECONDS_POOL.obtain(move, seconds);
}

function moveRandomSec(move, minSeconds, maxSeconds) {
	if (minSeconds==null) minSeconds = 1;
	if (maxSeconds==null) maxSeconds = 5;
	return CombinedMovesAdapter.MOVE_RANDOM_POOL.obtain(move, minSeconds, maxSeconds);
}
