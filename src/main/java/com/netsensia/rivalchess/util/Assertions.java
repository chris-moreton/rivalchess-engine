package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;

public class Assertions {

    public static boolean checkTrackedHash(EngineChessBoard board, String message) {
        final BoardHash boardHash = board.getBoardHash();

        final long tv = boardHash.getTrackedHashValue();
        final long av = boardHash.initialiseHashCode(board);

        if (tv != av) {
            System.out.println("What the hell!");
            System.out.println(board.getFen());
            System.out.println(message);
            System.exit(1);
        }

        return true;
    }

    public static boolean checkTrackedHash(EngineChessBoard board) {
        return checkTrackedHash(board, "");
    }
}
