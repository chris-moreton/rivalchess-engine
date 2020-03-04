package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.exception.UnexpectedEnumValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticExchangeEvaluationHelper {

    static final String VALUE = "value";
    static final String DIRECTION = "direction";

    private StaticExchangeEvaluationHelper() {}

    public static int staticExchangeEvaluation(EngineChessBoard board, int move) throws InvalidMoveException {

        List<Integer> captureList = new ArrayList<>();
        final int exchangeSquare = move & 63;

        captureList.add(((1L << exchangeSquare) == board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) ?
                        Piece.PAWN.getValue() :
                        RivalConstants.PIECE_VALUES.get(board.getSquareOccupant(exchangeSquare).getIndex()));

        if (!board.makeMove(new EngineMove(move & ~RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL))) {
            return -RivalConstants.INFINITY;
        }

        final int currentPieceOnSquare = board.getSquareOccupant(exchangeSquare).getIndex();
        int currentSquareValue = RivalConstants.PIECE_VALUES.get(currentPieceOnSquare);

        final int[] indexOfFirstAttackerInDirection = getIndexesOfFirstAttackersInEachDirection(board, exchangeSquare);

        int whiteKnightAttackCount = board.getWhiteKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(exchangeSquare) & board.getWhiteKnightBitboard());
        int blackKnightAttackCount = board.getBlackKnightBitboard() == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(exchangeSquare) & board.getBlackKnightBitboard());

        boolean isWhiteToMove = board.getMover() == Colour.WHITE;

        captureList = getCaptureList(board, captureList, exchangeSquare, currentSquareValue, indexOfFirstAttackerInDirection, whiteKnightAttackCount, blackKnightAttackCount, isWhiteToMove);

        board.unMakeMove();

        return getScoreFromCaptureList(captureList);
    }

    public static List<Integer> getCaptureList(
            EngineChessBoard board,
            List<Integer> captureList,
            int exchangeSquare,
            int currentSquareValue,
            int[] indexOfFirstAttackerInDirection,
            int whiteKnightAttackCount,
            int blackKnightAttackCount,
            boolean isWhiteToMove) {

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

        if (lowestPieceValueDirection > -1) {
            captureList.add(currentSquareValue);

            if (currentSquareValue != Piece.KING.getValue()) {
                currentSquareValue = lowestPieceValueDirectionAndValue.get(VALUE);

                if (lowestPieceValueDirection != 8) {
                    indexOfFirstAttackerInDirection[lowestPieceValueDirection] =
                            getIndexOfNextDirectionAttackerAfterIndex(
                                    board, exchangeSquare, lowestPieceValueDirection, indexOfFirstAttackerInDirection[lowestPieceValueDirection]);
                }

                return getCaptureList(
                        board,
                        captureList,
                        exchangeSquare,
                        currentSquareValue,
                        indexOfFirstAttackerInDirection,
                        whiteKnightAttackCount,
                        blackKnightAttackCount,
                        !isWhiteToMove);
            }
        }

        return captureList;
    }

    public static int getScoreFromCaptureList(List<Integer> captureList) {
        int score = 0;
        for (int i = captureList.size() - 1; i > 0; i--) {
            score = Math.max(0, captureList.get(i) - score);
        }
        return captureList.get(0) - score;
    }

    public static Map<String, Integer> getWeakestAttackerDirectionAndValue(EngineChessBoard board, int attackedSquare, int[] indexOfFirstAttackerInDirection, boolean isWhiteToMove) {

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

    public static int[] getIndexesOfFirstAttackersInEachDirection(EngineChessBoard board, int toSquare) {
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
        final Piece piece = board.getSquareOccupant(movedBitRef).getPiece();

        if (piece != Piece.NONE) {
            return isAttacker(board, direction, xInc, yInc, movedIndex, movedBitRef, piece) ? movedIndex : -1;
        }

        return getIndexOfNextDirectionAttackerAfterIndex(board, bitRef, direction, movedIndex);
    }

    private static boolean isAttacker(EngineChessBoard board, int direction, int xInc, int yInc, int movedIndex, int movedBitRef, Piece piece) {
        switch (piece) {
            case KING:
                return (movedIndex == 1);
            case PAWN:
                final int yIncWhereThisWouldBeAnAttackingPawn =
                        board.getSquareOccupant(movedBitRef) == SquareOccupant.WP ? -1 : 1;
                return ((movedIndex == 1)
                        && (yInc == yIncWhereThisWouldBeAnAttackingPawn)
                        && (xInc != 0));
            case QUEEN:
                return true;
            case ROOK:
                return ((direction & 1) == 0);
            case BISHOP:
                return ((direction & 1) != 0);
            case KNIGHT:
                return false;
            default:
                throw new UnexpectedEnumValue("Unexpected Piece enum");
        }
    }
}
