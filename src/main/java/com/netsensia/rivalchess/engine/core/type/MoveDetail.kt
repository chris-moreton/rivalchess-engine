package com.netsensia.rivalchess.engine.core.type

import com.netsensia.rivalchess.model.SquareOccupant

data class MoveDetail(
	var movePiece: SquareOccupant = SquareOccupant.NONE,
	var capturePiece: SquareOccupant = SquareOccupant.NONE,
	var move: Int = 0,
	var halfMoveCount: Byte = 0,
	var castlePrivileges: Byte = 0,
	var enPassantBitboard: Long = 0,
	var hashValue: Long = 0,
	var isOnNullMove: Boolean = false,
	var pawnHashValue: Long = 0
)