package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;

public class Assertions {
    public static boolean checkTrackedHash(EngineChessBoard board) {
        final BoardHash boardHash = board.getBoardHash();

        final long tv = boardHash.getTrackedHashValue();
        final long av = boardHash.initialiseHashCode(board);

        if (tv != av) {
            System.out.println("What the hell! " + board.getFen());
            System.exit(1);
        }

        return true;
    }
}
