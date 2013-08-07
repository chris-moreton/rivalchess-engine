package com.netadapt.rivalchess.util;

import com.netadapt.rivalchess.engine.RivalConstants;
import com.netadapt.rivalchess.model.BoardRef;
import com.netadapt.rivalchess.model.MoveRef;

public class ChessBoardConversion 
{
	public static BoardRef getBoardRefFromBitRef(int bitRef)
	{
		bitRef = 63 - bitRef;
		int x = bitRef % 8;
		int y = (int)Math.floor(bitRef / 8);
		return new BoardRef(x,y);
	}
	
	public static int getBitRefFromBoardRef(BoardRef boardRef)
	{
		return 63 - (8*boardRef.getYRank()) - boardRef.getXFile();
	}

	public static MoveRef getMoveRefFromEngineMove(int move) 
	{
		int from = (move >> 16) & 63;
		int to = move & 63;
		BoardRef boardRefFrom = ChessBoardConversion.getBoardRefFromBitRef(from);
		BoardRef boardRefTo = ChessBoardConversion.getBoardRefFromBitRef(to);

		MoveRef moveRef = new MoveRef(boardRefFrom, boardRefTo);

		int promotionPieceCode = move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
		switch (promotionPieceCode)
		{
			case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN :
				moveRef.setPromotedPieceCode(to >= 56 ? 'Q' : 'q');
				break;
			case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK : 
				moveRef.setPromotedPieceCode(to >= 56 ? 'R' : 'r');
				break;
			case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT : 
				moveRef.setPromotedPieceCode(to >= 56 ? 'N' : 'n');
				break;
			case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP : 
				moveRef.setPromotedPieceCode(to >= 56 ? 'B' : 'b');
			default:
				moveRef.setPromotedPieceCode('#');
				break;
		}
		
		return moveRef;
	}

	public static String getSimpleAlgebraicMoveFromCompactMove(int compactMove)
	{
		if (compactMove == 0)
		{
			return "null";
		}
		int from = (compactMove >> 16) & 63;
		int to = compactMove & 63;
		
		MoveRef moveRef = getMoveRefFromEngineMove(compactMove);
		char pp = moveRef.getPromotedPieceCode();
		
		return getSimpleAlgebraicFromBitRef(from) + getSimpleAlgebraicFromBitRef(to) + (pp == '#' || pp == ' ' ? "" : pp); 
	}

	public static String getSimpleAlgebraicFromBitRef(int bitRef)
	{
		BoardRef boardRef = ChessBoardConversion.getBoardRefFromBitRef(bitRef);
		
		char a = (char)(boardRef.getXFile() + 97);
		
		return "" + a + ((7-boardRef.getYRank()) + 1);
	}
	
	public static int getCompactMove(int[] move)
	{
		return (move[0] << 16) | move[1]; 
	}
	
	public static int getCompactMoveFromSimpleAlgebraic(String s)
	{
		int fromX = s.toUpperCase().charAt(0) - 65;
		int fromY = 7 - (s.toUpperCase().charAt(1) - 49);
		int toX = s.toUpperCase().charAt(2) - 65;
		int toY = 7 - (s.toUpperCase().charAt(3) - 49);
		
		int fromBitRef = getBitRefFromBoardRef(new BoardRef(fromX, fromY));
		int toBitRef = getBitRefFromBoardRef(new BoardRef(toX, toY));
		
		int[] move = new int[2];
		int l = s.length();
		char promotionPiece;
		if (l == 5)
		{
			promotionPiece = s.toUpperCase().charAt(4);
			switch (promotionPiece)
			{
				case 'Q' : toBitRef |= RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN; break;
				case 'R' : toBitRef |= RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK; break;
				case 'N' : toBitRef |= RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT; break;
				case 'B' : toBitRef |= RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP; break;
			}
		}
		
		return (fromBitRef << 16) | toBitRef;
	}
	
	public static int flipBitRefOnHorizontalAxis(int bitRef)
	{
		return (7-(bitRef / 8)) * 8 + (bitRef % 8);
	}

	public static int flipBitRefOnVerticalAxis(int bitRef)
	{
		return (bitRef / 8) * 8 + (7-(bitRef % 8));
	}
}
