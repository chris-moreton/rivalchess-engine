package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.SearchPath;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.sun.xml.internal.ws.api.pipe.Engine;

import java.util.ArrayList;
import java.util.List;

public class StaticExchangeEvaluatorPremium implements StaticExchangeEvaluator {

    public int staticExchangeEvaluation(EngineChessBoard board, EngineMove move) throws InvalidMoveException {
        final int captureSquare = move.compact & 63;
        final int captureValue = board.getSquareOccupant(captureSquare).getPiece().getValue();
        board.makeMove(move);
        final int seeScore = captureValue - seeSearch(board, captureSquare);
        board.unMakeMove();
        return seeScore;
    }

    public int seeSearch(EngineChessBoard board, int captureSquare) throws InvalidMoveException {

        final int evalScore = materialBalanceFromMoverPerspective(board);

        List<EngineMove> moves = getCaptureMovesOnSquare(captureSquare);

        for (EngineMove move : moves) {

        }

        int moveCount = 0;

        while (move != 0) {

            if (!shouldDeltaPrune(board, low, evalScore, move, isCheck)) {
                if (board.makeMove(new EngineMove(move))) {
                    legalMoveCount++;

                    newPath = quiesce(board, depth - 1, ply + 1, quiescePly + 1, -high, -low, (quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY && board.isCheck()));
                    newPath.score = -newPath.score;
                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath);
                    }
                    if (newPath.score >= high) {
                        board.unMakeMove();
                        return bestPath;
                    }
                    low = Math.max(low, newPath.score);

                    board.unMakeMove();
                }
            }

            move = getHighScoreMove(theseMoves);
        }

        if (isCheck && legalMoveCount == 0) {
            bestPath.score = -RivalConstants.VALUE_MATE;
        }

        return bestPath;
    }

    public int materialBalanceFromMoverPerspective(EngineChessBoard board) {
        return 0;
    }

    public List<EngineMove> getCaptureMovesOnSquare(int captureSquare) {
        return new ArrayList<>();
    }
}

