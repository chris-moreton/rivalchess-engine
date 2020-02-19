package com.netsensia.rivalchess.engine.core.type;

public class MoveDetail 
{
	public byte movePiece;
	public byte capturePiece;
	public int move;
	public byte halfMoveCount;
	public byte castlePrivileges;
	public long enPassantBitboard;
	public long hashValue;
	public boolean isOnNullMove;
	public long pawnHashValue;
}
