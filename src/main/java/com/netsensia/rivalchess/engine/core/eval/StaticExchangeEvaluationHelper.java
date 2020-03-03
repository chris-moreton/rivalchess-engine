package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

import java.util.HashMap;
import java.util.Map;

public class StaticExchangeEvaluationHelper {

    static final String VALUE = "value";
    static final String DIRECTION = "direction";

    private StaticExchangeEvaluationHelper() {}

    public static int staticExchangeEvaluation(EngineChessBoard board, int move) throws InvalidMoveException {

        final int[] captureList = new int[32];
        final int exchangeSquare = move & 63;

        captureList[0] =
                ((1L << exchangeSquare) == board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) ?
                        Piece.PAWN.getValue() :
                        RivalConstants.PIECE_VALUES.get(board.getSquareOccupant(exchangeSquare).getIndex());

        int numCaptures = 1;

        if (board.makeMove(new EngineMove(move & ~RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL))) {

            final int currentPieceOnSquare = board.getSquareOccupant(exchangeSquare).getIndex();
            int currentSquareValue = RivalConstants.PIECE_VALUES.get(currentPieceOnSquare);

            final int[] indexOfFirstAttackerInDirection = getIndexesOfFirstAttackersInEachDirection(board, exchangeSquare);

            int whiteKnightAttackCount = board.getWhiteKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(exchangeSquare) & board.getWhiteKnightBitboard());
            int blackKnightAttackCount = board.getBlackKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(exchangeSquare) & board.getBlackKnightBitboard());

            boolean isWhiteToMove = board.getMover() == Colour.WHITE;

            do {
                Map<String, Integer> lowestPieceValueDirectionAndValue =
                        getWeakestAttackerDirectionAndValue(board, exchangeSquare, indexOfFirstAttackerInDirection, isWhiteToMove);

                if (Piece.KNIGHT.getValue() < lowestPieceValueDirectionAndValue.get(VALUE) && (isWhiteToMove ? whiteKnightAttackCount : blackKnightAttackCount) > 0) {
                    if (isWhiteToMove) {
                        whiteKnightAttackCount--;
                    }
                    else {
                        blackKnightAttackCount--;
                    }
                    lowestPieceValueDirectionAndValue.put(VALUE, Piece.KNIGHT.getValue());
                    lowestPieceValueDirectionAndValue.put(DIRECTION, 8);

                }

                final int lowestPieceValueDirection = lowestPieceValueDirectionAndValue.get(DIRECTION);
                final int lowestPieceValue = lowestPieceValueDirectionAndValue.get(VALUE);

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
                                    board, exchangeSquare, lowestPieceValueDirection, indexOfFirstAttackerInDirection[lowestPieceValueDirection]);
                }

                isWhiteToMove = !isWhiteToMove;
            }
            while (true);

            board.unMakeMove();
        } else {
            return -RivalConstants.INFINITY;
        }

        return getScoreFromCaptureList(captureList, numCaptures);
    }

    private static int getScoreFromCaptureList(int[] captureList, int numCaptures) {
        int score = 0;
        for (int i = numCaptures - 1; i > 0; i--) {
            score = Math.max(0, captureList[i] - score);
        }
        return captureList[0] - score;
    }

    private static Map<String, Integer> getWeakestAttackerDirectionAndValue(EngineChessBoard board, int attackedSquare, int[] indexOfFirstAttackerInDirection, boolean isWhiteToMove) {

        Map<String, Integer> lowestPieceValueDirectionAndValue = new HashMap<>();

        lowestPieceValueDirectionAndValue.put(DIRECTION, -1);
        lowestPieceValueDirectionAndValue.put(VALUE, RivalConstants.INFINITY);

        for (int dir = 0; dir < 8; dir++) {
            if (indexOfFirstAttackerInDirection[dir] > 0) {
                final SquareOccupant attackingPiece = board.getSquareOccupant(attackedSquare + Bitboards.bitRefIncrements.get(dir) * indexOfFirstAttackerInDirection[dir]);
                if (isWhiteToMove == (attackingPiece.getColour() == Colour.WHITE) &&
                        Piece.fromSquareOccupant(attackingPiece).getValue() < lowestPieceValueDirectionAndValue.get(VALUE)) {
                    lowestPieceValueDirectionAndValue.put(DIRECTION, dir);
                    lowestPieceValueDirectionAndValue.put(VALUE, Piece.fromSquareOccupant(attackingPiece).getValue());
                }
            }
        }
        return lowestPieceValueDirectionAndValue;
    }

    private static int[] getIndexesOfFirstAttackersInEachDirection(EngineChessBoard board, int toSquare) {
        final int[] indexOfFirstAttackerInDirection = new int[8];

        indexOfFirstAttackerInDirection[0] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 0, 0);
        indexOfFirstAttackerInDirection[1] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 1, 0);
        indexOfFirstAttackerInDirection[2] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 2, 0);
        indexOfFirstAttackerInDirection[3] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 3, 0);
        indexOfFirstAttackerInDirection[4] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 4, 0);
        indexOfFirstAttackerInDirection[5] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 5, 0);
        indexOfFirstAttackerInDirection[6] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 6, 0);
        indexOfFirstAttackerInDirection[7] = getIndexOfNextDirectionAttackerAfterIndex(board, toSquare, 7, 0);

        return indexOfFirstAttackerInDirection;
    }

    public static int getIndexOfNextDirectionAttackerAfterIndex(
            final EngineChessBoard board,
            final int bitRef,
            final int direction,
            final int index) {

        final int xInc = Bitboards.xIncrements.get(direction);
        final int yInc = Bitboards.yIncrements.get(direction);

        final int movedIndex = index + 1;

        final int x = (bitRef % 8) + xInc * movedIndex;
        if (x < 0 || x > 7) {
            return -1;
        }

        final int y = (bitRef / 8) + yInc * movedIndex;
        if (y < 0 || y > 7) {
            return -1;
        }

        final int movedBitRef = bitRef + Bitboards.bitRefIncrements.get(direction) * movedIndex;

        switch (board.getSquareOccupant(movedBitRef).getPiece()) {
            case NONE:
                break;
            case KING:
                return (movedIndex == 1) ? 1 : -1;
            case PAWN:
                return ((movedIndex == 1)
                        && (yInc == (board.getSquareOccupant(movedBitRef) == SquareOccupant.WP ? -1 : 1))
                        && (xInc != 0)) ? 1 : -1;
            case QUEEN:
                return movedIndex;
            case ROOK:
                return ((direction & 1) == 0) ? movedIndex : -1;
            case BISHOP:
                return ((direction & 1) != 0) ? movedIndex : -1;
            case KNIGHT:
                return -1;
            default:
                throw new RuntimeException("Unexpected Piece enum");
        }

        return getIndexOfNextDirectionAttackerAfterIndex(board, bitRef, direction, movedIndex);
    }
}
