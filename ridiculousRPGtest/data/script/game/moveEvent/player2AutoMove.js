function createCombinedMoves() {
	var combinedMoves = CombinedMovesAdapter.$(true, true, []);
	combinedMoves.addMoveForSeconds(MoveDistanceAdapter.$(80, Direction.N), 4);
	combinedMoves.addMoveForSeconds(MoveDistanceAdapter.$(80, Direction.S), 4);
	return combinedMoves;
}
createCombinedMoves();