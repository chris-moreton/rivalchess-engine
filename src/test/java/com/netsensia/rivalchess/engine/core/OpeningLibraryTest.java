package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OpeningLibraryTest {

    private List<String> fens = new ArrayList<>();

    private void makeMove(String fen, String path) throws IllegalFenException {

        EngineChessBoard engineBoard = new EngineChessBoard();
        engineBoard.setBoard(FenUtils.getBoardModel(fen));
        engineBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        engineBoard.generateLegalMoves();

        for (int i=0; i<engineBoard.m_numLegalMoves; i++) {
            String sMove = ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineBoard.m_legalMoves[i]);

            try {
                if (engineBoard.isMoveLegal(engineBoard.m_legalMoves[i])) {
                    engineBoard.makeMove(engineBoard.m_legalMoves[i]);

                    String newPath = path + (path.equals("") ? "" : "_") + sMove.toUpperCase();
                    String newFen = engineBoard.getFen();
                    if (!fens.contains(newFen)) {
                        fens.add(newFen);
                        if (OpeningLibrary.getMove(newFen) != 0) {
                            System.out.println("public static final String " + newPath + " = \"" + newFen + "\";");
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
    public void getMove() throws IllegalFenException {

        makeMove(OpeningLibrary.START, "");

        System.out.println(fens.size());
    }
}
