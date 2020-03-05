package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.MoveOrder;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.constants.SearchState;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluator;
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluatorClassic;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.IllegalSearchStateException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.uci.EngineMonitor;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import com.netsensia.rivalchess.util.Numbers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;

import static com.netsensia.rivalchess.engine.core.hash.SearchHashHelper.isAlwaysReplaceHashTableEntryValid;
import static com.netsensia.rivalchess.engine.core.hash.SearchHashHelper.isHeightHashTableEntryValid;

public final class RivalSearch implements Runnable {

    private final PrintStream printStream;

    private final StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluatorClassic();

    private final MoveOrder[] moveOrderStatus = new MoveOrder[RivalConstants.MAX_TREE_DEPTH];

    private final List<List<Long>> drawnPositionsAtRoot;
    private final List<Integer> drawnPositionsAtRootCount = new ArrayList<>();
    private EngineChessBoard engineChessBoard
            = new EngineChessBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    private final List<Integer> mateKiller = new ArrayList<>();

    private final int[][] killerMoves;

    private final int[][][] historyMovesSuccess = new int[2][64][64];
    private final int[][][] historyMovesFail = new int[2][64][64];
    private final int[][][] historyPruneMoves = new int[2][64][64];
    private final int[][] orderedMoves;
    private final SearchPath[] searchPath;

    private final int[] depthZeroLegalMoves;
    private final int[] depthZeroMoveScores;

    private boolean isOkToSendInfo = false;

    private SearchState searchState;

    boolean quit = false;

    private int nodes = 0;
    private long millisSetByEngineMonitor;

    private int aspirationLow;
    private int aspirationHigh;

    protected int millisToThink;
    protected int nodesToSearch = Integer.MAX_VALUE;

    protected boolean m_abortingSearch = true;

    private long searchStartTime = -1;
    private long searchTargetEndTime;
    private long searchEndTime = 0;

    protected int m_finalDepthToSearch = 1;
    protected int iterativeDeepeningCurrentDepth = 0; // current search depth for iterative deepening

    private boolean useOpeningBook = RivalConstants.USE_INTERNAL_OPENING_BOOK;
    private boolean m_inBook = useOpeningBook;

    private int currentDepthZeroMove;
    private int currentDepthZeroMoveNumber;

    private SearchPath m_currentPath;
    private String currentPathString;

    private boolean m_isUCIMode = false;

    private static byte[] rivalKPKBitbase = null;

    public RivalSearch() throws IllegalFenException {
        this(System.out, FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    }

    public RivalSearch(PrintStream printStream) throws IllegalFenException {
        this(printStream, FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    }

    public RivalSearch(Board board) throws IllegalFenException {
        this(System.out, board);
    }

    public RivalSearch(PrintStream printStream, Board board) throws IllegalFenException {

        this.engineChessBoard.setBoard(board);

        drawnPositionsAtRoot = new ArrayList<>();
        drawnPositionsAtRoot.add(new ArrayList<>());
        drawnPositionsAtRoot.add(new ArrayList<>());

        this.printStream = printStream;

        this.setMillisSetByEngineMonitor(System.currentTimeMillis());

        this.m_currentPath = new SearchPath();
        this.currentPathString = "";

        searchState = SearchState.READY;

        this.searchPath = new SearchPath[RivalConstants.MAX_TREE_DEPTH];
        this.killerMoves = new int[RivalConstants.MAX_TREE_DEPTH][RivalConstants.NUM_KILLER_MOVES];
        for (int i = 0; i < RivalConstants.MAX_TREE_DEPTH; i++) {
            this.searchPath[i] = new SearchPath();
            this.killerMoves[i] = new int[RivalConstants.NUM_KILLER_MOVES];
        }

        orderedMoves = new int[RivalConstants.MAX_TREE_DEPTH][RivalConstants.MAX_LEGAL_MOVES];

        depthZeroLegalMoves = orderedMoves[0];
        depthZeroMoveScores = new int[RivalConstants.MAX_LEGAL_MOVES];
    }

    public void startEngineTimer(boolean isUCIMode) {
        this.m_isUCIMode = isUCIMode;
        EngineMonitor m_monitor = new EngineMonitor(this);
        new Timer().schedule(m_monitor, RivalConstants.UCI_TIMER_INTERVAL_MILLIS, RivalConstants.UCI_TIMER_INTERVAL_MILLIS);
    }

    public boolean isUCIMode() {
        return this.m_isUCIMode;
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        engineChessBoard.getBoardHashObject().setHashSizeMB(hashSizeMB);
    }

    public synchronized void setBoard(Board board) {
        engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(board);
        setBoard(engineChessBoard);
    }

    public synchronized void setBoard(EngineChessBoard engineBoard) {
        this.setEngineChessBoard(engineBoard);
        engineChessBoard.getBoardHashObject().incVersion();
        engineChessBoard.getBoardHashObject().setHashTable();
    }

    public synchronized void clearHash() {
        engineChessBoard.getBoardHashObject().clearHash();
    }

    public synchronized void newGame() {
        m_inBook = this.useOpeningBook;
        engineChessBoard.getBoardHashObject().clearHash();
    }

    public int evaluate(EngineChessBoard board) {

        if (board.getWhitePieceValues() + board.getBlackPieceValues() + board.getWhitePawnValues() + board.getBlackPawnValues() == 0)
            return 0;

        int sq;
        long bitboard;

        int whiteKingAttackedCount = 0;
        int blackKingAttackedCount = 0;
        long whiteAttacksBitboard = 0;
        long blackAttacksBitboard = 0;
        final long whitePawnAttacks = Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard());
        final long blackPawnAttacks = Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard());
        final long whitePieces = board.getBitboardByIndex(board.getMover() == Colour.WHITE ? RivalConstants.FRIENDLY : RivalConstants.ENEMY);
        final long blackPieces = board.getBitboardByIndex(board.getMover() == Colour.WHITE ? RivalConstants.ENEMY : RivalConstants.FRIENDLY);
        final long whiteKingDangerZone = Bitboards.kingMoves.get(board.getWhiteKingSquare()) | (Bitboards.kingMoves.get(board.getWhiteKingSquare()) << 8);
        final long blackKingDangerZone = Bitboards.kingMoves.get(board.getBlackKingSquare()) | (Bitboards.kingMoves.get(board.getBlackKingSquare()) >>> 8);

        final int materialDifference = board.getWhitePieceValues() - board.getBlackPieceValues() + board.getWhitePawnValues() - board.getBlackPawnValues();

        int eval = materialDifference;

        int pieceSquareTemp = 0;
        int pieceSquareTempEndGame = 0;

        bitboard = board.getWhitePawnBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
            pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(sq);
            pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(sq);
        }

        eval += Numbers.linearScale(board.getBlackPieceValues(), RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        pieceSquareTemp = 0;
        pieceSquareTempEndGame = 0;
        bitboard = board.getBlackPawnBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
            pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
            pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
        }

        eval -= Numbers.linearScale(board.getWhitePieceValues(), RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        eval += Numbers.linearScale(board.getBlackPieceValues(), Piece.ROOK.getValue(), RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(board.getWhiteKingSquare()), Bitboards.pieceSquareTableKing.get(board.getWhiteKingSquare()))
                - Numbers.linearScale(board.getWhitePieceValues(), Piece.ROOK.getValue(), RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare())), Bitboards.pieceSquareTableKing.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare())));

        int lastSq = -1;
        int file = -1;

        pieceSquareTemp = 0;
        bitboard = board.getWhiteRookBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (lastSq != -1 && file == (lastSq % 8)) eval += RivalConstants.VALUE_ROOKS_ON_SAME_FILE;

            pieceSquareTemp += Bitboards.pieceSquareTableRook.get(sq);

            final long allAttacks = Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            eval += RivalConstants.VALUE_ROOK_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone);

            file = sq % 8;

            if ((Bitboards.FILES.get(file) & board.getWhitePawnBitboard()) == 0)
                if ((Bitboards.FILES.get(file) & board.getBlackPawnBitboard()) == 0)
                    eval += RivalConstants.VALUE_ROOK_ON_OPEN_FILE;
                else
                    eval += RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE;

            lastSq = sq;
        }

        eval += (pieceSquareTemp * Math.min(board.getBlackPawnValues() / Piece.PAWN.getValue(), 6) / 6);

        if (Long.bitCount(board.getWhiteRookBitboard() & Bitboards.RANK_7) > 1 && (board.getBitboardByIndex(RivalConstants.BK) & Bitboards.RANK_8) != 0)
            eval += RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING;

        bitboard = board.getBlackRookBitboard();
        pieceSquareTemp = 0;
        lastSq = -1;
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (lastSq != -1 && file == (lastSq % 8)) eval -= RivalConstants.VALUE_ROOKS_ON_SAME_FILE;

            pieceSquareTemp += Bitboards.pieceSquareTableRook.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            file = sq % 8;

            final long allAttacks = Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];
            eval -= RivalConstants.VALUE_ROOK_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone);

            if ((Bitboards.FILES.get(file) & board.getBlackPawnBitboard()) == 0)
                if ((Bitboards.FILES.get(file) & board.getWhitePawnBitboard()) == 0)
                    eval -= RivalConstants.VALUE_ROOK_ON_OPEN_FILE;
                else
                    eval -= RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE;

            lastSq = sq;
        }

        eval -= (pieceSquareTemp * Math.min(board.getWhitePawnValues() / Piece.PAWN.getValue(), 6) / 6);

        if (Long.bitCount(board.getBlackRookBitboard() & Bitboards.RANK_2) > 1 && (board.getWhiteKingBitboard() & Bitboards.RANK_1) != 0)
            eval -= RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING;

        bitboard = board.getWhiteKnightBitboard();

        pieceSquareTemp = 0;
        pieceSquareTempEndGame = 0;
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            final long knightAttacks = Bitboards.knightMoves.get(sq);

            pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(sq);
            pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(sq);

            whiteAttacksBitboard |= knightAttacks;
            eval -= Long.bitCount(knightAttacks & (blackPawnAttacks | board.getWhitePawnBitboard())) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY;
        }

        eval += Numbers.linearScale(board.getBlackPieceValues() + board.getBlackPawnValues(), RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        pieceSquareTemp = 0;
        pieceSquareTempEndGame = 0;
        bitboard = board.getBlackKnightBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
            pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long knightAttacks = Bitboards.knightMoves.get(sq);

            blackAttacksBitboard |= knightAttacks;
            eval += Long.bitCount(knightAttacks & (whitePawnAttacks | board.getBlackPawnBitboard())) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY;
        }

        eval -= Numbers.linearScale(board.getWhitePieceValues() + board.getWhitePawnValues(), RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        bitboard = board.getWhiteQueenBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            eval += Bitboards.pieceSquareTableQueen.get(sq);

            final long allAttacks =
                    Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])] |
                            Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone) * 2;

            eval += RivalConstants.VALUE_QUEEN_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
        }

        bitboard = board.getBlackQueenBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            eval -= Bitboards.pieceSquareTableQueen.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long allAttacks =
                    Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])] |
                            Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone) * 2;

            eval -= RivalConstants.VALUE_QUEEN_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
        }

        final BoardHash boardHash = engineChessBoard.getBoardHashObject();
        eval += boardHash.getPawnHashEntry(board).getPawnScore();

        eval +=
                Numbers.linearScale((materialDifference > 0) ? board.getWhitePawnValues() : board.getBlackPawnValues(), 0, RivalConstants.TRADE_BONUS_UPPER_PAWNS, -30 * materialDifference / 100, 0) +
                        Numbers.linearScale((materialDifference > 0) ? board.getBlackPieceValues() + board.getBlackPawnValues() : board.getWhitePieceValues() + board.getWhitePawnValues(), 0, RivalConstants.TOTAL_PIECE_VALUE_PER_SIDE_AT_START, 30 * materialDifference / 100, 0);

        final int castlePrivs =
                (board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_WK) +
                        (board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_WQ) +
                        (board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_BK) +
                        (board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_BQ);

        if (castlePrivs != 0) {
            // Value of moving King to its queenside castle destination in the middle game
            int kingSquareBonusMiddleGame = Bitboards.pieceSquareTableKing.get(1) - Bitboards.pieceSquareTableKing.get(3);
            int kingSquareBonusEndGame = Bitboards.pieceSquareTableKingEndGame.get(1) - Bitboards.pieceSquareTableKingEndGame.get(3);
            int rookSquareBonus = Bitboards.pieceSquareTableRook.get(3) - Bitboards.pieceSquareTableRook.get(0);
            int kingSquareBonusScaled =
                    Numbers.linearScale(
                            board.getBlackPieceValues(),
                            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
                            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
                            kingSquareBonusEndGame,
                            kingSquareBonusMiddleGame);

            // don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
            // given by still having castling rights.
            int castleValue = kingSquareBonusScaled + rookSquareBonus;

            if (castleValue > 0) {
                int timeToCastleKingSide = 100;
                int timeToCastleQueenSide = 100;
                if ((board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_WK) != 0) {
                    timeToCastleKingSide = 2;
                    if ((board.getAllPiecesBitboard() & (1L << 1)) != 0) timeToCastleKingSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 2)) != 0) timeToCastleKingSide++;
                }
                if ((board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_WQ) != 0) {
                    timeToCastleQueenSide = 2;
                    if ((board.getAllPiecesBitboard() & (1L << 6)) != 0) timeToCastleQueenSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 5)) != 0) timeToCastleQueenSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 4)) != 0) timeToCastleQueenSide++;
                }
                eval += castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide);
            }

            kingSquareBonusScaled =
                    Numbers.linearScale(
                            board.getWhitePieceValues(),
                            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
                            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
                            kingSquareBonusEndGame,
                            kingSquareBonusMiddleGame);

            castleValue = kingSquareBonusScaled + rookSquareBonus;

            if (castleValue > 0) {
                int timeToCastleKingSide = 100;
                int timeToCastleQueenSide = 100;
                if ((board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_BK) != 0) {
                    timeToCastleKingSide = 2;
                    if ((board.getAllPiecesBitboard() & (1L << 57)) != 0) timeToCastleKingSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 58)) != 0) timeToCastleKingSide++;
                }
                if ((board.getCastlePrivileges() & RivalConstants.CASTLEPRIV_BQ) != 0) {
                    timeToCastleQueenSide = 2;
                    if ((board.getAllPiecesBitboard() & (1L << 60)) != 0) timeToCastleQueenSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 61)) != 0) timeToCastleQueenSide++;
                    if ((board.getAllPiecesBitboard() & (1L << 62)) != 0) timeToCastleQueenSide++;
                }
                eval -= castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide);
            }
        }

        final boolean whiteLightBishopExists = (board.getWhiteBishopBitboard() & Bitboards.LIGHT_SQUARES) != 0;
        final boolean whiteDarkBishopExists = (board.getWhiteBishopBitboard() & Bitboards.DARK_SQUARES) != 0;
        final boolean blackLightBishopExists = (board.getBlackBishopBitboard() & Bitboards.LIGHT_SQUARES) != 0;
        final boolean blackDarkBishopExists = (board.getBlackBishopBitboard() & Bitboards.DARK_SQUARES) != 0;

        final int whiteBishopColourCount = (whiteLightBishopExists ? 1 : 0) + (whiteDarkBishopExists ? 1 : 0);
        final int blackBishopColourCount = (blackLightBishopExists ? 1 : 0) + (blackDarkBishopExists ? 1 : 0);

        int bishopScore = 0;

        bitboard = board.getWhiteBishopBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            eval += Bitboards.pieceSquareTableBishop.get(sq);

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone);

            bishopScore += RivalConstants.VALUE_BISHOP_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
        }

        if (whiteBishopColourCount == 2)
            bishopScore += RivalConstants.VALUE_BISHOP_PAIR + ((8 - (board.getWhitePawnValues() / Piece.PAWN.getValue())) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS);

        bitboard = board.getBlackBishopBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            eval -= Bitboards.pieceSquareTableBishop.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone);

            bishopScore -= RivalConstants.VALUE_BISHOP_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
        }

        if (blackBishopColourCount == 2)
            bishopScore -= RivalConstants.VALUE_BISHOP_PAIR + ((8 - (board.getBlackPawnValues() / Piece.PAWN.getValue())) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS);

        if (whiteBishopColourCount == 1 && blackBishopColourCount == 1 && whiteLightBishopExists != blackLightBishopExists && board.getWhitePieceValues() == board.getBlackPieceValues()) {
            // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
            final int maxPenalty = (eval + bishopScore) / RivalConstants.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR; // mostly pawns as material is identical

            // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
            bishopScore -= Numbers.linearScale(
                    board.getWhitePieceValues() + board.getBlackPieceValues(),
                    RivalConstants.WRONG_COLOUR_BISHOP_MATERIAL_LOW,
                    RivalConstants.WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
                    maxPenalty,
                    0);
        }

        if (((board.getWhiteBishopBitboard() | board.getBlackBishopBitboard()) & Bitboards.A2A7H2H7) != 0) {
            if ((board.getWhiteBishopBitboard() & (1L << Bitboards.A7)) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Bitboards.B6)) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Bitboards.C7)) != 0)
                bishopScore -= RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY;

            if ((board.getWhiteBishopBitboard() & (1L << Bitboards.H7)) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Bitboards.G6)) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Bitboards.F7)) != 0)
                bishopScore -= (board.getWhiteQueenBitboard() == 0) ?
                        RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY :
                        RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY;

            if ((board.getBlackBishopBitboard() & (1L << Bitboards.A2)) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Bitboards.B3)) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Bitboards.C2)) != 0)
                bishopScore += RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY;

            if ((board.getBlackBishopBitboard() & (1L << Bitboards.H2)) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Bitboards.G3)) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Bitboards.F2)) != 0)
                bishopScore += (board.getBlackQueenBitboard() == 0) ?
                        RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY :
                        RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY;
        }

        eval += bishopScore;

        // Everything white attacks with pieces.  Does not include attacked pawns.
        whiteAttacksBitboard &= board.getBlackKnightBitboard() | board.getBlackRookBitboard() | board.getBlackQueenBitboard() | board.getBlackBishopBitboard();
        // Plus anything white attacks with pawns.
        whiteAttacksBitboard |= Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard());

        int temp = 0;

        bitboard = whiteAttacksBitboard & blackPieces & ~board.getBitboardByIndex(RivalConstants.BK);

        while (bitboard != 0) {
            bitboard ^= ((1L << (sq = Long.numberOfTrailingZeros(bitboard))));
            if (board.getSquareOccupant(sq).getIndex() == RivalConstants.BP) temp += Piece.PAWN.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.BN) temp += Piece.KNIGHT.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.BR) temp += Piece.ROOK.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.BQ) temp += Piece.QUEEN.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.BB) temp += Piece.BISHOP.getValue();
        }

        int threatScore = temp + temp * (temp / Piece.QUEEN.getValue());

        blackAttacksBitboard &= board.getWhiteKnightBitboard() | board.getWhiteRookBitboard() | board.getWhiteQueenBitboard() | board.getWhiteBishopBitboard();
        blackAttacksBitboard |= Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard());

        temp = 0;

        bitboard = blackAttacksBitboard & whitePieces & ~board.getWhiteKingBitboard();

        while (bitboard != 0) {
            bitboard ^= ((1L << (sq = Long.numberOfTrailingZeros(bitboard))));
            if (board.getSquareOccupant(sq).getIndex() == RivalConstants.WP) temp += Piece.PAWN.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.WN) temp += Piece.KNIGHT.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.WR) temp += Piece.ROOK.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.WQ) temp += Piece.QUEEN.getValue();
            else if (board.getSquareOccupant(sq).getIndex() == RivalConstants.WB) temp += Piece.BISHOP.getValue();
        }

        threatScore -= temp + temp * (temp / Piece.QUEEN.getValue());
        threatScore /= RivalConstants.THREAT_SCORE_DIVISOR;

        eval += threatScore;

        final int averagePiecesPerSide = (board.getWhitePieceValues() + board.getBlackPieceValues()) / 2;
        int whiteKingSafety = 0;
        int blackKingSafety = 0;
        int kingSafety = 0;
        if (averagePiecesPerSide > RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE) {

            whiteKingSafety = Evaluate.getWhiteKingRightWayScore(board);
            blackKingSafety = Evaluate.getBlackKingRightWayScore(board);

            int halfOpenFilePenalty = 0;
            int shieldValue = 0;
            if (board.getWhiteKingSquare() / 8 < 2) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.getWhiteKingSquare() % 8);

                shieldValue += RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * Long.bitCount(board.getWhitePawnBitboard() & kingShield)
                        - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * Long.bitCount(board.getBlackPawnBitboard() & (kingShield | (kingShield << 8)))
                        + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * Long.bitCount(board.getWhitePawnBitboard() & (kingShield << 8))
                        - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * Long.bitCount(board.getBlackPawnBitboard() & (kingShield << 16));

                shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS);

                if (((board.getWhiteKingBitboard() & Bitboards.F1G1) != 0) &&
                        ((board.getWhiteRookBitboard() & Bitboards.G1H1) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_G) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_H) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                } else if (((board.getWhiteKingBitboard() & Bitboards.B1C1) != 0) &&
                        ((board.getWhiteRookBitboard() & Bitboards.A1B1) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_A) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_B) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                }

                final long whiteOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.getWhitePawnBitboard())) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.getBlackPawnBitboard())) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            whiteKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty;

            shieldValue = 0;
            halfOpenFilePenalty = 0;
            if (board.getBlackKingSquare() / 8 >= 6) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.getBlackKingSquare() % 8) << 40;
                shieldValue += RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * Long.bitCount(board.getBlackPawnBitboard() & kingShield)
                        - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * Long.bitCount(board.getWhitePawnBitboard() & (kingShield | (kingShield >>> 8)))
                        + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * Long.bitCount(board.getBlackPawnBitboard() & (kingShield >>> 8))
                        - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * Long.bitCount(board.getWhitePawnBitboard() & (kingShield >>> 16));

                shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS);

                if (((board.getBlackKingBitboard() & Bitboards.F8G8) != 0) &&
                        ((board.getBlackRookBitboard() & Bitboards.G8H8) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_G) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_H) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                } else if (((board.getBlackKingBitboard() & Bitboards.B8C8) != 0) &&
                        ((board.getBlackRookBitboard() & Bitboards.A8B8) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_A) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_B) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                }

                final long whiteOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.getWhitePawnBitboard())) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.getBlackPawnBitboard())) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            blackKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty;

            kingSafety =
                    Numbers.linearScale(
                            averagePiecesPerSide,
                            RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE,
                            RivalConstants.KINGSAFETY_MAX_PIECE_BALANCE,
                            0,
                            (whiteKingSafety - blackKingSafety) + (blackKingAttackedCount - whiteKingAttackedCount) * RivalConstants.KINGSAFETY_ATTACK_MULTIPLIER);

        }

        eval += kingSafety;

        if (board.getWhitePieceValues() + board.getWhitePawnValues() + board.getBlackPieceValues() + board.getBlackPawnValues() <= RivalConstants.EVAL_ENDGAME_TOTAL_PIECES) {
            eval = endGameAdjustment(board, eval);
        }

        eval = board.getMover() == Colour.WHITE ? eval : -eval;

        return eval;
    }

    public int endGameAdjustment(EngineChessBoard board, int currentScore) {
        int eval = currentScore;

        if (rivalKPKBitbase != null && board.getWhitePieceValues() + board.getBlackPieceValues() == 0 && board.getWhitePawnValues() + board.getBlackPawnValues() == Piece.PAWN.getValue()) {
            if (board.getWhitePawnValues() == Piece.PAWN.getValue()) {
                return kpkLookup(
                        board.getWhiteKingSquare(),
                        board.getBlackKingSquare(),
                        Long.numberOfTrailingZeros(board.getWhitePawnBitboard()),
                        board.getMover() == Colour.WHITE);
            } else {
                // flip the position so that the black pawn becomes white, and negate the result
                return -kpkLookup(
                        Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare()),
                        Bitboards.bitFlippedHorizontalAxis.get(board.getWhiteKingSquare()),
                        Bitboards.bitFlippedHorizontalAxis.get(Long.numberOfTrailingZeros(board.getBlackPawnBitboard())),
                        board.getMover() == Colour.BLACK);
            }
        }

        if (board.getWhitePawnValues() + board.getBlackPawnValues() == 0 && board.getWhitePieceValues() < Piece.ROOK.getValue() && board.getBlackPieceValues() < Piece.ROOK.getValue())
            return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;

        if (eval > 0) {
            //noinspection ConstantConditions,ConditionCoveredByFurtherCondition
            if (board.getWhitePawnValues() == 0 && (board.getWhitePieceValues() == Piece.KNIGHT.getValue() || board.getWhitePieceValues() == Piece.BISHOP.getValue()))
                return eval - (int) (board.getWhitePieceValues() * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.getWhitePawnValues() == 0 && board.getWhitePieceValues() - Piece.BISHOP.getValue() <= board.getBlackPieceValues())
                return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR;
            else if (Long.bitCount(board.getAllPiecesBitboard()) > 3 && (board.getWhiteRookBitboard() | board.getWhiteKnightBitboard() | board.getWhiteQueenBitboard()) == 0) {
                // If this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw.
                // Do the same for H pawns

                if (((board.getWhitePawnBitboard() & ~Bitboards.FILE_A) == 0) &&
                        ((board.getWhiteBishopBitboard() & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.getBlackKingBitboard() & Bitboards.A8A7B8B7) != 0) || ((board.getWhitePawnBitboard() & ~Bitboards.FILE_H) == 0) &&
                                ((board.getWhiteBishopBitboard() & Bitboards.DARK_SQUARES) == 0) &&
                                ((board.getBlackKingBitboard() & Bitboards.H8H7G8G7) != 0)) {
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                }

            }
            if (board.getBlackPawnValues() == 0) {
                if (board.getWhitePieceValues() - board.getBlackPieceValues() > Piece.BISHOP.getValue()) {
                    int whiteKnightCount = Long.bitCount(board.getWhiteKnightBitboard());
                    int whiteBishopCount = Long.bitCount(board.getWhiteBishopBitboard());
                    if ((whiteKnightCount == 2) && (board.getWhitePieceValues() == 2 * Piece.KNIGHT.getValue()) && (board.getBlackPieceValues() == 0))
                        return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                    else if ((whiteKnightCount == 1) && (whiteBishopCount == 1) && (board.getWhitePieceValues() == Piece.KNIGHT.getValue() + Piece.BISHOP.getValue()) && board.getBlackPieceValues() == 0) {
                        eval = Piece.KNIGHT.getValue() + Piece.BISHOP.getValue() + RivalConstants.VALUE_SHOULD_WIN + (eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR);
                        final int kingSquare = board.getBlackKingSquare();

                        if ((board.getWhiteBishopBitboard() & Bitboards.DARK_SQUARES) != 0)
                            eval += (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        else
                            eval += (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;

                        return eval;
                    } else
                        return eval + RivalConstants.VALUE_SHOULD_WIN;
                }
            }
        }
        if (eval < 0) {
            //noinspection ConstantConditions,ConditionCoveredByFurtherCondition
            if (board.getBlackPawnValues() == 0 && (board.getBlackPieceValues() == Piece.KNIGHT.getValue() || board.getBlackPieceValues() == Piece.BISHOP.getValue()))
                return eval + (int) (board.getBlackPieceValues() * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.getBlackPawnValues() == 0 && board.getBlackPieceValues() - Piece.BISHOP.getValue() <= board.getWhitePieceValues())
                return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR;
            else if (Long.bitCount(board.getAllPiecesBitboard()) > 3 && (board.getBlackRookBitboard() | board.getBlackKnightBitboard() | board.getBlackQueenBitboard()) == 0) {
                if (((board.getBlackPawnBitboard() & ~Bitboards.FILE_A) == 0) &&
                        ((board.getBlackBishopBitboard() & Bitboards.DARK_SQUARES) == 0) &&
                        ((board.getWhiteKingBitboard() & Bitboards.A1A2B1B2) != 0))
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                else if (((board.getBlackPawnBitboard() & ~Bitboards.FILE_H) == 0) &&
                        ((board.getBlackBishopBitboard() & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.getWhiteKingBitboard() & Bitboards.H1H2G1G2) != 0))
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
            }
            if (board.getWhitePawnValues() == 0) {
                if (board.getBlackPieceValues() - board.getWhitePieceValues() > Piece.BISHOP.getValue()) {
                    int blackKnightCount = Long.bitCount(board.getBlackKnightBitboard());
                    int blackBishopCount = Long.bitCount(board.getBlackBishopBitboard());
                    if ((blackKnightCount == 2) && (board.getBlackPieceValues() == 2 * Piece.KNIGHT.getValue()) && (board.getWhitePieceValues() == 0))
                        return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                    else if ((blackKnightCount == 1) && (blackBishopCount == 1) && (board.getBlackPieceValues() == Piece.KNIGHT.getValue() + Piece.BISHOP.getValue()) && board.getWhitePieceValues() == 0) {
                        eval = -(Piece.KNIGHT.getValue() + Piece.BISHOP.getValue() + RivalConstants.VALUE_SHOULD_WIN) + (eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR);
                        final int kingSquare = board.getWhiteKingSquare();
                        if ((board.getBlackBishopBitboard() & Bitboards.DARK_SQUARES) != 0) {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        } else {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        }
                        return eval;
                    } else
                        return eval - RivalConstants.VALUE_SHOULD_WIN;
                }
            }
        }

        return eval;
    }

    public int kpkLookup(int attackingKingSquare, int defendingKingSquare, int pawnSquare, boolean isAttackerToMove) {
        if (attackingKingSquare % 8 >= 4) {
            /*
             * Flip board on vertical axis to bring White king to right-hand side of board
             */
            attackingKingSquare = Bitboards.bitFlippedVerticalAxis.get(attackingKingSquare);
            defendingKingSquare = Bitboards.bitFlippedVerticalAxis.get(defendingKingSquare);
            pawnSquare = Bitboards.bitFlippedVerticalAxis.get(pawnSquare);
        }

        int attackingKingBitbaseIndex = (attackingKingSquare / 8) * 4 + (attackingKingSquare % 8);
        int pawnBitbaseIndex = pawnSquare - 8;

        int index =
                (attackingKingBitbaseIndex * 64 * 48 * 2) +
                        (defendingKingSquare * 48 * 2) +
                        (pawnBitbaseIndex * 2) +
                        (isAttackerToMove ? 0 : 1);

        int byteIndex = index / 8;
        int bitIndex = index % 8;

        int byteElement = rivalKPKBitbase[byteIndex];
        boolean isWon = (byteElement & (1L << bitIndex)) != 0;

        if (!isWon) return 0;

        int pawnDistanceFromPromotion = 7 - (pawnSquare / 8);

        return Piece.QUEEN.getValue() - RivalConstants.ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE * pawnDistanceFromPromotion;
    }

    private int[] scoreQuiesceMoves(EngineChessBoard board, int ply, boolean includeChecks) throws InvalidMoveException {

        int moveCount = 0;

        int[] movesForSorting = orderedMoves[ply];

        for (int i = 0; movesForSorting[i] != 0; i++) {

            int move = movesForSorting[i];
            boolean isCapture = board.isCapture(move);

            // clear out additional info stored with the move
            move &= 0x00FFFFFF;

            final int score = getScore(board, move, includeChecks, isCapture);

            if (score > 0) {
                movesForSorting[moveCount++] = move | ((127 - score) << 24);
            }
        }

        movesForSorting[moveCount] = 0;

        return movesForSorting;
    }

    private int getScore(EngineChessBoard board, int move, boolean includeChecks, boolean isCapture) throws InvalidMoveException {
        int score = 0;

        final int promotionMask = (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL);
        if (isCapture) {
            final int see = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
            if (see > 0) {
                score = 100 + (int) (((double) see / Piece.QUEEN.getValue()) * 10);
            }
            if (promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                score += 9;
            }
        } else if (promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
            score = 116;
        } else if (includeChecks) {
            score = 100;
        }
        return score;
    }

    private int getHighScoreMove(EngineChessBoard board, int ply, int hashMove) throws InvalidMoveException {
        if (moveOrderStatus[ply] == MoveOrder.NONE && hashMove != 0) {
            for (int c = 0; orderedMoves[ply][c] != 0; c++) {
                if (orderedMoves[ply][c] == hashMove) {
                    orderedMoves[ply][c] = -1;
                    return hashMove;
                }
            }
        }

        if (moveOrderStatus[ply] == MoveOrder.NONE) {
            moveOrderStatus[ply] = MoveOrder.CAPTURES;
            if (scoreFullWidthCaptures(board, ply) == 0) {
                // no captures, so move to next stage
                scoreFullWidthMoves(board, ply);
                moveOrderStatus[ply] = MoveOrder.ALL;
            }
        }

        int move = getHighScoreMove(orderedMoves[ply]);

        if (move == 0 && moveOrderStatus[ply] == MoveOrder.CAPTURES) {
            // we move into here if we had some captures but they are now used up
            scoreFullWidthMoves(board, ply);
            moveOrderStatus[ply] = MoveOrder.ALL;
            move = getHighScoreMove(orderedMoves[ply]);
        }

        return move;
    }

    public int getHighScoreMove(int[] theseMoves) {
        int bestMove = 0;
        int bestIndex = -1;
        int bestScore = Integer.MAX_VALUE;

        for (int c = 0; theseMoves[c] != 0; c++) {
            if (theseMoves[c] != -1) {
                // update best move found so far, but don't consider moves with no score
                if (theseMoves[c] < bestScore && (theseMoves[c] >> 24) != 127) {
                    bestScore = theseMoves[c];
                    bestMove = theseMoves[c];
                    bestIndex = c;
                }
            }
        }

        if (bestIndex != -1) theseMoves[bestIndex] = -1;

        return bestMove & 0x00FFFFFF;
    }

    public SearchPath quiesce(EngineChessBoard board, final int depth, int ply, int quiescePly, int low, int high, boolean isCheck) throws InvalidMoveException {

        setNodes(getNodes() + 1);

        SearchPath newPath;
        SearchPath bestPath;

        bestPath = searchPath[ply];
        bestPath.reset();

        final int evalScore = evaluate(board);
        bestPath.score = isCheck ? -RivalConstants.VALUE_MATE : evalScore;

        if (depth == 0 || bestPath.score >= high) {
            return bestPath;
        }

        low = Math.max(bestPath.score, low);

        int[] theseMoves = orderedMoves[ply];

        if (isCheck) {
            board.setLegalMoves(theseMoves);
            scoreFullWidthMoves(board, ply);
        } else {
            board.setLegalQuiesceMoves(theseMoves, quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY);
            scoreQuiesceMoves(board, ply, quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY);
        }

        int move = getHighScoreMove(theseMoves);

        int legalMoveCount = 0;

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

    private boolean shouldDeltaPrune(EngineChessBoard board, int low, int evalScore, int move, boolean isCheck) {
        if (RivalConstants.USE_DELTA_PRUNING && !isCheck) {
            final int materialIncrease = (board.lastCapturePiece() > -1
                    ? RivalConstants.PIECE_VALUES.get(board.lastCapturePiece() % 6)
                    : 0) + getMaterialIncreaseForPromotion(move);

            return materialIncrease + evalScore + RivalConstants.DELTA_PRUNING_MARGIN < low;
        }

        return false;
    }

    private int getMaterialIncreaseForPromotion(int move) {
        switch (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
            case 0:
                return 0;
            case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                return Piece.QUEEN.getValue() - Piece.PAWN.getValue();
            case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                return Piece.BISHOP.getValue() - Piece.PAWN.getValue();
            case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                return Piece.KNIGHT.getValue() - Piece.PAWN.getValue();
            case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                return Piece.ROOK.getValue() - Piece.PAWN.getValue();
            default:
                return 0;
        }
    }

    private int scoreFullWidthCaptures(EngineChessBoard board, int ply) throws InvalidMoveException {
        int i, score;
        int count = 0;

        int[] movesForSorting = orderedMoves[ply];

        for (i = 0; movesForSorting[i] != 0; i++) {
            if (movesForSorting[i] != -1) {
                score = 0;

                final int toSquare = movesForSorting[i] & 63;
                Piece capturePiece = Piece.fromSquareOccupant(board.getSquareOccupant(toSquare));

                if (capturePiece == Piece.NONE &&
                        ((1L << toSquare) & board.getBitboardByIndex(RivalConstants.ENPASSANTSQUARE)) != 0 &&
                        board.getSquareOccupant((movesForSorting[i] >>> 16) & 63).getIndex() % 6 == RivalConstants.WP) {
                    capturePiece = Piece.PAWN;
                }

                movesForSorting[i] &= 0x00FFFFFF;

                if (movesForSorting[i] == mateKiller.get(ply)) {
                    score = 126;
                } else if (capturePiece != Piece.NONE) {
                    int see = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(movesForSorting[i]));
                    if (see > -RivalConstants.INFINITY) {
                        see = (int) (((double) see / Piece.QUEEN.getValue()) * 10);
                    }

                    if (see > 0) {
                        score = 110 + see;
                    }
                    else if ((movesForSorting[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                        score = 109;
                    }
                    else if (see == 0) {
                        score = 107;
                    }
                    else {
                        score = scoreLosingCapturesWithWinningHistory(board, ply, i, score, movesForSorting, toSquare);
                    }

                } else if ((movesForSorting[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                    score = 108;
                }

                if (score > 0) {
                    count++;
                }
                movesForSorting[i] |= ((127 - score) << 24);
            }
        }
        return count;
    }

    private int scoreLosingCapturesWithWinningHistory(EngineChessBoard board, int ply, int i, int score, int[] movesForSorting, int toSquare) {
        final int historyScore = historyScore(board.getMover() == Colour.WHITE, ((movesForSorting[i] >>> 16) & 63), toSquare);

        if (historyScore > 5) {
            score = historyScore;
        } else {
            for (int j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
                if (movesForSorting[i] == killerMoves[ply][j]) {
                    return 106 - j;
                }
            }
        }
        return score;
    }

    private int historyScore(boolean isWhite, int from, int to) {
        int success = historyMovesSuccess[isWhite ? 0 : 1][from][to];
        int fail = historyMovesFail[isWhite ? 0 : 1][from][to];
        int total = success + fail;
        if (total > 0) {
            return (success * 10 / total);
        }

        return 0;
    }

    private void scoreFullWidthMoves(EngineChessBoard board, int ply) {

        int[] movesForSorting = orderedMoves[ply];

        for (int i = 0; movesForSorting[i] != 0; i++) {
            if (movesForSorting[i] != -1) {
                final int fromSquare = ((movesForSorting[i] >>> 16) & 63);
                final int toSquare = (movesForSorting[i] & 63);

                movesForSorting[i] &= 0x00FFFFFF;

                int score = scoreKillerMoves(ply, i, movesForSorting);
                score = scoreHistoryHeuristic(board, score, fromSquare, toSquare);

                // must be a losing capture otherwise would have been scored in previous phase
                // give it a score of 1 to place towards the end of the list
                if (score == 0 && board.getSquareOccupant(toSquare) != SquareOccupant.NONE) {
                    score = 1;
                }

                if (score == 0 && RivalConstants.USE_PIECE_SQUARES_IN_MOVE_ORDERING) {
                    score = 50 + (scorePieceSquareValues(board, fromSquare, toSquare) / 2);
                }
                movesForSorting[i] |= ((127 - score) << 24);
            }
        }
    }

    private int scorePieceSquareValues(EngineChessBoard board, int fromSquare, int toSquare) {
        int ps = 0;
        if (board.getMover() == Colour.BLACK) {
            // piece square tables are set up from white's PoV
            fromSquare = Bitboards.bitFlippedHorizontalAxis.get(fromSquare);
            toSquare = Bitboards.bitFlippedHorizontalAxis.get(toSquare);
        }
        switch (board.getSquareOccupant(fromSquare).getIndex() % 6) {
            case RivalConstants.WP:
                ps =
                        Numbers.linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() : board.getWhitePieceValues()),
                                RivalConstants.PAWN_STAGE_MATERIAL_LOW,
                                RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
                                Bitboards.pieceSquareTablePawnEndGame.get(toSquare) - Bitboards.pieceSquareTablePawnEndGame.get(fromSquare),
                                Bitboards.pieceSquareTablePawn.get(toSquare) - Bitboards.pieceSquareTablePawn.get(fromSquare));
                break;
            case RivalConstants.WN:
                ps =
                        Numbers.linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() + board.getBlackPawnValues() : board.getWhitePieceValues() + board.getWhitePawnValues()),
                                RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
                                RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
                                Bitboards.pieceSquareTableKnightEndGame.get(toSquare) - Bitboards.pieceSquareTableKnightEndGame.get(fromSquare),
                                Bitboards.pieceSquareTableKnight.get(toSquare) - Bitboards.pieceSquareTableKnight.get(fromSquare));
                break;
            case RivalConstants.WB:
                ps = Bitboards.pieceSquareTableBishop.get(toSquare) - Bitboards.pieceSquareTableBishop.get(fromSquare);
                break;
            case RivalConstants.WR:
                ps = Bitboards.pieceSquareTableRook.get(toSquare) - Bitboards.pieceSquareTableRook.get(fromSquare);
                break;
            case RivalConstants.WQ:
                ps = Bitboards.pieceSquareTableQueen.get(toSquare) - Bitboards.pieceSquareTableQueen.get(fromSquare);
                break;
            case RivalConstants.WK:
                ps =
                        Numbers.linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() : board.getWhitePieceValues()),
                                Piece.ROOK.getValue(),
                                RivalConstants.OPENING_PHASE_MATERIAL,
                                Bitboards.pieceSquareTableKingEndGame.get(toSquare) - Bitboards.pieceSquareTableKingEndGame.get(fromSquare),
                                Bitboards.pieceSquareTableKing.get(toSquare) - Bitboards.pieceSquareTableKing.get(fromSquare));
                break;
            default:
                // do nothing
        }
        return ps;
    }

    private int scoreHistoryHeuristic(EngineChessBoard board, int score, int fromSquare, int toSquare) {
        if (score == 0 && RivalConstants.USE_HISTORY_HEURISTIC && historyMovesSuccess[board.getMover() == Colour.WHITE ? 0 : 1][fromSquare][toSquare] > 0) {
            score = 90 + historyScore(board.getMover() == Colour.WHITE, fromSquare, toSquare);
        }
        return score;
    }

    private int scoreKillerMoves(int ply, int i, int[] movesForSorting) {
        for (int j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
            if (movesForSorting[i] == killerMoves[ply][j]) {
                return 106 - j;
            }
        }
        return 0;
    }

    public SearchPath search(EngineChessBoard board, final int depth, int ply, int low, int high, int extensions, int recaptureSquare, boolean isCheck) throws InvalidMoveException {

        nodes++;

        if (this.getMillisSetByEngineMonitor() > this.searchTargetEndTime || nodes >= this.nodesToSearch) {
            this.m_abortingSearch = true;
            this.setOkToSendInfo(false);
            return null;
        }

        SearchPath newPath;
        SearchPath bestPath = searchPath[ply];

        bestPath.reset();

        if (board.previousOccurrencesOfThisPosition() == 2 || board.getHalfMoveCount() >= 100) {
            bestPath.score = RivalConstants.DRAW_CONTEMPT;
            return bestPath;
        }

        if (board.getWhitePieceValues() + board.getBlackPieceValues() + board.getWhitePawnValues() + board.getBlackPawnValues() == 0) {
            bestPath.score = 0;
            return bestPath;
        }

        final int depthRemaining = depth + (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL);

        byte flag = RivalConstants.UPPERBOUND;

        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);
        int hashMove = 0;

        if (RivalConstants.USE_HASH_TABLES) {
            if (RivalConstants.USE_HEIGHT_REPLACE_HASH && isHeightHashTableEntryValid(depthRemaining, board)) {
                boardHash.setHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_VERSION, boardHash.getHashTableVersion());
                hashMove = boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_MOVE);

                if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.LOWERBOUND) {
                    if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_SCORE) > low) {
                        low = boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                    }
                } else if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.UPPERBOUND) {
                    if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_SCORE) < high) {
                        high = boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                    }
                }

                if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.EXACTSCORE || low >= high) {
                    bestPath.score = boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                    bestPath.setPath(hashMove);
                    return bestPath;
                }
            }

            if (RivalConstants.USE_ALWAYS_REPLACE_HASH && hashMove == 0 && isAlwaysReplaceHashTableEntryValid(depthRemaining, board)) {

                hashMove = boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_MOVE);
                if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.LOWERBOUND) {
                    if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_SCORE) > low)
                        low = boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                } else if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.UPPERBOUND) {
                    if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_SCORE) < high)
                        high = boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                }

                if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) == RivalConstants.EXACTSCORE || low >= high) {
                    bestPath.score = boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_SCORE);
                    bestPath.setPath(hashMove);
                    return bestPath;
                }
            }
        }

        int checkExtend = 0;
        if ((extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
            if (RivalConstants.FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) {
                checkExtend = 1;
            }
        }

        if (depthRemaining <= 0) {
            bestPath = quiesce(board, RivalConstants.MAX_QUIESCE_DEPTH - 1, ply, 0, low, high, isCheck);
            if (bestPath.score < low) flag = RivalConstants.UPPERBOUND;
            else if (bestPath.score > high) flag = RivalConstants.LOWERBOUND;
            else
                flag = RivalConstants.EXACTSCORE;

            boardHash.storeHashMove(0, board, bestPath.score, flag, 0);
            return bestPath;
        }

        if (RivalConstants.USE_INTERNAL_ITERATIVE_DEEPENING && depthRemaining >= RivalConstants.IID_MIN_DEPTH && hashMove == 0 && board.isNotOnNullMove()) {
            boolean doIt = true;

            if (doIt) {
                if (depth - RivalConstants.IID_REDUCE_DEPTH > 0) {
                    newPath = search(board, (byte) (depth - RivalConstants.IID_REDUCE_DEPTH), ply, low, high, extensions, recaptureSquare, isCheck);
                    // it's not really a hash move, but this will cause the order routine to rank it first
                    if (newPath != null && newPath.height > 0) hashMove = newPath.move[0];
                }
            }
            bestPath.reset();
            // We reset this here because it may have been mucked about with during IID
            // Notice that the search calls with ply, not ply+1, because search is for this level (we haven't made a move)
        }

        int bestMoveForHash = 0;
        boolean scoutSearch = false;
        byte verifyingNullMoveDepthReduction = 0;
        int threatExtend = 0, pawnExtend = 0;

        int nullMoveReduceDepth = (depthRemaining > RivalConstants.NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE) ? RivalConstants.NULLMOVE_REDUCE_DEPTH + 1 : RivalConstants.NULLMOVE_REDUCE_DEPTH;
        if (RivalConstants.USE_NULLMOVE_PRUNING && !isCheck && board.isNotOnNullMove() && depthRemaining > 1) {
            if ((board.getMover() == Colour.WHITE ? board.getWhitePieceValues() : board.getBlackPieceValues()) >= RivalConstants.NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES &&
                    (board.getMover() == Colour.WHITE ? board.getWhitePawnValues() : board.getBlackPawnValues()) > 0) {
                board.makeNullMove();
                newPath = search(board, (byte) (depth - nullMoveReduceDepth - 1), ply + 1, -high, -low, extensions, -1, false);
                if (newPath != null) if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                if (!this.m_abortingSearch) {
                    if (-Objects.requireNonNull(newPath).score >= high) {
                        bestPath.score = -newPath.score;
                        board.unMakeNullMove();
                        return bestPath;
                    } else if (
                            RivalConstants.FRACTIONAL_EXTENSION_THREAT > 0 &&
                                    -newPath.score < -RivalConstants.MATE_SCORE_START &&
                                    (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                        threatExtend = 1;
                    } else {
                        threatExtend = 0;
                    }
                }
                board.unMakeNullMove();
            }
        }

        int[] theseMoves = orderedMoves[ply];
        board.setLegalMoves(theseMoves);
        moveOrderStatus[ply] = MoveOrder.NONE;

        boolean research;

        do {
            research = false;
            int legalMoveCount = 0;

            int futilityPruningEvaluation;

            boolean wasCheckBeforeMove = board.isCheck();

            // Check to see if we can futility prune this whole node
            boolean canFutilityPrune = false;
            int futilityScore = low;
            if (RivalConstants.USE_FUTILITY_PRUNING && depthRemaining < 4 && !wasCheckBeforeMove && threatExtend == 0 && Math.abs(low) < RivalConstants.MATE_SCORE_START && Math.abs(high) < RivalConstants.MATE_SCORE_START) {
                futilityPruningEvaluation = evaluate(board);
                futilityScore = futilityPruningEvaluation + RivalConstants.FUTILITY_MARGIN.get(depthRemaining - 1);
                if (futilityScore < low) canFutilityPrune = true;
            }

            int lateMoveReductionsMade = 0;
            int newExtensions = 0;
            int reductions = 0;

            int move;
            while ((move = getHighScoreMove(board, ply, hashMove)) != 0 && !this.m_abortingSearch) {
                final int targetPiece = board.getSquareOccupant(move & 63).getIndex();
                final int movePiece = board.getSquareOccupant((move >>> 16) & 63).getIndex();
                int recaptureExtend = 0;

                int newRecaptureSquare = -1;

                int currentSEEValue = -RivalConstants.INFINITY;
                if (RivalConstants.FRACTIONAL_EXTENSION_RECAPTURE > 0 && (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                    recaptureExtend = 0;

                    if (targetPiece != -1 && RivalConstants.PIECE_VALUES.get(movePiece).equals(RivalConstants.PIECE_VALUES.get(targetPiece))) {
                        currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
                        if (Math.abs(currentSEEValue) <= RivalConstants.RECAPTURE_EXTENSION_MARGIN)
                            newRecaptureSquare = (move & 63);
                    }

                    if ((move & 63) == recaptureSquare) {
                        if (currentSEEValue == -RivalConstants.INFINITY)
                            currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
                        if (Math.abs(currentSEEValue) > RivalConstants.PIECE_VALUES.get(board.getSquareOccupant(recaptureSquare).getIndex()) - RivalConstants.RECAPTURE_EXTENSION_MARGIN) {
                            recaptureExtend = 1;
                        }
                    }
                }

                if (board.makeMove(new EngineMove(move))) {
                    legalMoveCount++;

                    isCheck = board.isCheck();

                    if (RivalConstants.USE_FUTILITY_PRUNING && canFutilityPrune && !isCheck && board.wasCapture() && !board.wasPawnPush()) {
                        newPath = searchPath[ply + 1];
                        newPath.reset();
                        newPath.score = -futilityScore; // newPath.score gets reversed later
                    } else {
                        if ((extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                            pawnExtend = 0;
                            if (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0) {
                                if (board.wasPawnPush()) {
                                    pawnExtend = 1;
                                }
                            }
                        }

                        int partOfTree = ply / this.iterativeDeepeningCurrentDepth;
                        int maxNewExtensionsInThisPart = RivalConstants.MAX_NEW_EXTENSIONS_TREE_PART.get(Math.min(partOfTree, RivalConstants.LAST_EXTENSION_LAYER));

                        newExtensions =
                                extensions +
                                        Math.min(
                                                (checkExtend * RivalConstants.FRACTIONAL_EXTENSION_CHECK) +
                                                        (threatExtend * RivalConstants.FRACTIONAL_EXTENSION_THREAT) +
                                                        (recaptureExtend * RivalConstants.FRACTIONAL_EXTENSION_RECAPTURE) +
                                                        (pawnExtend * RivalConstants.FRACTIONAL_EXTENSION_PAWN),
                                                maxNewExtensionsInThisPart);

                        int lateMoveReduction = 0;
                        if (
                                RivalConstants.USE_LATE_MOVE_REDUCTIONS &&
                                        newExtensions == 0 &&
                                        legalMoveCount > RivalConstants.LMR_LEGALMOVES_BEFORE_ATTEMPT &&
                                        depthRemaining - verifyingNullMoveDepthReduction > 1 &&
                                        move != hashMove &&
                                        !wasCheckBeforeMove &&
                                        historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] <= RivalConstants.LMR_THRESHOLD &&
                                        board.wasCapture() &&
                                        (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0 || !board.wasPawnPush())) {
                            if (-evaluate(board) <= low + RivalConstants.LMR_CUT_MARGIN) {
                                lateMoveReduction = 1;
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] = RivalConstants.LMR_REPLACE_VALUE_AFTER_CUT;
                            }
                        }

                        //noinspection ConstantConditions
                        if (RivalConstants.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION > -1) {
                            lateMoveReductionsMade += lateMoveReduction;
                            if (lateMoveReductionsMade > RivalConstants.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION && depthRemaining > 3) {
                                lateMoveReduction = 2;
                            }
                        }

                        reductions = verifyingNullMoveDepthReduction + lateMoveReduction;
                        boolean lmrResearch;

                        do {
                            lmrResearch = false;
                            if (scoutSearch) {
                                newPath = search(engineChessBoard, (byte) (depth - 1) - reductions, ply + 1, -low - 1, -low, newExtensions, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                    else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                                if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                                    // research with normal window
                                    newPath = search(engineChessBoard, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck);
                                    if (newPath != null)
                                        if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                        else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                                }
                            } else {
                                newPath = search(board, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                    else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                            }
                            if (!this.m_abortingSearch && lateMoveReduction > 0 && -Objects.requireNonNull(newPath).score >= low) {
                                lmrResearch = RivalConstants.LMR_RESEARCH_ON_FAIL_HIGH;
                                lateMoveReduction = 0;
                            }
                        }
                        // this move lead to a beta cut-off so research without the reduction
                        while (lmrResearch);
                    }

                    if (!this.m_abortingSearch) {
                        Objects.requireNonNull(newPath).score = -newPath.score;

                        if (newPath.score >= high) {
                            if (RivalConstants.USE_HISTORY_HEURISTIC) {
                                historyMovesSuccess[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                                if (historyMovesSuccess[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] > RivalConstants.HISTORY_MAX_VALUE) {
                                    for (int i = 0; i < 2; i++)
                                        for (int j = 0; j < 64; j++)
                                            for (int k = 0; k < 64; k++) {
                                                if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2;
                                                if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2;
                                            }
                                }
                            }

                            if (RivalConstants.USE_LATE_MOVE_REDUCTIONS)
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += RivalConstants.LMR_ABOVE_ALPHA_ADDITION;

                            board.unMakeMove();
                            bestPath.setPath(move, newPath);
                            boardHash.storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depthRemaining);

                            if (RivalConstants.NUM_KILLER_MOVES > 0) {
                                if ((board.getBitboardByIndex(RivalConstants.ENEMY) & (move & 63)) == 0 || (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == 0) {
                                    // if this move is in second place, or if it's not in the table at all,
                                    // then move first to second, and replace first with this move
                                    if (killerMoves[ply][0] != move) {
                                        System.arraycopy(killerMoves[ply], 0, killerMoves[ply], 1, RivalConstants.NUM_KILLER_MOVES - 1);
                                        killerMoves[ply][0] = move;
                                    }
                                    if (RivalConstants.USE_MATE_HISTORY_KILLERS && newPath.score > RivalConstants.MATE_SCORE_START) {
                                        mateKiller.set(ply, move);
                                    }
                                }
                            }
                            return bestPath;
                        }

                        if (RivalConstants.USE_HISTORY_HEURISTIC) {
                            historyMovesFail[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                            if (historyMovesFail[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] > RivalConstants.HISTORY_MAX_VALUE) {
                                for (int i = 0; i < 2; i++)
                                    for (int j = 0; j < 64; j++)
                                        for (int k = 0; k < 64; k++) {
                                            if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2;
                                            if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2;
                                        }
                            }
                        }

                        if (newPath.score > bestPath.score) {
                            bestPath.setPath(move, newPath);
                        }

                        if (newPath.score > low) {
                            flag = RivalConstants.EXACTSCORE;
                            bestMoveForHash = move;
                            low = newPath.score;
                            scoutSearch = RivalConstants.USE_PV_SEARCH && depth - reductions + (newExtensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;

                            if (RivalConstants.USE_LATE_MOVE_REDUCTIONS)
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += RivalConstants.LMR_ABOVE_ALPHA_ADDITION;
                        } else if (RivalConstants.USE_LATE_MOVE_REDUCTIONS) {
                            historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] -= RivalConstants.LMR_NOT_ABOVE_ALPHA_REDUCTION;
                        }
                    }
                    board.unMakeMove();
                }
            }

            if (!this.m_abortingSearch) {
                if (legalMoveCount == 0) {
                    boolean isMate = board.getMover() == Colour.WHITE ? board.isSquareAttackedBy(board.getWhiteKingSquare(), Colour.BLACK) : board.isSquareAttackedBy(board.getBlackKingSquare(), Colour.WHITE);
                    bestPath.score = isMate ? -RivalConstants.VALUE_MATE : 0;
                    boardHash.storeHashMove(0, board, bestPath.score, RivalConstants.EXACTSCORE, RivalConstants.MAX_SEARCH_DEPTH);
                    return bestPath;
                }

                if (!research) {
                    boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, flag, depthRemaining);
                    return bestPath;
                }
            }
        }
        while (research);

        return null;
    }

    public SearchPath searchZero(EngineChessBoard board, byte depth, int ply, int low, int high) throws InvalidMoveException {

        setNodes(getNodes() + 1);

        int numMoves = 0;
        byte flag = RivalConstants.UPPERBOUND;
        int move;
        int bestMoveForHash = 0;

        move = orderedMoves[0][numMoves] & 0x00FFFFFF; // clear sort score

        SearchPath newPath;
        SearchPath bestPath = searchPath[0];
        bestPath.reset();

        int numLegalMovesAtDepthZero = 0;

        boolean scoutSearch = false;

        int checkExtend;
        int pawnExtend;

        while (move != 0 && !this.m_abortingSearch) {
            if (engineChessBoard.makeMove(new EngineMove(move))) {
                final boolean isCheck = board.isCheck();

                checkExtend = 0;
                pawnExtend = 0;
                if (RivalConstants.FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) {
                    checkExtend = 1;
                } else if (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0) {
                    if (board.wasPawnPush()) {
                        pawnExtend = 1;
                    }
                }

                int newExtensions =
                        Math.min(
                                (checkExtend * RivalConstants.FRACTIONAL_EXTENSION_CHECK) +
                                        (pawnExtend * RivalConstants.FRACTIONAL_EXTENSION_PAWN),
                                RivalConstants.FRACTIONAL_EXTENSION_FULL);

                numLegalMovesAtDepthZero++;

                currentDepthZeroMove = move;
                currentDepthZeroMoveNumber = numLegalMovesAtDepthZero;

                final BoardHash boardHash = engineChessBoard.getBoardHashObject();

                if (isDrawnAtRoot(engineChessBoard, 0)) {
                    newPath = new SearchPath();
                    newPath.score = 0;
                    newPath.setPath(move);
                } else {
                    if (scoutSearch) {
                        newPath = search(engineChessBoard, (byte) (depth - 1), ply + 1, -low - 1, -low, newExtensions, -1, isCheck);
                        if (newPath != null) {
                            if (newPath.score > RivalConstants.MATE_SCORE_START) {
                                newPath.score--;
                            }
                            else if (newPath.score < -RivalConstants.MATE_SCORE_START) {
                                newPath.score++;
                            }
                        }
                        if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                            newPath = search(engineChessBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
                            if (newPath != null) {
                                if (newPath.score > RivalConstants.MATE_SCORE_START) {
                                    newPath.score--;
                                } else {
                                    if (newPath.score < -RivalConstants.MATE_SCORE_START) {
                                        newPath.score++;
                                    }
                                }
                            }
                        }
                    } else {
                        newPath = search(engineChessBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
                        if (newPath != null) {
                            if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                            else {
                                if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                            }
                        }
                    }
                }
                if (!this.m_abortingSearch) {
                    Objects.requireNonNull(newPath).score = -Objects.requireNonNull(newPath).score;

                    if (newPath.score >= high) {
                        board.unMakeMove();
                        bestPath.setPath(move, newPath);
                        boardHash.storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depth);
                        depthZeroMoveScores[numMoves] = newPath.score;
                        return bestPath;
                    }

                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath);
                    }

                    if (newPath.score > low) {
                        flag = RivalConstants.EXACTSCORE;
                        bestMoveForHash = move;
                        low = newPath.score;

                        scoutSearch = RivalConstants.USE_PV_SEARCH && depth + (newExtensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;
                        m_currentPath.setPath(bestPath);
                        currentPathString = "" + m_currentPath;
                    }

                    depthZeroMoveScores[numMoves] = newPath.score;
                }
                engineChessBoard.unMakeMove();
            } else {
                depthZeroMoveScores[numMoves] = -RivalConstants.INFINITY;
            }
            numMoves++;
            move = orderedMoves[0][numMoves] & 0x00FFFFFF;
        }

        if (!this.m_abortingSearch) {
            if (numLegalMovesAtDepthZero == 1 && this.millisToThink < RivalConstants.MAX_SEARCH_MILLIS) {
                this.m_abortingSearch = true;
                m_currentPath.setPath(bestPath); // otherwise we will crash!
                currentPathString = "" + m_currentPath;
            } else {
                final BoardHash boardHash = engineChessBoard.getBoardHashObject();
                boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, flag, depth);
            }

            return bestPath;
        } else {
            return null;
        }
    }

    public Colour getMover() {
        return engineChessBoard.getMover();
    }

    public void makeMove(EngineMove engineMove) throws InvalidMoveException {
        engineChessBoard.makeMove(engineMove);
    }

    public void go() {

        initSearchVariables();
        setupMateAndKillerMoveTables();

        setupHistoryMoveTable();

        SearchPath path;

        try {

            engineChessBoard.setLegalMoves(depthZeroLegalMoves);
            int depthZeroMoveCount = 0;

            int c = 0;
            int[] depth1MovesTemp = new int[RivalConstants.MAX_LEGAL_MOVES];
            int move = depthZeroLegalMoves[c] & 0x00FFFFFF;
            drawnPositionsAtRootCount.add(0);
            drawnPositionsAtRootCount.add(0);
            int legal = 0;
            int bestNewbieScore = -RivalConstants.INFINITY;

            while (move != 0) {
                if (engineChessBoard.makeMove(new EngineMove(move))) {

                    List<Boolean> plyDraw = new ArrayList<>();
                    plyDraw.add(false);
                    plyDraw.add(false);

                    legal++;

                    if (this.iterativeDeepeningCurrentDepth < 1) // super beginner mode
                    {
                        SearchPath sp = quiesce(engineChessBoard, 40, 1, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY, engineChessBoard.isCheck());
                        sp.score = -sp.score;
                        if (sp.score > bestNewbieScore) {
                            bestNewbieScore = sp.score;
                            m_currentPath.reset();
                            m_currentPath.setPath(move);
                            m_currentPath.score = sp.score;
                            currentPathString = m_currentPath.toString();
                        }
                    } else if (legal == 1) {
                        // use this opportunity to set a move in the odd event that there is no time to search
                        m_currentPath.reset();
                        m_currentPath.setPath(move);
                        m_currentPath.score = 0;
                        currentPathString = m_currentPath.toString();
                    }
                    if (engineChessBoard.previousOccurrencesOfThisPosition() == 2) {
                        plyDraw.set(0, true);
                    }

                    engineChessBoard.setLegalMoves(depth1MovesTemp);

                    int c1 = -1;

                    while ((depth1MovesTemp[++c1] & 0x00FFFFFF) != 0) {
                        if (engineChessBoard.makeMove(new EngineMove(depth1MovesTemp[c1] & 0x00FFFFFF))) {
                            if (engineChessBoard.previousOccurrencesOfThisPosition() == 2) {
                                plyDraw.set(1, true);
                            }
                            engineChessBoard.unMakeMove();
                        }
                    }

                    final BoardHash boardHash = engineChessBoard.getBoardHashObject();
                    for (int i=0; i<=1; i++) {
                        if (Boolean.TRUE.equals(plyDraw.get(i))) {
                            drawnPositionsAtRoot.get(i).add(boardHash.getTrackedHashValue());
                        }
                    }

                    engineChessBoard.unMakeMove();

                }

                depthZeroMoveCount++;

                move = depthZeroLegalMoves[++c] & 0x00FFFFFF;

            }

            if (this.useOpeningBook && getFen().trim().equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -")) {
                this.m_inBook = true;
            }

            if (this.m_inBook) {
                int libraryMove = OpeningLibrary.getMove(getFen());
                if (libraryMove > 0 && engineChessBoard.isMoveLegal(libraryMove)) {
                    path = new SearchPath();
                    path.setPath(libraryMove);
                    m_currentPath = path;
                    currentPathString = "" + m_currentPath;
                    setSearchComplete();
                    return;
                } else {
                    this.m_inBook = false;
                }
            }

            while (depthZeroLegalMoves[depthZeroMoveCount] != 0) depthZeroMoveCount++;

            scoreFullWidthMoves(engineChessBoard, 0);

            for (byte depth = 1; depth <= this.m_finalDepthToSearch && !this.m_abortingSearch; depth++) {
                this.iterativeDeepeningCurrentDepth = depth;

                if (depth > 1) setOkToSendInfo(true);

                if (RivalConstants.USE_ASPIRATION_WINDOW) {
                    path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);

                    if (!this.m_abortingSearch && Objects.requireNonNull(path).score <= aspirationLow) {
                        aspirationLow = -RivalConstants.INFINITY;
                        path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);
                    } else if (!this.m_abortingSearch && path.score >= aspirationHigh) {
                        aspirationHigh = RivalConstants.INFINITY;
                        path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);
                    }

                    if (!this.m_abortingSearch && (Objects.requireNonNull(path).score <= aspirationLow || path.score >= aspirationHigh)) {
                        path = searchZero(engineChessBoard, depth, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY);
                    }

                    if (!this.m_abortingSearch) {
                        m_currentPath.setPath(Objects.requireNonNull(path));
                        currentPathString = "" + m_currentPath;
                        aspirationLow = path.score - RivalConstants.ASPIRATION_RADIUS;
                        aspirationHigh = path.score + RivalConstants.ASPIRATION_RADIUS;
                    }
                } else {
                    path = searchZero(engineChessBoard, depth, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY);
                }

                if (!this.m_abortingSearch) {
                    m_currentPath.setPath(Objects.requireNonNull(path));
                    currentPathString = "" + m_currentPath;
                    if (path.score > RivalConstants.MATE_SCORE_START) {
                        setSearchComplete();
                        return;
                    }
                    for (int pass = 1; pass < depthZeroMoveCount; pass++) {
                        for (int i = 0; i < depthZeroMoveCount - pass; i++) {
                            if (depthZeroMoveScores[i] < depthZeroMoveScores[i + 1]) {
                                int tempScore;
                                tempScore = depthZeroMoveScores[i];
                                depthZeroMoveScores[i] = depthZeroMoveScores[i + 1];
                                depthZeroMoveScores[i + 1] = tempScore;

                                int tempMove;
                                tempMove = orderedMoves[0][i];
                                orderedMoves[0][i] = orderedMoves[0][i + 1];
                                orderedMoves[0][i + 1] = tempMove;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            printStream.println(e.getStackTrace());
        } finally {
            setSearchComplete();
        }
    }

    private void initSearchVariables() {
        searchState = SearchState.SEARCHING;
        m_abortingSearch = false;

        final BoardHash boardHash = engineChessBoard.getBoardHashObject();
        boardHash.incVersion();

        searchStartTime = System.currentTimeMillis();
        searchEndTime = 0;
        searchTargetEndTime = this.searchStartTime + this.millisToThink - RivalConstants.UCI_TIMER_INTERVAL_MILLIS;

        aspirationLow = -RivalConstants.INFINITY;
        aspirationHigh = RivalConstants.INFINITY;
        nodes = 0;
    }

    private void setupMateAndKillerMoveTables() {
        for (int i = 0; i < RivalConstants.MAX_TREE_DEPTH; i++) {
            this.mateKiller.add(-1);
            for (int j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
                this.killerMoves[i][j] = -1;
            }
        }
    }

    private void setupHistoryMoveTable() {
        for (int i = 0; i < 64; i++)
            for (int j = 0; j < 64; j++) {
                historyMovesSuccess[0][i][j] = 0;
                historyMovesSuccess[1][i][j] = 0;
                historyMovesFail[0][i][j] = 0;
                historyMovesFail[1][i][j] = 0;
                historyPruneMoves[0][i][j] = RivalConstants.LMR_INITIAL_VALUE;
                historyPruneMoves[1][i][j] = RivalConstants.LMR_INITIAL_VALUE;
            }
    }

    public synchronized SearchState getEngineState() {
        return searchState;
    }

    public void setMillisToThink(int millisToThink) {
        this.millisToThink = millisToThink;

        if (this.millisToThink < RivalConstants.MIN_SEARCH_MILLIS) {
            this.millisToThink = RivalConstants.MIN_SEARCH_MILLIS;
        }
    }

    public void setNodesToSearch(int nodesToSearch) {
        this.nodesToSearch = nodesToSearch;
    }

    public void setSearchDepth(int searchDepth) {
        if (searchDepth < 0) {
            searchDepth = 1;
        }

        this.m_finalDepthToSearch = searchDepth;
    }

    public int getNodes() {
        return this.nodes;
    }

    public synchronized boolean isSearching() {
        return searchState == SearchState.SEARCHING || searchState == SearchState.REQUESTED;
    }

    public String getCurrentScoreHuman() {
        int score = this.m_currentPath.getScore();
        int abs = Math.abs(score);
        if (abs > RivalConstants.MATE_SCORE_START) {
            int mateIn = ((RivalConstants.VALUE_MATE - abs) + 1) / 2;
            return "mate " + (score < 0 ? "-" : "") + mateIn;
        }
        return "cp " + score;
    }

    public int getCurrentScore() {
        return this.m_currentPath.getScore();
    }

    public long getSearchDuration() {
        long timePassed = 0;
        switch (searchState) {
            case READY:
                timePassed = 0;
                break;
            case SEARCHING:
                timePassed = System.currentTimeMillis() - this.searchStartTime;
                break;
            case COMPLETE:
                timePassed = this.searchEndTime - this.searchStartTime;
                break;
            case REQUESTED:
                timePassed = 0;
                break;
            default:
                throw new IllegalSearchStateException("Illegal Search State " + searchState);
        }
        return timePassed;
    }

    public int getNodesPerSecond() {
        long timePassed = getSearchDuration();
        if (timePassed == 0) {
            return 0;
        } else {
            return (int) (((double) this.getNodes() / (double) timePassed) * 1000.0);
        }
    }

    private boolean isDrawnAtRoot(EngineChessBoard board, int ply) {
        int i;
        final BoardHash boardHash = engineChessBoard.getBoardHashObject();
        for (i = 0; i < drawnPositionsAtRootCount.get(ply); i++) {
            if (drawnPositionsAtRoot.get(ply).get(i).equals(boardHash.getTrackedHashValue())) {
                return true;
            }
        }
        return false;
    }

    public int getIterativeDeepeningDepth() {
        return this.iterativeDeepeningCurrentDepth;
    }

    public int getCurrentMove() {
        return this.m_currentPath.move[0];
    }

    public String getCurrentPathString() {
        // don't want to calculate it if called from another thread
        return this.currentPathString;
    }

    public synchronized void startSearch() {
        searchState = SearchState.REQUESTED;
    }

    public synchronized void stopSearch() {
        this.m_abortingSearch = true;
    }

    public synchronized void setSearchComplete() {
        this.searchEndTime = System.currentTimeMillis();
        searchState = SearchState.COMPLETE;
    }

    public void setUseOpeningBook(boolean useBook) {
        this.useOpeningBook = RivalConstants.USE_INTERNAL_OPENING_BOOK && useBook;
        this.m_inBook = this.useOpeningBook;
    }

    public boolean isAbortingSearch() {
        return this.m_abortingSearch;
    }

    public void quit() {
        this.quit = true;
    }

    public void run() {
        while (!quit) {
            Thread.yield();
            if (searchState == SearchState.REQUESTED) {
                go();

                setOkToSendInfo(false);

                if (m_isUCIMode) {
                    String s1 =
                            "info" +
                                    " currmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(currentDepthZeroMove) +
                                    " currmovenumber " + currentDepthZeroMoveNumber +
                                    " depth " + getIterativeDeepeningDepth() +
                                    " score " + getCurrentScoreHuman() +
                                    " pv " + getCurrentPathString() +
                                    " time " + getSearchDuration() +
                                    " nodes " + getNodes() +
                                    " nps " + getNodesPerSecond();

                    String s2 = "bestmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(getCurrentMove());
                    printStream.println(s1);
                    printStream.println(s2);
                }
            }
        }
    }

    public boolean isOkToSendInfo() {
        return isOkToSendInfo;
    }

    public void setOkToSendInfo(boolean okToSendInfo) {
        this.isOkToSendInfo = okToSendInfo;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public long getMillisSetByEngineMonitor() {
        return millisSetByEngineMonitor;
    }

    public void setMillisSetByEngineMonitor(long millisSetByEngineMonitor) {
        this.millisSetByEngineMonitor = millisSetByEngineMonitor;
    }

    public void setEngineChessBoard(EngineChessBoard engineChessBoard) {
        this.engineChessBoard = engineChessBoard;
    }

    public String getFen() {
        return engineChessBoard.getFen();
    }

    public int getCurrentDepthZeroMoveNumber() {
        return currentDepthZeroMoveNumber;
    }

    public int getCurrentDepthZeroMove() {
        return currentDepthZeroMove;
    }

}
