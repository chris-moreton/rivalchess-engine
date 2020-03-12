package com.netsensia.rivalchess.engine.core.type;

import com.netsensia.rivalchess.model.SquareOccupant;

public class MoveDetail
{
	public SquareOccupant movePiece;
	public SquareOccupant capturePiece;
	public int move;
	public byte halfMoveCount;
	public byte castlePrivileges;
	public long enPassantBitboard;
	public long hashValue;
	public boolean isOnNullMove;
	public long pawnHashValue;
}
