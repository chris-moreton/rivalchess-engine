package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.engine.core.bitboards.EngineBitboards;
import com.netsensia.rivalchess.engine.core.hash.BoardHashCalculator;
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.engine.core.type.MoveDetail;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.util.FenUtils;

import java.util.List;

public final class EngineChessBoard {

    private EngineBitboards engineBitboards = new EngineBitboards();
    private BoardHashCalculator hashCalculator = new ZorbristHashCalculator();

    private int castlePrivileges;
    private boolean isWhiteToMove;
    private byte whiteKingSquare;
    private byte blackKingSquare;
    private int numMovesMade;

    private final byte[] squareContents = new byte[64];

    private boolean isOnNullMove = false;

    private int[] legalMoves;
    private int numLegalMoves;

    private MoveDetail[] moveList;

    private int halfMoveCount = 0;

    public EngineChessBoard() throws IllegalFenException {
        this(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    }

    public EngineChessBoard(Board board) {
        initArrays();
        setBoard(board);
    }

    public void setBoard(Board board) {
        this.numMovesMade = 0;
        this.halfMoveCount = board.getHalfMoveCount();
        setBitboards(board);
    }

    private void initArrays() {
        int size = RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES;
        this.moveList = new MoveDetail[size];
        for (int i = 0; i < size; i++) {
            this.moveList[i] = new MoveDetail();
        }
    }

    public SquareOccupant getSquareOccupant(int bitRef) {

        return SquareOccupant.fromIndex(squareContents[bitRef]);
    }

    public Piece getPiece(int bitRef) {
        switch (SquareOccupant.fromIndex(squareContents[bitRef])) {
            case WP:
            case BP:
                return Piece.PAWN;
            case WB:
            case BB:
                return Piece.BISHOP;
            case WN:
            case BN:
                return Piece.KNIGHT;
            case WR:
            case BR:
                return Piece.ROOK;
            case WQ:
            case BQ:
                return Piece.QUEEN;
            case WK:
            case BK:
                return Piece.KING;
            default:
                return Piece.NONE;
        }
    }

    public boolean isSquareEmpty(int bitRef) {
        return squareContents[bitRef] == -1;
    }

    public boolean isCapture(int move) {
        int toSquare = move & 63;

        boolean isCapture = !isSquareEmpty(toSquare);

        if (!isCapture &&
                ((1L << toSquare) & engineBitboards.getPieceBitboard(RivalConstants.ENPASSANTSQUARE)) != 0 &&
                squareContents[(move >>> 16) & 63] % 6 == RivalConstants.WP) {
            isCapture = true;
        }
        return isCapture;
    }

    public boolean isGameOver() throws InvalidMoveException {
        generateLegalMoves();

        for (int i = 0; i< numLegalMoves; i++) {
            if (isMoveLegal(legalMoves[i])) {
                return false;
            }
        }

        return true;
    }

    public boolean isNonMoverInCheck() {
        return isWhiteToMove ?
                isSquareAttackedBy(blackKingSquare, Colour.WHITE) :
                isSquareAttackedBy(whiteKingSquare, Colour.BLACK);
    }

    public boolean isCheck() {
        return isWhiteToMove ?
                isSquareAttackedBy(whiteKingSquare, Colour.BLACK) :
                isSquareAttackedBy(blackKingSquare, Colour.WHITE);
    }

    public boolean isSquareAttackedBy(final int attackedSquare, final Colour attacker) {

        if ((engineBitboards.pieceBitboards[SquareOccupant.WN.ofColour(attacker)] & Bitboards.knightMoves.get(attackedSquare)) != 0 ||
                (engineBitboards.pieceBitboards[SquareOccupant.WK.ofColour(attacker)] & Bitboards.kingMoves.get(attackedSquare)) != 0 ||
                (engineBitboards.pieceBitboards[SquareOccupant.WP.ofColour(attacker)]
                        & Bitboards.getPawnMovesCaptureOfColour(attacker.opponent()).get(attackedSquare)) != 0)
            return true;

        int pieceSquare;

        long bitboardBishop =
                attacker == Colour.WHITE ? engineBitboards.pieceBitboards[RivalConstants.WB] | engineBitboards.pieceBitboards[RivalConstants.WQ] :
                        engineBitboards.pieceBitboards[RivalConstants.BB] | engineBitboards.pieceBitboards[RivalConstants.BQ];

        while (bitboardBishop != 0) {
            bitboardBishop ^= (1L << (pieceSquare = Long.numberOfTrailingZeros(bitboardBishop)));
            if ((Bitboards.magicBitboards.magicMovesBishop[pieceSquare][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[pieceSquare]) * MagicBitboards.magicNumberBishop[pieceSquare]) >>> MagicBitboards.magicNumberShiftsBishop[pieceSquare])] & (1L << attackedSquare)) != 0)
                return true;
        }

        long bitboardRook =
                attacker == Colour.WHITE ? engineBitboards.pieceBitboards[RivalConstants.WR] | engineBitboards.pieceBitboards[RivalConstants.WQ] :
                        engineBitboards.pieceBitboards[RivalConstants.BR] | engineBitboards.pieceBitboards[RivalConstants.BQ];

        while (bitboardRook != 0) {
            bitboardRook ^= (1L << (pieceSquare = Long.numberOfTrailingZeros(bitboardRook)));
            if ((Bitboards.magicBitboards.magicMovesRook[pieceSquare][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[pieceSquare]) * MagicBitboards.magicNumberRook[pieceSquare]) >>> MagicBitboards.magicNumberShiftsRook[pieceSquare])] & (1L << attackedSquare)) != 0)
                return true;
        }

        return false;
    }

    private void addPossiblePromotionMoves(final int fromSquareMoveMask, long bitboard, boolean queenCapturesOnly) {
        int toSquare;

        while (bitboard != 0) {
            bitboard ^= (1L << (toSquare = Long.numberOfTrailingZeros(bitboard)));

            if (toSquare >= 56 || toSquare <= 7) {
                this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN);
                if (!queenCapturesOnly) {
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT);
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK);
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP);
                }
            } else {
                this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare;
            }
        }
    }


    private void addMoves(int fromSquareMask, long bitboard) {
        int toSquare;

        while (bitboard != 0) {
            bitboard ^= (1L << (toSquare = Long.numberOfTrailingZeros(bitboard)));
            this.legalMoves[this.numLegalMoves++] = fromSquareMask | toSquare;
        }
    }

    public void generateLegalMoves() {

        clearLegalMovesArray();

        generateKnightMoves(this.isWhiteToMove ? engineBitboards.pieceBitboards[RivalConstants.WN] : engineBitboards.pieceBitboards[RivalConstants.BN]);

        generateKingMoves(this.isWhiteToMove ? this.whiteKingSquare : this.blackKingSquare);

        generatePawnMoves(this.isWhiteToMove ? engineBitboards.pieceBitboards[RivalConstants.WP] : engineBitboards.pieceBitboards[RivalConstants.BP],
                this.isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture);

        generateSliderMoves(RivalConstants.WR, RivalConstants.BR, Bitboards.magicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook);

        generateSliderMoves(RivalConstants.WB, RivalConstants.BB, Bitboards.magicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop);

        this.legalMoves[this.numLegalMoves] = 0;

    }

    private void clearLegalMovesArray() {
        this.numLegalMoves = 0;
        this.legalMoves[this.numLegalMoves] = 0;
    }

    private void generateSliderMoves(final int whitePieceConstant, final int blackPieceConstant, long[][] magicMovesRook, long[] occupancyMaskRook, long[] magicNumberRook, int[] magicNumberShiftsRook) {
        long rookBitboard;
        rookBitboard =
                this.isWhiteToMove ? engineBitboards.pieceBitboards[whitePieceConstant] | engineBitboards.pieceBitboards[RivalConstants.WQ]
                        : engineBitboards.pieceBitboards[blackPieceConstant] | engineBitboards.pieceBitboards[RivalConstants.BQ];

        while (rookBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(rookBitboard);
            rookBitboard ^= (1L << bitRef);
            addMoves(
                    bitRef << 16,
                    magicMovesRook[bitRef][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY]);
        }
    }

    private void generatePawnMoves(long pawnBitboard, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves) {
        int bitRef;
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            pawnBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(pawnBitboard)));
            bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.ALL];

            bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves);

            bitboardPawnMoves = getBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves);

            addPossiblePromotionMoves(bitRef << 16, bitboardPawnMoves, false);
        }
    }

    private void generateKingMoves(int kingSquare) {
        if (this.isWhiteToMove) {
            generateKingMoves(RivalConstants.CASTLEPRIV_WK, Bitboards.WHITEKINGSIDECASTLESQUARES, 3, Colour.BLACK, RivalConstants.CASTLEPRIV_WQ, Bitboards.WHITEQUEENSIDECASTLESQUARES, 4);
        } else {
            generateKingMoves(RivalConstants.CASTLEPRIV_BK, Bitboards.BLACKKINGSIDECASTLESQUARES, 59, Colour.WHITE, RivalConstants.CASTLEPRIV_BQ, Bitboards.BLACKQUEENSIDECASTLESQUARES, 60);
        }

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY]);
    }

    private void generateKingMoves(final int castlePriveleges, final long kingSideCastleSquares, int i, Colour opponent, int castleprivWq, long whitequeensidecastlesquares, int i4) {
        if ((castlePrivileges & castlePriveleges) != 0L && (engineBitboards.pieceBitboards[RivalConstants.ALL] & kingSideCastleSquares) == 0L) {
            if (!isSquareAttackedBy(i, opponent) && !isSquareAttackedBy(i-1, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (i << 16) | i-2;
            }
        }
        if ((castlePrivileges & castleprivWq) != 0L && (engineBitboards.pieceBitboards[RivalConstants.ALL] & whitequeensidecastlesquares) == 0L) {
            if (!isSquareAttackedBy(i, opponent) && !isSquareAttackedBy(i4, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (i << 16) | i4+1;
            }
        }
    }

    private void generateKnightMoves(long knightBitboard) {
        int bitRef;
        while (knightBitboard != 0) {
            knightBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(knightBitboard)));
            addMoves(bitRef << 16, Bitboards.knightMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY]);
        }
    }

    private long getBitboardPawnCaptureMoves(int bitRef, List<Long> bitboardMaskCapturePawnMoves, long bitboardPawnMoves) {
        bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.pieceBitboards[RivalConstants.ENEMY];

        if (this.isWhiteToMove) {
            if ((engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] & Bitboards.RANK_6) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
            }
        } else {
            if ((engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] & Bitboards.RANK_3) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
            }
        }
        return bitboardPawnMoves;
    }

    private long getBitboardPawnJumpMoves(long bitboardPawnMoves) {
        long bitboardJumpMoves;

        if (this.isWhiteToMove) {
            bitboardJumpMoves = bitboardPawnMoves << 8L; // if we can move one, maybe we can move two
            bitboardJumpMoves &= Bitboards.RANK_4; // only counts if move is to fourth rank
        } else {
            bitboardJumpMoves = bitboardPawnMoves >> 8L;
            bitboardJumpMoves &= Bitboards.RANK_5;
        }
        bitboardJumpMoves &= ~engineBitboards.pieceBitboards[RivalConstants.ALL]; // only if square empty
        bitboardPawnMoves |= bitboardJumpMoves;

        return bitboardPawnMoves;
    }

    public void generateLegalQuiesceMoves(boolean includeChecks) {

        final long possibleDestinations = engineBitboards.pieceBitboards[RivalConstants.ENEMY];

        clearLegalMovesArray();

        final int kingSquare = this.isWhiteToMove ? this.whiteKingSquare : this.blackKingSquare;
        final int enemyKingSquare = this.isWhiteToMove ? this.blackKingSquare : this.whiteKingSquare;

        generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                this.isWhiteToMove ? engineBitboards.pieceBitboards[RivalConstants.WN] : engineBitboards.pieceBitboards[RivalConstants.BN]);

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & possibleDestinations);

        generateQuiescePawnMoves(includeChecks,
                this.isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture,
                enemyKingSquare,
                this.isWhiteToMove ? engineBitboards.pieceBitboards[RivalConstants.WP] : engineBitboards.pieceBitboards[RivalConstants.BP]);

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Bitboards.magicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook, RivalConstants.WR, RivalConstants.BR);

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Bitboards.magicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop, RivalConstants.WB, RivalConstants.BB);

        this.legalMoves[this.numLegalMoves] = 0;
    }

    private void generateQuiesceSliderMoves(boolean includeChecks, int enemyKingSquare, long[][] magicMovesRook, long[] occupancyMaskRook, long[] magicNumberRook, int[] magicNumberShiftsRook, final int whiteSliderConstant, final int blackSliderConstant) {
        long rookBitboard;
        int bitRef;
        long rookCheckSquares = magicMovesRook[enemyKingSquare][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare]) >>> magicNumberShiftsRook[enemyKingSquare])];

        rookBitboard =
                this.isWhiteToMove ? engineBitboards.pieceBitboards[whiteSliderConstant] | engineBitboards.pieceBitboards[RivalConstants.WQ]
                        : engineBitboards.pieceBitboards[blackSliderConstant] | engineBitboards.pieceBitboards[RivalConstants.BQ];

        while (rookBitboard != 0) {
            rookBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(rookBitboard)));
            long rookMoves = magicMovesRook[bitRef][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY];
            if (includeChecks) {
                addMoves(bitRef << 16, rookMoves & (rookCheckSquares | engineBitboards.pieceBitboards[RivalConstants.ENEMY]));
            } else {
                addMoves(bitRef << 16, rookMoves & engineBitboards.pieceBitboards[RivalConstants.ENEMY]);
            }
        }
    }

    private void generateQuiescePawnMoves(boolean includeChecks, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves, int enemyKingSquare, long pawnBitboard) {
        int bitRef;
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            pawnBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(pawnBitboard)));

            bitboardPawnMoves = 0;

            if (includeChecks) {
                bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.ALL];

                bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves);

                if (this.isWhiteToMove) {
                    bitboardPawnMoves &= Bitboards.blackPawnMovesCapture.get(enemyKingSquare);
                } else {
                    bitboardPawnMoves &= Bitboards.whitePawnMovesCapture.get(enemyKingSquare);
                }
            }

            // promotions
            bitboardPawnMoves |= (bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.ALL]) & (Bitboards.RANK_1 | Bitboards.RANK_8);

            bitboardPawnMoves = getBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves);

            addPossiblePromotionMoves(bitRef << 16, bitboardPawnMoves, true);
        }
    }

    private void generateQuiesceKnightMoves(boolean includeChecks, int enemyKingSquare, long knightBitboard) {
        int bitRef;
        long possibleDestinations;
        while (knightBitboard != 0) {
            knightBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(knightBitboard)));
            if (includeChecks) {
                possibleDestinations = engineBitboards.pieceBitboards[RivalConstants.ENEMY] | (Bitboards.knightMoves.get(enemyKingSquare) & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY]);
            } else {
                possibleDestinations = engineBitboards.pieceBitboards[RivalConstants.ENEMY];
            }
            addMoves(bitRef << 16, Bitboards.knightMoves.get(bitRef) & possibleDestinations);
        }
    }

    public void setLegalQuiesceMoves(int[] moveArray, boolean includeChecks) {
        this.legalMoves = moveArray;
        generateLegalQuiesceMoves(includeChecks);
    }

    public void setLegalMoves(int[] moveArray) {
        this.legalMoves = moveArray;
        generateLegalMoves();
    }

    public void setBitboards(Board board) {
        byte bitNum;
        long bitSet;
        int pieceIndex;
        char piece;

        this.engineBitboards.pieceBitboards = new long[RivalConstants.NUM_BITBOARDS];

        for (int i = 0; i < 64; i++) {
            squareContents[i] = -1;
        }

        for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
            engineBitboards.pieceBitboards[i] = 0;
        }

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                bitNum = (byte) (63 - (8 * y) - x);
                bitSet = 1L << bitNum;
                piece = board.getPieceCode(x, y);
                switch (piece) {
                    case 'P':
                        squareContents[bitNum] = RivalConstants.WP;
                        pieceIndex = RivalConstants.WP;
                        break;
                    case 'p':
                        squareContents[bitNum] = RivalConstants.BP;
                        pieceIndex = RivalConstants.BP;
                        break;
                    case 'N':
                        squareContents[bitNum] = RivalConstants.WN;
                        pieceIndex = RivalConstants.WN;
                        break;
                    case 'n':
                        squareContents[bitNum] = RivalConstants.BN;
                        pieceIndex = RivalConstants.BN;
                        break;
                    case 'B':
                        squareContents[bitNum] = RivalConstants.WB;
                        pieceIndex = RivalConstants.WB;
                        break;
                    case 'b':
                        squareContents[bitNum] = RivalConstants.BB;
                        pieceIndex = RivalConstants.BB;
                        break;
                    case 'R':
                        squareContents[bitNum] = RivalConstants.WR;
                        pieceIndex = RivalConstants.WR;
                        break;
                    case 'r':
                        squareContents[bitNum] = RivalConstants.BR;
                        pieceIndex = RivalConstants.BR;
                        break;
                    case 'Q':
                        squareContents[bitNum] = RivalConstants.WQ;
                        pieceIndex = RivalConstants.WQ;
                        break;
                    case 'q':
                        squareContents[bitNum] = RivalConstants.BQ;
                        pieceIndex = RivalConstants.BQ;
                        break;
                    case 'K':
                        squareContents[bitNum] = RivalConstants.WK;
                        pieceIndex = RivalConstants.WK;
                        this.whiteKingSquare = bitNum;
                        break;
                    case 'k':
                        squareContents[bitNum] = RivalConstants.BK;
                        pieceIndex = RivalConstants.BK;
                        this.blackKingSquare = bitNum;
                        break;
                    default:
                        pieceIndex = -1;
                }
                if (pieceIndex != -1) {
                    engineBitboards.pieceBitboards[pieceIndex] = engineBitboards.pieceBitboards[pieceIndex] | bitSet;
                }
            }
        }

        this.isWhiteToMove = board.isWhiteToMove();

        int ep = board.getEnPassantFile();
        if (ep == -1) {
            engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0;
        } else {
            if (board.isWhiteToMove()) {
                engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (40 + (7 - ep));
            } else {
                engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (16 + (7 - ep));
            }
        }

        this.castlePrivileges = 0;
        this.castlePrivileges |= (board.isWhiteKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WK : 0);
        this.castlePrivileges |= (board.isWhiteQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WQ : 0);
        this.castlePrivileges |= (board.isBlackKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BK : 0);
        this.castlePrivileges |= (board.isBlackQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BQ : 0);

        calculateSupplementaryBitboards();
    }

    public void calculateSupplementaryBitboards() {
        if (this.isWhiteToMove) {
            engineBitboards.pieceBitboards[RivalConstants.FRIENDLY] =
                    engineBitboards.pieceBitboards[RivalConstants.WP] | engineBitboards.pieceBitboards[RivalConstants.WN] |
                            engineBitboards.pieceBitboards[RivalConstants.WB] | engineBitboards.pieceBitboards[RivalConstants.WQ] |
                            engineBitboards.pieceBitboards[RivalConstants.WK] | engineBitboards.pieceBitboards[RivalConstants.WR];

            engineBitboards.pieceBitboards[RivalConstants.ENEMY] =
                    engineBitboards.pieceBitboards[RivalConstants.BP] | engineBitboards.pieceBitboards[RivalConstants.BN] |
                            engineBitboards.pieceBitboards[RivalConstants.BB] | engineBitboards.pieceBitboards[RivalConstants.BQ] |
                            engineBitboards.pieceBitboards[RivalConstants.BK] | engineBitboards.pieceBitboards[RivalConstants.BR];
        } else {
            engineBitboards.pieceBitboards[RivalConstants.ENEMY] =
                    engineBitboards.pieceBitboards[RivalConstants.WP] | engineBitboards.pieceBitboards[RivalConstants.WN] |
                            engineBitboards.pieceBitboards[RivalConstants.WB] | engineBitboards.pieceBitboards[RivalConstants.WQ] |
                            engineBitboards.pieceBitboards[RivalConstants.WK] | engineBitboards.pieceBitboards[RivalConstants.WR];

            engineBitboards.pieceBitboards[RivalConstants.FRIENDLY] =
                    engineBitboards.pieceBitboards[RivalConstants.BP] | engineBitboards.pieceBitboards[RivalConstants.BN] |
                            engineBitboards.pieceBitboards[RivalConstants.BB] | engineBitboards.pieceBitboards[RivalConstants.BQ] |
                            engineBitboards.pieceBitboards[RivalConstants.BK] | engineBitboards.pieceBitboards[RivalConstants.BR];
        }

        this.engineBitboards.pieceBitboards[RivalConstants.ALL] = engineBitboards.pieceBitboards[RivalConstants.FRIENDLY] | engineBitboards.pieceBitboards[RivalConstants.ENEMY];
    }

    public boolean isNotOnNullMove() {
        return !this.isOnNullMove;
    }

    public void makeNullMove() {
        this.isWhiteToMove = !this.isWhiteToMove;

        long t = this.engineBitboards.pieceBitboards[RivalConstants.FRIENDLY];
        this.engineBitboards.pieceBitboards[RivalConstants.FRIENDLY] = this.engineBitboards.pieceBitboards[RivalConstants.ENEMY];
        this.engineBitboards.pieceBitboards[RivalConstants.ENEMY] = t;

        this.isOnNullMove = true;
    }

    public void unMakeNullMove() {
        makeNullMove();
        this.isOnNullMove = false;
    }

    public boolean makeMove(EngineMove engineMove) throws InvalidMoveException {
        final int compactMove = engineMove.compact;

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        final int capturePiece = this.squareContents[moveTo];
        final int movePiece = this.squareContents[moveFrom];

        this.moveList[this.numMovesMade].capturePiece = -1;
        this.moveList[this.numMovesMade].move = compactMove;
        this.moveList[this.numMovesMade].hashValue = hashCalculator.getHash(this);
        this.moveList[this.numMovesMade].isOnNullMove = this.isOnNullMove;
        this.moveList[this.numMovesMade].pawnHashValue = hashCalculator.getPawnHash(this);
        this.moveList[this.numMovesMade].halfMoveCount = (byte) this.halfMoveCount;
        this.moveList[this.numMovesMade].enPassantBitboard = this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
        this.moveList[this.numMovesMade].castlePrivileges = (byte) this.castlePrivileges;

        this.isOnNullMove = false;

        this.halfMoveCount++;

        this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0;

        this.moveList[this.numMovesMade].movePiece = (byte) movePiece;
        this.engineBitboards.pieceBitboards[movePiece] ^= fromMask | toMask;

        this.squareContents[moveFrom] = -1;
        this.squareContents[moveTo] = (byte) movePiece;

        if (this.isWhiteToMove) {
            if (movePiece == RivalConstants.WP) {
                this.halfMoveCount = 0;

                if ((toMask & Bitboards.RANK_4) != 0 && (fromMask & Bitboards.RANK_2) != 0) {
                    this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = fromMask << 8L;
                } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
                    this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= toMask >>> 8;
                    this.moveList[this.numMovesMade].capturePiece = RivalConstants.BP;
                    this.squareContents[moveTo - 8] = -1;
                } else if ((compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) != 0) {
                    switch (compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                            this.engineBitboards.pieceBitboards[RivalConstants.WQ] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.WQ;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                            this.engineBitboards.pieceBitboards[RivalConstants.WR] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.WR;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                            this.engineBitboards.pieceBitboards[RivalConstants.WN] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.WN;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                            this.engineBitboards.pieceBitboards[RivalConstants.WB] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.WB;
                            break;
                        default:
                            throw new InvalidMoveException(
                                    "compactMove " + compactMove + " produced invalid promotion piece");
                    }
                    this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask;
                }
            } else if (movePiece == RivalConstants.WR) {
                if (moveFrom == Bitboards.A1) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
                else if (moveFrom == Bitboards.H1) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
            } else if (movePiece == RivalConstants.WK) {
                this.whiteKingSquare = moveTo;
                this.castlePrivileges &= RivalConstants.CASTLEPRIV_WNONE;
                if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                    this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
                    this.squareContents[Bitboards.H1] = -1;
                    this.squareContents[Bitboards.F1] = RivalConstants.WR;

                } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                    this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
                    this.squareContents[Bitboards.A1] = -1;
                    this.squareContents[Bitboards.D1] = RivalConstants.WR;
                }
            }

            if (capturePiece >= RivalConstants.BP) {
                this.moveList[this.numMovesMade].capturePiece = (byte) capturePiece;
                this.halfMoveCount = 0;
                this.engineBitboards.pieceBitboards[capturePiece] ^= toMask;

                if (capturePiece != RivalConstants.BP) {
                    if (capturePiece == RivalConstants.BR) {
                        if (toMask == Bitboards.BLACKKINGSIDEROOKMASK)
                            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
                        else if (toMask == Bitboards.BLACKQUEENSIDEROOKMASK)
                            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
                    }
                }
            }
        } else {
            if (movePiece == RivalConstants.BP) {
                this.halfMoveCount = 0;

                if ((toMask & Bitboards.RANK_5) != 0 && (fromMask & Bitboards.RANK_7) != 0) {
                    this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = toMask << 8L;
                } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
                    this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask << 8;
                    this.moveList[this.numMovesMade].capturePiece = RivalConstants.WP;
                    this.squareContents[moveTo + 8] = -1;
                } else if ((compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) != 0) {
                    switch (compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                            this.engineBitboards.pieceBitboards[RivalConstants.BQ] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.BQ;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                            this.engineBitboards.pieceBitboards[RivalConstants.BR] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.BR;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                            this.engineBitboards.pieceBitboards[RivalConstants.BN] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.BN;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                            this.engineBitboards.pieceBitboards[RivalConstants.BB] |= toMask;
                            this.squareContents[moveTo] = RivalConstants.BB;
                            break;
                        default: throw new InvalidMoveException(
                                "compactMove " + compactMove + " produced invalid promotion piece");
                    }
                    this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= toMask;
                }
            } else if (movePiece == RivalConstants.BR) {
                if (moveFrom == Bitboards.A8) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
                else if (moveFrom == Bitboards.H8) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
            } else if (movePiece == RivalConstants.BK) {
                this.castlePrivileges &= RivalConstants.CASTLEPRIV_BNONE;
                this.blackKingSquare = moveTo;
                if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                    this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
                    this.squareContents[Bitboards.H8] = -1;
                    this.squareContents[Bitboards.F8] = RivalConstants.BR;
                } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                    this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
                    this.squareContents[Bitboards.A8] = -1;
                    this.squareContents[Bitboards.D8] = RivalConstants.BR;
                }
            }

            if (capturePiece != -1) {
                this.moveList[this.numMovesMade].capturePiece = (byte) capturePiece;

                this.halfMoveCount = 0;
                this.engineBitboards.pieceBitboards[capturePiece] ^= toMask;
                if (capturePiece != RivalConstants.WP) {
                    if (capturePiece == RivalConstants.WR) {
                        if (toMask == Bitboards.WHITEKINGSIDEROOKMASK)
                            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
                        else if (toMask == Bitboards.WHITEQUEENSIDEROOKMASK)
                            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
                    }
                }
            }
        }

        this.isWhiteToMove = !this.isWhiteToMove;
        this.numMovesMade++;

        calculateSupplementaryBitboards();

        if (isNonMoverInCheck()) {
            unMakeMove();
            return false;
        } else {
            return true;
        }
    }

    public void unMakeMove() throws InvalidMoveException {
        this.numMovesMade--;

        this.halfMoveCount = this.moveList[this.numMovesMade].halfMoveCount;
        this.isWhiteToMove = !this.isWhiteToMove;
        this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = this.moveList[this.numMovesMade].enPassantBitboard;
        this.castlePrivileges = this.moveList[this.numMovesMade].castlePrivileges;
        this.isOnNullMove = this.moveList[this.numMovesMade].isOnNullMove;

        final int fromSquare = (this.moveList[this.numMovesMade].move >>> 16) & 63;
        final int toSquare = this.moveList[this.numMovesMade].move & 63;
        final long fromMask = (1L << fromSquare);
        final long toMask = (1L << toSquare);

        this.squareContents[fromSquare] = this.moveList[this.numMovesMade].movePiece;
        this.squareContents[toSquare] = -1;

        // deal with en passants first, they are special moves and capture moves, so just get them out of the way
        if (!unMakeEnPassants(toSquare, fromMask, toMask)) {

            // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
            replaceCapturedPiece(toSquare, toMask);

            // for promotions, remove promotion piece from toSquare
            if (!removePromotionPiece(fromMask, toMask)) {

                // now that promotions are out of the way, we can remove the moving piece from toSquare and put it back on fromSquare
                final byte movePiece = replaceMovedPiece(fromSquare, fromMask, toMask);

                // for castles, replace the rook
                replaceCastledRook(fromMask, toMask, movePiece);
            }
        }

        calculateSupplementaryBitboards();
    }

    private byte replaceMovedPiece(int fromSquare, long fromMask, long toMask) {
        final byte movePiece = this.moveList[this.numMovesMade].movePiece;
        this.engineBitboards.pieceBitboards[movePiece] ^= toMask | fromMask;
        if (movePiece == RivalConstants.WK) this.whiteKingSquare = (byte) fromSquare;
        else if (movePiece == RivalConstants.BK) this.blackKingSquare = (byte) fromSquare;

        return movePiece;
    }

    private void replaceCastledRook(long fromMask, long toMask, byte movePiece) {
        if (movePiece == RivalConstants.WK) {
            if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H1] = RivalConstants.WR;
                this.squareContents[Bitboards.F1] = -1;
            } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A1] = RivalConstants.WR;
                this.squareContents[Bitboards.D1] = -1;

            }
        } else if (movePiece == RivalConstants.BK) {
            if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H8] = RivalConstants.BR;
                this.squareContents[Bitboards.F8] = -1;

            } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A8] = RivalConstants.BR;
                this.squareContents[Bitboards.D8] = -1;

            }
        }
    }

    private boolean removePromotionPiece(long fromMask, long toMask) throws InvalidMoveException {
        final int promotionPiece = this.moveList[this.numMovesMade].move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
        if (promotionPiece != 0) {
            if (this.isWhiteToMove) {
                this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= fromMask;

                switch (promotionPiece) {
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        this.engineBitboards.pieceBitboards[RivalConstants.WQ] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        this.engineBitboards.pieceBitboards[RivalConstants.WB] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        this.engineBitboards.pieceBitboards[RivalConstants.WN] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= toMask;
                        break;
                    default:
                        throw new InvalidMoveException("Illegal promotion piece " + promotionPiece);
                }
            } else {
                this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= fromMask;

                switch (promotionPiece) {
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        this.engineBitboards.pieceBitboards[RivalConstants.BQ] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        this.engineBitboards.pieceBitboards[RivalConstants.BB] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        this.engineBitboards.pieceBitboards[RivalConstants.BN] ^= toMask;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= toMask;
                        break;
                    default:
                        throw new InvalidMoveException("Invalid promotionPiece " + promotionPiece);
                }
            }
            return true;
        }
        return false;
    }

    public int getWhitePieceValues() {
        return
                Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WN.getIndex()]) * Piece.KNIGHT.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WR.getIndex()]) * Piece.ROOK.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WB.getIndex()]) * Piece.BISHOP.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WQ.getIndex()]) * Piece.QUEEN.getValue();

    }

    public int getBlackPieceValues() {
        return
                Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BN.getIndex()]) * Piece.KNIGHT.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BR.getIndex()]) * Piece.ROOK.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BB.getIndex()]) * Piece.BISHOP.getValue() +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BQ.getIndex()]) * Piece.QUEEN.getValue();

    }

    public int getWhitePawnValues() {
        return Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WP.getIndex()]) * Piece.PAWN.getValue();

    }

    public int getBlackPawnValues() {
        return Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BP.getIndex()]) * Piece.PAWN.getValue();
    }

    private void replaceCapturedPiece(int toSquare, long toMask) {
        final byte capturePiece = this.moveList[this.numMovesMade].capturePiece;
        if (capturePiece != -1) {
            this.squareContents[toSquare] = capturePiece;

            this.engineBitboards.pieceBitboards[capturePiece] ^= toMask;
        }
    }

    private boolean unMakeEnPassants(int toSquare, long fromMask, long toMask) {
        if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            if (this.moveList[this.numMovesMade].movePiece == RivalConstants.WP) {
                this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask | fromMask;
                this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= toMask >>> 8;
                this.squareContents[toSquare - 8] = RivalConstants.BP;


                return true;
            } else if (this.moveList[this.numMovesMade].movePiece == RivalConstants.BP) {
                this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= toMask | fromMask;
                this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask << 8;
                this.squareContents[toSquare + 8] = RivalConstants.WP;


                return true;
            }
        }
        return false;
    }

    public int lastCapturePiece() {
        return this.moveList[this.numMovesMade - 1].capturePiece;
    }

    public boolean wasCapture() {
        return this.moveList[this.numMovesMade - 1].capturePiece == -1;
    }

    public boolean wasPawnPush() {
        int toSquare = this.moveList[this.numMovesMade - 1].move & 63;
        int movePiece = this.moveList[this.numMovesMade - 1].movePiece;

        if (movePiece % 6 != RivalConstants.WP) return false;

        if (toSquare >= 48 || toSquare <= 15) return true;

        if (!isWhiteToMove) // white made the last move
        {
            if (toSquare >= 40)
                return (Long.bitCount(Bitboards.whitePassedPawnMask.get(toSquare) & engineBitboards.pieceBitboards[RivalConstants.BP]) == 0);
        } else {
            if (toSquare <= 23)
                return (Long.bitCount(Bitboards.blackPassedPawnMask.get(toSquare) & engineBitboards.pieceBitboards[RivalConstants.WP]) == 0);
        }

        return false;
    }

    public long getWhitePawnBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WP];
    }

    public long getBlackPawnBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BP];
    }

    public long getWhiteKingBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WK];
    }

    public long getBlackKingBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BK];
    }

    public long getWhiteQueenBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WQ];
    }

    public long getBlackQueenBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BQ];
    }

    public long getWhiteRookBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WR];
    }

    public long getBlackRookBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BR];
    }

    public long getWhiteKnightBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WN];
    }

    public long getBlackKnightBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BN];
    }

    public long getWhiteBishopBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.WB];
    }

    public long getBlackBishopBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.BB];
    }

    public long getAllPiecesBitboard() {
        return engineBitboards.pieceBitboards[RivalConstants.ALL];
    }

    public int getWhiteKingSquare() {
        return whiteKingSquare;
    }

    public int getBlackKingSquare() {
        return blackKingSquare;
    }

    public long getBitboardByIndex(int index) {
        return engineBitboards.pieceBitboards[index];
    }

    public int getHalfMoveCount() {
        return halfMoveCount;
    }

    public int getCastlePrivileges() {
        return castlePrivileges;
    }

    public Colour getMover() {
        return isWhiteToMove ? Colour.WHITE : Colour.BLACK;
    }

    public int getNumLegalMoves() {
        return numLegalMoves;
    }

    public int getLegalMoveByIndex(int index) {
        return legalMoves[index];
    }

    public int previousOccurrencesOfThisPosition() {
        int occurrences = 0;
        for (int i = this.numMovesMade - 2; i >= 0 && i >= this.numMovesMade - this.halfMoveCount; i -= 2) {
            if (this.moveList[i].hashValue == hashCalculator.getHash(this)) {
                occurrences++;
            }
        }

        return occurrences;
    }

    public String getFen() {

        StringBuilder fen = getFenBoard();

        fen.append(' ');
        fen.append(isWhiteToMove ? 'w' : 'b');
        fen.append(' ');

        boolean noPrivs = true;

        if ((castlePrivileges & RivalConstants.CASTLEPRIV_WK) != 0) {
            fen.append('K');
            noPrivs = false;
        }
        if ((castlePrivileges & RivalConstants.CASTLEPRIV_WQ) != 0) {
            fen.append('Q');
            noPrivs = false;
        }
        if ((castlePrivileges & RivalConstants.CASTLEPRIV_BK) != 0) {
            fen.append('k');
            noPrivs = false;
        }
        if ((castlePrivileges & RivalConstants.CASTLEPRIV_BQ) != 0) {
            fen.append('q');
            noPrivs = false;
        }

        if (noPrivs) fen.append('-');

        fen.append(' ');
        long bitboard = engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
        if (Long.bitCount(bitboard) > 0) {
            int epSquare = Long.numberOfTrailingZeros(bitboard);
            char file = (char) (7 - (epSquare % 8));
            char rank = (char) (epSquare <= 23 ? 2 : 5);
            fen.append((char) (file + 'a'));
            fen.append((char) (rank + '1'));
        } else {
            fen.append('-');
        }

        fen.append(' ');
        fen.append(getHalfMoveCount());
        fen.append(' ');
        fen.append(numMovesMade / 2 + 1);
        return fen.toString();
    }

    private StringBuilder getFenBoard() {

        char[] board = getCharBoard();
        StringBuilder fen = new StringBuilder();

        char spaces = '0';
        for (int i = 63; i >= 0; i--) {

            if (board[i] == 0) {
                spaces++;
            } else {
                if (spaces > '0') {
                    fen.append(spaces);
                    spaces = '0';
                }
                fen.append(board[i]);
            }

            if (i % 8 == 0) {
                if (spaces > '0') {
                    fen.append(spaces);
                    spaces = '0';
                }

                if (i > 0) {
                    fen.append('/');
                }
            }
        }

        return fen;
    }

    private char[] getCharBoard() {
        char[] board = new char[64];
        char[] pieces = new char[]{'P', 'N', 'B', 'Q', 'K', 'R', 'p', 'n', 'b', 'q', 'k', 'r'};

        for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
            List<Integer> bitsSet = Bitboards.getSetBits(engineBitboards.pieceBitboards[i]);
            for (int bitSet : bitsSet) {
                board[bitSet] = pieces[i];
            }
        }
        return board;
    }

    public boolean isMoveLegal(int moveToVerify) throws InvalidMoveException {
        moveToVerify &= 0x00FFFFFF;
        int[] moves = new int[RivalConstants.MAX_GAME_MOVES];
        this.setLegalMoves(moves);
        int i = 0;
        while (moves[i] != 0) {
            EngineMove engineMove = new EngineMove(moves[i] & 0x00FFFFFF);
            if (this.makeMove(engineMove)) {
                if (engineMove.compact == moveToVerify) {
                    this.unMakeMove();
                    return true;
                }
                this.unMakeMove();
            }
            i++;
        }
        return false;
    }

    public long pawnHashCode() {
        return hashCalculator.getPawnHash(this);
    }

    public long boardHashCode() {
        return hashCalculator.getHash(this);
    }

    @Override
    public int hashCode() {
        return squareContents.hashCode() + (this.getMover() == Colour.WHITE ? 1 : 0);
    }

}
