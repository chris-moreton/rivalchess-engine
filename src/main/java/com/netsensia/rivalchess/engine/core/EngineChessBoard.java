package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.engine.core.eval.PieceValue;
import com.netsensia.rivalchess.enums.BitboardType;
import com.netsensia.rivalchess.engine.core.bitboards.EngineBitboards;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.engine.core.type.MoveDetail;
import com.netsensia.rivalchess.enums.PromotionPieceMask;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.util.FenUtils;
import com.sun.corba.se.impl.activation.ProcessMonitorThread;

import java.util.Arrays;
import java.util.List;

import static com.netsensia.rivalchess.model.SquareOccupant.WK;

/**
 * Represents the state of a chessboard including bitboard states and the Zorbrish hash value.
 */
public final class EngineChessBoard {

    final private EngineBitboards engineBitboards = new EngineBitboards();
    final private BoardHash boardHash = new BoardHash();

    private int castlePrivileges;
    private boolean isWhiteToMove;
    private byte whiteKingSquare;
    private byte blackKingSquare;
    private int numMovesMade;

    private final SquareOccupant[] squareContents = new SquareOccupant[64];

    private boolean isOnNullMove = false;

    private int[] legalMoves;
    private int numLegalMoves;

    private MoveDetail[] moveList;

    private int halfMoveCount = 0;

    public EngineChessBoard() {
        this(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    }

    public EngineChessBoard(Board board) {
        initArrays();
        setBoard(board);
    }

    public void setBoard(Board board) {
        this.numMovesMade = 0;
        this.halfMoveCount = board.getHalfMoveCount();
        setEngineBoardVars(board);
        boardHash.setHashTableVersion(0);
        boardHash.setHashSizeMB(RivalConstants.DEFAULT_HASHTABLE_SIZE_MB);
        boardHash.initialiseHashCode(this);
    }

    private void initArrays() {
        int size = RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES;
        this.moveList = new MoveDetail[size];
        for (int i = 0; i < size; i++) {
            this.moveList[i] = new MoveDetail();
        }
    }

    public BoardHash getBoardHashObject() {
        return boardHash;
    }

    public SquareOccupant getSquareOccupant(int bitRef) {

        return squareContents[bitRef];
    }

    public Piece getPiece(int bitRef) {
        switch (squareContents[bitRef]) {
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
        return squareContents[bitRef] == SquareOccupant.NONE;
    }

    public boolean isCapture(int move) {
        int toSquare = move & 63;

        boolean isCapture = !isSquareEmpty(toSquare);

        if (!isCapture &&
                ((1L << toSquare) & engineBitboards.getPieceBitboard(RivalConstants.ENPASSANTSQUARE)) != 0 &&
                squareContents[(move >>> 16) & 63].getPiece() == Piece.PAWN) {
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
                engineBitboards.isSquareAttackedBy(blackKingSquare, Colour.WHITE) :
                engineBitboards.isSquareAttackedBy(whiteKingSquare, Colour.BLACK);
    }

    public boolean isCheck() {
        return isWhiteToMove ?
                engineBitboards.isSquareAttackedBy(whiteKingSquare, Colour.BLACK) :
                engineBitboards.isSquareAttackedBy(blackKingSquare, Colour.WHITE);
    }

    public EngineBitboards getEngineBitboards() {
        return engineBitboards;
    }

    private void addPossiblePromotionMoves(final int fromSquareMoveMask, long bitboard, boolean queenCapturesOnly) {

        while (bitboard != 0) {
            final int toSquare = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << (toSquare));

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
        while (bitboard != 0) {
            final int toSquare = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << (toSquare));
            this.legalMoves[this.numLegalMoves++] = fromSquareMask | toSquare;
        }
    }

    public void generateLegalMoves() {

        clearLegalMovesArray();

        generateKnightMoves(this.isWhiteToMove
                ? engineBitboards.getPieceBitboard(BitboardType.WN)
                : engineBitboards.getPieceBitboard(BitboardType.BN));

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
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(pawnBitboard);
            pawnBitboard ^= (1L << (bitRef));
            bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.ALL];

            bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves);

            bitboardPawnMoves = getBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves);

            addPossiblePromotionMoves(bitRef << 16, bitboardPawnMoves, false);
        }
    }

    private void generateKingMoves(int kingSquare) {

        if (this.isWhiteToMove) {
            generateWhiteKingMoves();
        } else {
            generateBlackKingMoves();
        }

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY]);
    }

    private void generateWhiteKingMoves() {

        final int whiteKingStartSquare = 3;
        final int whiteQueenStartSquare = 4;
        final Colour opponent = Colour.BLACK;

        if ((castlePrivileges & RivalConstants.CASTLEPRIV_WK) != 0L &&
            (engineBitboards.pieceBitboards[RivalConstants.ALL] & Bitboards.WHITEKINGSIDECASTLESQUARES) == 0L &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare-1, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (whiteKingStartSquare << 16) | whiteKingStartSquare-2;
        }

        if ((castlePrivileges & RivalConstants.CASTLEPRIV_WQ) != 0L &&
            (engineBitboards.pieceBitboards[RivalConstants.ALL] & Bitboards.WHITEQUEENSIDECASTLESQUARES) == 0L &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
            !engineBitboards.isSquareAttackedBy(whiteQueenStartSquare, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (whiteKingStartSquare << 16) | whiteQueenStartSquare+1;
        }
    }

    private void generateBlackKingMoves() {

        final int blackKingStartSquare = 59;
        final Colour opponent = Colour.WHITE;
        final int blackQueenStartSquare = 60;

        if ((castlePrivileges & RivalConstants.CASTLEPRIV_BK) != 0L &&
                (engineBitboards.pieceBitboards[RivalConstants.ALL] & Bitboards.BLACKKINGSIDECASTLESQUARES) == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare-1, opponent)) {
            this.legalMoves[this.numLegalMoves++] = (blackKingStartSquare << 16) | blackKingStartSquare-2;
        }

        if ((castlePrivileges & RivalConstants.CASTLEPRIV_BQ) != 0L &&
                (engineBitboards.pieceBitboards[RivalConstants.ALL] & Bitboards.BLACKQUEENSIDECASTLESQUARES) == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackQueenStartSquare, opponent)) {
            this.legalMoves[this.numLegalMoves++] = (blackKingStartSquare << 16) | blackQueenStartSquare+1;
        }
    }

    private void generateKnightMoves(long knightBitboard) {
        while (knightBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(knightBitboard);
            knightBitboard ^= (1L << (bitRef));
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

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.ROOK, RivalConstants.WR, RivalConstants.BR);

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.BISHOP, RivalConstants.WB, RivalConstants.BB);

        this.legalMoves[this.numLegalMoves] = 0;
    }

    private void generateQuiesceSliderMoves(boolean includeChecks, int enemyKingSquare, Piece piece, final int whiteSliderConstant, final int blackSliderConstant) {

        final long[][] magicMovesRook = piece == Piece.ROOK ? Bitboards.magicBitboards.magicMovesRook : Bitboards.magicBitboards.magicMovesBishop;
        final long[] occupancyMaskRook = piece == Piece.ROOK ? MagicBitboards.occupancyMaskRook : MagicBitboards.occupancyMaskBishop;
        final long[] magicNumberRook = piece == Piece.ROOK ? MagicBitboards.magicNumberRook : MagicBitboards.magicNumberBishop;
        final int[] magicNumberShiftsRook = piece == Piece.ROOK ? MagicBitboards.magicNumberShiftsRook : MagicBitboards.magicNumberShiftsBishop;

        final long rookCheckSquares = magicMovesRook[enemyKingSquare][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare]) >>> magicNumberShiftsRook[enemyKingSquare])];

        long pieceBitboard =
                this.isWhiteToMove ? engineBitboards.pieceBitboards[whiteSliderConstant] | engineBitboards.pieceBitboards[RivalConstants.WQ]
                        : engineBitboards.pieceBitboards[blackSliderConstant] | engineBitboards.pieceBitboards[RivalConstants.BQ];

        while (pieceBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(pieceBitboard);
            pieceBitboard ^= (1L << (bitRef));
            long pieceMoves = magicMovesRook[bitRef][(int) (((engineBitboards.pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~engineBitboards.pieceBitboards[RivalConstants.FRIENDLY];
            if (includeChecks) {
                addMoves(bitRef << 16, pieceMoves & (rookCheckSquares | engineBitboards.pieceBitboards[RivalConstants.ENEMY]));
            } else {
                addMoves(bitRef << 16, pieceMoves & engineBitboards.pieceBitboards[RivalConstants.ENEMY]);
            }
        }
    }

    private void generateQuiescePawnMoves(boolean includeChecks, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves, int enemyKingSquare, long pawnBitboard) {
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(pawnBitboard);
            pawnBitboard ^= (1L << (bitRef));

            bitboardPawnMoves = 0;

            if (includeChecks) {

                bitboardPawnMoves = getBitboardPawnJumpMoves(
                        bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.pieceBitboards[RivalConstants.ALL]);

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
        long possibleDestinations;
        while (knightBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(knightBitboard);
            knightBitboard ^= (1L << (bitRef));
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

    public void setEngineBoardVars(Board board) {
        this.isWhiteToMove = board.isWhiteToMove();

        engineBitboards.reset();

        setSquareContents(board);
        setEnPassantBitboard(board);
        setCastlePrivileges(board);
        calculateSupplementaryBitboards();
    }

    private void setSquareContents(Board board) {
        byte bitNum;
        long bitSet;
        int pieceIndex;

        Arrays.fill(squareContents, (byte)-1);

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                bitNum = (byte) (63 - (8 * y) - x);
                bitSet = 1L << bitNum;
                final SquareOccupant squareOccupant = board.getSquareOccupant(x, y);
                squareContents[bitNum] = squareOccupant;
                pieceIndex = squareOccupant.getIndex();

                if (pieceIndex != -1) {
                    engineBitboards.pieceBitboards[pieceIndex] |= bitSet;
                }
            }
        }
    }

    private void setEnPassantBitboard(Board board) {
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
    }

    private void setCastlePrivileges(Board board) {
        this.castlePrivileges = 0;
        this.castlePrivileges |= (board.isWhiteKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WK : 0);
        this.castlePrivileges |= (board.isWhiteQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WQ : 0);
        this.castlePrivileges |= (board.isBlackKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BK : 0);
        this.castlePrivileges |= (board.isBlackQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BQ : 0);
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

        boardHash.makeNullMove();

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

        final SquareOccupant capturePiece = this.squareContents[moveTo];
        final SquareOccupant movePiece = this.squareContents[moveFrom];

        this.moveList[this.numMovesMade].capturePiece = SquareOccupant.NONE;
        this.moveList[this.numMovesMade].move = compactMove;

        this.moveList[this.numMovesMade].hashValue = boardHash.getTrackedHashValue();
        this.moveList[this.numMovesMade].isOnNullMove = this.isOnNullMove;
        this.moveList[this.numMovesMade].pawnHashValue = boardHash.getTrackedPawnHashValue();
        this.moveList[this.numMovesMade].halfMoveCount = (byte) this.halfMoveCount;
        this.moveList[this.numMovesMade].enPassantBitboard = this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
        this.moveList[this.numMovesMade].castlePrivileges = (byte) this.castlePrivileges;
        this.moveList[this.numMovesMade].movePiece = movePiece;

        boardHash.move(this, engineMove);

        this.isOnNullMove = false;

        this.halfMoveCount++;

        engineBitboards.setPieceBitboard(RivalConstants.ENPASSANTSQUARE, 0);

        engineBitboards.movePiece(movePiece, compactMove);

        this.squareContents[moveFrom] = SquareOccupant.NONE;
        this.squareContents[moveTo] = movePiece;

        makeNonTrivialMoveTypeAdjustments(compactMove, capturePiece, movePiece);

        this.isWhiteToMove = !this.isWhiteToMove;
        this.numMovesMade++;

        calculateSupplementaryBitboards();

        if (isNonMoverInCheck()) {
            unMakeMove();
            return false;
        }

        return true;
    }

    private void makeNonTrivialMoveTypeAdjustments(int compactMove, SquareOccupant capturePiece, SquareOccupant movePiece) throws InvalidMoveException {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long toMask = 1L << moveTo;

        if (this.isWhiteToMove) {
            if (movePiece == SquareOccupant.WP) {
                makeSpecialWhitePawnMoveAdjustments(compactMove);
            } else if (movePiece == SquareOccupant.WR) {
                adjustCastlePrivilegesForWhiteRookMove(moveFrom);
            } else if (movePiece == SquareOccupant.WK) {
                adjustKingVariablesForWhiteKingMove(compactMove);
            }
            if (capturePiece.getColour() == Colour.BLACK) {
                makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece, toMask);
            }
        } else {
            if (movePiece == SquareOccupant.BP) {
                makeSpecialBlackPawnMoveAdjustments(compactMove);
            } else if (movePiece == SquareOccupant.BR) {
                adjustCastlePrivilegesForBlackRookMove(moveFrom);
            } else if (movePiece == SquareOccupant.BK) {
                adjustKingVariablesForBlackKingMove(compactMove);
            }
            if (capturePiece != SquareOccupant.NONE) {
                makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece, toMask);
            }
        }
    }

    private void makeAdjustmentsFollowingCaptureOfWhitePiece(SquareOccupant capturePiece, long toMask) {
        this.moveList[this.numMovesMade].capturePiece = capturePiece;

        this.halfMoveCount = 0;
        this.engineBitboards.pieceBitboards[capturePiece.getIndex()] ^= toMask;
        if (capturePiece != SquareOccupant.WP && capturePiece == SquareOccupant.WR) {
            if (toMask == Bitboards.WHITEKINGSIDEROOKMASK) {
                this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
            }
            else if (toMask == Bitboards.WHITEQUEENSIDEROOKMASK) {
                this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
            }
        }
    }

    private void adjustKingVariablesForBlackKingMove(int compactMove) {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.castlePrivileges &= RivalConstants.CASTLEPRIV_BNONE;
        this.blackKingSquare = moveTo;
        if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
            this.squareContents[Bitboards.H8] = SquareOccupant.NONE;
            this.squareContents[Bitboards.F8] = SquareOccupant.BR;
        } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
            this.squareContents[Bitboards.A8] = SquareOccupant.NONE;
            this.squareContents[Bitboards.D8] = SquareOccupant.BR;
        }
    }

    private void adjustCastlePrivilegesForBlackRookMove(byte moveFrom) {
        if (moveFrom == Bitboards.A8) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
        else if (moveFrom == Bitboards.H8) this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
    }

    private void makeSpecialBlackPawnMoveAdjustments(int compactMove) throws InvalidMoveException {
        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.halfMoveCount = 0;

        if ((toMask & Bitboards.RANK_5) != 0 && (fromMask & Bitboards.RANK_7) != 0) {
            this.engineBitboards.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = toMask << 8L;
        } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask << 8;
            this.moveList[this.numMovesMade].capturePiece = SquareOccupant.WP;
            this.squareContents[moveTo + 8] = SquareOccupant.NONE;
        } else if ((compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) != 0) {
            final int promotionPieceMask = compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
            switch (PromotionPieceMask.fromValue(promotionPieceMask)) {
                case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                    this.engineBitboards.pieceBitboards[RivalConstants.BQ] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.BQ;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                    this.engineBitboards.pieceBitboards[RivalConstants.BR] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.BR;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                    this.engineBitboards.pieceBitboards[RivalConstants.BN] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.BN;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                    this.engineBitboards.pieceBitboards[RivalConstants.BB] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.BB;
                    break;
                default: throw new InvalidMoveException(
                        "compactMove " + compactMove + " produced invalid promotion piece");
            }
            this.engineBitboards.pieceBitboards[RivalConstants.BP] ^= toMask;
        }
    }

    private void makeAdjustmentsFollowingCaptureOfBlackPiece(SquareOccupant capturePiece, long toMask) {
        this.moveList[this.numMovesMade].capturePiece = capturePiece;
        this.halfMoveCount = 0;
        this.engineBitboards.pieceBitboards[capturePiece.getIndex()] ^= toMask;

        if (capturePiece != SquareOccupant.BP && capturePiece == SquareOccupant.BR) {
            if (toMask == Bitboards.BLACKKINGSIDEROOKMASK) {
                this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
            }
            else if (toMask == Bitboards.BLACKQUEENSIDEROOKMASK) {
                this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
            }
        }
    }

    private void adjustKingVariablesForWhiteKingMove(int compactMove) {
        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.whiteKingSquare = moveTo;
        this.castlePrivileges &= RivalConstants.CASTLEPRIV_WNONE;
        if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
            this.squareContents[Bitboards.H1] = SquareOccupant.NONE;
            this.squareContents[Bitboards.F1] = SquareOccupant.WR;

        } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
            this.squareContents[Bitboards.A1] = SquareOccupant.NONE;
            this.squareContents[Bitboards.D1] = SquareOccupant.WR;
        }
    }

    private void adjustCastlePrivilegesForWhiteRookMove(byte moveFrom) {
        if (moveFrom == Bitboards.A1) {
            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
        } else if (moveFrom == Bitboards.H1) {
            this.castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
        }
    }

    private void makeSpecialWhitePawnMoveAdjustments(int compactMove) throws InvalidMoveException {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.halfMoveCount = 0;

        if ((toMask & Bitboards.RANK_4) != 0 && (fromMask & Bitboards.RANK_2) != 0) {
            this.engineBitboards.setPieceBitboard(RivalConstants.ENPASSANTSQUARE, fromMask << 8L);
        } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            this.engineBitboards.xorPieceBitboard(SquareOccupant.BP.getIndex(), toMask >>> 8);
            this.moveList[this.numMovesMade].capturePiece = SquareOccupant.BP;
            this.squareContents[moveTo - 8] = SquareOccupant.NONE;
        } else if ((compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue()) != 0) {
            final PromotionPieceMask promotionPieceMask = PromotionPieceMask.fromValue(compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue());
            switch (promotionPieceMask) {
                case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                    this.engineBitboards.pieceBitboards[SquareOccupant.WQ.getIndex()] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.WQ;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                    this.engineBitboards.pieceBitboards[SquareOccupant.WR.getIndex()] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.WR;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                    this.engineBitboards.pieceBitboards[SquareOccupant.WN.getIndex()] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.WN;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                    this.engineBitboards.pieceBitboards[SquareOccupant.WB.getIndex()] |= toMask;
                    this.squareContents[moveTo] = SquareOccupant.WB;
                    break;
                default:
                    throw new InvalidMoveException(
                            "compactMove " + compactMove + " produced invalid promotion piece");
            }
            this.engineBitboards.pieceBitboards[RivalConstants.WP] ^= toMask;
        }
    }

    public MoveDetail getLastMoveMade() {
        return moveList[numMovesMade];
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

        boardHash.unMove(this);

        this.squareContents[fromSquare] = this.moveList[this.numMovesMade].movePiece;
        this.squareContents[toSquare] = SquareOccupant.NONE;

        // deal with en passants first, they are special moves and capture moves, so just get them out of the way
        if (!unMakeEnPassants(toSquare, fromMask, toMask)) {

            // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
            replaceCapturedPiece(toSquare, toMask);

            // for promotions, remove promotion piece from toSquare
            if (!removePromotionPiece(fromMask, toMask)) {

                // now that promotions are out of the way, we can remove the moving piece from toSquare and put it back on fromSquare
                final SquareOccupant movePiece = replaceMovedPiece(fromSquare, fromMask, toMask);

                // for castles, replace the rook
                replaceCastledRook(fromMask, toMask, movePiece);
            }
        }

        calculateSupplementaryBitboards();

    }

    private SquareOccupant replaceMovedPiece(int fromSquare, long fromMask, long toMask) {
        final SquareOccupant movePiece = this.moveList[this.numMovesMade].movePiece;
        this.engineBitboards.pieceBitboards[movePiece.getIndex()] ^= toMask | fromMask;
        if (movePiece == SquareOccupant.WK) this.whiteKingSquare = (byte) fromSquare;
        else if (movePiece == SquareOccupant.BK) this.blackKingSquare = (byte) fromSquare;

        return movePiece;
    }

    private void replaceCastledRook(long fromMask, long toMask, SquareOccupant movePiece) {
        if (movePiece == SquareOccupant.WK) {
            if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H1] = SquareOccupant.WR;
                this.squareContents[Bitboards.F1] = SquareOccupant.NONE;
            } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A1] = SquareOccupant.WR;
                this.squareContents[Bitboards.D1] = SquareOccupant.NONE;

            }
        } else if (movePiece == SquareOccupant.BK) {
            if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H8] = SquareOccupant.BR;
                this.squareContents[Bitboards.F8] = SquareOccupant.NONE;

            } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A8] = SquareOccupant.BR;
                this.squareContents[Bitboards.D8] = SquareOccupant.NONE;

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
                Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WN.getIndex()]) * PieceValue.getValue(Piece.KNIGHT) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WR.getIndex()]) * PieceValue.getValue(Piece.ROOK) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WB.getIndex()]) * PieceValue.getValue(Piece.BISHOP) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WQ.getIndex()]) * PieceValue.getValue(Piece.QUEEN);

    }

    public int getBlackPieceValues() {
        return
                Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BN.getIndex()]) * PieceValue.getValue(Piece.KNIGHT) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BR.getIndex()]) * PieceValue.getValue(Piece.ROOK) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BB.getIndex()]) * PieceValue.getValue(Piece.BISHOP) +
                        Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BQ.getIndex()]) * PieceValue.getValue(Piece.QUEEN);

    }

    public int getWhitePawnValues() {
        return Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.WP.getIndex()]) * PieceValue.getValue(Piece.PAWN);

    }

    public int getBlackPawnValues() {
        return Long.bitCount(engineBitboards.pieceBitboards[SquareOccupant.BP.getIndex()]) * PieceValue.getValue(Piece.PAWN);
    }

    private void replaceCapturedPiece(int toSquare, long toMask) {
        final SquareOccupant capturePiece = this.moveList[this.numMovesMade].capturePiece;
        if (capturePiece != SquareOccupant.NONE) {
            this.squareContents[toSquare] = capturePiece;

            this.engineBitboards.pieceBitboards[capturePiece.getIndex()] ^= toMask;
        }
    }

    private boolean unMakeEnPassants(int toSquare, long fromMask, long toMask) {
        if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            if (this.moveList[this.numMovesMade].movePiece == SquareOccupant.WP) {
                this.engineBitboards.pieceBitboards[SquareOccupant.WP.getIndex()] ^= toMask | fromMask;
                this.engineBitboards.pieceBitboards[SquareOccupant.BP.getIndex()] ^= toMask >>> 8;
                this.squareContents[toSquare - 8] = SquareOccupant.BP;


                return true;
            } else if (this.moveList[this.numMovesMade].movePiece == SquareOccupant.BP) {
                this.engineBitboards.pieceBitboards[SquareOccupant.BP.getIndex()] ^= toMask | fromMask;
                this.engineBitboards.pieceBitboards[SquareOccupant.WP.getIndex()] ^= toMask << 8;
                this.squareContents[toSquare + 8] = SquareOccupant.WP;


                return true;
            }
        }
        return false;
    }

    public SquareOccupant lastCapturePiece() {
        return this.moveList[this.numMovesMade - 1].capturePiece;
    }

    public boolean wasCapture() {
        return this.moveList[this.numMovesMade - 1].capturePiece == SquareOccupant.NONE;
    }

    public boolean wasPawnPush() {
        int toSquare = this.moveList[this.numMovesMade - 1].move & 63;
        SquareOccupant movePiece = this.moveList[this.numMovesMade - 1].movePiece;

        if (movePiece.getPiece() != Piece.PAWN) {
            return false;
        }

        if (toSquare >= 48 || toSquare <= 15) {
            return true;
        }

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
        final long boardHashCode = boardHash.getTrackedHashValue();

        int occurrences = 0;
        for (int i = this.numMovesMade - 2; i >= 0 && i >= this.numMovesMade - this.halfMoveCount; i -= 2) {
            if (this.moveList[i].hashValue == boardHashCode) {
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

    public long trackedBoardHashCode() {
        return boardHash.getTrackedHashValue();
    }

    public long trackedPawnHashCode() {
        return boardHash.getTrackedPawnHashValue();
    }

}
