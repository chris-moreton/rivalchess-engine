package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.model.Colour;

public class Evaluate {

    private static final int KINGSAFETY_RIGHTWAY_DIVISOR = 4;

    private Evaluate() {}

    private static int scoreRightWayPositions(EngineChessBoard board, int h1, int h2, int h3, int g2, int g3, int f1, int f2, int f3, int f4, boolean isWhite, int cornerColour) {
        int safety = 0;
        final int offset = isWhite ? 0 : 6;

        if (((board.getAllPiecesBitboard() & (1L << h1)) != 0) ||
                ((board.getBitboardByIndex(RivalConstants.WR + offset) & (1L << f1)) == 0)) {
            return 0;
        }

        if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << f2)) != 0) {
            if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << g2)) != 0) {
                safety = checkForPositionsAOrD(board, h2, h3, f3, isWhite, cornerColour, safety);
            } else {
                safety = checkForPositionsBOrC(board, h2, h3, g2, g3, isWhite, safety);
            }
        } else {
            if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << f4)) != 0) {
                safety = checkForPositionE(board, h2, g2, f3, isWhite, safety);
            } else {
                safety = checkForPositionFOrH(board, h2, h3, g2, f3, isWhite, safety);
            }
        }

        return safety / KINGSAFETY_RIGHTWAY_DIVISOR;
    }

    private static int checkForPositionFOrH(EngineChessBoard board, int h2, int h3, int g2, int f3, boolean isWhite, int safety) {
        final int offset = isWhite ? 0 : 6;

        if (((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << f3)) != 0)
            && ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << g2)) != 0)) {
                if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h2)) != 0) {
                    // (F)
                    safety -= 10;
                } else {
                    if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h3)) != 0) {
                        // (H)
                        safety -= 30;
                    }
                }
        }
        return safety;
    }

    private static int checkForPositionE(EngineChessBoard board, int h2, int g2, int f3, boolean isWhite, int safety) {
        final int offset = isWhite ? 0 : 6;

        if (((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << g2)) != 0)
            && ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h2)) != 0)) {
                // (E)
                safety += 80;
                if (((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h2)) != 0)
                    && ((board.getBitboardByIndex(RivalConstants.WN + offset) & (1L << f3)) != 0)) {
                        safety += 40;
                    }
        }
        return safety;
    }

    private static int checkForPositionsBOrC(EngineChessBoard board, int h2, int h3, int g2, int g3, boolean isWhite, int safety) {
        final int offset = isWhite ? 0 : 6;

        if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << g3)) != 0) {
            if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h2)) != 0) {
                if ((board.getBitboardByIndex(RivalConstants.WB + offset) & (1L << g2)) != 0) {
                    // (B)
                    safety += 100;
                }
            } else {
                if (((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h3)) != 0)
                    && ((board.getBitboardByIndex(RivalConstants.WB + offset) & (1L << g2)) != 0)) {
                        // (C)
                        safety += 70;
                }
            }
        }
        return safety;
    }

    private static int checkForPositionsAOrD(EngineChessBoard board, int h2, int h3, int f3, boolean isWhite, int cornerColour, int safety) {
        final int offset = isWhite ? 0 : 6;

        if ((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h2)) != 0) {
            // (A)
            safety += 120;
        } else {
            safety = checkForPositionD(board, h3, f3, isWhite, cornerColour, safety);
        }

        return safety;
    }

    private static int checkForPositionD(EngineChessBoard board, int h3, int f3, boolean isWhite, int cornerColour, int safety) {
        final int offset = isWhite ? 0 : 6;

        if (((board.getBitboardByIndex(RivalConstants.WP + offset) & (1L << h3)) != 0)
            && ((board.getBitboardByIndex(RivalConstants.WN + offset) & (1L << f3)) != 0)) {
                // (D)
                safety += 70;
                // check for bishop of same colour as h3
                long bits = (cornerColour == Colour.WHITE.getValue() ? Bitboards.LIGHT_SQUARES : Bitboards.DARK_SQUARES);
                if ((bits & board.getBitboardByIndex(RivalConstants.WB + offset)) != 0) {
                    safety -= 30;
            }
        }
        return safety;
    }

    public static int getWhiteKingRightWayScore(EngineChessBoard engineChessBoard) {

        if (engineChessBoard.getWhiteKingSquare() == 1 || engineChessBoard.getWhiteKingSquare() == 8) {
            return scoreRightWayPositions(engineChessBoard,
                    0, 8, 16, 9, 17, 2, 10, 18, 26, true,
                    Colour.WHITE.getValue());
        }

        return 0;
    }

    public static int getBlackKingRightWayScore(EngineChessBoard engineChessBoard) {
        if (engineChessBoard.getBlackKingSquare() == 57 || engineChessBoard.getBlackKingSquare() == 48) {
            return scoreRightWayPositions(engineChessBoard,
                    56, 48, 40, 49, 41, 58, 50, 42, 34,false,
                    Colour.BLACK.getValue());
        }

        return 0;
    }
}
