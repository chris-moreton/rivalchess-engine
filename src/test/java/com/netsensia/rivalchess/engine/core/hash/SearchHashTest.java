package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.config.SearchConfig;
import com.netsensia.rivalchess.engine.core.ConstantsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.enums.HashIndex;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import junit.framework.TestCase;

public class SearchHashTest extends TestCase {

    public void testIsHeightHashTableEntryValid() throws InvalidMoveException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
        final BoardHash boardHash = engineBoard.getBoardHashObject();

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e7e5"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g8f6"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b1c3"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b8c6"));

        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                engineBoard, 2, (byte)HashValueType.EXACT.getIndex(), 4);

        engineBoard.unMakeMove();
        engineBoard.unMakeMove();
        engineBoard.unMakeMove();
        engineBoard.unMakeMove();

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b1c3"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b8c6"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));

        assertFalse(SearchHashKt.isHeightHashTableEntryValid(2, engineBoard));

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g8f6"));

        assertTrue(SearchHashKt.isHeightHashTableEntryValid(2, engineBoard));

        // gets correct move from use height hash table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        // has not been added to always replace table, no need as it was accepted by use height table
        assertFalse(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact ==
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        // entry does not have enough height
        assertFalse(SearchHashKt.isHeightHashTableEntryValid(5, engineBoard));

        // try to store another move with lower height
        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineBoard, 2, (byte)HashValueType.EXACT.getIndex(), 3);

        // move from original entry still valid
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        // new move valid in always replace table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        // try to store another move with heigher height
        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineBoard, 2, (byte)HashValueType.EXACT.getIndex(), 6);

        // move from new entry now valid
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        // old entry has been added to always replace table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HashIndex.MOVE.getIndex()));

        for (int i = 0; i < SearchConfig.MAXIMUM_HASH_AGE.getValue(); i++) {
            boardHash.incVersion();
            assertTrue(SearchHashKt.isHeightHashTableEntryValid(2, engineBoard));
        }

        boardHash.incVersion();
        // Hash table is too old
        assertFalse(SearchHashKt.isHeightHashTableEntryValid(2, engineBoard));

    }

    public void testIsAlwaysReplaceHashTableEntryValid() {
    }
}