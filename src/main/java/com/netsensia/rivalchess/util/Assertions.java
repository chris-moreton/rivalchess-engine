package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.hash.BoardHash;
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator;

public class Assertions {

    public static boolean checkTrackedHash(EngineBoard board, String message) {
        final BoardHash boardHash = board.getBoardHashObject();

        final long tv = boardHash.getTrackedHashValue();
        final long av = ZorbristHashCalculator.calculateHash(board);

        if (tv != av) {
            System.out.println("What the hell!");
            System.out.println(board.getFen());
            System.out.println(message);
            System.exit(1);
        }

        return true;
    }

    public static boolean checkTrackedHash(EngineBoard board) {
        return checkTrackedHash(board, "");
    }
}
