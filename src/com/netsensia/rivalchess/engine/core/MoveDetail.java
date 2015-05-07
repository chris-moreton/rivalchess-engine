package com.netsensia.rivalchess.engine.core;

public class MoveDetail 
{
	byte movePiece;
	byte capturePiece;
	int move;
	byte halfMoveCount;
	byte castlePrivileges;
	long enPassantBitboard;
	long hashValue;
	boolean isOnNullMove;
	long pawnHashValue;
}
