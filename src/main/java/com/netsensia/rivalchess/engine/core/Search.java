package com.netsensia.rivalchess.engine.core;

import com.ea.async.Async;
import com.netsensia.rivalchess.bitboards.BitboardType;
import com.netsensia.rivalchess.config.Evaluation;
import com.netsensia.rivalchess.config.Extensions;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.config.IterativeDeepening;
import com.netsensia.rivalchess.config.LateMoveReductions;
import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.config.SearchConfig;
import com.netsensia.rivalchess.config.Uci;
import com.netsensia.rivalchess.engine.core.board.BoardExtensionsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.eval.PieceSquareTables;
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
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.openings.OpeningLibrary;
import com.netsensia.rivalchess.uci.EngineMonitor;
import com.netsensia.rivalchess.util.ChessBoardConversion;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;

import static com.netsensia.rivalchess.bitboards.BitboardConstantsKt.*;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.evaluate;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.linearScale;
import static com.netsensia.rivalchess.engine.core.eval.PieceValueKt.pieceValue;
import static com.netsensia.rivalchess.engine.core.hash.SearchHashKt.isAlwaysReplaceHashTableEntryValid;
import static com.netsensia.rivalchess.engine.core.hash.SearchHashKt.isHeightHashTableEntryValid;
import static com.netsensia.rivalchess.enums.HashIndex.FLAG;
import static com.netsensia.rivalchess.enums.HashIndex.MOVE;
import static com.netsensia.rivalchess.enums.HashIndex.SCORE;
import static com.netsensia.rivalchess.enums.HashIndex.VERSION;

public final class Search implements Runnable {

    private final PrintStream printStream;

    private final StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluatorPremium();

    private final MoveOrder[] moveOrderStatus = new MoveOrder[Limit.MAX_TREE_DEPTH.getValue()];

    private final List<List<Long>> drawnPositionsAtRoot;
    private final List<Integer> drawnPositionsAtRootCount = new ArrayList<>();
    private EngineBoard engineBoard
            = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
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

        this.engineBoard.setBoard(board);

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
        engineBoard.getBoardHashObject().setHashSizeMB(hashSizeMB);
    }

    public synchronized void setBoard(Board board) {
        engineBoard = new EngineBoard();
        engineBoard.setBoard(board);
        setBoard(engineBoard);
    }

    public synchronized void setBoard(EngineBoard engineBoard) {
        this.setEngineBoard(engineBoard);
        this.engineBoard.getBoardHashObject().incVersion();
        this.engineBoard.getBoardHashObject().setHashTable();
    }

    public synchronized void clearHash() {
        engineBoard.getBoardHashObject().clearHash();
    }

    public synchronized void newGame() {
        m_inBook = this.useOpeningBook;
        engineBoard.getBoardHashObject().clearHash();
    }

    static {
        Async.init();
    }

    private int[] scoreQuiesceMoves(EngineBoard board, int ply, boolean includeChecks) throws InvalidMoveException {

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

    private int getScore(EngineBoard board, int move, boolean includeChecks, boolean isCapture) throws InvalidMoveException {
        int score = 0;

        final int promotionMask = (move & PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.getValue());
        if (isCapture) {
            final int see = staticExchangeEvaluator.staticExchangeEvaluation(board, new EngineMove(move));
            if (see > 0) {
                score = 100 + (int) (((double) see / pieceValue(Piece.QUEEN)) * 10);
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

    private int getHighScoreMove(EngineBoard board, int ply, int hashMove) throws InvalidMoveException {
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

    public SearchPath quiesce(EngineBoard board, final int depth, int ply, int quiescePly, int low, int high, boolean isCheck) throws InvalidMoveException {

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

    private boolean shouldDeltaPrune(EngineBoard board, int low, int evalScore, int move, boolean isCheck) {
        if (FeatureFlag.USE_DELTA_PRUNING.isActive() && !isCheck) {
            final int materialIncrease = (board.lastCapturePiece() != SquareOccupant.NONE
                    ? pieceValue(board.lastCapturePiece().getPiece())
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
                return pieceValue(Piece.QUEEN) - pieceValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                return pieceValue(Piece.BISHOP) - pieceValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                return pieceValue(Piece.KNIGHT) - pieceValue(Piece.PAWN);
            case PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                return pieceValue(Piece.ROOK) - pieceValue(Piece.PAWN);
            default:
                return 0;
        }
    }

    private int scoreFullWidthCaptures(EngineBoard board, int ply) throws InvalidMoveException {
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
                        see = (int) (((double) see / pieceValue(Piece.QUEEN)) * 10);
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

    private int scoreLosingCapturesWithWinningHistory(EngineBoard board, int ply, int i, int score, int[] movesForSorting, int toSquare) {
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

    private void scoreFullWidthMoves(EngineBoard board, int ply) {

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

    private int scorePieceSquareValues(EngineBoard board, int fromSquare, int toSquare) {
        if (board.getMover() == Colour.BLACK) {
            // piece square tables are set up from white's PoV
            fromSquare = getBitFlippedHorizontalAxis().get(fromSquare);
            toSquare = getBitFlippedHorizontalAxis().get(toSquare);
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
                                pieceValue(Piece.ROOK),
                                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                                PieceSquareTables.kingEndGame.get(toSquare) - PieceSquareTables.kingEndGame.get(fromSquare),
                                PieceSquareTables.king.get(toSquare) - PieceSquareTables.king.get(fromSquare));
            default:
                return 0;
        }
    }

    private int scoreHistoryHeuristic(EngineBoard board, int score, int fromSquare, int toSquare) {
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

    public SearchPath search(
            final EngineBoard board,
            final int depth,
            final int ply,
            int low,
            int high,
            final int extensions,
            final int recaptureSquare,
            boolean isCheck) throws InvalidMoveException {

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

        if (BoardExtensionsKt.onlyKingsRemain(board)) {
            bestPath.score = 0;
            return bestPath;
        }

        final int depthRemaining = depth + (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue());

        int flag = HashValueType.UPPER.getIndex();

        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);
        int hashMove = 0;

        if (FeatureFlag.USE_HASH_TABLES.isActive()) {
            if (FeatureFlag.USE_HEIGHT_REPLACE_HASH.isActive() && isHeightHashTableEntryValid(depthRemaining, board)) {
                boardHash.setHashTableUseHeight(hashIndex + VERSION.getIndex(), boardHash.getHashTableVersion());
                hashMove = boardHash.useHeight(hashIndex + MOVE.getIndex());

                if (boardHash.useHeight(hashIndex + FLAG.getIndex()) == HashValueType.LOWER.getIndex()) {
                    if (boardHash.useHeight(hashIndex + SCORE.getIndex()) > low)
                        low = boardHash.useHeight(hashIndex + SCORE.getIndex());
                } else if (boardHash.useHeight(hashIndex + FLAG.getIndex()) == HashValueType.UPPER.getIndex() &&
                      (boardHash.useHeight(hashIndex + SCORE.getIndex()) < high))
                    high = boardHash.useHeight(hashIndex + SCORE.getIndex());

                if (boardHash.useHeight(hashIndex + FLAG.getIndex()) == HashValueType.EXACT.getIndex() || low >= high) {
                    bestPath.score = boardHash.useHeight(hashIndex + SCORE.getIndex());
                    bestPath.setPath(hashMove);
                    return bestPath;
                }
            }

            if (FeatureFlag.USE_ALWAYS_REPLACE_HASH.isActive() && hashMove == 0 && isAlwaysReplaceHashTableEntryValid(depthRemaining, board)) {

                hashMove = boardHash.ignoreHeight(hashIndex + MOVE.getIndex());
                if (boardHash.ignoreHeight(hashIndex + FLAG.getIndex()) == HashValueType.LOWER.getIndex()) {
                    if (boardHash.ignoreHeight(hashIndex + SCORE.getIndex()) > low)
                        low = boardHash.ignoreHeight(hashIndex + SCORE.getIndex());
                } else if (boardHash.ignoreHeight(hashIndex + FLAG.getIndex()) == HashValueType.UPPER.getIndex() &&
                       (boardHash.ignoreHeight(hashIndex + SCORE.getIndex()) < high))
                        high = boardHash.ignoreHeight(hashIndex + SCORE.getIndex());

                if (boardHash.ignoreHeight(hashIndex + FLAG.getIndex()) == HashValueType.EXACT.getIndex() || low >= high) {
                    bestPath.score = boardHash.ignoreHeight(hashIndex + SCORE.getIndex());
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
            if (bestPath.score < low) flag = HashValueType.UPPER.getIndex();
            else if (bestPath.score > high) flag = HashValueType.LOWER.getIndex();
            else
                flag = HashValueType.EXACT.getIndex();

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
                                newPath = search(engineBoard, (byte) (depth - 1) - reductions, ply + 1, -low - 1, -low, newExtensions, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) newPath.score--;
                                    else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) newPath.score++;
                                if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                                    // research with normal window
                                    newPath = search(engineBoard, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck);
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
                            boardHash.storeHashMove(move, board, newPath.score, (byte)HashValueType.LOWER.getIndex(), depthRemaining);

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
                            flag = HashValueType.EXACT.getIndex();
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
                    boardHash.storeHashMove(0, board, bestPath.score, (byte)HashValueType.EXACT.getIndex(), Limit.MAX_SEARCH_DEPTH.getValue());
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

    public SearchPath searchZero(EngineBoard board, byte depth, int ply, int low, int high) throws InvalidMoveException {

        setNodes(getNodes() + 1);

        int numMoves = 0;
        int flag = HashValueType.UPPER.getIndex();
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
            if (engineBoard.makeMove(new EngineMove(move))) {
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

                final BoardHash boardHash = engineBoard.getBoardHashObject();

                if (isDrawnAtRoot(engineBoard, 0)) {
                    newPath = new SearchPath();
                    newPath.score = 0;
                    newPath.setPath(move);
                } else {
                    if (scoutSearch) {
                        newPath = search(engineBoard, (byte) (depth - 1), ply + 1, -low - 1, -low, newExtensions, -1, isCheck);
                        if (newPath != null) {
                            if (newPath.score > Evaluation.MATE_SCORE_START.getValue()) {
                                newPath.score--;
                            }
                            else if (newPath.score < -Evaluation.MATE_SCORE_START.getValue()) {
                                newPath.score++;
                            }
                        }
                        if (!this.m_abortingSearch && -Objects.requireNonNull(newPath).score > low) {
                            newPath = search(engineBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
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
                        newPath = search(engineBoard, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck);
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
                        boardHash.storeHashMove(move, board, newPath.score, (byte)HashValueType.LOWER.getIndex(), depth);
                        depthZeroMoveScores[numMoves] = newPath.score;
                        return bestPath;
                    }

                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath);
                    }

                    if (newPath.score > low) {
                        flag = HashValueType.EXACT.getIndex();
                        bestMoveForHash = move;
                        low = newPath.score;

                        scoutSearch = FeatureFlag.USE_PV_SEARCH.isActive() && depth + (newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.getValue())
                                >= SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.getValue();
                        m_currentPath.setPath(bestPath);
                        currentPathString = "" + m_currentPath;
                    }

                    depthZeroMoveScores[numMoves] = newPath.score;
                }
                engineBoard.unMakeMove();
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
                final BoardHash boardHash = engineBoard.getBoardHashObject();
                boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, (byte)flag, depth);
            }

            return bestPath;
        } else {
            return null;
        }
    }

    public Colour getMover() {
        return engineBoard.getMover();
    }

    public void makeMove(EngineMove engineMove) throws InvalidMoveException {
        engineBoard.makeMove(engineMove);
    }

    public void go() {

        initSearchVariables();
        setupMateAndKillerMoveTables();

        setupHistoryMoveTable();

        SearchPath path;

        try {

            engineBoard.setLegalMoves(depthZeroLegalMoves);
            int depthZeroMoveCount = 0;

            int c = 0;
            int[] depth1MovesTemp = new int[Limit.MAX_LEGAL_MOVES.getValue()];
            int move = depthZeroLegalMoves[c] & 0x00FFFFFF;
            drawnPositionsAtRootCount.add(0);
            drawnPositionsAtRootCount.add(0);
            int legal = 0;
            int bestNewbieScore = -Integer.MAX_VALUE;

            while (move != 0) {
                if (engineBoard.makeMove(new EngineMove(move))) {

                    List<Boolean> plyDraw = new ArrayList<>();
                    plyDraw.add(false);
                    plyDraw.add(false);

                    legal++;

                    if (this.iterativeDeepeningCurrentDepth < 1) // super beginner mode
                    {
                        SearchPath sp = quiesce(engineBoard, 40, 1, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, engineBoard.isCheck());
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
                    if (engineBoard.previousOccurrencesOfThisPosition() == 2) {
                        plyDraw.set(0, true);
                    }

                    engineBoard.setLegalMoves(depth1MovesTemp);

                    int c1 = -1;

                    while ((depth1MovesTemp[++c1] & 0x00FFFFFF) != 0) {
                        if (engineBoard.makeMove(new EngineMove(depth1MovesTemp[c1] & 0x00FFFFFF))) {
                            if (engineBoard.previousOccurrencesOfThisPosition() == 2) {
                                plyDraw.set(1, true);
                            }
                            engineBoard.unMakeMove();
                        }
                    }

                    final BoardHash boardHash = engineBoard.getBoardHashObject();
                    for (int i=0; i<=1; i++) {
                        if (Boolean.TRUE.equals(plyDraw.get(i))) {
                            drawnPositionsAtRoot.get(i).add(boardHash.getTrackedHashValue());
                        }
                    }

                    engineBoard.unMakeMove();

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

            scoreFullWidthMoves(engineBoard, 0);

            for (byte depth = 1; depth <= this.m_finalDepthToSearch && !this.m_abortingSearch; depth++) {
                this.iterativeDeepeningCurrentDepth = depth;

                if (depth > 1) setOkToSendInfo(true);

                if (FeatureFlag.USE_ASPIRATION_WINDOW.isActive()) {
                    path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh);

                    if (!this.m_abortingSearch && Objects.requireNonNull(path).score <= aspirationLow) {
                        aspirationLow = -Integer.MAX_VALUE;
                        path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh);
                    } else if (!this.m_abortingSearch && path.score >= aspirationHigh) {
                        aspirationHigh = Integer.MAX_VALUE;
                        path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh);
                    }

                    if (!this.m_abortingSearch && (Objects.requireNonNull(path).score <= aspirationLow || path.score >= aspirationHigh)) {
                        path = searchZero(engineBoard, depth, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }

                    if (!this.m_abortingSearch) {
                        m_currentPath.setPath(Objects.requireNonNull(path));
                        currentPathString = "" + m_currentPath;
                        aspirationLow = path.score - SearchConfig.ASPIRATION_RADIUS.getValue();
                        aspirationHigh = path.score + SearchConfig.ASPIRATION_RADIUS.getValue();
                    }
                } else {
                    path = searchZero(engineBoard, depth, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
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

        final BoardHash boardHash = engineBoard.getBoardHashObject();
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
        int score = this.m_currentPath.score;
        int abs = Math.abs(score);
        if (abs > Evaluation.MATE_SCORE_START.getValue()) {
            int mateIn = ((Evaluation.VALUE_MATE.getValue() - abs) + 1) / 2;
            return "mate " + (score < 0 ? "-" : "") + mateIn;
        }
        return "cp " + score;
    }

    public int getCurrentScore() {
        return this.m_currentPath.score;
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

    private boolean isDrawnAtRoot(EngineBoard board, int ply) {
        int i;
        final BoardHash boardHash = engineBoard.getBoardHashObject();
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

    public void setEngineBoard(EngineBoard engineBoard) {
        this.engineBoard = engineBoard;
    }

    public String getFen() {
        return engineBoard.getFen();
    }

    public int getCurrentDepthZeroMoveNumber() {
        return currentDepthZeroMoveNumber;
    }

    public int getCurrentDepthZeroMove() {
        return currentDepthZeroMove;
    }

}
