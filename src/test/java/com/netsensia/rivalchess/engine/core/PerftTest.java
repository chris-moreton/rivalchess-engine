package com.netsensia.rivalchess.engine.core;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;

import com.netsensia.rivalchess.exception.EvaluationFlipException;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netsensia.rivalchess.uci.UCIController;

public class PerftTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerftTest.class);

    private void assertPerftScore(String fen, int depth, int expectedScore) throws EvaluationFlipException {

        EngineChessBoard engineBoard = new EngineChessBoard(new Bitboards());
        NumberFormat nf = NumberFormat.getInstance();

        engineBoard.setBoard(UCIController.getBoardModel(fen));
        long start = System.currentTimeMillis();
        long nodes = UCIController.getPerft(engineBoard, depth);

        assertEquals(expectedScore, nodes);

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        double seconds = elapsed / 1000.0;
        double nps = nodes / seconds;

        if (LOGGER.isInfoEnabled()) {
            String nodesPerSecond = nf.format(nps);
            LOGGER.info("Nodes per second = {}", nodesPerSecond);
        }
    }

    @Test
    public void testPerftScore() throws EvaluationFlipException {

        assertPerftScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 6, 38633283);
        assertPerftScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", 4, 20541);
        assertPerftScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609);
        assertPerftScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - 1 67", 2, 279);
        assertPerftScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 5, 11139762);
        assertPerftScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 7, 178633661);
        assertPerftScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 5, 193690690);
        assertPerftScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 4, 182838);

    }
}