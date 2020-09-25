package com.netsensia.rivalchess.engine.hash;

import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversionKt;
import junit.framework.TestCase;

import static com.netsensia.rivalchess.config.SearchKt.MAXIMUM_HASH_AGE;
import static com.netsensia.rivalchess.consts.GameKt.FEN_START_POS;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.makeMove;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.unMakeMove;
import static com.netsensia.rivalchess.consts.HashIndexKt.HASHENTRY_MOVE;
import static com.netsensia.rivalchess.consts.HashValueTypeKt.EXACT;

public class SearchHashTest extends TestCase {

    public void testIsHeightHashTableEntryValid() {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));
        final com.netsensia.rivalchess.engine.hash.BoardHash boardHash = engineBoard.boardHashObject;

        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("e2e4").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("e7e5").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("g1f3").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("g8f6").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b1c3").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b8c6").compact, false, true);

        boardHash.storeHashMove(
                ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                engineBoard, 2, (byte)EXACT, 4);

        unMakeMove(engineBoard, true);
        unMakeMove(engineBoard, true);
        unMakeMove(engineBoard, true);
        unMakeMove(engineBoard, true);

        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b1c3").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b8c6").compact, false, true);
        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("g1f3").compact, false, true);

        assertFalse(com.netsensia.rivalchess.engine.hash.SearchHashKt.isHeightHashTableEntryValid(2, boardHash, engineBoard.boardHashObject.getHashIndex(engineBoard)));

        makeMove(engineBoard, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("g8f6").compact, false, true);

        assertTrue(com.netsensia.rivalchess.engine.hash.SearchHashKt.isHeightHashTableEntryValid(2, boardHash, engineBoard.boardHashObject.getHashIndex(engineBoard)));

        // gets correct move from use height hash table
        assertEquals(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        // has not been added to always replace table, no need as it was accepted by use height table
        assertFalse(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d2d4").compact ==
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        // entry does not have enough height
        assertFalse(com.netsensia.rivalchess.engine.hash.SearchHashKt.isHeightHashTableEntryValid(5, boardHash, engineBoard.boardHashObject.getHashIndex(engineBoard)));

        // try to store another move with lower height
        boardHash.storeHashMove(
                ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineBoard, 2, (byte)EXACT, 3);

        // move from original entry still valid
        assertEquals(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        // new move valid in always replace table
        assertEquals(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        // try to store another move with heigher height
        boardHash.storeHashMove(
                ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                engineBoard, 2, (byte)EXACT, 6);

        // move from new entry now valid
        assertEquals(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("h2h3").compact,
                boardHash.useHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        // old entry has been added to always replace table
        assertEquals(ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d2d4").compact,
                boardHash.ignoreHeight(
                        boardHash.getHashIndex(engineBoard) + HASHENTRY_MOVE));

        for (int i = 0; i < MAXIMUM_HASH_AGE; i++) {
            boardHash.incVersion();
            assertTrue(com.netsensia.rivalchess.engine.hash.SearchHashKt.isHeightHashTableEntryValid(2, boardHash, engineBoard.boardHashObject.getHashIndex(engineBoard)));
        }

        boardHash.incVersion();
        // Hash table is too old
        assertFalse(com.netsensia.rivalchess.engine.hash.SearchHashKt.isHeightHashTableEntryValid(2, boardHash, engineBoard.boardHashObject.getHashIndex(engineBoard)));

    }
}