package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;

public class Evaluate {

    public static int scoreRightWayPositions(EngineChessBoard board, int h1, int h2, int h3, int g2, int g3, int f1, int f2, int f3, int f4, int offset, int cornerColour) {
        int safety = 0;

        if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << h1)) == 0) {
            if ((board.m_pieceBitboards[RivalConstants.WR + offset] & (1L << f1)) != 0) {
                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f2)) != 0) {
                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                            // (A)
                            safety += 120;
                        } else {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WN + offset] & (1L << f3)) != 0) {
                                    // (D)
                                    safety += 70;
                                    // check for bishop of same colour as h3
                                    long bits = (cornerColour == RivalConstants.WHITE ? Bitboards.LIGHT_SQUARES : Bitboards.DARK_SQUARES);
                                    if ((bits & board.m_pieceBitboards[RivalConstants.WB + offset]) != 0) {
                                        safety -= 30;
                                    }
                                }
                            }
                        }
                    } else {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g3)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WB + offset] & (1L << g2)) != 0) {
                                    // (B)
                                    safety += 100;
                                }
                            } else {
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                    if ((board.m_pieceBitboards[RivalConstants.WB + offset] & (1L << g2)) != 0) {
                                        // (C)
                                        safety += 70;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f4)) != 0) {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                // (E)
                                safety += 50;
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                    if ((board.m_pieceBitboards[RivalConstants.WN + offset] & (1L << f3)) != 0) {
                                        safety += 40;
                                    }
                                }
                            }
                        }
                    } else {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f3)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                    // (F)
                                    safety += 25;
                                } else {
                                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                        // (H)
                                        safety -= 30;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return safety / RivalConstants.KINGSAFETY_RIGHTWAY_DIVISOR;
    }

}
