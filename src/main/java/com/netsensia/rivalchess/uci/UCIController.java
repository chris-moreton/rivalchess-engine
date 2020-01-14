package com.netsensia.rivalchess.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.engine.test.epd.EPDRunner;
import com.netsensia.rivalchess.exception.EvaluationFlipException;
import com.netsensia.rivalchess.model.board.BoardModel;
import com.netsensia.rivalchess.model.board.FenChess;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class UCIController implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UCIController.class);

    private int whiteTime;
    private int blackTime;
    private int whiteInc;
    private int blackInc;
    private int movesToGo;
    private int maxDepth;
    private int maxNodes;
    private int moveTime;
    private boolean isInfinite;

    private RivalSearch rivalSearch;
    private int timeMultiple;
    private PrintStream printStream;

    private BoardModel boardModel = new BoardModel();
    private FenChess fenChess = new FenChess(boardModel);
    private EngineChessBoard engineBoard = new EngineChessBoard();

    public UCIController(RivalSearch engine, int timeMultiple, PrintStream printStream) {
        rivalSearch = engine;
        this.timeMultiple = timeMultiple;
        this.printStream = printStream;
        EngineMonitor.setPrintStream(this.printStream);
    }

    @Override
    public void run() {
        printStream.println("Welcome to Rival Chess UCI");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;

        Date todaysDate = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMdd-HH-mm-ss-S");
        String formattedDate = formatter.format(todaysDate);
        rivalSearch.setUseOpeningBook(false);

        try {

            while ((s = in.readLine()) != null) {
                processUCICommand(s);
            }

        } catch (IOException | NumberFormatException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public void processUCICommand(String s) {
        if (s.trim().length() != 0) {
            String[] parts = s.split(" ");
            if (parts.length > 0) {
                handleIfUciCommand(parts);
                handleIfVarCommand(parts);
                handleIfEpdCommand(s, parts);
                handleIfIsReadyCommand(parts);
                handleIfUciNewGameCommand(parts);
                handleIfPositionCommand(s, parts);
                handleIfGoCommand(parts);
                handleIfSetOptionCommand(parts);
                handleIfStopCommand(parts);
                handleIfQuitCommand(parts);
            }
        }
    }

    private void handleIfQuitCommand(String[] parts) {
        if (parts[0].equals("quit")) {
            System.exit(0);
        }
    }

    private void handleIfStopCommand(String[] parts) {
        if (parts[0].equals("stop")) {
            rivalSearch.stopSearch();
            waitForSearchToComplete();
        }
    }

    private void handleIfSetOptionCommand(String[] parts) {
        if (parts[0].equals("setoption")) {
            handleIfSetOptionNameCommand(parts);
        }
    }

    private void handleIfSetOptionNameCommand(String[] parts) {
        if (parts[1].equals("name")) {
            handleIfSetOptionNameClearHashCommand(parts);
            handleIfSetOptionHashValueCommand(parts);
            handleIfSetOptionOwnBookCommand(parts);
        }
    }

    private void handleIfSetOptionOwnBookCommand(String[] parts) {
        if (parts[2].equals("OwnBook") && parts[3].equals("value")) {
            rivalSearch.setUseOpeningBook(parts[4].equals("true"));
        }
    }

    private void handleIfSetOptionHashValueCommand(String[] parts) {
        if (parts[2].equals("Hash") && parts[3].equals("value")) {
            rivalSearch.setHashSizeMB(Integer.parseInt(parts[4]));
        }
    }

    private void handleIfSetOptionNameClearHashCommand(String[] parts) {
        if (parts[2].equals("Clear") && parts[3].equals("Hash")) {
            rivalSearch.clearHash();
        }
    }

    private void handleIfGoCommand(String[] parts) {

        if (parts[0].equals("go")) {
            whiteTime = -1;
            blackTime = -1;
            whiteInc = -1;
            blackInc = -1;
            movesToGo = 0;
            moveTime = -1;
            isInfinite = false;
            maxDepth = -1;
            for (int i = 1; i < parts.length; i++) {
                handleIfGoWtime(parts, i);
                handleIfGoBtime(parts, i);
                handleIfGoWinc(parts, i);
                handleIfGoBinc(parts, i);
                handleIfGoMovesTogo(parts, i);
                handleIfGoDepth(parts, i);
                handleIfGoNodes(parts, i);
                handleIfGoMoveTime(parts, i);
                handleIfGoInfinite(parts, i);
            }

            setSearchOptions();

            rivalSearch.startSearch();
        }
    }

    private void setSearchOptions() {
        if (isInfinite) {
            rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
            rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
            rivalSearch.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
        } else if (moveTime != -1) {
            rivalSearch.setMillisToThink(moveTime);
            rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
            rivalSearch.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
        } else if (maxDepth != -1) {
            rivalSearch.setSearchDepth(maxDepth);
            rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
            rivalSearch.setNodesToSearch(RivalConstants.MAX_NODES_TO_SEARCH);
        } else if (maxNodes != -1) {
            rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
            rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
            rivalSearch.setNodesToSearch(maxNodes);
        } else if (whiteTime != -1) {
            int calcTime = (engineBoard.m_isWhiteToMove ? whiteTime : blackTime) / (movesToGo == 0 ? 120 : movesToGo);
            int guaranteedTime = (engineBoard.m_isWhiteToMove ? whiteInc : blackInc);
            int timeToThink = calcTime + guaranteedTime - RivalConstants.UCI_TIMER_SAFTEY_MARGIN_MILLIS;
            rivalSearch.setMillisToThink(timeToThink);
            rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
        }
    }

    private void handleIfGoInfinite(String[] parts, int i) {
        if (parts[i].equals("infinite")) {
            isInfinite = true;
        }
    }

    private void handleIfGoMoveTime(String[] parts, int i) {
        if (parts[i].equals("movetime")) {
            moveTime = Integer.parseInt(parts[i + 1]) * timeMultiple;
        }
    }

    private void handleIfGoNodes(String[] parts, int i) {
        if (parts[i].equals("nodes")) {
            maxNodes = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfGoDepth(String[] parts, int i) {
        if (parts[i].equals("depth")) {
            maxDepth = Integer.parseInt(parts[i + 1]);
            rivalSearch.setSearchDepth(maxDepth);
        }
    }

    private void handleIfGoMovesTogo(String[] parts, int i) {
        if (parts[i].equals("movestogo")) {
            movesToGo = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfGoBinc(String[] parts, int i) {
        if (parts[i].equals("binc")) {
            blackInc = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfGoWinc(String[] parts, int i) {
        if (parts[i].equals("winc")) {
            whiteInc = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfGoBtime(String[] parts, int i) {
        if (parts[i].equals("btime")) {
            blackTime = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfGoWtime(String[] parts, int i) {
        if (parts[i].equals("wtime")) {
            whiteTime = Integer.parseInt(parts[i + 1]);
        }
    }

    private void handleIfPositionCommand(String s, String[] parts) {

        if (parts[0].equals("position")) {
            waitForSearchToComplete();
            setStartPosition(s, fenChess, parts);

            engineBoard.setBoard(boardModel);
            rivalSearch.setBoard(engineBoard);

            playMovesFromPosition(parts);
            if (RivalConstants.UCI_DEBUG) {
                rivalSearch.m_board.printLegalMoves();
                rivalSearch.m_board.printBoard();
            }
        }
    }

    private void playMovesFromPosition(String[] parts) {
        final int l = parts.length;

        if (l > 2) {
            for (int pos = 2; pos < l; pos++) {
                if (parts[pos].equals("moves")) {
                    for (int i = pos + 1; i < l; i++) {
                        rivalSearch.m_board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(parts[i]));
                    }
                    break;
                }
            }
        }
    }

    private void setStartPosition(String s, FenChess fenChess, String[] parts) {
        if (parts[1].equals("startpos")) {
            fenChess.setFromStr(EngineChessBoard.START_POS);
        } else {
            fenChess.setFromStr(s.substring(12).trim());
        }
    }

    private void handleIfUciNewGameCommand(String[] parts) {
        if (parts[0].equals("ucinewgame")) {
            waitForSearchToComplete();
            rivalSearch.newGame();
        }
    }

    private void handleIfIsReadyCommand(String[] parts) {
        if (parts[0].equals("isready")) {
            EngineMonitor.sendUCI("readyok");
        }
    }

    private void handleIfUciCommand(String[] parts) {
        if (parts[0].equals("uci")) {
            EngineMonitor.sendUCI("id name Rival (build " + RivalConstants.VERSION + ")");
            EngineMonitor.sendUCI("id author Chris Moreton");
            EngineMonitor.sendUCI("option name Hash type spin default 1 min 1 max 256");
            EngineMonitor.sendUCI("uciok");
        }
    }

    private void handleIfVarCommand(String[] parts) {
        if (parts[0].equals("var")) {
            showEngineVar(parts[1]);
        }
    }

    private void handleIfEpdCommand(String s, String[] parts) {
        if (parts[0].equals("epd")) {
            Pattern p = Pattern.compile("epd \"(.*)\" (.*) (.*)");
            Matcher m = p.matcher(s);
            if (m.find()) {
                EPDRunner epdRunner = new EPDRunner();
                epdRunner.go(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            }
        }
    }

    public void waitForSearchToComplete() {
        int state;
        rivalSearch.stopSearch();
        do {
            state = rivalSearch.getEngineState();
        }
        while (state != RivalConstants.SEARCHSTATE_READY && state != RivalConstants.SEARCHSTATE_SEARCHCOMPLETE);
    }

    public static BoardModel getBoardModel(String fen) throws EvaluationFlipException {
        BoardModel boardModel = new BoardModel();
        FenChess fenChess = new FenChess(boardModel);
        String invertedFen = invertFen(fen);

        RivalSearch testSearcher = new RivalSearch();
        EngineChessBoard testBoard = new EngineChessBoard();

        fenChess.setFromStr(invertedFen);
        testBoard.setBoard(boardModel);
        int eval1 = testSearcher.evaluate(testBoard);

        fenChess.setFromStr(fen);
        testBoard.setBoard(boardModel);
        int eval2 = testSearcher.evaluate(testBoard);

        if (eval1 != eval2) {
            throw new EvaluationFlipException("Eval flip error for " + fen + " " + invertedFen + " " + eval1 + " " + eval2);
        }

        return boardModel;
    }

    public static String invertFen(String fen) {
        fen = fen.replace(" b ", " . ");
        fen = fen.replace(" w ", " ; ");

        fen = fen.replace('Q', 'z');
        fen = fen.replace('K', 'x');
        fen = fen.replace('N', 'c');
        fen = fen.replace('B', 'v');
        fen = fen.replace('R', 'm');
        fen = fen.replace('P', ',');

        fen = fen.replace('q', 'Q');
        fen = fen.replace('k', 'K');
        fen = fen.replace('n', 'N');
        fen = fen.replace('b', 'B');
        fen = fen.replace('r', 'R');
        fen = fen.replace('p', 'P');

        fen = fen.replace('z', 'q');
        fen = fen.replace('x', 'k');
        fen = fen.replace('c', 'n');
        fen = fen.replace('v', 'b');
        fen = fen.replace('m', 'r');
        fen = fen.replace(',', 'p');

        fen = fen.replace(" . ", " w ");
        fen = fen.replace(" ; ", " b ");

        String[] fenParts = fen.split(" ");
        String[] boardParts = fenParts[0].split("/");

        String newFen =
                boardParts[7] + "/" +
                        boardParts[6] + "/" +
                        boardParts[5] + "/" +
                        boardParts[4] + "/" +
                        boardParts[3] + "/" +
                        boardParts[2] + "/" +
                        boardParts[1] + "/" +
                        boardParts[0];

        StringBuilder newFenBuilder = new StringBuilder(newFen);

        for (int i = 1; i < fenParts.length; i++) {
            newFenBuilder.append(" " + fenParts[i]);
        }

        return newFenBuilder.toString();
    }

    public static long getPerft(EngineChessBoard board, int depth) {
        if (depth == 0) return 1;
        long nodes = 0;
        int moveNum = 0;

        int[] legalMoves = new int[RivalConstants.MAX_LEGAL_MOVES];

        board.setLegalMoves(legalMoves);
        while (legalMoves[moveNum] != 0) {
            if (board.makeMove(legalMoves[moveNum])) {
                nodes += getPerft(board, depth - 1);
                board.unMakeMove();
            }
            moveNum++;
        }

        return nodes;
    }

    public void showEngineVar(String varName) {
        try {
            Class<?> c = Class.forName("com.netsensia.rivalchess.engine.core.RivalConstants");
            Field field = c.getDeclaredField(varName);

            printStream.println("Class: " + c.getName());

            printStream.println(varName + " = " + field.get(RivalConstants.getInstance()));

        } catch (ClassNotFoundException | SecurityException |
                NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.info(e.getMessage());
        }

    }

}
