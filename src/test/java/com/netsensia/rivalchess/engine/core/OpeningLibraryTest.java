package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OpeningLibraryTest {

    private List<String> fens = new ArrayList<>();

    private void makeMove(String fen, String path) throws IllegalFenException {

        EngineChessBoard engineBoard = new EngineChessBoard();
        engineBoard.setBoard(FenUtils.getBoardModel(fen));
        engineBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        engineBoard.generateLegalMoves();

        for (int i=0; i<engineBoard.getNumLegalMoves(); i++) {
            String sMove = ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineBoard.getLegalMoveByIndex(i));

            try {
                if (engineBoard.isMoveLegal(engineBoard.getLegalMoveByIndex(i))) {
                    engineBoard.makeMove(engineBoard.getLegalMoveByIndex(i));

                    String newPath = path + (path.equals("") ? "" : "_") + sMove.toUpperCase();
                    String newFen = engineBoard.getFen();
                    if (!fens.contains(newFen)) {
                        fens.add(newFen);
                        if (OpeningLibrary.getMove(newFen) != 0) {
                            makeMove(newFen, newPath);
                        }
                    }
                    engineBoard.unMakeMove();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(sMove + " from  " + fen + " is dodgy!");
                System.exit(0);
            }
        }
    }

    @Test
    public void testLegalityOfMoves() throws IllegalFenException {
        makeMove(OpeningLibrary.START, "");
    }

    @Test
    public void testSpecificMoves() {

        for (int i=0; i<1000; i++) {
            assertTrue(Arrays.asList(
                    "b8c6", "g7g6", "a7a6").contains(ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(
                    OpeningLibrary.getMove(OpeningLibrary.E2E4_C7C5_G1F3_D7D6_D2D4_C5D4_F3D4_G8F6_B1C3))));
        }
    }
}
