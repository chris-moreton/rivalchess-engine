package com.netsensia.rivalchess.engine.core;

import com.ea.async.Async;
import com.netsensia.rivalchess.bitboards.BitboardType;
import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.config.Evaluation;
import com.netsensia.rivalchess.config.Extensions;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.config.IterativeDeepening;
import com.netsensia.rivalchess.config.LateMoveReductions;
import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.config.SearchConfig;
import com.netsensia.rivalchess.config.Uci;
import com.netsensia.rivalchess.engine.core.eval.PieceSquareTables;
import com.netsensia.rivalchess.engine.core.eval.PieceValue;
import com.netsensia.rivalchess.enums.CastleBitMask;
import com.netsensia.rivalchess.enums.HashIndex;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.enums.MoveOrder;
import com.netsensia.rivalchess.enums.PromotionPieceMask;
import com.netsensia.rivalchess.enums.SearchState;
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluator;
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluatorPremium;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.IllegalSearchStateException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.openings.OpeningLibrary;
import com.netsensia.rivalchess.uci.EngineMonitor;
import com.netsensia.rivalchess.util.ChessBoardConversion;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getBlackPawnAttacks;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getWhitePawnAttacks;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.southFill;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.squareList;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.bishopAttackMap;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.castlingEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.tradePawnBonusWhenMoreMaterial;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.tradePieceBonusWhenMoreMaterial;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.whiteEvaluation;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.blackEvaluation;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.blackRookOpenFilesEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.blackRookPieceSquareSum;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.combineAttacks;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.doubledRooksEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.kingAttackCount;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.knightAttackMap;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.materialDifference;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.linearScale;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.queenAttackMap;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.rookAttackMap;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.rookEnemyPawnMultiplier;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.twoBlackRooksTrappingKingEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.twoWhiteRooksTrappingKingEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.whiteKingSquareEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.whitePawnPieceSquareEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.whiteRookOpenFilesEval;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.whiteRookPieceSquareSum;
import static com.netsensia.rivalchess.engine.core.hash.SearchHashHelper.isAlwaysReplaceHashTableEntryValid;
import static com.netsensia.rivalchess.engine.core.hash.SearchHashHelper.isHeightHashTableEntryValid;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.onlyKingRemains;

public final class Search implements Runnable {

    private final PrintStream printStream;

    private final StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluatorPremium();

    private final MoveOrder[] moveOrderStatus = new MoveOrder[Limit.MAX_TREE_DEPTH.getValue()];

    private final List<List<Long>> drawnPositionsAtRoot;
    private final List<Integer> drawnPositionsAtRootCount = new ArrayList<>();
    private EngineChessBoard engineChessBoard
            = new EngineChessBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
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

    private int millisToThink;
    private int nodesToSearch = Integer.MAX_VALUE;

    private boolean m_abortingSearch = true;

    private long searchStartTime = -1;
    private long searchTargetEndTime;
    private long searchEndTime = 0;

    private int m_finalDepthToSearch = 1;
    private int iterativeDeepeningCurrentDepth = 0; // current search depth for iterative deepening

    private boolean useOpeningBook = FeatureFlag.USE_INTERNAL_OPENING_BOOK.isActive();
    private boolean m_inBook = useOpeningBook;

    private int currentDepthZeroMove;
    private int currentDepthZeroMoveNumber;

    private SearchPath m_currentPath;
    private String currentPathString;

    private boolean m_isUCIMode = false;

    private static final byte[] rivalKPKBitbase = null;

    public Search() throws IllegalFenException {
        this(System.out, FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
    }

    public Search(PrintStream printStream) throws IllegalFenException {
        this(printStream, FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
    }

    public Search(Board board) throws IllegalFenException {
        this(System.out, board);
    }

    public Search(PrintStream printStream, Board board) throws IllegalFenException {

        this.engineChessBoard.setBoard(board);

        drawnPositionsAtRoot = new ArrayList<>();
        drawnPositionsAtRoot.add(new ArrayList<>());
        drawnPositionsAtRoot.add(new ArrayList<>());

        this.printStream = printStream;

        this.setMillisSetByEngineMonitor(System.currentTimeMillis());

        this.m_currentPath = new SearchPath();
        this.currentPathString = "";

        searchState = SearchState.READY;

        this.searchPath = new SearchPath[Limit.MAX_TREE_DEPTH.getValue()];
        this.killerMoves = new int[Limit.MAX_TREE_DEPTH.getValue()][SearchConfig.NUM_KILLER_MOVES.getValue()];
        for (int i = 0; i < Limit.MAX_TREE_DEPTH.getValue(); i++) {
            this.searchPath[i] = new SearchPath();
            this.killerMoves[i] = new int[SearchConfig.NUM_KILLER_MOVES.getValue()];
        }

        orderedMoves = new int[Limit.MAX_TREE_DEPTH.getValue()][Limit.MAX_LEGAL_MOVES.getValue()];

        depthZeroLegalMoves = orderedMoves[0];
        depthZeroMoveScores = new int[Limit.MAX_LEGAL_MOVES.getValue()];
    }

    public void startEngineTimer(boolean isUCIMode) {
        this.m_isUCIMode = isUCIMode;
        EngineMonitor m_monitor = new EngineMonitor(this);
        new Timer().schedule(m_monitor, Uci.UCI_TIMER_INTERVAL_MILLIS.getValue(), Uci.UCI_TIMER_INTERVAL_MILLIS.getValue());
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

    static {
        Async.init();
    }

    public int evaluate(EngineChessBoard board) {

        if (onlyKingRemains(board)) {
            return 0;
        }

        int sq;
        long bitboard;

        final long whitePieces = board.getBitboard(board.getMover() == Colour.WHITE ? BitboardType.FRIENDLY : BitboardType.ENEMY);
        final long blackPieces = board.getBitboard(board.getMover() == Colour.WHITE ? BitboardType.ENEMY : BitboardType.FRIENDLY);
        final long whiteKingDangerZone = Bitboards.kingMoves.get(board.getWhiteKingSquare()) | (Bitboards.kingMoves.get(board.getWhiteKingSquare()) << 8);
        final long blackKingDangerZone = Bitboards.kingMoves.get(board.getBlackKingSquare()) | (Bitboards.kingMoves.get(board.getBlackKingSquare()) >>> 8);

        final List<Integer> whiteRookSquares = squareList(board.getBitboard(BitboardType.WR));
        final Map<Integer, Long> whiteRookAttacks = rookAttackMap(board, whiteRookSquares);
        final List<Integer> blackRookSquares = squareList(board.getBitboard(BitboardType.BR));
        final  Map<Integer, Long> blackRookAttacks = rookAttackMap(board, blackRookSquares);
        final List<Integer> whiteKnightSquares = squareList(board.getBitboard(BitboardType.WN));
        final Map<Integer, Long> whiteKnightAttacks = knightAttackMap(whiteKnightSquares);
        final List<Integer> blackKnightSquares = squareList(board.getBitboard(BitboardType.BN));
        final Map<Integer, Long> blackKnightAttacks = knightAttackMap(blackKnightSquares);
        final List<Integer> whiteQueenSquares = squareList(board.getBitboard(BitboardType.WQ));
        final Map<Integer, Long> whiteQueenAttacks = queenAttackMap(board, whiteQueenSquares);
        final List<Integer> blackQueenSquares = squareList(board.getBitboard(BitboardType.BQ));
        final  Map<Integer, Long> blackQueenAttacks = queenAttackMap(board, blackQueenSquares);
        final List<Integer> whiteBishopSquares = squareList(board.getBitboard(BitboardType.WB));
        final Map<Integer, Long> whiteBishopAttacks = bishopAttackMap(board, whiteBishopSquares);
        final List<Integer> blackBishopSquares = squareList(board.getBitboard(BitboardType.BB));
        final  Map<Integer, Long> blackBishopAttacks = bishopAttackMap(board, blackBishopSquares);

        final int materialDifference = materialDifference(board);

        int blackKingAttackedCount =
                kingAttackCount(blackKingDangerZone, whiteRookAttacks) +
                        kingAttackCount(blackKingDangerZone, whiteQueenAttacks) * 2;

        int whiteKingAttackedCount =
                kingAttackCount(whiteKingDangerZone, blackRookAttacks) +
                        kingAttackCount(whiteKingDangerZone, blackQueenAttacks) * 2;

        long whiteAttacksBitboard = combineAttacks(whiteRookAttacks) |
                combineAttacks(whiteQueenAttacks) |
                combineAttacks(whiteBishopAttacks) |
                combineAttacks(whiteKnightAttacks);

        long blackAttacksBitboard = combineAttacks(blackRookAttacks) |
                combineAttacks(blackQueenAttacks) |
                combineAttacks(blackBishopAttacks) |
                combineAttacks(blackKnightAttacks);

        int eval = materialDifference;

        eval += whiteEvaluation(board) - blackEvaluation(board);

        eval += engineChessBoard.getBoardHashObject().getPawnHashEntry(board).getPawnScore();

        eval += tradePawnBonusWhenMoreMaterial(board, materialDifference);
        eval += tradePieceBonusWhenMoreMaterial(board, materialDifference);

        eval += castlingEval(board);

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

            eval += PieceSquareTables.bishop.get(sq);

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone);

            bishopScore += Evaluation.getBishopMobilityValue(Long.bitCount(allAttacks & ~whitePieces));
        }

        if (whiteBishopColourCount == 2)
            bishopScore += Evaluation.VALUE_BISHOP_PAIR.getValue() + ((8 - (board.getWhitePawnValues() / PieceValue.getValue(Piece.PAWN))) * Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.getValue());

        bitboard = board.getBlackBishopBitboard();
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            eval -= PieceSquareTables.bishop.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.getAllPiecesBitboard() & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone);

            bishopScore -= Evaluation.getBishopMobilityValue(Long.bitCount(allAttacks & ~blackPieces));
        }

        if (blackBishopColourCount == 2)
            bishopScore -= Evaluation.VALUE_BISHOP_PAIR.getValue() + ((8 - (board.getBlackPawnValues() / PieceValue.getValue(Piece.PAWN))) * Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.getValue());

        if (whiteBishopColourCount == 1 && blackBishopColourCount == 1 && whiteLightBishopExists != blackLightBishopExists && board.getWhitePieceValues() == board.getBlackPieceValues()) {
            // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
            final int maxPenalty = (eval + bishopScore) / Evaluation.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR.getValue(); // mostly pawns as material is identical

            // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
            bishopScore -= linearScale(
                    board.getWhitePieceValues() + board.getBlackPieceValues(),
                    Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_LOW.getValue(),
                    Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_HIGH.getValue(),
                    maxPenalty,
                    0);
        }

        if (((board.getWhiteBishopBitboard() | board.getBlackBishopBitboard()) & Bitboards.A2A7H2H7) != 0) {
            if ((board.getWhiteBishopBitboard() & (1L << Square.A7.getBitRef())) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Square.B6.getBitRef())) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Square.C7.getBitRef())) != 0)
                bishopScore -= Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.getValue();

            if ((board.getWhiteBishopBitboard() & (1L << Square.H7.getBitRef())) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Square.G6.getBitRef())) != 0 &&
                    (board.getBlackPawnBitboard() & (1L << Square.F7.getBitRef())) != 0)
                bishopScore -= (board.getWhiteQueenBitboard() == 0) ?
                        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.getValue() :
                        Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.getValue();

            if ((board.getBlackBishopBitboard() & (1L << Square.A2.getBitRef())) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Square.B3.getBitRef())) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Square.C2.getBitRef())) != 0)
                bishopScore += Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.getValue();

            if ((board.getBlackBishopBitboard() & (1L << Square.H2.getBitRef())) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Square.G3.getBitRef())) != 0 &&
                    (board.getWhitePawnBitboard() & (1L << Square.F2.getBitRef())) != 0)
                bishopScore += (board.getBlackQueenBitboard() == 0) ?
                        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.getValue() :
                        Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.getValue();
        }

        eval += bishopScore;

        // Everything white attacks with pieces.  Does not include attacked pawns.
        whiteAttacksBitboard &= board.getBlackKnightBitboard() | board.getBlackRookBitboard() | board.getBlackQueenBitboard() | board.getBlackBishopBitboard();
        // Plus anything white attacks with pawns.
        whiteAttacksBitboard |= getWhitePawnAttacks(board.getWhitePawnBitboard());

        int temp = 0;

        bitboard = whiteAttacksBitboard;

        while (bitboard != 0) {
            bitboard ^= ((1L << (sq = Long.numberOfTrailingZeros(bitboard))));
            if (board.getSquareOccupant(sq) == SquareOccupant.BP) temp += PieceValue.getValue(Piece.PAWN);
            else if (board.getSquareOccupant(sq) == SquareOccupant.BN) temp += PieceValue.getValue(Piece.KNIGHT);
            else if (board.getSquareOccupant(sq) == SquareOccupant.BR) temp += PieceValue.getValue(Piece.ROOK);
            else if (board.getSquareOccupant(sq) == SquareOccupant.BQ) temp += PieceValue.getValue(Piece.QUEEN);
            else if (board.getSquareOccupant(sq) == SquareOccupant.BB) temp += PieceValue.getValue(Piece.BISHOP);
        }

        int threatScore = temp + temp * (temp / PieceValue.getValue(Piece.QUEEN));

        blackAttacksBitboard &= board.getWhiteKnightBitboard() | board.getWhiteRookBitboard() | board.getWhiteQueenBitboard() | board.getWhiteBishopBitboard();
        blackAttacksBitboard |= getBlackPawnAttacks(board.getBlackPawnBitboard());

        temp = 0;

        bitboard = blackAttacksBitboard;

        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
            if (board.getSquareOccupant(sq) == SquareOccupant.WP) temp += PieceValue.getValue(Piece.PAWN);
            else if (board.getSquareOccupant(sq) == SquareOccupant.WN) temp += PieceValue.getValue(Piece.KNIGHT);
            else if (board.getSquareOccupant(sq) == SquareOccupant.WR) temp += PieceValue.getValue(Piece.ROOK);
            else if (board.getSquareOccupant(sq) == SquareOccupant.WQ) temp += PieceValue.getValue(Piece.QUEEN);
            else if (board.getSquareOccupant(sq) == SquareOccupant.WB) temp += PieceValue.getValue(Piece.BISHOP);
        }

        threatScore -= temp + temp * (temp / PieceValue.getValue(Piece.QUEEN));
        threatScore /= Evaluation.THREAT_SCORE_DIVISOR.getValue();

        eval += threatScore;

        final int averagePiecesPerSide = (board.getWhitePieceValues() + board.getBlackPieceValues()) / 2;
        int whiteKingSafety = 0;
        int blackKingSafety = 0;
        int kingSafety = 0;
        if (averagePiecesPerSide > Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.getValue()) {

            whiteKingSafety = Evaluate.getWhiteKingRightWayScore(board);
            blackKingSafety = Evaluate.getBlackKingRightWayScore(board);

            int halfOpenFilePenalty = 0;
            int shieldValue = 0;
            if (board.getWhiteKingSquare() / 8 < 2) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.getWhiteKingSquare() % 8);

                shieldValue += Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.getValue() * Long.bitCount(board.getWhitePawnBitboard() & kingShield)
                        - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.getValue() * Long.bitCount(board.getBlackPawnBitboard() & (kingShield | (kingShield << 8)))
                        + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.getValue() * Long.bitCount(board.getWhitePawnBitboard() & (kingShield << 8))
                        - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.getValue() * Long.bitCount(board.getBlackPawnBitboard() & (kingShield << 16));

                shieldValue = Math.min(shieldValue, Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.getValue());

                if (((board.getWhiteKingBitboard() & Bitboards.F1G1) != 0) &&
                        ((board.getWhiteRookBitboard() & Bitboards.G1H1) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_G) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_H) != 0)) {
                    shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.getValue();
                } else if (((board.getWhiteKingBitboard() & Bitboards.B1C1) != 0) &&
                        ((board.getWhiteRookBitboard() & Bitboards.A1B1) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_A) != 0) &&
                        ((board.getWhitePawnBitboard() & Bitboards.FILE_B) != 0)) {
                    shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.getValue();
                }

                final long whiteOpen = southFill(kingShield, 8) & (~southFill(board.getWhitePawnBitboard(), 8)) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.getValue() * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.getValue() * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = southFill(kingShield, 8) & (~southFill(board.getBlackPawnBitboard(), 8)) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.getValue() * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.getValue() * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            whiteKingSafety += Evaluation.KINGSAFETY_SHIELD_BASE.getValue() + shieldValue - halfOpenFilePenalty;

            shieldValue = 0;
            halfOpenFilePenalty = 0;
            if (board.getBlackKingSquare() / 8 >= 6) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.getBlackKingSquare() % 8) << 40;
                shieldValue += Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.getValue() * Long.bitCount(board.getBlackPawnBitboard() & kingShield)
                        - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.getValue() * Long.bitCount(board.getWhitePawnBitboard() & (kingShield | (kingShield >>> 8)))
                        + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.getValue() * Long.bitCount(board.getBlackPawnBitboard() & (kingShield >>> 8))
                        - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.getValue() * Long.bitCount(board.getWhitePawnBitboard() & (kingShield >>> 16));

                shieldValue = Math.min(shieldValue, Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.getValue());

                if (((board.getBlackKingBitboard() & Bitboards.F8G8) != 0) &&
                        ((board.getBlackRookBitboard() & Bitboards.G8H8) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_G) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_H) != 0)) {
                    shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.getValue();
                } else if (((board.getBlackKingBitboard() & Bitboards.B8C8) != 0) &&
                        ((board.getBlackRookBitboard() & Bitboards.A8B8) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_A) != 0) &&
                        ((board.getBlackPawnBitboard() & Bitboards.FILE_B) != 0)) {
                    shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.getValue();
                }

                final long whiteOpen = southFill(kingShield, 8) & (~southFill(board.getWhitePawnBitboard(), 8)) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.getValue() * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.getValue() * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = southFill(kingShield, 8) & (~southFill(board.getBlackPawnBitboard(), 8)) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.getValue() * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.getValue() * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            blackKingSafety += Evaluation.KINGSAFETY_SHIELD_BASE.getValue() + shieldValue - halfOpenFilePenalty;

            kingSafety =
                    linearScale(
                            averagePiecesPerSide,
                            Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.getValue(),
                            Evaluation.KINGSAFETY_MAX_PIECE_BALANCE.getValue(),
                            0,
                            (whiteKingSafety - blackKingSafety) + (blackKingAttackedCount - whiteKingAttackedCount) * Evaluation.KINGSAFETY_ATTACK_MULTIPLIER.getValue());

        }

        eval += kingSafety;

        if (board.getWhitePieceValues() + board.getWhitePawnValues() + board.getBlackPieceValues() + board.getBlackPawnValues() <= Evaluation.EVAL_ENDGAME_TOTAL_PIECES.getValue()) {
            eval = endGameAdjustment(board, eval);
        }

        eval = board.getMover() == Colour.WHITE ? eval : -eval;

        return eval;
    }

    public int endGameAdjustment(EngineChessBoard board, int currentScore) {
        int eval = currentScore;

        if (rivalKPKBitbase != null && board.getWhitePieceValues() + board.getBlackPieceValues() == 0 && board.getWhitePawnValues() + board.getBlackPawnValues() == PieceValue.getValue(Piece.PAWN)) {
            if (board.getWhitePawnValues() == PieceValue.getValue(Piece.PAWN)) {
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

        if (board.getWhitePawnValues() + board.getBlackPawnValues() == 0 && board.getWhitePieceValues() < PieceValue.getValue(Piece.ROOK) && board.getBlackPieceValues() < PieceValue.getValue(Piece.ROOK))
            return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();

        if (eval > 0) {
            if (board.getWhitePawnValues() == 0 && (board.getWhitePieceValues() == PieceValue.getValue(Piece.KNIGHT) || board.getWhitePieceValues() == PieceValue.getValue(Piece.BISHOP)))
                return eval - (int) (board.getWhitePieceValues() * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.getWhitePawnValues() == 0 && board.getWhitePieceValues() - PieceValue.getValue(Piece.BISHOP) <= board.getBlackPieceValues())
                return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.getValue();
            else if (Long.bitCount(board.getAllPiecesBitboard()) > 3 && (board.getWhiteRookBitboard() | board.getWhiteKnightBitboard() | board.getWhiteQueenBitboard()) == 0) {
                // If this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw.
                // Do the same for H pawns

                if (((board.getWhitePawnBitboard() & ~Bitboards.FILE_A) == 0) &&
                        ((board.getWhiteBishopBitboard() & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.getBlackKingBitboard() & Bitboards.A8A7B8B7) != 0) || ((board.getWhitePawnBitboard() & ~Bitboards.FILE_H) == 0) &&
                                ((board.getWhiteBishopBitboard() & Bitboards.DARK_SQUARES) == 0) &&
                                ((board.getBlackKingBitboard() & Bitboards.H8H7G8G7) != 0)) {
                    return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();
                }

            }
            if (board.getBlackPawnValues() == 0) {
                if (board.getWhitePieceValues() - board.getBlackPieceValues() > PieceValue.getValue(Piece.BISHOP)) {
                    int whiteKnightCount = Long.bitCount(board.getWhiteKnightBitboard());
                    int whiteBishopCount = Long.bitCount(board.getWhiteBishopBitboard());
                    if ((whiteKnightCount == 2) && (board.getWhitePieceValues() == 2 * PieceValue.getValue(Piece.KNIGHT)) && (board.getBlackPieceValues() == 0))
                        return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();
                    else if ((whiteKnightCount == 1) && (whiteBishopCount == 1) && (board.getWhitePieceValues() == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP)) && board.getBlackPieceValues() == 0) {
                        eval = PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.getValue() + (eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.getValue());
                        final int kingSquare = board.getBlackKingSquare();

                        if ((board.getWhiteBishopBitboard() & Bitboards.DARK_SQUARES) != 0)
                            eval += (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.getValue();
                        else
                            eval += (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.getValue();

                        return eval;
                    } else
                        return eval + Evaluation.VALUE_SHOULD_WIN.getValue();
                }
            }
        }
        if (eval < 0) {
            if (board.getBlackPawnValues() == 0 && (board.getBlackPieceValues() == PieceValue.getValue(Piece.KNIGHT) || board.getBlackPieceValues() == PieceValue.getValue(Piece.BISHOP)))
                return eval + (int) (board.getBlackPieceValues() * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.getBlackPawnValues() == 0 && board.getBlackPieceValues() - PieceValue.getValue(Piece.BISHOP) <= board.getWhitePieceValues())
                return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.getValue();
            else if (Long.bitCount(board.getAllPiecesBitboard()) > 3 && (board.getBlackRookBitboard() | board.getBlackKnightBitboard() | board.getBlackQueenBitboard()) == 0) {
                if (((board.getBlackPawnBitboard() & ~Bitboards.FILE_A) == 0) &&
                        ((board.getBlackBishopBitboard() & Bitboards.DARK_SQUARES) == 0) &&
                        ((board.getWhiteKingBitboard() & Bitboards.A1A2B1B2) != 0))
                    return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();
                else if (((board.getBlackPawnBitboard() & ~Bitboards.FILE_H) == 0) &&
                        ((board.getBlackBishopBitboard() & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.getWhiteKingBitboard() & Bitboards.H1H2G1G2) != 0))
                    return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();
            }
            if (board.getWhitePawnValues() == 0) {
                if (board.getBlackPieceValues() - board.getWhitePieceValues() > PieceValue.getValue(Piece.BISHOP)) {
                    int blackKnightCount = Long.bitCount(board.getBlackKnightBitboard());
                    int blackBishopCount = Long.bitCount(board.getBlackBishopBitboard());
                    if ((blackKnightCount == 2) && (board.getBlackPieceValues() == 2 * PieceValue.getValue(Piece.KNIGHT)) && (board.getWhitePieceValues() == 0))
                        return eval / Evaluation.ENDGAME_DRAW_DIVISOR.getValue();
                    else if ((blackKnightCount == 1) && (blackBishopCount == 1) && (board.getBlackPieceValues() == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP)) && board.getWhitePieceValues() == 0) {
                        eval = -(PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.getValue()) + (eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.getValue());
                        final int kingSquare = board.getWhiteKingSquare();
                        if ((board.getBlackBishopBitboard() & Bitboards.DARK_SQUARES) != 0) {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.getValue();
                        } else {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.getValue();
                        }
                        return eval;
                    } else
                        return eval - Evaluation.VALUE_SHOULD_WIN.getValue();
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

        return PieceValue.getValue(Piece.QUEEN) - Evaluation.ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE.getValue() * pawnDistanceFromPromotion;
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

        final int promotionMask = (move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue());
        if (isCapture) {
            final int see = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
            if (see > 0) {
                score = 100 + (int) (((double) see / PieceValue.getValue(Piece.QUEEN)) * 10);
            }
            if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue()) {
                score += 9;
            }
        } else if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue()) {
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
            if (theseMoves[c] != -1 && theseMoves[c] < bestScore && (theseMoves[c] >> 24) != 127) {
                // update best move found so far, but don't consider moves with no score
                bestScore = theseMoves[c];
                bestMove = theseMoves[c];
                bestIndex = c;
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
        bestPath.score = isCheck ? -Evaluation.VALUE_MATE.getValue() : evalScore;

        if (depth == 0 || bestPath.score >= high) {
            return bestPath;
        }

        low = Math.max(bestPath.score, low);

        int[] theseMoves = orderedMoves[ply];

        if (isCheck) {
            board.setLegalMoves(theseMoves);
            scoreFullWidthMoves(board, ply);
        } else {
            board.setLegalQuiesceMoves(theseMoves, quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.getValue());
            scoreQuiesceMoves(board, ply, quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.getValue());
        }

        int move = getHighScoreMove(theseMoves);

        int legalMoveCount = 0;

        while (move != 0) {

            if (!shouldDeltaPrune(board, low, evalScore, move, isCheck)) {
                if (board.makeMove(new EngineMove(move))) {
                    legalMoveCount++;

                    newPath = quiesce(board, depth - 1, ply + 1, quiescePly + 1, -high, -low, (quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.getValue() && board.isCheck()));
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
            bestPath.score = -Evaluation.VALUE_MATE.getValue();
        }

        return bestPath;
    }

    private boolean shouldDeltaPrune(EngineChessBoard board, int low, int evalScore, int move, boolean isCheck) {
        if (FeatureFlag.USE_DELTA_PRUNING.isActive() && !isCheck) {
            final int materialIncrease = (board.lastCapturePiece() != SquareOccupant.NONE
                    ? PieceValue.getValue(board.lastCapturePiece().getPiece())
                    : 0) + getMaterialIncreaseForPromotion(move);

            return materialIncrease + evalScore + SearchConfig.DELTA_PRUNING_MARGIN.getValue() < low;
        }

        return false;
    }

    private int getMaterialIncreaseForPromotion(final int move) {
        final int promotionMaskValue = move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue();
        if (promotionMaskValue == 0) {
            return 0;
        }
        switch (PromotionPieceMask.fromValue(promotionMaskValue)) {
            case PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                return PieceValue.getValue(Piece.QUEEN) - PieceValue.getValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                return PieceValue.getValue(Piece.BISHOP) - PieceValue.getValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                return PieceValue.getValue(Piece.KNIGHT) - PieceValue.getValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                return PieceValue.getValue(Piece.ROOK) - PieceValue.getValue(Piece.PAWN);
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
                        ((1L << toSquare) & board.getBitboardByIndex(BitboardType.ENPASSANTSQUARE.getIndex())) != 0 &&
                        board.getSquareOccupant((movesForSorting[i] >>> 16) & 63).getPiece() == Piece.PAWN) {
                    capturePiece = Piece.PAWN;
                }

                movesForSorting[i] &= 0x00FFFFFF;

                if (movesForSorting[i] == mateKiller.get(ply)) {
                    score = 126;
                } else if (capturePiece != Piece.NONE) {
                    int see = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(movesForSorting[i]));
                    if (see > -Integer.MAX_VALUE) {
                        see = (int) (((double) see / PieceValue.getValue(Piece.QUEEN)) * 10);
                    }

                    if (see > 0) {
                        score = 110 + see;
                    }
                    else if ((movesForSorting[i] & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue())
                            == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue()) {
                        score = 109;
                    }
                    else if (see == 0) {
                        score = 107;
                    }
                    else {
                        score = scoreLosingCapturesWithWinningHistory(board, ply, i, score, movesForSorting, toSquare);
                    }

                } else if ((movesForSorting[i] & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue())
                        == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.getValue()) {
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
            for (int j = 0; j < SearchConfig.NUM_KILLER_MOVES.getValue(); j++) {
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

                if (score == 0 && FeatureFlag.USE_PIECE_SQUARES_IN_MOVE_ORDERING.isActive()) {
                    score = 50 + (scorePieceSquareValues(board, fromSquare, toSquare) / 2);
                }
                movesForSorting[i] |= ((127 - score) << 24);
            }
        }
    }

    private int scorePieceSquareValues(EngineChessBoard board, int fromSquare, int toSquare) {
        if (board.getMover() == Colour.BLACK) {
            // piece square tables are set up from white's PoV
            fromSquare = Bitboards.bitFlippedHorizontalAxis.get(fromSquare);
            toSquare = Bitboards.bitFlippedHorizontalAxis.get(toSquare);
        }
        switch (board.getSquareOccupant(fromSquare).getPiece()) {
            case PAWN:
                return
                        linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() : board.getWhitePieceValues()),
                                Evaluation.PAWN_STAGE_MATERIAL_LOW.getValue(),
                                Evaluation.PAWN_STAGE_MATERIAL_HIGH.getValue(),
                                PieceSquareTables.pawnEndGame.get(toSquare) - PieceSquareTables.pawnEndGame.get(fromSquare),
                                PieceSquareTables.pawn.get(toSquare) - PieceSquareTables.pawn.get(fromSquare));
            case KNIGHT:
                return
                        linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() + board.getBlackPawnValues() : board.getWhitePieceValues() + board.getWhitePawnValues()),
                                Evaluation.KNIGHT_STAGE_MATERIAL_LOW.getValue(),
                                Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.getValue(),
                                PieceSquareTables.knightEndGame.get(toSquare) - PieceSquareTables.knightEndGame.get(fromSquare),
                                PieceSquareTables.knight.get(toSquare) - PieceSquareTables.knight.get(fromSquare));
            case BISHOP:
                return PieceSquareTables.bishop.get(toSquare) - PieceSquareTables.bishop.get(fromSquare);
            case ROOK:
                return PieceSquareTables.rook.get(toSquare) - PieceSquareTables.rook.get(fromSquare);
            case QUEEN:
                return PieceSquareTables.queen.get(toSquare) - PieceSquareTables.queen.get(fromSquare);
            case KING:
                return
                        linearScale(
                                (board.getMover() == Colour.WHITE ? board.getBlackPieceValues() : board.getWhitePieceValues()),
                                PieceValue.getValue(Piece.ROOK),
                                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                                PieceSquareTables.kingEndGame.get(toSquare) - PieceSquareTables.kingEndGame.get(fromSquare),
                                PieceSquareTables.king.get(toSquare) - PieceSquareTables.king.get(fromSquare));
            default:
                return 0;
        }
    }

    private int scoreHistoryHeuristic(EngineChessBoard board, int score, int fromSquare, int toSquare) {
        if (score == 0 && FeatureFlag.USE_HISTORY_HEURISTIC.isActive() && historyMovesSuccess[board.getMover() == Colour.WHITE ? 0 : 1][fromSquare][toSquare] > 0) {
            score = 90 + historyScore(board.getMover() == Colour.WHITE, fromSquare, toSquare);
        }
        return score;
    }

    private int scoreKillerMoves(int ply, int i, int[] movesForSorting) {
        for (int j = 0; j < SearchConfig.NUM_KILLER_MOVES.getValue(); j++) {
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
            bestPath.score = Evaluation.DRAW_CONTEMPT.getValue();
            return bestPath;
        }

        if (board.getWhitePieceValues() + board.getBlackPieceValues() + board.getWhitePawnValues() + board.getBlackPawnValues() == 0) {
            bestPath.score = 0;
            return bestPath;
        }

        final int depthRemaining = depth + (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue());

        int flag = HashValueType.UPPERBOUND.getIndex();

        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);
        int hashMove = 0;

        if (FeatureFlag.USE_HASH_TABLES.isActive()) {
            if (FeatureFlag.USE_HEIGHT_REPLACE_HASH.isActive() && isHeightHashTableEntryValid(depthRemaining, board)) {
                boardHash.setHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_VERSION.getIndex(), boardHash.getHashTableVersion());
                hashMove = boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_MOVE.getIndex());

                if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.LOWERBOUND.getIndex()) {
                    if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()) > low) {
                        low = boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                    }
                } else if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.UPPERBOUND.getIndex()) {
                    if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()) < high) {
                        high = boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                    }
                }

                if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.EXACTSCORE.getIndex() || low >= high) {
                    bestPath.score = boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                    bestPath.setPath(hashMove);
                    return bestPath;
                }
            }

            if (FeatureFlag.USE_ALWAYS_REPLACE_HASH.isActive() && hashMove == 0 && isAlwaysReplaceHashTableEntryValid(depthRemaining, board)) {

                hashMove = boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_MOVE.getIndex());
                if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.LOWERBOUND.getIndex()) {
                    if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()) > low)
                        low = boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                } else if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.UPPERBOUND.getIndex()) {
                    if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()) < high)
                        high = boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                }

                if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) == HashValueType.EXACTSCORE.getIndex() || low >= high) {
                    bestPath.score = boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_SCORE.getIndex());
                    bestPath.setPath(hashMove);
                    return bestPath;
                }
            }
        }

        int checkExtend = 0;
        if ((extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue()) < Limit.MAX_EXTENSION_DEPTH.getValue()) {
            if (Extensions.FRACTIONAL_EXTENSION_CHECK.getValue() > 0 && isCheck) {
                checkExtend = 1;
            }
        }

        if (depthRemaining <= 0) {
            bestPath = quiesce(board, Limit.MAX_QUIESCE_DEPTH.getValue() - 1, ply, 0, low, high, isCheck);
            if (bestPath.score < low) flag = HashValueType.UPPERBOUND.getIndex();
            else if (bestPath.score > high) flag = HashValueType.LOWERBOUND.getIndex();
            else
                flag = HashValueType.EXACTSCORE.getIndex();

            boardHash.storeHashMove(0, board, bestPath.score, (byte)flag, 0);
            return bestPath;
        }

        if (FeatureFlag.USE_INTERNAL_ITERATIVE_DEEPENING.isActive()
                && depthRemaining >= IterativeDeepening.IID_MIN_DEPTH.getValue()
                && hashMove == 0 && board.isNotOnNullMove()) {
            boolean doIt = true;

            if (doIt) {
                if (depth - IterativeDeepening.IID_REDUCE_DEPTH.getValue() > 0) {
                    newPath = search(board, (byte) (depth - IterativeDeepening.IID_REDUCE_DEPTH.getValue()), ply, low, high, extensions, recaptureSquare, isCheck);
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

        int nullMoveReduceDepth = (depthRemaining > SearchConfig.NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE.getValue())
                ? SearchConfig.NULLMOVE_REDUCE_DEPTH.getValue() + 1
                : SearchConfig.NULLMOVE_REDUCE_DEPTH.getValue();
        if (FeatureFlag.USE_NULL_MOVE_PRUNING.isActive() && !isCheck && board.isNotOnNullMove() && depthRemaining > 1) {
            if ((board.getMover() == Colour.WHITE
                    ? board.getWhitePieceValues()
                    : board.getBlackPieceValues()) >= SearchConfig.NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES.getValue() &&
                    (board.getMover() == Colour.WHITE ? board.getWhitePawnValues() : board.getBlackPawnValues()) > 0) {
                board.makeNullMove();
                newPath = search(board, (byte) (depth - nullMoveReduceDepth - 1), ply + 1, -high, -low, extensions, -1, false);
                if (newPath != null) if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                if (!this.m_abortingSearch) {
                    if (-Objects.requireNonNull(newPath).score >= high) {
                        bestPath.score = -newPath.score;
                        board.unMakeNullMove();
                        return bestPath;
                    } else if (
                            Extensions.FRACTIONAL_EXTENSION_THREAT.getValue() > 0 &&
                                    -newPath.score < -Evaluation.MATE_SCORE_START.getValue() &&
                                    (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue()) < Limit.MAX_EXTENSION_DEPTH.getValue()) {
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
            if (FeatureFlag.USE_FUTILITY_PRUNING.isActive() && depthRemaining < 4 && !wasCheckBeforeMove && threatExtend == 0 && Math.abs(low) < Evaluation.MATE_SCORE_START.getValue() && Math.abs(high) < Evaluation.MATE_SCORE_START.getValue()) {
                futilityPruningEvaluation = evaluate(board);
                futilityScore = futilityPruningEvaluation + SearchConfig.getFutilityMargin(depthRemaining - 1);
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

                int currentSEEValue = -Integer.MAX_VALUE;
                if (Extensions.FRACTIONAL_EXTENSION_RECAPTURE.getValue() > 0 && (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue())
                        < Limit.MAX_EXTENSION_DEPTH.getValue()) {
                    recaptureExtend = 0;

                    if (targetPiece != -1 && Evaluation.getPieceValues().get(movePiece).equals(Evaluation.getPieceValues().get(targetPiece))) {
                        currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
                        if (Math.abs(currentSEEValue) <= Extensions.RECAPTURE_EXTENSION_MARGIN.getValue())
                            newRecaptureSquare = (move & 63);
                    }

                    if ((move & 63) == recaptureSquare) {
                        if (currentSEEValue == -Integer.MAX_VALUE)
                            currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
                        if (Math.abs(currentSEEValue) > Evaluation.getPieceValue(board.getSquareOccupant(recaptureSquare))
                                - Extensions.RECAPTURE_EXTENSION_MARGIN.getValue()) {
                            recaptureExtend = 1;
                        }
                    }
                }

                if (board.makeMove(new EngineMove(move))) {
                    legalMoveCount++;

                    isCheck = board.isCheck();

                    if (FeatureFlag.USE_FUTILITY_PRUNING.isActive() && canFutilityPrune && !isCheck && board.wasCapture() && !board.wasPawnPush()) {
                        newPath = searchPath[ply + 1];
                        newPath.reset();
                        newPath.score = -futilityScore; // newPath.score gets reversed later
                    } else {
                        if ((extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue())
                                < Limit.MAX_EXTENSION_DEPTH.getValue()) {
                            pawnExtend = 0;
                            if (Extensions.FRACTIONAL_EXTENSION_PAWN.getValue() > 0) {
                                if (board.wasPawnPush()) {
                                    pawnExtend = 1;
                                }
                            }
                        }

                        int partOfTree = ply / this.iterativeDeepeningCurrentDepth;
                        int maxNewExtensionsInThisPart =
                                Extensions.getMaxNewExtensionsTreePart().get(
                                        Math.min(partOfTree, Extensions.LAST_EXTENSION_LAYER.getValue()));

                        newExtensions =
                                extensions +
                                        Math.min(
                                                (checkExtend * Extensions.FRACTIONAL_EXTENSION_CHECK.getValue()) +
                                                        (threatExtend * Extensions.FRACTIONAL_EXTENSION_THREAT.getValue()) +
                                                        (recaptureExtend * Extensions.FRACTIONAL_EXTENSION_RECAPTURE.getValue()) +
                                                        (pawnExtend * Extensions.FRACTIONAL_EXTENSION_PAWN.getValue()),
                                                maxNewExtensionsInThisPart);

                        int lateMoveReduction = 0;
                        if (
                                FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive() &&
                                        newExtensions == 0 &&
                                        legalMoveCount > LateMoveReductions.LMR_LEGALMOVES_BEFORE_ATTEMPT.getValue() &&
                                        depthRemaining - verifyingNullMoveDepthReduction > 1 &&
                                        move != hashMove &&
                                        !wasCheckBeforeMove &&
                                        historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                                <= LateMoveReductions.LMR_THRESHOLD.getValue() &&
                                        board.wasCapture() &&
                                        (Extensions.FRACTIONAL_EXTENSION_PAWN.getValue() > 0 || !board.wasPawnPush())) {
                            if (-evaluate(board) <= low + LateMoveReductions.LMR_CUT_MARGIN.getValue()) {
                                lateMoveReduction = 1;
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                        = LateMoveReductions.LMR_REPLACE_VALUE_AFTER_CUT.getValue();
                            }
                        }

                        //noinspection ConstantConditions
                        if (LateMoveReductions.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION.getValue() > -1) {
                            lateMoveReductionsMade += lateMoveReduction;
                            if (lateMoveReductionsMade > LateMoveReductions.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION.getValue() && depthRemaining > 3) {
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
                                    if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                                    else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                                if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                                    // research with normal window
                                    newPath = search(engineChessBoard, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck);
                                    if (newPath != null)
                                        if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                                        else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                                }
                            } else {
                                newPath = search(board, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                                    else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                            }
                            if (!this.m_abortingSearch && lateMoveReduction > 0 && -Objects.requireNonNull(newPath).score >= low) {
                                lmrResearch = FeatureFlag.LMR_RESEARCH_ON_FAIL_HIGH.isActive();
                                lateMoveReduction = 0;
                            }
                        }
                        // this move lead to a beta cut-off so research without the reduction
                        while (lmrResearch);
                    }

                    if (!this.m_abortingSearch) {
                        Objects.requireNonNull(newPath).score = -newPath.score;

                        if (newPath.score >= high) {
                            if (FeatureFlag.USE_HISTORY_HEURISTIC.isActive()) {
                                historyMovesSuccess[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                                if (historyMovesSuccess[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                        > SearchConfig.HISTORY_MAX_VALUE.getValue()) {
                                    for (int i = 0; i < 2; i++)
                                        for (int j = 0; j < 64; j++)
                                            for (int k = 0; k < 64; k++) {
                                                if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2;
                                                if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2;
                                            }
                                }
                            }

                            if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive())
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                        += LateMoveReductions.LMR_ABOVE_ALPHA_ADDITION.getValue();

                            board.unMakeMove();
                            bestPath.setPath(move, newPath);
                            boardHash.storeHashMove(move, board, newPath.score, (byte)HashValueType.LOWERBOUND.getIndex(), depthRemaining);

                            if (SearchConfig.NUM_KILLER_MOVES.getValue() > 0) {
                                if ((board.getBitboard(BitboardType.ENEMY) & (move & 63)) == 0
                                        || (move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue()) == 0) {
                                    // if this move is in second place, or if it's not in the table at all,
                                    // then move first to second, and replace first with this move
                                    if (killerMoves[ply][0] != move) {
                                        System.arraycopy(killerMoves[ply], 0, killerMoves[ply], 1, SearchConfig.NUM_KILLER_MOVES.getValue() - 1);
                                        killerMoves[ply][0] = move;
                                    }
                                    if (FeatureFlag.USE_MATE_HISTORY_KILLERS.isActive() && newPath.score > Evaluation.MATE_SCORE_START.getValue()) {
                                        mateKiller.set(ply, move);
                                    }
                                }
                            }
                            return bestPath;
                        }

                        if (FeatureFlag.USE_HISTORY_HEURISTIC.isActive()) {
                            historyMovesFail[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                            if (historyMovesFail[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                    > SearchConfig.HISTORY_MAX_VALUE.getValue()) {
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
                            flag = HashValueType.EXACTSCORE.getIndex();
                            bestMoveForHash = move;
                            low = newPath.score;
                            scoutSearch = FeatureFlag.USE_PV_SEARCH.isActive() && depth - reductions
                                    + (newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue())
                                    >= SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.getValue();

                            if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive())
                                historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                        += LateMoveReductions.LMR_ABOVE_ALPHA_ADDITION.getValue();
                        } else if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive()) {
                            historyPruneMoves[board.getMover() == Colour.WHITE ? 1 : 0][(move >>> 16) & 63][(move & 63)]
                                    -= LateMoveReductions.LMR_NOT_ABOVE_ALPHA_REDUCTION.getValue();
                        }
                    }
                    board.unMakeMove();
                }
            }

            if (!this.m_abortingSearch) {
                if (legalMoveCount == 0) {
                    bestPath.score = board.isCheck() ? -Evaluation.VALUE_MATE.getValue() : 0;
                    boardHash.storeHashMove(0, board, bestPath.score, (byte)HashValueType.EXACTSCORE.getIndex(), Limit.MAX_SEARCH_DEPTH.getValue());
                    return bestPath;
                }

                if (!research) {
                    boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, (byte)flag, depthRemaining);
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
        int flag = HashValueType.UPPERBOUND.getIndex();
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
                if (Extensions.FRACTIONAL_EXTENSION_CHECK.getValue() > 0 && isCheck) {
                    checkExtend = 1;
                } else if (Extensions.FRACTIONAL_EXTENSION_PAWN.getValue() > 0) {
                    if (board.wasPawnPush()) {
                        pawnExtend = 1;
                    }
                }

                int newExtensions =
                        Math.min(
                                (checkExtend * Extensions.FRACTIONAL_EXTENSION_CHECK.getValue()) +
                                        (pawnExtend * Extensions.FRACTIONAL_EXTENSION_PAWN.getValue()),
                                Extensions.FRACTIONAL_EXTENSION_FULL.getValue());

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
                            if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) {
                                newPath.score--;
                            }
                            else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) {
                                newPath.score++;
                            }
                        }
                        if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                            newPath = search(engineChessBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
                            if (newPath != null) {
                                if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) {
                                    newPath.score--;
                                } else {
                                    if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) {
                                        newPath.score++;
                                    }
                                }
                            }
                        }
                    } else {
                        newPath = search(engineChessBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
                        if (newPath != null) {
                            if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                            else {
                                if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                            }
                        }
                    }
                }
                if (!this.m_abortingSearch) {
                    Objects.requireNonNull(newPath).score = -Objects.requireNonNull(newPath).score;

                    if (newPath.score >= high) {
                        board.unMakeMove();
                        bestPath.setPath(move, newPath);
                        boardHash.storeHashMove(move, board, newPath.score, (byte)HashValueType.LOWERBOUND.getIndex(), depth);
                        depthZeroMoveScores[numMoves] = newPath.score;
                        return bestPath;
                    }

                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath);
                    }

                    if (newPath.score > low) {
                        flag = HashValueType.EXACTSCORE.getIndex();
                        bestMoveForHash = move;
                        low = newPath.score;

                        scoutSearch = FeatureFlag.USE_PV_SEARCH.isActive() && depth + (newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue())
                                >= SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.getValue();
                        m_currentPath.setPath(bestPath);
                        currentPathString = "" + m_currentPath;
                    }

                    depthZeroMoveScores[numMoves] = newPath.score;
                }
                engineChessBoard.unMakeMove();
            } else {
                depthZeroMoveScores[numMoves] = -Integer.MAX_VALUE;
            }
            numMoves++;
            move = orderedMoves[0][numMoves] & 0x00FFFFFF;
        }

        if (!this.m_abortingSearch) {
            if (numLegalMovesAtDepthZero == 1 && this.millisToThink < Limit.MAX_SEARCH_MILLIS.getValue()) {
                this.m_abortingSearch = true;
                m_currentPath.setPath(bestPath); // otherwise we will crash!
                currentPathString = "" + m_currentPath;
            } else {
                final BoardHash boardHash = engineChessBoard.getBoardHashObject();
                boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, (byte)flag, depth);
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
            int[] depth1MovesTemp = new int[Limit.MAX_LEGAL_MOVES.getValue()];
            int move = depthZeroLegalMoves[c] & 0x00FFFFFF;
            drawnPositionsAtRootCount.add(0);
            drawnPositionsAtRootCount.add(0);
            int legal = 0;
            int bestNewbieScore = -Integer.MAX_VALUE;

            while (move != 0) {
                if (engineChessBoard.makeMove(new EngineMove(move))) {

                    List<Boolean> plyDraw = new ArrayList<>();
                    plyDraw.add(false);
                    plyDraw.add(false);

                    legal++;

                    if (this.iterativeDeepeningCurrentDepth < 1) // super beginner mode
                    {
                        SearchPath sp = quiesce(engineChessBoard, 40, 1, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, engineChessBoard.isCheck());
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
                Move libraryMove = OpeningLibrary.getMove(getFen());
                // Todo - check for legality
                if (libraryMove != null) {
                    path = new SearchPath();
                    path.setPath(new EngineMove(libraryMove).compact);
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

                if (FeatureFlag.USE_ASPIRATION_WINDOW.isActive()) {
                    path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);

                    if (!this.m_abortingSearch && Objects.requireNonNull(path).score <= aspirationLow) {
                        aspirationLow = -Integer.MAX_VALUE;
                        path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);
                    } else if (!this.m_abortingSearch && path.score >= aspirationHigh) {
                        aspirationHigh = Integer.MAX_VALUE;
                        path = searchZero(engineChessBoard, depth, 0, aspirationLow, aspirationHigh);
                    }

                    if (!this.m_abortingSearch && (Objects.requireNonNull(path).score <= aspirationLow || path.score >= aspirationHigh)) {
                        path = searchZero(engineChessBoard, depth, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }

                    if (!this.m_abortingSearch) {
                        m_currentPath.setPath(Objects.requireNonNull(path));
                        currentPathString = "" + m_currentPath;
                        aspirationLow = path.score - SearchConfig.ASPIRATION_RADIUS.getValue();
                        aspirationHigh = path.score + SearchConfig.ASPIRATION_RADIUS.getValue();
                    }
                } else {
                    path = searchZero(engineChessBoard, depth, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                }

                if (!this.m_abortingSearch) {
                    m_currentPath.setPath(Objects.requireNonNull(path));
                    currentPathString = "" + m_currentPath;
                    if (path.score > Evaluation.MATE_SCORE_START.getValue()) {
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
        searchTargetEndTime = this.searchStartTime + this.millisToThink - Uci.UCI_TIMER_INTERVAL_MILLIS.getValue();

        aspirationLow = -Integer.MAX_VALUE;
        aspirationHigh = Integer.MAX_VALUE;
        nodes = 0;
    }

    private void setupMateAndKillerMoveTables() {
        for (int i = 0; i < Limit.MAX_TREE_DEPTH.getValue(); i++) {
            this.mateKiller.add(-1);
            for (int j = 0; j < SearchConfig.NUM_KILLER_MOVES.getValue(); j++) {
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
                historyPruneMoves[0][i][j] = LateMoveReductions.LMR_INITIAL_VALUE.getValue();
                historyPruneMoves[1][i][j] = LateMoveReductions.LMR_INITIAL_VALUE.getValue();
            }
    }

    public synchronized SearchState getEngineState() {
        return searchState;
    }

    public void setMillisToThink(int millisToThink) {
        this.millisToThink = millisToThink;

        if (this.millisToThink < Limit.MIN_SEARCH_MILLIS.getValue()) {
            this.millisToThink = Limit.MIN_SEARCH_MILLIS.getValue();
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
        if (abs > Evaluation.MATE_SCORE_START.getValue()) {
            int mateIn = ((Evaluation.VALUE_MATE.getValue() - abs) + 1) / 2;
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
        this.useOpeningBook = FeatureFlag.USE_INTERNAL_OPENING_BOOK.isActive() && useBook;
        this.m_inBook = this.useOpeningBook;
    }

    public boolean isAbortingSearch() {
        return this.m_abortingSearch;
    }

    public void quit() {
        quit = true;
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
