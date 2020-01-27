package com.netsensia.rivalchess.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.util.FenUtils;
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

    private final RivalSearch rivalSearch;
    private final int timeMultiple;
    private final PrintStream printStream;

    private final EngineChessBoard engineBoard = new EngineChessBoard();

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
            rivalSearch.quit();
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

            try {
                engineBoard.setBoard(getBoardModel(s, parts));
                rivalSearch.setBoard(engineBoard);

                playMovesFromPosition(parts);

            } catch (IllegalFenException e) {
                this.printStream.println("Illegal fen");
            }

        }
    }

    private void playMovesFromPosition(String[] parts) {
        final int l = parts.length;

        if (l > 2) {
            for (int pos = 2; pos < l; pos++) {
                if (parts[pos].equals("moves")) {
                    for (int i = pos + 1; i < l; i++) {
                        rivalSearch.getEngineChessBoard().makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(parts[i]));
                    }
                    break;
                }
            }
        }
    }

    private Board getBoardModel(String s, String[] parts) throws IllegalFenException {
        Board board;
        if (parts[1].equals("startpos")) {
            board = FenUtils.getBoardModel(EngineChessBoard.START_POS);
        } else {
            board = FenUtils.getBoardModel(s.substring(12).trim());
        }
        return board;
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

    public void waitForSearchToComplete() {
        int state;
        rivalSearch.stopSearch();
        do {
            state = rivalSearch.getEngineState();
        }
        while (state != RivalConstants.SEARCHSTATE_READY && state != RivalConstants.SEARCHSTATE_SEARCHCOMPLETE);
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
