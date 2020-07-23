package com.netsensia.rivalchess.epd;

import com.netsensia.rivalchess.enums.SearchState;
import com.netsensia.rivalchess.engine.search.Search;
import com.netsensia.rivalchess.exception.IllegalEpdItemException;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.util.EpdItem;
import com.netsensia.rivalchess.util.EpdReader;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.netsensia.rivalchess.config.LimitKt.MAX_SEARCH_DEPTH;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getPgnMoveFromCompactMove;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.hasItem;

@SuppressWarnings("squid:S106")
@Ignore
public class WacTest {

    private static final int MAX_NODES_TO_SEARCH = 500000;
    private static final int MAX_SEARCH_SECONDS = 1000;
    private static Search search;
    private static int fails = 0;
    private static final boolean RECALCULATE_FAILURES = true;

    @BeforeClass
    public static void setup() {
        search = new Search();
        new Thread(search).start();
    }

    private final List<String> failingPositions = Collections.unmodifiableList(Arrays.asList(
            "WAC.002", // Fail 1
            "WAC.071", // Fail 2
            "WAC.080", // Fail 3
            "WAC.087", // Fail 4
            "WAC.091", // Fail 5
            "WAC.092", // Fail 6
            "WAC.100", // Fail 7
            "WAC.120", // Fail 8
            "WAC.141", // Fail 9
            "WAC.145", // Fail 10
            "WAC.151", // Fail 11
            "WAC.152", // Fail 12
            "WAC.163", // Fail 13
            "WAC.200", // Fail 14
            "WAC.213", // Fail 15
            "WAC.222", // Fail 16
            "WAC.229", // Fail 17
            "WAC.230", // Fail 18
            "WAC.237", // Fail 19
            "WAC.247", // Fail 20
            "WAC.248", // Fail 21
            "WAC.252", // Fail 22
            "WAC.265", // Fail 23
            "WAC.269", // Fail 24
            "WAC.274", // Fail 25
            "WAC.287", // Fail 26
            "WAC.291", // Fail 27
            "WAC.297" // Fail 28
    ));

    @Test
    public void winAtChess() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.001", true);
    }

    @Test
    public void winAtChessFails() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.001", false);
    }

    private void testPosition(EpdItem epdItem, boolean expectedToPass) throws IllegalFenException {

        Board board = Board.fromFen(epdItem.getFen());

        search.quit();

        search = new Search();
        new Thread(search).start();

        search.setBoard(board);
        search.setSearchDepth(MAX_SEARCH_DEPTH - 2);
        search.setNodesToSearch(epdItem.getMaxNodesToSearch());
        search.setMillisToThink(MAX_SEARCH_SECONDS * 1000);

        search.clearHash();
        search.startSearch();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        if (!RECALCULATE_FAILURES) System.out.println(epdItem.getId() + " " + dtf.format(now));

        try {
            await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !search.isSearching());
        } catch (ConditionTimeoutException e) {
            search.stopSearch();

            SearchState state;
            do state = search.getEngineState(); while (state != SearchState.READY && state != SearchState.COMPLETE);
        }

        final String move = getPgnMoveFromCompactMove(
                search.getCurrentMove(), epdItem.getFen());

        if (RECALCULATE_FAILURES) {
            if (!epdItem.getBestMoves().contains(move)) {
                fails++;
                System.out.println("\"" + epdItem.getId() + "\", // Fail " + fails);
            }
        } else {
            System.out.println("Looking for " + move + " in " + epdItem.getBestMoves());

            if (expectedToPass) {
                Assert.assertTrue(epdItem.getBestMoves().contains(move));
            } else {
                Assert.assertFalse(epdItem.getBestMoves().contains(move));
            }
        }
    }

    public void runEpdSuite(String filename, String startAtId, boolean expectedToPass)
            throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("epd/" + filename)).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        runEpdSuite(startAtId, epdReader, expectedToPass);
    }

    private void runEpdSuite(String startAtId, EpdReader epdReader, boolean expectedToPass) throws IllegalFenException, InterruptedException {
        boolean processTests = false;

        for (EpdItem epdItem : epdReader) {
            epdItem.setMaxNodesToSearch(MAX_NODES_TO_SEARCH);
            processTests = processTests || (epdItem.getId().equals(startAtId));
            if (processTests && (RECALCULATE_FAILURES || expectedToPass != failingPositions.contains(epdItem.getId()))) {
                testPosition(epdItem, expectedToPass);
            }
        }
    }

}
