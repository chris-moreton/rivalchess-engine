package com.netsensia.rivalchess.uci;

import java.io.PrintStream;
import java.util.TimerTask;

import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.util.ChessBoardConversion;

public class EngineMonitor extends TimerTask {
    private final RivalSearch m_engine;
    private static PrintStream m_out;

    public EngineMonitor(RivalSearch engine) {
        m_engine = engine;
    }

    public static void setPrintStream(PrintStream out) {
        m_out = out;
    }

    public static void sendUCI(String s) {
        m_out.println(s);
    }

    public void printInfo() {
        int depth = m_engine.getIterativeDeepeningDepth();
        sendUCI(
                "info" +
                        " currmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(m_engine.m_currentDepthZeroMove) +
                        " currmovenumber " + m_engine.m_currentDepthZeroMoveNumber +
                        " depth " + depth +
                        " score " + m_engine.getCurrentScoreHuman() +
                        " pv " + m_engine.getCurrentPathString().trim() +
                        " time " + m_engine.getSearchDuration() +
                        " nodes " + m_engine.getNodes() +
                        " nps " + m_engine.getNodesPerSecond());
    }

    public void run() {
        m_engine.setMillisSetByEngineMonitor(System.currentTimeMillis());

        if (m_engine.isUCIMode()) {
            if (m_engine.isOkToSendInfo()) {
                int state = m_engine.getEngineState();
                if (state == RivalConstants.SEARCHSTATE_SEARCHING && !m_engine.isAbortingSearch()) {
                    printInfo();
                }
            }
        }
    }
}
