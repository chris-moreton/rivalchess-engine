//package com.netsensia.rivalchess.engine.test;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import com.netsensia.rivalchess.engine.EngineStub;
//import com.netsensia.rivalchess.engine.core.Bitboards;
//import com.netsensia.rivalchess.engine.core.EngineChessBoard;
//import com.netsensia.rivalchess.engine.core.RivalConstants;
//import com.netsensia.rivalchess.engine.core.RivalSearch;
//import com.netsensia.rivalchess.engine.test.epd.EPDRunner;
//import com.netsensia.rivalchess.exception.EvaluationFlipException;
//import com.netsensia.rivalchess.exception.IllegalFenException;
//import com.netsensia.rivalchess.model.BoardModel;
//import com.netsensia.rivalchess.model.BoardRef;
//import com.netsensia.rivalchess.model.board.FenChess;
//import com.netsensia.rivalchess.uci.UCIController;
//import com.netsensia.rivalchess.util.ChessBoardConversion;
//
//public final class RivalTester {
//    private static boolean m_isDebug = false;
//
//    public static final int MODE_SUITE = 0;
//    public static final int MODE_SINGLE = 1;
//    public static final int MODE_EPD = 2;
//    public static final int MODE_PERFT = 3;
//    public static final int MODE_TESTHASH = 4;
//
//    private static int mode = MODE_SINGLE;
//
//    private static int SUITE_DEPTH_ADDITION = 1;
//
//    // Time:  40.14 Nodes:  40,287,926 NPS: 1,003,635
//    // Time:  16.06 Nodes:  13,651,026 NPS: 849,842
//
//    // MacPro: Time:  10.05 Nodes:  14,786,928 NPS: 1,470,604
//
//    private static int totalNodes = 0;
//    private static int totalTime = 0;
//    private static NumberFormat nf = NumberFormat.getInstance();
//
//    public static void main(String args[]) throws EvaluationFlipException {
//        if (mode == MODE_EPD)
//            new EPDRunner().go("/Users/Chris/git/chess/rival-chess-android-engine/test/epd/arasan18-bestmovesonly.epd", 3, 200000);
//        if (mode == MODE_TESTHASH) testHash();
//
//        EngineStub engineStub = new EngineStub();
//
//        if (m_isDebug) testHash();
//
//        int hashSize = 4;
//
//        if (mode == MODE_SINGLE) {
//            m_isDebug = true;
//            engineStub.setDebug(m_isDebug);
//
//            int searchDifficulty = 1;
//            int searchMethod = RivalConstants.SEARCH_TYPE_DEPTH;
//
//            BoardModel bm = getBoardModel("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
//
//            engineStub.m_rivalSearch.setUseOpeningBook(false);
//            generateMoveAndDisplay(searchDifficulty, "Single", engineStub, bm, searchMethod, hashSize);
//
//            System.exit(0);
//
//        } else if (mode == MODE_SUITE) {
//            m_isDebug = false;
//            engineStub.setDebug(m_isDebug);
//            engineStub.m_rivalSearch.setUseOpeningBook(false);
//
//            generateMoveAndDisplay(7, "QProb", engineStub, getBoardModel("5r2/p2Pk1p1/1p2B2p/3P4/1R3P2/5r2/1P5P/6K1 w - - 10 33"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "Passant", engineStub, getBoardModel("rnbqkbnr/ppppp1pp/8/4P3/5pP1/8/PPPP1P1P/RNBQKBNR b QKqk g3 0 3"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "KpK", engineStub, getBoardModel("8/8/8/8/8/p1k5/8/1K6 w - - 24 96"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "Start", engineStub, getBoardModel("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "KQkrp 3", engineStub, getBoardModel("8/8/1P6/5pr1/8/4R3/7k/2K5 w - -"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(9, "KQkrp 1", engineStub, getBoardModel("8/2p4P/8/kr6/6R1/8/8/1K6 w - -"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "Promote", engineStub, getBoardModel("rnb1qbnr/pppPk1pp/8/8/8/8/PPPP1PPP/RNBQKBNR w QK - 1 5"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "P5", engineStub, getBoardModel("2r5/p2Qbkpp/4p3/5p2/2P4q/P2P2N1/1r3P1P/R3K2b w Q - 0 20"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "Ruy", engineStub, getBoardModel("r1bqk2r/2pp1ppp/p1n2n2/1pb1p3/4P3/1B3N2/PPPP1PPP/RNBQ1RK1 w qk - 4 7"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "WAC 1", engineStub, getBoardModel("2rr3k/pp3pp1/1nnqbN1p/3pN3/2pP4/2P3Q1/PPB4P/R4RK1 w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(10, "EndSwap", engineStub, getBoardModel("5k2/5p1p/p3n1p1/P5P1/2BK1P1P/8/8/8 w - - 5 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(9, "HPawn3", engineStub, getBoardModel("8/8/7p/P2k3p/7p/2b5/8/6K1 w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "KRKn 5", engineStub, getBoardModel("4n3/7R/8/8/8/8/8/2K2k2 w - -"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(9, "KNPkp 1", engineStub, getBoardModel("8/8/8/8/5kp1/P7/8/1K1N4 w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "WAC 3", engineStub, getBoardModel("5rk1/1ppb3p/p1pb4/6q1/3P1p1r/2P1R2P/PP1BQ1P1/5RKN w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(11, "WAC 6", engineStub, getBoardModel("7k/p7/1R5K/6r1/6p1/6P1/8/8 w - - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "WAC 7", engineStub, getBoardModel("rnbqkb1r/pppp1ppp/8/4P3/6n1/7P/PPPNPPP1/R1BQKBNR b KQkq - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "WAC 8", engineStub, getBoardModel("r4q1k/p2bR1rp/2p2Q1N/5p2/5p2/2P5/PP3PPP/R5K1 w - - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(9, "WAC 9", engineStub, getBoardModel("3q1rk1/p4pp1/2pb3p/3p4/6Pr/1PNQ4/P1PB1PP1/4RRK1 b - - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "WAC 10", engineStub, getBoardModel("2br2k1/2q3rn/p2NppQ1/2p1P3/Pp5R/4P3/1P3PPP/3R2K1 w - - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "WAC 11", engineStub, getBoardModel("r1b1kb1r/3q1ppp/pBp1pn2/8/Np3P2/5B2/PPP3PP/R2Q1RK1 w kq - 0 1 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "P1", engineStub, getBoardModel("r3kbnr/pp1qpppp/3pb3/8/3BP3/2NB1N2/PPP2PPP/R3K2R b QKqk - 4 8"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "Qc3 bad", engineStub, getBoardModel("2r3k1/q4ppp/3p4/1r1Pp3/2n3P1/pN1R1P1P/P1P5/2KRQ3 w - - 8 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(12, "P2", engineStub, getBoardModel("8/5p2/5k1p/p6P/PP2K1P1/8/8/8 b - - 0 67"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "B Prom", engineStub, getBoardModel("2r5/Br4pp/1P1k4/3P1p2/1R1R1P2/p6P/1n3P2/6K1 w - - 2 35"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "Qa6 bad", engineStub, getBoardModel("2r2rk1/p4ppp/2Q5/4b3/1q6/4P3/PP3PPP/R1B2K1R w - - 3 19"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "P3", engineStub, getBoardModel("8/6nk/8/1p2P1Q1/1P4PP/P4q2/8/6K1 w - - 3 56"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(5, "P4", engineStub, getBoardModel("5rk1/1b3p1p/pp3p2/3n1N2/1P6/P1qB1PP1/3Q3P/4R1K1 w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "Pawn Up", engineStub, getBoardModel("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 0 56"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "Heavy D", engineStub, getBoardModel("4k3/1p3pp1/7p/1p6/1P2P2P/2PRKB2/4N1P1/2r1q3 b - - 0 40 "), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "P6", engineStub, getBoardModel("r2r3k/5ppp/p7/3p4/P1pN1Nq1/5nPP/1PR5/4RK2 w - -"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(5, "P7", engineStub, getBoardModel("r1b4k/1p1n4/p7/3Q4/8/2qBB2P/PrP2PP1/b2R2RK w - - 9 6"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(9, "P8", engineStub, getBoardModel("8/8/6p1/7p/7P/R4k2/p6K/r7 b - - 95 105"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "P9", engineStub, getBoardModel("8/8/R5p1/3k3p/7P/8/p6K/r7 b - - 95 104"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(5, "P10", engineStub, getBoardModel("rq3rk1/2bn1ppn/2p1p3/p2p2PR/P1PP4/N3B3/4PP2/R2QKB2 b Q - 0 21"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "P11", engineStub, getBoardModel("r2r3k/5ppp/p7/Q2p4/P1pN1Nq1/5nPP/1PR5/4RK2 b - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "P12", engineStub, getBoardModel("8/2pb4/4k3/5pp1/8/r4PbB/8/3R2K1 w - f6 0 35"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(5, "P13", engineStub, getBoardModel("8/p5pp/4pk2/5p2/1QP5/P7/5P1P/R3K2N b Q - 0 5"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(5, "P14", engineStub, getBoardModel("2r3k1/1rnq1pb1/2Rp1npp/1p1Pp3/1P2P3/3BBPPN/3Q2K1/4R3 b - - 3 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(6, "P15", engineStub, getBoardModel("r1b1kbnr/2ppqppp/p1n5/1p2p3/4P3/1B3N2/PPPP1PPP/RNBQK2R w QKqk - 2 6"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(8, "B+N", engineStub, getBoardModel("8/2k5/8/8/4K3/2B5/4N3/8 w - - 0 1"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//            generateMoveAndDisplay(7, "RvR", engineStub, getBoardModel("8/8/3k4/6R1/8/4K3/r7/8 b - - 54 117"), RivalConstants.SEARCH_TYPE_DEPTH, hashSize);
//
//            System.out.println(
//                    "Time: " + pad((double) totalTime / 1000, 6) +
//                            "Nodes: " + pad(totalNodes, 11) +
//                            "NPS: " + pad((int) (((double) totalNodes / totalTime) * 1000.0), 7));
//
//            showAfterSearchStats(engineStub);
//        }
//
//        System.exit(0);
//    }
//
//    public static String pad(double i, int size) {
//        NumberFormat f = NumberFormat.getInstance();
//        f.setMaximumFractionDigits(2);
//        f.setMinimumFractionDigits(2);
//        String s = f.format(i).toString();
//        return pad(s, size);
//    }
//
//    public static String padLeft(String s, int size) {
//        int l = s.length();
//        for (int i = l; i < size; i++) {
//            s += " ";
//        }
//        return s;
//    }
//
//    public static String pad(int i, int size) {
//        String s = NumberFormat.getInstance().format(i).toString();
//        return pad(s, size);
//    }
//
//    public static String pad(String s, int size) {
//        String retStr = "";
//        int l = s.length();
//        for (int i = l; i < size; i++) {
//            retStr += " ";
//        }
//        return retStr + s + " ";
//    }
//
//    public static void generateMoveAndDisplay(
//            int difficulty,
//            String name,
//            EngineStub engineStub,
//            BoardModel boardModel,
//            int engineMode,
//            int hashSizeMB) {
//        generateMoveAndDisplay(difficulty, name, engineStub, boardModel, engineMode, hashSizeMB, new String[]{""}, 0);
//    }
//
//    public static void generateMoveAndDisplay(
//            int difficulty,
//            String name,
//            EngineStub engineStub,
//            BoardModel boardModel,
//            int engineMode,
//            int hashSizeMB,
//            String[] correctMoves,
//            int extraPlies) {
//        if (m_isDebug) showTime();
//
//        engineStub.setEngineDifficulty(difficulty + SUITE_DEPTH_ADDITION);
//        engineStub.setHashSizeMB(hashSizeMB);
//        engineStub.setEngineMode(engineMode);
//
//        engineStub.startEngine(boardModel, null);
//
//        System.out.println(
//                padLeft(name, 10) +
//                        "Depth: " + pad(engineStub.getCurrentDepthIteration(), 2) +
//                        "Time: " + pad((double) engineStub.getSearchDuration() / 1000, 6) +
//                        "Score: " + pad((double) engineStub.getCurrentScore() / 100, 5) +
//                        "Nodes: " + pad(engineStub.getNodes(), 11) +
//                        "NPS: " + pad(engineStub.getNodesPerSecond(), 7) +
//                        "Height: " + pad(engineStub.getCurrentPath().height, 7) +
//                        "Path: " + engineStub.getCurrentPath());
//
//        totalTime += engineStub.getSearchDuration();
//        totalNodes += engineStub.getNodes();
//
//        if (m_isDebug) showAfterSearchStats(engineStub);
//    }
//
//    public static void showAfterSearchStats(EngineStub engineStub) {
//        System.out.println(
//                "EvalHashStored: " + pad(engineStub.getEvalHashValuesStored(), 11) +
//                        "EvalHashRetrieved: " + pad(engineStub.getEvalHashValuesRetrieved(), 11) +
//                        "PawnHashStored: " + pad(engineStub.getPawnHashValuesStored(), 11) +
//                        "PawnHashRetrieved: " + pad(engineStub.getPawnHashValuesRetrieved(), 11) +
//                        "PawnQuickRetrieved: " + pad(engineStub.getPawnQuickValuesRetrieved(), 11)
//        );
//
//        System.out.println("Futility Prunes=" + nf.format(engineStub.m_rivalSearch.m_futilityPrunes));
//        System.out.println("Pawn Extensions=" + nf.format(engineStub.m_rivalSearch.pawnExtensions));
//        System.out.println("Check Extensions=" + nf.format(engineStub.m_rivalSearch.checkExtensions));
//        System.out.println("Threat Extensions=" + nf.format(engineStub.m_rivalSearch.threatExtensions) + "/" + nf.format(engineStub.m_rivalSearch.immediateThreatExtensions));
//        System.out.println("Recapture Extensions=" + nf.format(engineStub.m_rivalSearch.recaptureExtensions) + " / " + nf.format(engineStub.m_rivalSearch.recaptureExtensionAttempts));
//        System.out.println("Late Move Reductions=" + nf.format(engineStub.m_rivalSearch.lateMoveReductions) + "/" + nf.format(engineStub.m_rivalSearch.lateMoveDoubleReductions));
//
//        engineStub.m_rivalSearch.printHashPopulationStats();
//        engineStub.m_rivalSearch.showProfileTimes();
//    }
//
//    public static void make(EngineChessBoard engineBoard, String sMove) {
//        int move = ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(sMove);
//        engineBoard.makeMove(move);
//        engineBoard.unMakeMove();
//        engineBoard.makeMove(move);
//    }
//
//    public static void testHash() throws IllegalFenException {
//        EngineChessBoard engineBoard = new EngineChessBoard();
//
//        BoardModel boardModel = new BoardModel();
//        FenChess fenChess = new FenChess(boardModel);
//        fenChess.setFromStr("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
//        engineBoard.setBoard(boardModel);
//
//        make(engineBoard, "e2e4");
//        make(engineBoard, "c7c5");
//        make(engineBoard, "g1f3");
//        make(engineBoard, "e7e6");
//        make(engineBoard, "d2d4");
//        make(engineBoard, "c5d4");
//        make(engineBoard, "f3d4");
//        make(engineBoard, "a7a6");
//        make(engineBoard, "f1d3");
//        make(engineBoard, "d8c7");
//        make(engineBoard, "e1g1");
//        make(engineBoard, "g8f6");
//        make(engineBoard, "b1d2");
//        make(engineBoard, "d7d5");
//        make(engineBoard, "d1e2");
//        make(engineBoard, "d5e4");
//        make(engineBoard, "d2e4");
//        make(engineBoard, "f8e7");
//        make(engineBoard, "c1e3");
//        make(engineBoard, "e8g8");
//        make(engineBoard, "e4f6");
//        make(engineBoard, "e7f6");
//        make(engineBoard, "e2h5");
//        make(engineBoard, "g7g6");
//        make(engineBoard, "h5f3");
//        make(engineBoard, "f6e5");
//        make(engineBoard, "f3h3");
//        make(engineBoard, "c7d8");
//        make(engineBoard, "d4f3");
//        make(engineBoard, "e5b2");
//        make(engineBoard, "f3g5");
//        make(engineBoard, "h7h5");
//        make(engineBoard, "a1d1");
//        make(engineBoard, "b8d7");
//        make(engineBoard, "g5e6");
//        make(engineBoard, "f7e6");
//        make(engineBoard, "h3e6");
//        make(engineBoard, "f8f7");
//        make(engineBoard, "e6g6");
//        make(engineBoard, "f7g7");
//        make(engineBoard, "g6h5");
//        make(engineBoard, "d8c7");
//        make(engineBoard, "h5d5");
//        make(engineBoard, "g8h8");
//        make(engineBoard, "d5h5");
//        make(engineBoard, "h8g8");
//        make(engineBoard, "h5d5");
//        make(engineBoard, "g8h8");
//        make(engineBoard, "d5h5");
//        make(engineBoard, "h8g8");
//
//        System.out.println(engineBoard.getFen());
//
//        System.out.println(engineBoard.m_hashValue + "/" + engineBoard.m_pawnHashValue);
//
//        fenChess.setFromStr("r1b3k1/1pqn2r1/p7/7Q/8/3BB3/PbP2PPP/3R1RK1 w - -");
//        engineBoard.setBoard(boardModel);
//
//        System.out.println(engineBoard.m_hashValue + "/" + engineBoard.m_pawnHashValue);
//
//        System.exit(0);
//    }
//
//    public static void showTime() {
//        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
//
//        Date now = new Date();
//
//        String strDate = sdfDate.format(now);
//        String strTime = sdfTime.format(now);
//
//        System.out.println("Date: " + strDate);
//        System.out.println("Time: " + strTime);
//    }
//
//    public static void setBoardState(EngineChessBoard engineBoard, int[] moves, int moveNum) throws EvaluationFlipException, IllegalFenException {
//        engineBoard.setBoard(UCIController.getBoardModel("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
//        for (int i = 0; i < moveNum; i++) {
//            engineBoard.makeMove(moves[i]);
//        }
//    }
//
//    public static BoardModel getBoardModel(String fen) throws EvaluationFlipException, IllegalFenException {
//        return UCIController.getBoardModel(fen);
//    }
//
//    public static void rivalVersusRival() throws EvaluationFlipException, IllegalFenException {
//        RivalConstants.USE_INTERNAL_OPENING_BOOK = true;
//
//        int whiteWins = 0;
//        int blackWins = 0;
//        int draw3 = 0;
//        int draw50 = 0;
//        int tooLong = 0;
//        int stalemate = 0;
//
//        RivalSearch searcherWhite = new RivalSearch();
//        RivalSearch searcherBlack = new RivalSearch();
//
//        int games = 300;
//        int duplicates = 0;
//        boolean printBoards = true;
//        boolean printMoves = true;
//
//        String positionsAtMove20[] = new String[games];
//
//        searcherWhite.setHashSizeMB(64);
//        searcherBlack.setHashSizeMB(64);
//
//        for (int gameNum = 1; gameNum <= games; gameNum++) {
//            EngineChessBoard engineBoard = new EngineChessBoard();
//            engineBoard.setBoard(getBoardModel("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
//
//            searcherWhite.setBoard(engineBoard);
//            searcherBlack.setBoard(engineBoard);
//
//            searcherWhite.startEngineTimer(false);
//            searcherBlack.startEngineTimer(false);
//
//            searcherWhite.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
//            searcherBlack.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
//
//            searcherWhite.setMillisToThink(40);
//            searcherBlack.setMillisToThink(40);
//
//            int gameState;
//            int move;
//
//            int[] gameMoves = new int[RivalConstants.MAX_GAME_MOVES];
//            int moveNum = 0;
//
//
//            do {
//                setBoardState(engineBoard, gameMoves, moveNum);
//                searcherWhite.go();
//                move = searcherWhite.getCurrentMove();
//                if (printMoves) System.out.println(ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(move));
//                if (printBoards) engineBoard.printBoard();
//                setBoardState(engineBoard, gameMoves, moveNum);
//                engineBoard.makeMove(move);
//                gameState = engineBoard.getGameState();
//                gameMoves[moveNum++] = move;
//                if (gameState == EngineChessBoard.GAMESTATE_INPLAY) {
//                    setBoardState(engineBoard, gameMoves, moveNum);
//                    searcherBlack.go();
//                    move = searcherBlack.getCurrentMove();
//                    if (printMoves)
//                        System.out.println(ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(move));
//                    if (printBoards) engineBoard.printBoard();
//                    setBoardState(engineBoard, gameMoves, moveNum);
//                    engineBoard.makeMove(move);
//                    gameState = engineBoard.getGameState();
//                    gameMoves[moveNum++] = move;
//                }
//                if (engineBoard.m_movesMade == 20) {
//                    String fen = engineBoard.getFen();
//                    System.out.println("===============");
//                    System.out.println("Move 20");
//                    System.out.println("===============");
//                    engineBoard.printBoard();
//                    for (int i = 0; i < gameNum - 1; i++) {
//                        if (positionsAtMove20[i] != null && positionsAtMove20[i].equals(fen)) {
//                            System.out.println("Duplicate Game");
//                            duplicates++;
//                        }
//                    }
//                    positionsAtMove20[gameNum - 1] = fen;
//                }
//            }
//            while (gameState == EngineChessBoard.GAMESTATE_INPLAY);
//
//            switch (gameState) {
//                case EngineChessBoard.GAMESTATE_CHECKMATE:
//                    if (engineBoard.m_isWhiteToMove) blackWins++;
//                    else whiteWins++;
//                    break;
//                case EngineChessBoard.GAMESTATE_STALEMATE:
//                    stalemate++;
//                    break;
//                case EngineChessBoard.GAMESTATE_THREEFOLD_DRAW:
//                    draw3++;
//                    break;
//                case EngineChessBoard.GAMESTATE_FIFTYMOVE_DRAW:
//                    draw50++;
//                    break;
//                case EngineChessBoard.GAMESTATE_GAMETOOLONG_DRAW:
//                    tooLong++;
//                    break;
//            }
//
//            System.out.println("===============");
//            System.out.println("End");
//            System.out.println("===============");
//            engineBoard.printBoard();
//            engineBoard.printPreviousMoves();
//            System.out.println(whiteWins + "/" + blackWins + "/" + stalemate + "/" + draw3 + "/" + draw50);
//            System.out.println("Duplicates = " + duplicates);
//            System.out.println("Game too long draws = " + tooLong);
//        }
//        System.exit(0);
//    }
//
//    public static void generateKPKBitBase() {
//        BoardRef boardRef;
//        EngineChessBoard engineBoard = new EngineChessBoard();
//
//        int draws = 0;
//        int wins = 0;
//        int illegal = 0;
//
//        int size = 64 * 48 * 32 * 2 / 8;
//        byte[] kpkBitbase = new byte[size];
//        for (int i = 0; i < size; i++) kpkBitbase[i] = 0;
//
//        RivalSearch rivalSearch = new RivalSearch();
//        rivalSearch.setHashSizeMB(128);
//        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
//        rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH);
//
//        for (int mover = 0; mover < 2; mover++)
//            for (int whiteKingIndex = 0; whiteKingIndex < 32; whiteKingIndex++)
//                for (int blackKingSquare = 0; blackKingSquare < 64; blackKingSquare++)
//                    for (int whitePawnSquare = 8; whitePawnSquare < 56; whitePawnSquare++) {
//                        int whiteKingSquare = ((whiteKingIndex / 4) * 8) + (whiteKingIndex % 4);
//
//                        if (whiteKingSquare != blackKingSquare && whiteKingSquare != whitePawnSquare && blackKingSquare != whitePawnSquare) {
//                            BoardModel boardModel = new BoardModel();
//
//                            boardRef = ChessBoardConversion.getBoardRefFromBitRef(whiteKingSquare);
//                            int whiteKingX = boardRef.getXFile();
//                            int whiteKingY = boardRef.getYRank();
//
//                            boardRef = ChessBoardConversion.getBoardRefFromBitRef(blackKingSquare);
//                            int blackKingX = boardRef.getXFile();
//                            int blackKingY = boardRef.getYRank();
//
//                            boardRef = ChessBoardConversion.getBoardRefFromBitRef(whitePawnSquare);
//                            int whitePawnX = boardRef.getXFile();
//                            int whitePawnY = boardRef.getYRank();
//
//                            boardModel.setPieceCode(whiteKingX, whiteKingY, 'K');
//                            boardModel.setPieceCode(blackKingX, blackKingY, 'k');
//                            boardModel.setPieceCode(whitePawnX, whitePawnY, 'P');
//                            boardModel.setWhiteKingSideCastleAvailable(false);
//                            boardModel.setWhiteQueenSideCastleAvailable(false);
//                            boardModel.setBlackKingSideCastleAvailable(false);
//                            boardModel.setBlackQueenSideCastleAvailable(false);
//                            boardModel.setEnPassantFile(-1);
//                            boardModel.setWhiteToMove(mover == 0);
//
//                            engineBoard.setBoard(boardModel);
//
//                            if (!engineBoard.isNonMoverInCheck()) {
//                                System.out.println(engineBoard);
//                                System.out.println("# " + (draws + wins));
//
//                                rivalSearch.setBoard(engineBoard);
//                                rivalSearch.clearHash();
//
//                                rivalSearch.go();
////								int correctScore = rivalSearch.kpkLookup(whiteKingSquare, blackKingSquare, whitePawnSquare, mover == 0) == 0 ? 0 : 1;
//                                int score = rivalSearch.getCurrentScore() == 0 ? 0 : 1;
//
////								if (score != correctScore)
////								{
////									System.out.println("Table says " + correctScore + " Rival said " + rivalSearch.getCurrentScore());
////									System.exit(0);
////								}
//
//                                int index =
//                                        (whiteKingIndex * 64 * 48 * 2) +
//                                                (blackKingSquare * 48 * 2) +
//                                                ((whitePawnSquare - 8) * 2) +
//                                                mover;
//
//                                int byteIndex = index / 8;
//                                int bitIndex = index % 8;
//
//                                if (kpkBitbase[byteIndex] == -1) {
//                                    System.out.println("ERROR ON INDEX " + byteIndex);
//                                    System.exit(0);
//                                }
//
//                                if (score != 0) {
//                                    wins++;
//                                    kpkBitbase[byteIndex] |= (1L << bitIndex);
//                                } else {
//                                    draws++;
//                                }
//                            } else {
//                                illegal++;
//                            }
//                        } else {
//                            illegal++;
//                        }
//                        System.out.println("Draws = " + draws + " Wins = " + wins + " Illegal = " + illegal);
//                    }
//
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream("kpk.rival.generated");
//            out.write(kpkBitbase);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void generateKQKPBitBase() {
//        BoardRef boardRef;
//        EngineChessBoard engineBoard = new EngineChessBoard();
//
//        int draws = 0;
//        int wins = 0;
//        int illegal = 0;
//        int unknown = 0;
//        int losses = 0;
//
//        int size = 64 * 16 * 32 * 16 * 2 / 8;
//        byte[] kqkpBitbase = new byte[size];
//        for (int i = 0; i < size; i++) kqkpBitbase[i] = 0;
//
//        RivalSearch rivalSearch = new RivalSearch();
//        rivalSearch.setHashSizeMB(128);
//        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
//        rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH);
//
//        for (int mover = 0; mover < 2; mover++)
//            for (int whiteKingIndex = 0; whiteKingIndex < 32; whiteKingIndex++)
//                for (int blackKingSquare = 0; blackKingSquare < 16; blackKingSquare++)
//                    for (int blackPawnSquare = 8; blackPawnSquare < 24; blackPawnSquare++)
//                        for (int whiteQueenSquare = 0; whiteQueenSquare < 64; whiteQueenSquare++) {
//                            int whiteKingSquare = ((whiteKingIndex / 4) * 8) + (whiteKingIndex % 4);
//
//                            long testBitboard = (1L << whiteKingSquare);
//                            testBitboard |= (1L << blackKingSquare);
//                            testBitboard |= (1L << whiteQueenSquare);
//                            testBitboard |= (1L << blackPawnSquare);
//
//                            if (Long.bitCount(testBitboard) == 4) {
//                                BoardModel boardModel = new BoardModel();
//
//                                boardRef = ChessBoardConversion.getBoardRefFromBitRef(whiteKingSquare);
//                                int whiteKingX = boardRef.getXFile();
//                                int whiteKingY = boardRef.getYRank();
//
//                                boardRef = ChessBoardConversion.getBoardRefFromBitRef(blackKingSquare);
//                                int blackKingX = boardRef.getXFile();
//                                int blackKingY = boardRef.getYRank();
//
//                                boardRef = ChessBoardConversion.getBoardRefFromBitRef(blackPawnSquare);
//                                int blackPawnX = boardRef.getXFile();
//                                int blackPawnY = boardRef.getYRank();
//
//                                boardRef = ChessBoardConversion.getBoardRefFromBitRef(whiteQueenSquare);
//                                int whiteQueenX = boardRef.getXFile();
//                                int whiteQueenY = boardRef.getYRank();
//
//                                boardModel.setPieceCode(whiteKingX, whiteKingY, 'K');
//                                boardModel.setPieceCode(whiteQueenX, whiteQueenY, 'Q');
//                                boardModel.setPieceCode(blackKingX, blackKingY, 'k');
//                                boardModel.setPieceCode(blackPawnX, blackPawnY, 'p');
//                                boardModel.setBlackKingSideCastleAvailable(false);
//                                boardModel.setBlackQueenSideCastleAvailable(false);
//                                boardModel.setWhiteKingSideCastleAvailable(false);
//                                boardModel.setWhiteQueenSideCastleAvailable(false);
//                                boardModel.setEnPassantFile(-1);
//                                boardModel.setWhiteToMove(mover == 0);
//
//                                engineBoard.setBoard(boardModel);
//
//                                if (!engineBoard.isNonMoverInCheck()) {
//                                    rivalSearch.setBoard(engineBoard);
//
//                                    int score = 0;
//                                    int searchScore = 0;
//                                    boolean done = false;
//                                    for (int depth = 18; depth <= 18 && !done; depth++) {
//                                        rivalSearch.setSearchDepth(depth);
//                                        rivalSearch.go();
//
//                                        searchScore = rivalSearch.getCurrentScore();
//                                        if (searchScore == 0) {
//                                            score = 0;
//                                            done = true;
//                                        }
//                                        if ((mover == 0 && searchScore > 9000) || (mover == 1 && searchScore < -9000)) {
//                                            score = 1;
//                                            done = true;
//                                        }
//                                        if ((mover == 0 && searchScore < -9000) || (mover == 1 && searchScore > 9000)) {
//                                            score = 2;
//                                            done = true;
//                                        }
//                                    }
//
//                                    int index =
//                                            (whiteKingIndex * 64 * 16 * 16 * 2) +
//                                                    (whiteQueenSquare * 16 * 16 * 2) +
//                                                    (blackKingSquare * 16 * 2) +
//                                                    ((blackPawnSquare - 8) * 2) +
//                                                    mover;
//
//                                    int byteIndex = index / 8;
//                                    int bitIndex = index % 8;
//
//                                    if (done) {
//                                        if (score == 1) {
//                                            //rivalSearch.knownKQKPWonPositions[wins] = engineBoard.m_hashValue;
//                                            wins++;
//                                            kqkpBitbase[byteIndex] |= (1L << bitIndex);
//                                        } else if (score == 2) {
//                                            //rivalSearch.knownKQKPLostPositions[losses] = engineBoard.m_hashValue;
//                                            losses++;
//                                        } else {
//                                            //rivalSearch.knownKQKPDrawnPositions[draws] = engineBoard.m_hashValue;
//                                            draws++;
//                                        }
//                                    } else {
//                                        //rivalSearch.knownKQKPProbablyDrawnPositions[unknown] = engineBoard.m_hashValue;
//                                        unknown++;
//                                        System.out.println(engineBoard);
//                                        System.out.println("# " + (draws + wins + illegal));
//                                        System.out.println("Draws = " + draws + " Wins = " + wins + " Losses = " + losses + " Unknown = " + unknown + " Illegal = " + illegal);
//                                    }
//                                } else {
//                                    illegal++;
//                                }
//                            } else {
//                                illegal++;
//                            }
//                            if ((draws + wins + illegal) % 100 == 0) {
//                                System.out.println("Draws = " + draws + " Wins = " + wins + " Losses = " + losses + " Unknown = " + unknown + " Illegal = " + illegal);
//                                System.out.print("{");
//                                for (int i = 0; i < draws; i++) {
//                                    //System.out.print(rivalSearch.knownKQKPDrawnPositions[i]);
//                                    if (i != draws - 1) {
//                                        System.out.print(",");
//                                    }
//                                    if (i % 25 == 24) {
//                                        System.out.println();
//                                    }
//                                }
//                                System.out.println("};");
//                            }
//                        }
//
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream("kqkp.rival");
//            out.write(kqkpBitbase);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("KQKP analysis complete");
//    }
//
//}
