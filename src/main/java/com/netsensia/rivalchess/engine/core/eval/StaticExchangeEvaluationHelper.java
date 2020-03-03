package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

public class StaticExchangeEvaluationHelper {

    private StaticExchangeEvaluationHelper() {}

    public static int staticExchangeEvaluation(EngineChessBoard board, int move) throws InvalidMoveException {

        final int[] captureList = new int[32];

        final int toSquare = move & 63;

        captureList[0] =
                ((1L << toSquare) == board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) ?
                        Piece.PAWN.getValue() :
                        RivalConstants.PIECE_VALUES.get(board.getSquareOccupant(toSquare).getIndex());

        int numCaptures = 1;

        if (board.makeMove(new EngineMove(move & ~RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL))) {

            final int currentPieceOnSquare = board.getSquareOccupant(toSquare).getIndex();
            int currentSquareValue = RivalConstants.PIECE_VALUES.get(currentPieceOnSquare);

            final int[] indexOfFirstAttackerInDirection = new int[8];

            indexOfFirstAttackerInDirection[0] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 0, 0);
            indexOfFirstAttackerInDirection[1] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 1, 0);
            indexOfFirstAttackerInDirection[2] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 2, 0);
            indexOfFirstAttackerInDirection[3] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 3, 0);
            indexOfFirstAttackerInDirection[4] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 4, 0);
            indexOfFirstAttackerInDirection[5] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 5, 0);
            indexOfFirstAttackerInDirection[6] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 6, 0);
            indexOfFirstAttackerInDirection[7] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 7, 0);

            int whiteKnightAttackCount = board.getWhiteKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(toSquare) & board.getWhiteKnightBitboard());
            int blackKnightAttackCount = board.getBlackKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(toSquare) & board.getBlackKnightBitboard());

            boolean isWhiteToMove = board.getMover() == Colour.WHITE;

            int lowestPieceValueDirection;
            int lowestPieceValue;

            do {
                lowestPieceValueDirection = -1;
                lowestPieceValue = RivalConstants.INFINITY;
                for (int dir = 0; dir < 8; dir++) {
                    if (indexOfFirstAttackerInDirection[dir] > 0) {
                        final int attackingPiece = board.getSquareOccupant(toSquare + Bitboards.bitRefIncrements.get(dir) * indexOfFirstAttackerInDirection[dir]).getIndex();
                        final boolean isAttackingPieceSameColourAsMover = isWhiteToMove == (attackingPiece <= RivalConstants.WR);
                        if (isAttackingPieceSameColourAsMover) {
                            if (RivalConstants.PIECE_VALUES.get(attackingPiece) < lowestPieceValue) {
                                lowestPieceValueDirection = dir;
                                lowestPieceValue = RivalConstants.PIECE_VALUES.get(attackingPiece);
                            }
                        }
                    }
                }

                if (Piece.KNIGHT.getValue() < lowestPieceValue && (isWhiteToMove ? whiteKnightAttackCount : blackKnightAttackCount) > 0) {
                    if (isWhiteToMove) {
                        whiteKnightAttackCount--;
                    }
                    else {
                        blackKnightAttackCount--;
                    }
                    lowestPieceValue = Piece.KNIGHT.getValue();
                    lowestPieceValueDirection = 8;
                }

                if (lowestPieceValueDirection == -1) {
                    break;
                }

                captureList[numCaptures++] = currentSquareValue;

                if (currentSquareValue == RivalConstants.PIECE_VALUES.get(RivalConstants.WK)) {
                    break;
                }

                currentSquareValue = lowestPieceValue;

                if (lowestPieceValueDirection != 8) {
                    indexOfFirstAttackerInDirection[lowestPieceValueDirection] =
                            getIndexOfNextDirectionAttackerAfterIndex(
                                    board, toSquare, lowestPieceValueDirection, indexOfFirstAttackerInDirection[lowestPieceValueDirection]);
                }

                isWhiteToMove = !isWhiteToMove;
            }
            while (true);

            board.unMakeMove();
        } else {
            return -RivalConstants.INFINITY;
        }

        int score = 0;
        for (int i = numCaptures - 1; i > 0; i--) {
            score = Math.max(0, captureList[i] - score);
        }
        return captureList[0] - score;
    }

    public static int getIndexOfNextDirectionAttackerAfterIndex(EngineChessBoard board, int bitRef, int direction, int index) {
        final int xInc = Bitboards.xIncrements.get(direction);
        final int yInc = Bitboards.yIncrements.get(direction);
        int pieceType;
        index++;
        int x = (bitRef % 8) + xInc * index;
        if (x < 0 || x > 7) {
            return -1;
        }
        int y = (bitRef / 8) + yInc * index;
        if (y < 0 || y > 7) {
            return -1;
        }
        bitRef += Bitboards.bitRefIncrements.get(direction) * index;
        do {
            pieceType = board.getSquareOccupant(bitRef).getIndex();
            switch (pieceType % 6) {
                case -1:
                    break;
                case RivalConstants.WK:
                    return (index == 1) ? 1 : -1;
                case RivalConstants.WP:
                    return ((index == 1) && (yInc == (pieceType == RivalConstants.WP ? -1 : 1)) && (xInc != 0)) ? 1 : -1;
                case RivalConstants.WQ:
                    return index;
                case RivalConstants.WR:
                    return ((direction & 1) == 0) ? index : -1;
                case RivalConstants.WB:
                    return ((direction & 1) != 0) ? index : -1;
                case RivalConstants.WN:
                    return -1;
            }
            x += xInc;
            if (x < 0 || x > 7) {
                return -1;
            }
            y += yInc;
            if (y < 0 || y > 7) {
                return -1;
            }
            bitRef += Bitboards.bitRefIncrements.get(direction);
        }
        while (index++ > 0);
        return -1;
    }
}
