package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.io.IOException;
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

                            // remove en passant part
                            newFen = newFen.replaceAll(" a3", " -")
                            .replaceAll(" b3", " -")
                            .replaceAll(" c3", " -")
                            .replaceAll(" d3", " -")
                            .replaceAll(" e3", " -")
                            .replaceAll(" f3", " -")
                            .replaceAll(" g3", " -")
                            .replaceAll(" h3", " -")
                            .replaceAll(" a6", " -")
                            .replaceAll(" b6", " -")
                            .replaceAll(" c6", " -")
                            .replaceAll(" d6", " -")
                            .replaceAll(" e6", " -")
                            .replaceAll(" f6", " -")
                            .replaceAll(" g6", " -")
                            .replaceAll(" h6", " -");

                            ProcessBuilder processBuilder = new ProcessBuilder();

                            String regex = "'s/\"" + newFen.replaceAll("/","\\\\/") + "\"/" + newPath + "/g'";
                            String command = "sed -i " + regex + " /home/chrismoreton/git/chris-moreton/rival-chess-android-engine/src/main/java/com/netsensia/rivalchess/engine/core/OpeningLibrary.java";
                            processBuilder.command("bash", "-c", command);
                            processBuilder.start();
                            Thread.sleep(1000);
                            makeMove(newFen, newPath);
                        }
                    }
                    engineBoard.unMakeMove();
                }
            } catch (ArrayIndexOutOfBoundsException | IOException | InterruptedException e) {
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
