package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.Search;

public final class RivalUCI {
    @SuppressWarnings("squid:S106")
    private static final Search SEARCH = new Search(System.out);

    public static void main(String[] args) {

        int timeMultiple;
        if (args.length > 1 && args[0].equals("tm")) {
            timeMultiple = Integer.parseInt(args[1]);
        } else {
            timeMultiple = 1;
        }

        SEARCH.startEngineTimer(true);
        SEARCH.setHashSizeMB(32);

        new Thread(SEARCH).start();

        @SuppressWarnings("squid:S106")
        UCIController uciController = new UCIController(
                SEARCH,
                timeMultiple,
                System.out);

        new Thread(uciController).start();
    }
}
