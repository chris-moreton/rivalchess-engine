package com.netsensia.rivalchess.uci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.netsensia.rivalchess.config.BuildInfo;
import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.config.Uci;
import com.netsensia.rivalchess.engine.core.ConstantsKt;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.enums.SearchState;
import com.netsensia.rivalchess.engine.core.Search;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.util.FenUtils;
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

    private final Search search;
    private final int timeMultiple;
    private final PrintStream printStream;

    public UCIController(Search engine, int timeMultiple, PrintStream printStream) {
        search = engine;
        this.timeMultiple = timeMultiple;
        this.printStream = printStream;
        EngineMonitor.setPrintStream(this.printStream);
    }

    @Override
    public void run() {
        printStream.println("Welcome to Rival Chess UCI");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;

        search.setUseOpeningBook(false);

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
            search.quit();
            System.exit(0);
        }
    }

    private void handleIfStopCommand(String[] parts) {
        if (parts[0].equals("stop")) {
            search.stopSearch();
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
            search.setUseOpeningBook(parts[4].equals("true"));
        }
    }

    private void handleIfSetOptionHashValueCommand(String[] parts) {
        if (parts[2].equals("Hash") && parts[3].equals("value")) {
            search.setHashSizeMB(Integer.parseInt(parts[4]));
        }
    }

    private void handleIfSetOptionNameClearHashCommand(String[] parts) {
        if (parts[2].equals("Clear") && parts[3].equals("Hash")) {
            search.clearHash();
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

            search.startSearch();
        }
    }

    private void setSearchOptions() {
        if (isInfinite) {
            search.setMillisToThink(Limit.MAX_SEARCH_MILLIS.getValue());
            search.setSearchDepth(Limit.MAX_SEARCH_DEPTH.getValue() - 2);
            search.setNodesToSearch(Limit.MAX_NODES_TO_SEARCH.getValue());
        } else if (moveTime != -1) {
            search.setMillisToThink(moveTime);
            search.setSearchDepth(Limit.MAX_SEARCH_DEPTH.getValue() - 2);
            search.setNodesToSearch(Limit.MAX_NODES_TO_SEARCH.getValue());
        } else if (maxDepth != -1) {
            search.setSearchDepth(maxDepth);
            search.setMillisToThink(Limit.MAX_SEARCH_MILLIS.getValue());
            search.setNodesToSearch(Limit.MAX_NODES_TO_SEARCH.getValue());
        } else if (maxNodes != -1) {
            search.setSearchDepth(Limit.MAX_SEARCH_DEPTH.getValue() - 2);
            search.setMillisToThink(Limit.MAX_SEARCH_MILLIS.getValue());
            search.setNodesToSearch(maxNodes);
        } else if (whiteTime != -1) {
            int calcTime = (search.getMover() == Colour.WHITE ? whiteTime : blackTime) / (movesToGo == 0 ? 120 : movesToGo);
            int guaranteedTime = (search.getMover() == Colour.WHITE ? whiteInc : blackInc);
            int timeToThink = calcTime + guaranteedTime - Uci.UCI_TIMER_SAFTEY_MARGIN_MILLIS.getValue();
            search.setMillisToThink(Limit.MAX_SEARCH_DEPTH.getValue() - 2);
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
            search.setSearchDepth(maxDepth);
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
                search.setBoard(getBoardModel(s, parts));

                playMovesFromPosition(parts);

            } catch (IllegalFenException | InvalidMoveException e) {
                this.printStream.println("Illegal fen");
            }

        }
    }

    private void playMovesFromPosition(String[] parts) throws InvalidMoveException {
        final int l = parts.length;

        if (l > 2) {
            for (int pos = 2; pos < l; pos++) {
                if (parts[pos].equals("moves")) {
                    for (int i = pos + 1; i < l; i++) {
                        search.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(parts[i]));
                    }
                    break;
                }
            }
        }
    }

    private Board getBoardModel(String s, String[] parts) throws IllegalFenException {
        Board board;
        if (parts[1].equals("startpos")) {
            board = FenUtils.getBoardModel(ConstantsKt.FEN_START_POS);
        } else {
            board = FenUtils.getBoardModel(s.substring(12).trim());
        }
        return board;
    }

    private void handleIfUciNewGameCommand(String[] parts) {
        if (parts[0].equals("ucinewgame")) {
            waitForSearchToComplete();
            search.newGame();
        }
    }

    private void handleIfIsReadyCommand(String[] parts) {
        if (parts[0].equals("isready")) {
            EngineMonitor.sendUCI("readyok");
        }
    }

    private void handleIfUciCommand(String[] parts) {
        if (parts[0].equals("uci")) {
            EngineMonitor.sendUCI("id name Rival (version " + BuildInfo.VERSION.getValue() + " build " + BuildInfo.BUILD.getValue() + ")");
            EngineMonitor.sendUCI("id author Chris Moreton");
            EngineMonitor.sendUCI("option name Hash type spin default 1 min 1 max 256");
            EngineMonitor.sendUCI("uciok");
        }
    }

    public void waitForSearchToComplete() {
        SearchState state;
        search.stopSearch();
        do {
            state = search.getEngineState();
        }
        while (state != SearchState.READY && state != SearchState.COMPLETE);
    }


}
