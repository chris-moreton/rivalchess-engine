package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

import java.util.ArrayList;
import java.util.List;

public class StaticExchangeEvaluatorPremium implements StaticExchangeEvaluator {

    public int staticExchangeEvaluation(EngineChessBoard board, EngineMove move) throws InvalidMoveException {
        final int captureSquare = move.compact & 63;

        return seeSearch(board, captureSquare);
    }

    public int seeSearch(EngineChessBoard board, int captureSquare) throws InvalidMoveException {

        final int captureValue =
                (((1L << captureSquare) == board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) ?
                        Piece.PAWN.getValue() :
                        board.getSquareOccupant(captureSquare).getPiece().getValue());

        final int materialBalance = materialBalanceFromMoverPerspective(board);
        int bestScore = materialBalance;

        List<EngineMove> moves = getCaptureMovesOnSquare(captureSquare);

        for (EngineMove move : moves) {

            if (board.makeMove(move)) {
                final int seeScore = captureValue - seeSearch(board, captureSquare);
                board.unMakeMove();
                if (seeScore > bestScore) {
                    bestScore = seeScore;
                }
            }
        }

        return captureValue + (materialBalance - bestScore);

    }

    public int materialBalanceFromMoverPerspective(EngineChessBoard board) {
        final int whiteMaterial = board.getWhitePawnValues() + board.getWhitePieceValues();
        final int blackMaterial = board.getBlackPawnValues() + board.getBlackPieceValues();

        if (board.getMover() == Colour.WHITE) {
            return whiteMaterial - blackMaterial;
        }

        return blackMaterial - whiteMaterial;
    }

    public List<EngineMove> getCaptureMovesOnSquare(int captureSquare) {
        int[] moves = new int[RivalConstants.MAX_LEGAL_MOVES];
        List<EngineMove> moveList = new ArrayList<>();
        for (int move : moves) {
            if ((move & 63) == captureSquare) {
                moveList.add(new EngineMove(move));
            }
        }
        return moveList;
    }
}

