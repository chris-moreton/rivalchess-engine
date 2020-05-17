package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.engine.core.board.BoardExtensionsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.enums.PromotionPieceMask;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;

public class ChessBoardConversion
{

	public static Square getBoardRefFromBitRef(int bitRef)
	{
		bitRef = 63 - bitRef;
		int x = bitRef % 8;
		int y = bitRef / 8;
		return Square.fromCoords(x,y);
	}
	
	public static int getBitRefFromBoardRef(Square boardRef)
	{
		return 63 - (8*boardRef.getYRank()) - boardRef.getXFile();
	}

	public static Move getMoveRefFromEngineMove(int move)
	{
		int from = (move >> 16) & 63;
		int to = move & 63;
		Square boardRefFrom = ChessBoardConversion.getBoardRefFromBitRef(from);
		Square boardRefTo = ChessBoardConversion.getBoardRefFromBitRef(to);

		final int promotionPieceCode = move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue();

		if (promotionPieceCode != 0) {
			switch (PromotionPieceMask.fromValue(promotionPieceCode)) {
				case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
					return new Move(boardRefFrom, boardRefTo, to >= 56 ? SquareOccupant.WQ : SquareOccupant.BQ);
				case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
					return new Move(boardRefFrom, boardRefTo, to >= 56 ? SquareOccupant.WR : SquareOccupant.BR);
				case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
					return new Move(boardRefFrom, boardRefTo, to >= 56 ? SquareOccupant.WN : SquareOccupant.BN);
				case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
					return new Move(boardRefFrom, boardRefTo, to >= 56 ? SquareOccupant.WB : SquareOccupant.BB);
				default:
					throw new RuntimeException("Unexpected error");
			}
		}

		return new Move(boardRefFrom, boardRefTo, SquareOccupant.NONE);

	}

	public static String getSimpleAlgebraicMoveFromCompactMove(int compactMove)
	{
		if (compactMove == 0)
		{
			return "zero";
		}
		int from = (compactMove >> 16) & 63;
		int to = compactMove & 63;
		
		Move move = getMoveRefFromEngineMove(compactMove);
		String pp = move.getPromotedPiece() == SquareOccupant.NONE
				? ""
				: String.valueOf(move.getPromotedPiece().toChar()).trim();

		return getSimpleAlgebraicFromBitRef(from) + getSimpleAlgebraicFromBitRef(to) + pp;
	}

	public static String getSimpleAlgebraicFromBitRef(int bitRef)
	{
		Square boardRef = ChessBoardConversion.getBoardRefFromBitRef(bitRef);
		
		char a = (char)(boardRef.getXFile() + 97);
		
		return "" + a + ((7-boardRef.getYRank()) + 1);
	}

	public static String getPgnMoveFromCompactMove(int move, String fen)
			throws IllegalFenException, InvalidMoveException {

		EngineBoard board = new EngineBoard();
		board.setBoard(FenUtils.getBoardModel(fen));

		String pgnMove = "";
		int to = move & 63;
		int from = (move >>> 16) & 63;
		int promotionPiece = move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue();
		
		switch (board.getSquareOccupant(from).getPiece())
		{
			case KNIGHT : pgnMove = "N"; break;
			case KING : pgnMove = "K"; break;
			case QUEEN : pgnMove = "Q"; break;
			case BISHOP : pgnMove = "B"; break;
			case ROOK : pgnMove = "R"; break;
			case PAWN : break;
			default:
				throw new InvalidMoveException("No piece found on source square");
		}
		
		char qualifier = ' ';
		
		int[] legalMoves = new int[Limit.MAX_LEGAL_MOVES.getValue()];
		board.setLegalMoves(legalMoves);
		
		int moveCount = 0;
		int legalMove = legalMoves[moveCount] & 0x00FFFFFF;
		while (legalMove != 0)
		{
			final int legalMoveTo = legalMove & 63;
			final int legalMoveFrom = (legalMove >>> 16) & 63;
			if (legalMoveTo == to)
			{
				if (board.getSquareOccupant(legalMoveFrom).getIndex() == board.getSquareOccupant(from).getIndex())
				{
					if (legalMoveFrom != from)
					{
						if (legalMoveFrom % 8 == from % 8) // same file
						{
							qualifier = (char)((int)'1' + (from / 8)); 
						}
						else
						{
							qualifier = (char)((int)'a' + (7-(from % 8))); 
						}
					}
				}
			}
			moveCount ++;
			legalMove = legalMoves[moveCount] & 0x00FFFFFF;
		}
		
		if (qualifier != ' ' ) pgnMove += qualifier;
		
		if (board.getSquareOccupant(to).getIndex() != -1)
		{
			if (board.getSquareOccupant(from).getPiece() == Piece.PAWN)
			{
				pgnMove += (char)((int)'a' + (7-(from % 8)));
			}
			pgnMove += "x";
		}
		
		pgnMove += ChessBoardConversion.getSimpleAlgebraicFromBitRef(to);

		if (promotionPiece != 0) {
			switch (PromotionPieceMask.fromValue(promotionPiece)) {
				case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
					pgnMove += "=Q";
					break;
				case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
					pgnMove += "=N";
					break;
				case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
					pgnMove += "=B";
					break;
				case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
					pgnMove += "=R";
					break;
			}
		}
		
		if (board.makeMove(new EngineMove(move)))
		{
			if (BoardExtensionsKt.isCheck(board)) pgnMove += "+";
			board.unMakeMove();
		}
		
		return pgnMove;
	}
	
	public static EngineMove getEngineMoveFromSimpleAlgebraic(String s)
	{
		int fromX = s.toUpperCase().charAt(0) - 65;
		int fromY = 7 - (s.toUpperCase().charAt(1) - 49);
		int toX = s.toUpperCase().charAt(2) - 65;
		int toY = 7 - (s.toUpperCase().charAt(3) - 49);
		
		int fromBitRef = getBitRefFromBoardRef(Square.fromCoords(fromX, fromY));
		int toBitRef = getBitRefFromBoardRef(Square.fromCoords(toX, toY));
		
		int l = s.length();
		if (l == 5)
		{
			switch (s.toUpperCase().charAt(4))
			{
				case 'Q' : toBitRef |= PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue(); break;
				case 'R' : toBitRef |= PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.getValue(); break;
				case 'N' : toBitRef |= PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.getValue(); break;
				case 'B' : toBitRef |= PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.getValue(); break;
			}
		}
		
		return new EngineMove((fromBitRef << 16) | toBitRef);
	}
}
