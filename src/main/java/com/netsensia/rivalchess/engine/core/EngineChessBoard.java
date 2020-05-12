package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.config.Hash;
import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.bitboards.BitboardType;
import com.netsensia.rivalchess.bitboards.EngineBitboards;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.engine.core.type.MoveDetail;
import com.netsensia.rivalchess.enums.CastleBitMask;
import com.netsensia.rivalchess.enums.PromotionPieceMask;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getSetBits;
import static com.netsensia.rivalchess.engine.core.eval.PieceValueKt.pieceValue;

/**
 * R
 * epresents the state of a chessboard including bitboard states and the Zorbrish hash value.
 */
public final class EngineChessBoard {

    private final EngineBitboards engineBitboards = new EngineBitboards();
    private final BoardHash boardHash = new BoardHash();

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
        this(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
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
        boardHash.setHashSizeMB(Hash.DEFAULT_HASHTABLE_SIZE_MB.getValue());
        boardHash.initialiseHashCode(this);
    }

    private void initArrays() {
        int size = Limit.MAX_TREE_DEPTH.getValue() + Limit.MAX_GAME_MOVES.getValue();
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

    public List<SquareOccupant> getSquareOccupants() {
        return Arrays.asList(squareContents);
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
                ((1L << toSquare) & engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE)) != 0 &&
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
                this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue());
                if (!queenCapturesOnly) {
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.getValue());
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.getValue());
                    this.legalMoves[this.numLegalMoves++] = fromSquareMoveMask | toSquare | (PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.getValue());
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

        generatePawnMoves(this.isWhiteToMove ? engineBitboards.getPieceBitboard(BitboardType.WP) : engineBitboards.getPieceBitboard(BitboardType.BP),
                this.isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture);

        generateSliderMoves(SquareOccupant.WR.getIndex(), SquareOccupant.BR.getIndex(), Bitboards.magicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook);

        generateSliderMoves(SquareOccupant.WB.getIndex(), SquareOccupant.BB.getIndex(), Bitboards.magicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop);

        this.legalMoves[this.numLegalMoves] = 0;

    }

    private void clearLegalMovesArray() {
        this.numLegalMoves = 0;
        this.legalMoves[this.numLegalMoves] = 0;
    }

    private void generateSliderMoves(final int whitePieceConstant, final int blackPieceConstant, long[][] magicMovesRook, long[] occupancyMaskRook, long[] magicNumberRook, int[] magicNumberShiftsRook) {
        long rookBitboard;
        rookBitboard =
                this.isWhiteToMove
                        ? engineBitboards.getPieceBitboard(BitboardType.fromIndex(whitePieceConstant))
                            | engineBitboards.getPieceBitboard(BitboardType.WQ)
                        : engineBitboards.getPieceBitboard(BitboardType.fromIndex(blackPieceConstant))
                            | engineBitboards.getPieceBitboard(BitboardType.BQ);

        while (rookBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(rookBitboard);
            rookBitboard ^= (1L << bitRef);
            addMoves(
                    bitRef << 16,
                    magicMovesRook[bitRef][(int) (((engineBitboards.getPieceBitboard(BitboardType.ALL) & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~engineBitboards.getPieceBitboard(BitboardType.FRIENDLY));
        }
    }

    private void generatePawnMoves(long pawnBitboard, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves) {
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(pawnBitboard);
            pawnBitboard ^= (1L << (bitRef));
            bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.getPieceBitboard(BitboardType.ALL);

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

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & ~engineBitboards.getPieceBitboard(BitboardType.FRIENDLY));
    }

    private void generateWhiteKingMoves() {

        final int whiteKingStartSquare = 3;
        final int whiteQueenStartSquare = 4;
        final Colour opponent = Colour.BLACK;

        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_WK.getValue()) != 0L &&
            (engineBitboards.getPieceBitboard(BitboardType.ALL) & Bitboards.WHITEKINGSIDECASTLESQUARES) == 0L &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare-1, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (whiteKingStartSquare << 16) | whiteKingStartSquare-2;
        }

        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_WQ.getValue()) != 0L &&
            (engineBitboards.getPieceBitboard(BitboardType.ALL) & Bitboards.WHITEQUEENSIDECASTLESQUARES) == 0L &&
            !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
            !engineBitboards.isSquareAttackedBy(whiteQueenStartSquare, opponent)) {
                this.legalMoves[this.numLegalMoves++] = (whiteKingStartSquare << 16) | whiteQueenStartSquare+1;
        }
    }

    private void generateBlackKingMoves() {

        final int blackKingStartSquare = 59;
        final Colour opponent = Colour.WHITE;
        final int blackQueenStartSquare = 60;

        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_BK.getValue()) != 0L &&
                (engineBitboards.getPieceBitboard(BitboardType.ALL) & Bitboards.BLACKKINGSIDECASTLESQUARES) == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare-1, opponent)) {
            this.legalMoves[this.numLegalMoves++] = (blackKingStartSquare << 16) | blackKingStartSquare-2;
        }

        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_BQ.getValue()) != 0L &&
                (engineBitboards.getPieceBitboard(BitboardType.ALL) & Bitboards.BLACKQUEENSIDECASTLESQUARES) == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackQueenStartSquare, opponent)) {
            this.legalMoves[this.numLegalMoves++] = (blackKingStartSquare << 16) | blackQueenStartSquare+1;
        }
    }

    private void generateKnightMoves(long knightBitboard) {
        while (knightBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(knightBitboard);
            knightBitboard ^= (1L << (bitRef));
            addMoves(bitRef << 16, Bitboards.knightMoves.get(bitRef) & ~engineBitboards.getPieceBitboard(BitboardType.FRIENDLY));
        }
    }

    private long getBitboardPawnCaptureMoves(int bitRef, List<Long> bitboardMaskCapturePawnMoves, long bitboardPawnMoves) {
        bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.getPieceBitboard(BitboardType.ENEMY);

        if (this.isWhiteToMove) {
            if ((engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) & Bitboards.RANK_6) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE);
            }
        } else {
            if ((engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) & Bitboards.RANK_3) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE);
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
        bitboardJumpMoves &= ~engineBitboards.getPieceBitboard(BitboardType.ALL); // only if square empty
        bitboardPawnMoves |= bitboardJumpMoves;

        return bitboardPawnMoves;
    }

    public void generateLegalQuiesceMoves(boolean includeChecks) {

        final long possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY);

        clearLegalMovesArray();

        final int kingSquare = this.isWhiteToMove ? this.whiteKingSquare : this.blackKingSquare;
        final int enemyKingSquare = this.isWhiteToMove ? this.blackKingSquare : this.whiteKingSquare;

        generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                this.isWhiteToMove ? engineBitboards.getPieceBitboard(BitboardType.WN) : engineBitboards.getPieceBitboard(BitboardType.BN));

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & possibleDestinations);

        generateQuiescePawnMoves(includeChecks,
                this.isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture,
                enemyKingSquare,
                this.isWhiteToMove ? engineBitboards.getPieceBitboard(BitboardType.WP) : engineBitboards.getPieceBitboard(BitboardType.BP));

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.ROOK, SquareOccupant.WR.getIndex(), SquareOccupant.BR.getIndex());

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.BISHOP, SquareOccupant.WB.getIndex(), SquareOccupant.BB.getIndex());

        this.legalMoves[this.numLegalMoves] = 0;
    }

    private void generateQuiesceSliderMoves(boolean includeChecks, int enemyKingSquare, Piece piece, final int whiteSliderConstant, final int blackSliderConstant) {

        final long[][] magicMovesRook = piece == Piece.ROOK ? Bitboards.magicBitboards.magicMovesRook : Bitboards.magicBitboards.magicMovesBishop;
        final long[] occupancyMaskRook = piece == Piece.ROOK ? MagicBitboards.occupancyMaskRook : MagicBitboards.occupancyMaskBishop;
        final long[] magicNumberRook = piece == Piece.ROOK ? MagicBitboards.magicNumberRook : MagicBitboards.magicNumberBishop;
        final int[] magicNumberShiftsRook = piece == Piece.ROOK ? MagicBitboards.magicNumberShiftsRook : MagicBitboards.magicNumberShiftsBishop;

        final long rookCheckSquares = magicMovesRook[enemyKingSquare][(int) (((engineBitboards.getPieceBitboard(BitboardType.ALL) & occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare]) >>> magicNumberShiftsRook[enemyKingSquare])];

        long pieceBitboard =
                this.isWhiteToMove ? engineBitboards.getPieceBitboard(
                        BitboardType.fromIndex(whiteSliderConstant)) | engineBitboards.getPieceBitboard(BitboardType.WQ)
                        : engineBitboards.getPieceBitboard(
                                BitboardType.fromIndex(blackSliderConstant)) | engineBitboards.getPieceBitboard(BitboardType.BQ);

        while (pieceBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(pieceBitboard);
            pieceBitboard ^= (1L << (bitRef));
            long pieceMoves = magicMovesRook[bitRef][(int) (((engineBitboards.getPieceBitboard(BitboardType.ALL) & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~engineBitboards.getPieceBitboard(BitboardType.FRIENDLY);
            if (includeChecks) {
                addMoves(bitRef << 16, pieceMoves & (rookCheckSquares | engineBitboards.getPieceBitboard(BitboardType.ENEMY)));
            } else {
                addMoves(bitRef << 16, pieceMoves & engineBitboards.getPieceBitboard(BitboardType.ENEMY));
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
                        bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.getPieceBitboard(BitboardType.ALL));

                if (this.isWhiteToMove) {
                    bitboardPawnMoves &= Bitboards.blackPawnMovesCapture.get(enemyKingSquare);
                } else {
                    bitboardPawnMoves &= Bitboards.whitePawnMovesCapture.get(enemyKingSquare);
                }
            }

            // promotions
            bitboardPawnMoves |= (bitboardMaskForwardPawnMoves.get(bitRef) & ~engineBitboards.getPieceBitboard(BitboardType.ALL)) & (Bitboards.RANK_1 | Bitboards.RANK_8);

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
                possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY) | (Bitboards.knightMoves.get(enemyKingSquare) & ~engineBitboards.getPieceBitboard(BitboardType.FRIENDLY));
            } else {
                possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY);
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
        this.isWhiteToMove = board.getSideToMove() == Colour.WHITE;

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

        Arrays.fill(squareContents, SquareOccupant.NONE);

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                bitNum = (byte) (63 - (8 * y) - x);
                bitSet = 1L << bitNum;
                final SquareOccupant squareOccupant = board.getSquareOccupant(Square.fromCoords(x,y));
                squareContents[bitNum] = squareOccupant;
                pieceIndex = squareOccupant.getIndex();

                if (pieceIndex != -1) {
                    engineBitboards.orPieceBitboard(BitboardType.fromIndex(pieceIndex), bitSet);
                }

                if (squareOccupant == SquareOccupant.WK) {
                    whiteKingSquare = bitNum;
                }

                if (squareOccupant == SquareOccupant.BK) {
                    blackKingSquare = bitNum;
                }
            }
        }
    }

    private void setEnPassantBitboard(Board board) {
        int ep = board.getEnPassantFile();
        if (ep == -1) {
            engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0);
        } else {
            if (board.getSideToMove() == Colour.WHITE) {
                engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L << (40 + (7 - ep)));
            } else {
                engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L << (16 + (7 - ep)));
            }
        }
    }

    private void setCastlePrivileges(Board board) {
        this.castlePrivileges = 0;
        this.castlePrivileges |= (board.isKingSideCastleAvailable(Colour.WHITE) ? CastleBitMask.CASTLEPRIV_WK.getValue() : 0);
        this.castlePrivileges |= (board.isQueenSideCastleAvailable(Colour.WHITE) ? CastleBitMask.CASTLEPRIV_WQ.getValue() : 0);
        this.castlePrivileges |= (board.isKingSideCastleAvailable(Colour.BLACK) ? CastleBitMask.CASTLEPRIV_BK.getValue() : 0);
        this.castlePrivileges |= (board.isQueenSideCastleAvailable(Colour.BLACK) ? CastleBitMask.CASTLEPRIV_BQ.getValue() : 0);
    }

    public void calculateSupplementaryBitboards() {
        if (this.isWhiteToMove) {
            engineBitboards.setPieceBitboard(BitboardType.FRIENDLY,
                    engineBitboards.getPieceBitboard(BitboardType.WP) | engineBitboards.getPieceBitboard(BitboardType.WN) |
                            engineBitboards.getPieceBitboard(BitboardType.WB) | engineBitboards.getPieceBitboard(BitboardType.WQ) |
                            engineBitboards.getPieceBitboard(BitboardType.WK) | engineBitboards.getPieceBitboard(BitboardType.WR));

            engineBitboards.setPieceBitboard(BitboardType.ENEMY,
                    engineBitboards.getPieceBitboard(BitboardType.BP) | engineBitboards.getPieceBitboard(BitboardType.BN) |
                            engineBitboards.getPieceBitboard(BitboardType.BB) | engineBitboards.getPieceBitboard(BitboardType.BQ) |
                            engineBitboards.getPieceBitboard(BitboardType.BK) | engineBitboards.getPieceBitboard(BitboardType.BR));
        } else {
            engineBitboards.setPieceBitboard(BitboardType.ENEMY,
                    engineBitboards.getPieceBitboard(BitboardType.WP) | engineBitboards.getPieceBitboard(BitboardType.WN) |
                            engineBitboards.getPieceBitboard(BitboardType.WB) | engineBitboards.getPieceBitboard(BitboardType.WQ) |
                            engineBitboards.getPieceBitboard(BitboardType.WK) | engineBitboards.getPieceBitboard(BitboardType.WR));

            engineBitboards.setPieceBitboard(BitboardType.FRIENDLY,
                    engineBitboards.getPieceBitboard(BitboardType.BP) | engineBitboards.getPieceBitboard(BitboardType.BN) |
                            engineBitboards.getPieceBitboard(BitboardType.BB) | engineBitboards.getPieceBitboard(BitboardType.BQ) |
                            engineBitboards.getPieceBitboard(BitboardType.BK) | engineBitboards.getPieceBitboard(BitboardType.BR));
        }

        this.engineBitboards.setPieceBitboard(BitboardType.ALL, engineBitboards.getPieceBitboard(BitboardType.FRIENDLY) | engineBitboards.getPieceBitboard(BitboardType.ENEMY));
    }

    public boolean isNotOnNullMove() {
        return !this.isOnNullMove;
    }

    public void makeNullMove() {

        boardHash.makeNullMove();

        this.isWhiteToMove = !this.isWhiteToMove;

        long t = this.engineBitboards.getPieceBitboard(BitboardType.FRIENDLY);
        this.engineBitboards.setPieceBitboard(BitboardType.FRIENDLY, this.engineBitboards.getPieceBitboard(BitboardType.ENEMY));
        this.engineBitboards.setPieceBitboard(BitboardType.ENEMY, t);

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
        this.moveList[this.numMovesMade].enPassantBitboard = this.engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE);
        this.moveList[this.numMovesMade].castlePrivileges = (byte) this.castlePrivileges;
        this.moveList[this.numMovesMade].movePiece = movePiece;

        boardHash.move(this, engineMove);

        this.isOnNullMove = false;

        this.halfMoveCount++;

        engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0);

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
            if (capturePiece != SquareOccupant.NONE) {
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
        this.engineBitboards.xorPieceBitboard(capturePiece.getIndex(), toMask);
        if (capturePiece == SquareOccupant.WR) {
            if (toMask == Bitboards.WHITEKINGSIDEROOKMASK) {
                this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_WK.getValue();
            }
            else if (toMask == Bitboards.WHITEQUEENSIDEROOKMASK) {
                this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_WQ.getValue();
            }
        }
    }

    private void adjustKingVariablesForBlackKingMove(final int compactMove) {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.castlePrivileges &= CastleBitMask.CASTLEPRIV_BNONE.getValue();
        this.blackKingSquare = moveTo;
        if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.BR, Bitboards.BLACKKINGSIDECASTLEROOKMOVE);
            this.squareContents[Square.H8.getBitRef()] = SquareOccupant.NONE;
            this.squareContents[Square.F8.getBitRef()] = SquareOccupant.BR;
        } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.BR, Bitboards.BLACKQUEENSIDECASTLEROOKMOVE);
            this.squareContents[Square.A8.getBitRef()] = SquareOccupant.NONE;
            this.squareContents[Square.D8.getBitRef()] = SquareOccupant.BR;
        }
    }

    private void adjustCastlePrivilegesForBlackRookMove(byte moveFrom) {
        if (moveFrom == Square.A8.getBitRef()) this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_BQ.getValue();
        else if (moveFrom == Square.H8.getBitRef()) this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_BK.getValue();
    }

    private void makeSpecialBlackPawnMoveAdjustments(int compactMove) throws InvalidMoveException {
        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.halfMoveCount = 0;

        if ((toMask & Bitboards.RANK_5) != 0 && (fromMask & Bitboards.RANK_7) != 0) {
            this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, toMask << 8L);
        } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask << 8);
            this.moveList[this.numMovesMade].capturePiece = SquareOccupant.WP;
            this.squareContents[moveTo + 8] = SquareOccupant.NONE;
        } else if ((compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue()) != 0) {
            final int promotionPieceMask = compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue();
            switch (PromotionPieceMask.fromValue(promotionPieceMask)) {
                case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                    this.engineBitboards.orPieceBitboard(BitboardType.BQ, toMask);
                    this.squareContents[moveTo] = SquareOccupant.BQ;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                    this.engineBitboards.orPieceBitboard(BitboardType.BR, toMask);
                    this.squareContents[moveTo] = SquareOccupant.BR;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                    this.engineBitboards.orPieceBitboard(BitboardType.BN, toMask);
                    this.squareContents[moveTo] = SquareOccupant.BN;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                    this.engineBitboards.orPieceBitboard(BitboardType.BB, toMask);
                    this.squareContents[moveTo] = SquareOccupant.BB;
                    break;
                default: throw new InvalidMoveException(
                        "compactMove " + compactMove + " produced invalid promotion piece");
            }
            this.engineBitboards.xorPieceBitboard(BitboardType.BP, toMask);
        }
    }

    private void makeAdjustmentsFollowingCaptureOfBlackPiece(SquareOccupant capturePiece, long toMask) {
        this.moveList[this.numMovesMade].capturePiece = capturePiece;
        this.halfMoveCount = 0;
        this.engineBitboards.xorPieceBitboard(capturePiece.getIndex(), toMask);

        if (capturePiece == SquareOccupant.BR) {
            if (toMask == Bitboards.BLACKKINGSIDEROOKMASK) {
                this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_BK.getValue();
            }
            else if (toMask == Bitboards.BLACKQUEENSIDEROOKMASK) {
                this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_BQ.getValue();
            }
        }
    }

    private void adjustKingVariablesForWhiteKingMove(int compactMove) {
        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.whiteKingSquare = moveTo;
        this.castlePrivileges &= CastleBitMask.CASTLEPRIV_WNONE.getValue();
        if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.WR, Bitboards.WHITEKINGSIDECASTLEROOKMOVE);
            this.squareContents[Square.H1.getBitRef()] = SquareOccupant.NONE;
            this.squareContents[Square.F1.getBitRef()] = SquareOccupant.WR;

        } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.WR, Bitboards.WHITEQUEENSIDECASTLEROOKMOVE);
            this.squareContents[Square.A1.getBitRef()] = SquareOccupant.NONE;
            this.squareContents[Square.D1.getBitRef()] = SquareOccupant.WR;
        }
    }

    private void adjustCastlePrivilegesForWhiteRookMove(byte moveFrom) {
        if (moveFrom == Square.A1.getBitRef()) {
            this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_WQ.getValue();
        } else if (moveFrom == Square.H1.getBitRef()) {
            this.castlePrivileges &= ~CastleBitMask.CASTLEPRIV_WK.getValue();
        }
    }

    private void makeSpecialWhitePawnMoveAdjustments(int compactMove) throws InvalidMoveException {

        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        this.halfMoveCount = 0;

        if ((toMask & Bitboards.RANK_4) != 0 && (fromMask & Bitboards.RANK_2) != 0) {
            this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, fromMask << 8L);
        } else if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            this.engineBitboards.xorPieceBitboard(SquareOccupant.BP.getIndex(), toMask >>> 8);
            this.moveList[this.numMovesMade].capturePiece = SquareOccupant.BP;
            this.squareContents[moveTo - 8] = SquareOccupant.NONE;
        } else if ((compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue()) != 0) {
            final PromotionPieceMask promotionPieceMask = PromotionPieceMask.fromValue(compactMove & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue());
            switch (promotionPieceMask) {
                case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                    engineBitboards.orPieceBitboard(BitboardType.WQ, toMask);
                    this.squareContents[moveTo] = SquareOccupant.WQ;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                    engineBitboards.orPieceBitboard(BitboardType.WR, toMask);
                    this.squareContents[moveTo] = SquareOccupant.WR;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                    engineBitboards.orPieceBitboard(BitboardType.WN, toMask);
                    this.squareContents[moveTo] = SquareOccupant.WN;
                    break;
                case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                    engineBitboards.orPieceBitboard(BitboardType.WB, toMask);
                    this.squareContents[moveTo] = SquareOccupant.WB;
                    break;
                default:
                    throw new InvalidMoveException(
                            "compactMove " + compactMove + " produced invalid promotion piece");
            }
            this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask);
        }
    }

    public MoveDetail getLastMoveMade() {
        return moveList[numMovesMade];
    }

    public void unMakeMove() throws InvalidMoveException {

        this.numMovesMade--;

        this.halfMoveCount = this.moveList[this.numMovesMade].halfMoveCount;

        this.isWhiteToMove = !this.isWhiteToMove;

        this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, this.moveList[this.numMovesMade].enPassantBitboard);
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
        this.engineBitboards.xorPieceBitboard(movePiece.getIndex(), toMask | fromMask);
        if (movePiece == SquareOccupant.WK) {
            this.whiteKingSquare = (byte) fromSquare;
        }
        else if (movePiece == SquareOccupant.BK) {
            this.blackKingSquare = (byte) fromSquare;
        }

        return movePiece;
    }

    private void replaceCastledRook(long fromMask, long toMask, SquareOccupant movePiece) {
        if (movePiece == SquareOccupant.WK) {
            if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.xorPieceBitboard(BitboardType.WR, Bitboards.WHITEKINGSIDECASTLEROOKMOVE);
                this.squareContents[Square.H1.getBitRef()] = SquareOccupant.WR;
                this.squareContents[Square.F1.getBitRef()] = SquareOccupant.NONE;
            } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.xorPieceBitboard(BitboardType.WR, Bitboards.WHITEQUEENSIDECASTLEROOKMOVE);
                this.squareContents[Square.A1.getBitRef()] = SquareOccupant.WR;
                this.squareContents[Square.D1.getBitRef()] = SquareOccupant.NONE;

            }
        } else if (movePiece == SquareOccupant.BK) {
            if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                this.engineBitboards.xorPieceBitboard(BitboardType.BR, Bitboards.BLACKKINGSIDECASTLEROOKMOVE);
                this.squareContents[Square.H8.getBitRef()] = SquareOccupant.BR;
                this.squareContents[Square.F8.getBitRef()] = SquareOccupant.NONE;

            } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                this.engineBitboards.xorPieceBitboard(BitboardType.BR, Bitboards.BLACKQUEENSIDECASTLEROOKMOVE);
                this.squareContents[Square.A8.getBitRef()] = SquareOccupant.BR;
                this.squareContents[Square.D8.getBitRef()] = SquareOccupant.NONE;

            }
        }
    }

    private boolean removePromotionPiece(long fromMask, long toMask) throws InvalidMoveException {
        final int promotionPiece = this.moveList[this.numMovesMade].move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue();
        if (promotionPiece != 0) {
            if (this.isWhiteToMove) {
                this.engineBitboards.xorPieceBitboard(BitboardType.WP, fromMask);

                switch (PromotionPieceMask.fromValue(promotionPiece)) {
                    case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        engineBitboards.xorPieceBitboard(BitboardType.WQ, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        engineBitboards.xorPieceBitboard(BitboardType.WB, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        engineBitboards.xorPieceBitboard(BitboardType.WN, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        engineBitboards.xorPieceBitboard(BitboardType.WR, toMask);
                        break;
                    default:
                        throw new InvalidMoveException("Illegal promotion piece " + promotionPiece);
                }
            } else {
                engineBitboards.xorPieceBitboard(BitboardType.BP, fromMask);

                switch (PromotionPieceMask.fromValue(promotionPiece)) {
                    case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        engineBitboards.xorPieceBitboard(BitboardType.BQ, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        engineBitboards.xorPieceBitboard(BitboardType.BB, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        engineBitboards.xorPieceBitboard(BitboardType.BN, toMask);
                        break;
                    case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        engineBitboards.xorPieceBitboard(BitboardType.BR, toMask);
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
                Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WN)) * pieceValue(Piece.KNIGHT) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WR)) * pieceValue(Piece.ROOK) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WB)) * pieceValue(Piece.BISHOP) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WQ)) * pieceValue(Piece.QUEEN);

    }

    public int getBlackPieceValues() {
        return
                Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BN)) * pieceValue(Piece.KNIGHT) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BR)) * pieceValue(Piece.ROOK) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BB)) * pieceValue(Piece.BISHOP) +
                        Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BQ)) * pieceValue(Piece.QUEEN);

    }

    public int getWhitePawnValues() {
        return Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WP)) * pieceValue(Piece.PAWN);

    }

    public int getBlackPawnValues() {
        return Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BP)) * pieceValue(Piece.PAWN);
    }

    private void replaceCapturedPiece(int toSquare, long toMask) {
        final SquareOccupant capturePiece = this.moveList[this.numMovesMade].capturePiece;
        if (capturePiece != SquareOccupant.NONE) {
            this.squareContents[toSquare] = capturePiece;

            this.engineBitboards.xorPieceBitboard(capturePiece.getIndex(), toMask);
        }
    }

    private boolean unMakeEnPassants(int toSquare, long fromMask, long toMask) {
        if (toMask == this.moveList[this.numMovesMade].enPassantBitboard) {
            if (this.moveList[this.numMovesMade].movePiece == SquareOccupant.WP) {
                this.engineBitboards.xorPieceBitboard(BitboardType.WP,toMask | fromMask);
                this.engineBitboards.xorPieceBitboard(BitboardType.BP,toMask >>> 8);
                this.squareContents[toSquare - 8] = SquareOccupant.BP;


                return true;
            } else if (this.moveList[this.numMovesMade].movePiece == SquareOccupant.BP) {
                this.engineBitboards.xorPieceBitboard(BitboardType.BP, toMask | fromMask);
                this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask << 8);
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
                return Long.bitCount(Bitboards.whitePassedPawnMask.get(toSquare) &
                                engineBitboards.getPieceBitboard(BitboardType.BP)) == 0;
        } else {
            if (toSquare <= 23)
                return (Long.bitCount(Bitboards.blackPassedPawnMask.get(toSquare) &
                        engineBitboards.getPieceBitboard(BitboardType.WP)) == 0);
        }

        return false;
    }

    public long getWhitePawnBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WP);
    }

    public long getBlackPawnBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BP);
    }

    public long getWhiteKingBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WK);
    }

    public long getBlackKingBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BK);
    }

    public long getWhiteQueenBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WQ);
    }

    public long getBlackQueenBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BQ);
    }

    public long getWhiteRookBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WR);
    }

    public long getBlackRookBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BR);
    }

    public long getWhiteKnightBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WN);
    }

    public long getBlackKnightBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BN);
    }

    public long getWhiteBishopBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.WB);
    }

    public long getBlackBishopBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.BB);
    }

    public long getAllPiecesBitboard() {
        return engineBitboards.getPieceBitboard(BitboardType.ALL);
    }

    public int getWhiteKingSquare() {
        return whiteKingSquare;
    }

    public int getBlackKingSquare() {
        return blackKingSquare;
    }

    @Deprecated
    public long getBitboardByIndex(int index) {
        return engineBitboards.getPieceBitboard(BitboardType.fromIndex(index));
    }

    public long getBitboard(final BitboardType bitboardType) {
        return engineBitboards.getPieceBitboard(bitboardType);
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

        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_WK.getValue()) != 0) {
            fen.append('K');
            noPrivs = false;
        }
        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_WQ.getValue()) != 0) {
            fen.append('Q');
            noPrivs = false;
        }
        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_BK.getValue()) != 0) {
            fen.append('k');
            noPrivs = false;
        }
        if ((castlePrivileges & CastleBitMask.CASTLEPRIV_BQ.getValue()) != 0) {
            fen.append('q');
            noPrivs = false;
        }

        if (noPrivs) fen.append('-');

        fen.append(' ');
        long bitboard = engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE);
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

        for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
            List<Integer> bitsSet = getSetBits(engineBitboards.getPieceBitboard(BitboardType.fromIndex(i)), new ArrayList<>());
            for (int bitSet : bitsSet) {
                board[bitSet] = pieces[i];
            }
        }
        return board;
    }

    public boolean isMoveLegal(int moveToVerify) throws InvalidMoveException {
        moveToVerify &= 0x00FFFFFF;
        int[] moves = new int[Limit.MAX_GAME_MOVES.getValue()];
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
