package com.netsensia.rivalchess.bitboards;

import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.SquareOccupant;

import java.util.Arrays;

import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getFirstOccupiedSquare;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.isBishopAttackingSquare;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.isRookAttackingSquare;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getPawnMovesCaptureOfColour;
import static com.netsensia.rivalchess.bitboards.BitboardConstantsKt.*;

public class EngineBitboards {

    private static EngineBitboards INSTANCE;

    private long[] pieceBitboards;

    public static EngineBitboards getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new EngineBitboards();
        }

        return INSTANCE;
    }

    private EngineBitboards () {
        reset();
    }

    public long getAllPieceBitboard() {
        return getPieceBitboard(BitboardType.ALL);
    }

    public void xorPieceBitboard(int i, long xorBy) {
        this.pieceBitboards[i] ^= xorBy;
    }

    public void xorPieceBitboard(BitboardType type, long xorBy) {
        this.pieceBitboards[type.getIndex()] ^= xorBy;
    }

    public void orPieceBitboard(BitboardType type, long xorBy) {
        this.pieceBitboards[type.getIndex()] |= xorBy;
    }

    public void reset() {
        pieceBitboards = new long[BitboardType.getNumBitboardTypes()];
        Arrays.fill(pieceBitboards, 0);
    }

    public void setPieceBitboard(BitboardType type, long bitboard) {
        pieceBitboards[type.getIndex()] = bitboard;
    }

    public long getPieceBitboard(BitboardType type) {
        return pieceBitboards[type.getIndex()];
    }

    public void movePiece(SquareOccupant piece, int compactMove) {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        pieceBitboards[piece.getIndex()] ^= fromMask | toMask;
    }

    public long getRookMovePiecesBitboard(Colour colour) {
        return colour == Colour.WHITE
                ? getPieceBitboard(BitboardType.WR) | getPieceBitboard(BitboardType.WQ)
                : getPieceBitboard(BitboardType.BR) | getPieceBitboard(BitboardType.BQ);
    }

    public long getBishopMovePiecesBitboard(Colour colour) {
        return colour == Colour.WHITE
                ? getPieceBitboard(BitboardType.WB) | getPieceBitboard(BitboardType.WQ)
                : getPieceBitboard(BitboardType.BB) | getPieceBitboard(BitboardType.BQ);
    }

    public boolean isSquareAttackedBy(final int attackedSquare, final Colour attacker) {

        if ((pieceBitboards[SquareOccupant.WN.ofColour(attacker).getIndex()] & getKnightMoves().get(attackedSquare)) != 0 ||
                (pieceBitboards[SquareOccupant.WK.ofColour(attacker).getIndex()] & getKingMoves().get(attackedSquare)) != 0 ||
                (pieceBitboards[SquareOccupant.WP.ofColour(attacker).getIndex()]
                        & getPawnMovesCaptureOfColour(attacker.opponent()).get(attackedSquare)) != 0)
            return true;

        long bitboardBishop = getBishopMovePiecesBitboard(attacker);

        while (bitboardBishop != 0) {
            final int pieceSquare = getFirstOccupiedSquare(bitboardBishop);
            bitboardBishop ^= (1L << (pieceSquare));
            if (isBishopAttackingSquare(attackedSquare, pieceSquare, getAllPieceBitboard())) {
                return true;
            }
        }

        long bitboardRook = getRookMovePiecesBitboard(attacker);

        while (bitboardRook != 0) {
            final int pieceSquare = Long.numberOfTrailingZeros(bitboardRook);
            bitboardRook ^= (1L << (pieceSquare));
            if (isRookAttackingSquare(attackedSquare, pieceSquare, getAllPieceBitboard())) {
                return true;
            }
        }

        return false;
    }
}
