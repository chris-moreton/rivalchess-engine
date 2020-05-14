package com.netsensia.rivalchess.engine.core.type

import com.netsensia.rivalchess.model.SquareOccupant

class MoveDetail {
    @JvmField
	var movePiece: SquareOccupant = SquareOccupant.NONE
    @JvmField
	var capturePiece: SquareOccupant = SquareOccupant.NONE
    @JvmField
	var move = 0
    @JvmField
	var halfMoveCount: Byte = 0
    @JvmField
	var castlePrivileges: Byte = 0
    @JvmField
	var enPassantBitboard: Long = 0
    @JvmField
	var hashValue: Long = 0
    @JvmField
	var isOnNullMove = false
    @JvmField
	var pawnHashValue: Long = 0
}