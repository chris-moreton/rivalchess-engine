package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import junit.framework.TestCase;

public class SearchHashHelperTest extends TestCase {

    public void testIsHeightHashTableEntryValid() throws InvalidMoveException {
        EngineChessBoard engineChessBoard = new EngineChessBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
        final BoardHash boardHash = engineChessBoard.getBoardHashObject();

        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e7e5"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g8f6"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b1c3"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b8c6"));

        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                engineChessBoard, 2, RivalConstants.EXACTSCORE, 4);

        engineChessBoard.unMakeMove();
        engineChessBoard.unMakeMove();
        engineChessBoard.unMakeMove();
        engineChessBoard.unMakeMove();

        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b1c3"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b8c6"));
        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));

        assertFalse(SearchHashHelper.isHeightHashTableEntryValid(2, engineChessBoard));

        engineChessBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g8f6"));

        assertTrue(SearchHashHelper.isHeightHashTableEntryValid(2, engineChessBoard));

        // gets correct move from use height hash table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.getHashTableUseHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        // has not been added to always replace table, no need as it was accepted by use height table
        assertFalse(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact ==
                boardHash.getHashTableIgnoreHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        // entry does not have enough height
        assertFalse(SearchHashHelper.isHeightHashTableEntryValid(5, engineChessBoard));

        // try to store another move with lower height
        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineChessBoard, 2, RivalConstants.EXACTSCORE, 3);

        // move from original entry still valid
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.getHashTableUseHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        // new move valid in always replace table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.getHashTableIgnoreHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        // try to store another move with heigher height
        boardHash.storeHashMove(
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineChessBoard, 2, RivalConstants.EXACTSCORE, 6);

        // move from new entry now valid
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.getHashTableUseHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        // old entry has been added to always replace table
        assertEquals(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.getHashTableIgnoreHeight(
                        boardHash.getHashIndex(engineChessBoard) + RivalConstants.HASHENTRY_MOVE));

        for (int i=0; i < RivalConstants.MAXIMUM_HASH_AGE; i++) {
            boardHash.incVersion();
            assertTrue(SearchHashHelper.isHeightHashTableEntryValid(2, engineChessBoard));
        }

        boardHash.incVersion();
        // Hash table is too old
        assertFalse(SearchHashHelper.isHeightHashTableEntryValid(2, engineChessBoard));

    }

    public void testIsAlwaysReplaceHashTableEntryValid() {
    }
}