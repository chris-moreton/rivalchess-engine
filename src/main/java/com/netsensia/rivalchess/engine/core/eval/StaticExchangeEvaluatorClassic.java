package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

import java.util.ArrayList;
import java.util.List;

import static com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluationHelper.getCaptureList;
import static com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluationHelper.getIndexesOfFirstAttackersInEachDirection;
import static com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluationHelper.getScoreFromCaptureList;

public class StaticExchangeEvaluatorClassic implements StaticExchangeEvaluator {

    public int staticExchangeEvaluation(EngineChessBoard board, EngineMove move) throws InvalidMoveException {

        List<Integer> captureList = new ArrayList<>();
        final int exchangeSquare = move.compact & 63;

        captureList.add(((1L << exchangeSquare) == board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) ?
                Piece.PAWN.getValue() :
                RivalConstants.PIECE_VALUES.get(board.getSquareOccupant(exchangeSquare).getIndex()));

        if (!board.makeMove(new EngineMove(move.compact & ~RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL))) {
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

}
