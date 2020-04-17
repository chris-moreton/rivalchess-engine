package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

import java.util.ArrayList;
import java.util.List;

public class StaticExchangeEvaluatorPremium implements StaticExchangeEvaluator {

    public int staticExchangeEvaluation(EngineChessBoard board, EngineMove move) throws InvalidMoveException {
        final int captureSquare = move.compact & 63;
        final int materialBalance = materialBalanceFromMoverPerspective(board);

        if (board.makeMove(move)) {
            final int seeValue = -seeSearch(board, captureSquare) - materialBalance;
            board.unMakeMove();
            return seeValue;
        }

        return -Integer.MAX_VALUE;
    }

    public int seeSearch(EngineChessBoard board, int captureSquare) throws InvalidMoveException {

        final int materialBalance = materialBalanceFromMoverPerspective(board);

        int bestScore = materialBalance;

        List<EngineMove> moves = getCaptureMovesOnSquare(board, captureSquare);

        for (EngineMove move : moves) {

            if (board.makeMove(move)) {
                final int seeScore = -seeSearch(board, captureSquare);
                board.unMakeMove();
                if (seeScore > bestScore) {
                    bestScore = seeScore;
                }
            }
        }
        
        return bestScore;
    }

    public int materialBalanceFromMoverPerspective(EngineChessBoard board) {
        final int whiteMaterial = board.getWhitePawnValues() + board.getWhitePieceValues();
        final int blackMaterial = board.getBlackPawnValues() + board.getBlackPieceValues();

        if (board.getMover() == Colour.WHITE) {
            return whiteMaterial - blackMaterial;
        }

        return blackMaterial - whiteMaterial;
    }

    public List<EngineMove> getCaptureMovesOnSquare(EngineChessBoard board, int captureSquare) {
        int[] moves = new int[Limit.MAX_LEGAL_MOVES.getValue()];
        final boolean includeChecks = false;
        board.setLegalQuiesceMoves(moves, includeChecks);
        List<EngineMove> moveList = new ArrayList<>();

        int moveNum = 0;
        int move = moves[moveNum];
        while (move != 0) {
            if ((move & 63) == captureSquare) {
                moveList.add(new EngineMove(move));
            }
            move = moves[++moveNum];
        }
        return moveList;
    }
}

