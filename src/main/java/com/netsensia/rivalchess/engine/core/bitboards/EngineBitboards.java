package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.RivalConstants;

import java.util.Arrays;

public class EngineBitboards {

    /**
     * @deprecated Use getters and setters
     */
    @Deprecated
    public long[] pieceBitboards;

    public EngineBitboards() {
        reset();
    }

    public void setPieceBitboard(int i, long bitboard) {
        pieceBitboards[i] = bitboard;
    }

    public long getPieceBitboard(int i) {
        return pieceBitboards[i];
    }

    public long getAllPieceBitboard() {
        return pieceBitboards[RivalConstants.ALL];
    }

    public void xorPieceBitboard(int i, long xorBy) {
        this.pieceBitboards[i] ^= xorBy;
    }

    public void reset() {
        pieceBitboards = new long[RivalConstants.NUM_BITBOARDS];
        Arrays.fill(pieceBitboards, 0);
    }

    public void movePiece(int piece, int compactMove) {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        pieceBitboards[piece] ^= fromMask | toMask;
    }

    public long getRookMovePiecesBitboard(Colour colour) {
        return colour == Colour.WHITE
                ? pieceBitboards[RivalConstants.WR] | pieceBitboards[RivalConstants.WQ]
                : pieceBitboards[RivalConstants.BR] | pieceBitboards[RivalConstants.BQ];
    }

    public long getBishopMovePiecesBitboard(Colour colour) {
        return colour == Colour.WHITE
                ? pieceBitboards[RivalConstants.WB] | pieceBitboards[RivalConstants.WQ]
                : pieceBitboards[RivalConstants.BB] | pieceBitboards[RivalConstants.BQ];
    }

    public boolean isSquareAttackedBy(final int attackedSquare, final Colour attacker) {

        if ((pieceBitboards[SquareOccupant.WN.ofColour(attacker)] & Bitboards.knightMoves.get(attackedSquare)) != 0 ||
                (pieceBitboards[SquareOccupant.WK.ofColour(attacker)] & Bitboards.kingMoves.get(attackedSquare)) != 0 ||
                (pieceBitboards[SquareOccupant.WP.ofColour(attacker)]
                        & Bitboards.getPawnMovesCaptureOfColour(attacker.opponent()).get(attackedSquare)) != 0)
            return true;

        long bitboardBishop = getBishopMovePiecesBitboard(attacker);

        while (bitboardBishop != 0) {
            final int pieceSquare = Bitboards.getFirstOccupiedSquare(bitboardBishop);
            bitboardBishop ^= (1L << (pieceSquare));
            if (Bitboards.isBishopAttackingSquare(attackedSquare, pieceSquare, getAllPieceBitboard())) {
                return true;
            }
        }

        long bitboardRook = getRookMovePiecesBitboard(attacker);

        while (bitboardRook != 0) {
            final int pieceSquare = Long.numberOfTrailingZeros(bitboardRook);
            bitboardRook ^= (1L << (pieceSquare));
            if (Bitboards.isRookAttackingSquare(attackedSquare, pieceSquare, getAllPieceBitboard())) {
                return true;
            }
        }

        return false;
    }
}
