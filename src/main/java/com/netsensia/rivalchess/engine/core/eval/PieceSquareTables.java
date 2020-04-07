package com.netsensia.rivalchess.engine.core.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PieceSquareTables {

    public static final List<Integer> pawn = Collections.unmodifiableList(Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            -6, 4, 4, -15, -15, 4, 4, -6,
            -6, 4, 2, 5, 5, 2, 4, -6,
            -6, 4, 5, 16, 16, 5, 4, -6,
            -5, 4, 10, 20, 20, 10, 4, -5,
            3, 12, 20, 28, 28, 20, 12, 3,
            8, 16, 24, 32, 32, 24, 16, 8,
            0, 0, 0, 0, 0, 0, 0, 0));

    public static final List<Integer> pawnEndGame = Collections.unmodifiableList(Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            -20, 0, 0, 0, 0, 0, 0, -20,
            -15, 5, 5, 5, 5, 5, 5, -15,
            -10, 10, 10, 10, 10, 10, 10, -10,
            5, 25, 25, 25, 25, 25, 25, 5,
            20, 30, 35, 35, 35, 35, 30, 20,
            25, 40, 45, 45, 45, 45, 40, 25,
            0, 0, 0, 0, 0, 0, 0, 0
    ));

    public static final List<Integer> knight = Collections.unmodifiableList(Arrays.asList(
            -50, -40, -30, -20, -20, -30, -40, -50,
            -40, -30, -10, 0, 0, -10, -30, -40,
            -20, -10, 0, 0, 0, 0, -10, -20,
            -17, 0, 3, 20, 20, 3, 0, -17,
            -17, 0, 10, 20, 20, 10, 0, -17,
            -20, 5, 7, 15, 15, 7, 5, -20,
            -40, -30, -10, 0, 0, -10, -30, -40,
            -50, -40, -30, -20, -20, -30, -40, -50
    ));

    public static final List<Integer> knightEndGame = Collections.unmodifiableList(Arrays.asList(
            -50, -40, -30, -20, -20, -30, -40, -50,
            -40, -30, -10, -5, -5, -10, -30, -40,
            -30, -10, 0, 10, 10, 0, -10, -30,
            -20, -5, 10, 20, 20, 10, -5, -20,
            -20, -5, 10, 20, 20, 10, -5, -20,
            -30, -10, 0, 10, 10, 0, -10, -30,
            -40, -30, -10, -5, -5, -10, -30, -40,
            -50, -40, -30, -20, -20, -30, -40, -50
    ));

    public static final List<Integer> bishop = Collections.unmodifiableList(Arrays.asList(
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 5, 2, 2, 2, 2, 5, 0,
            0, 3, 5, 5, 5, 5, 3, 0,
            0, 2, 5, 5, 5, 5, 2, 0,
            0, 2, 5, 5, 5, 5, 2, 0,
            0, 2, 5, 5, 5, 5, 2, 0,
            0, 5, 2, 2, 2, 2, 5, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    ));

    public static final List<Integer> rook = Collections.unmodifiableList(Arrays.asList(
            0, 3, 5, 5, 5, 5, 3, 0,
            -3, 2, 5, 5, 5, 5, 2, -3,
            -2, 0, 0, 2, 2, 0, 0, -2,
            -2, 0, 0, 0, 0, 0, 0, -2,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            15, 20, 20, 20, 20, 20, 20, 15,
            0, 3, 5, 5, 5, 5, 3, 0
    ));

    public static final List<Integer> queen = Collections.unmodifiableList(Arrays.asList(
            -10, -5, 0, 0, 0, 0, -5, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 5, 5, 6, 6, 5, 5, 0,
            0, 5, 6, 6, 6, 6, 5, 0,
            0, 5, 6, 6, 6, 6, 5, 0,
            0, 5, 5, 6, 6, 5, 5, 0,
            -5, 0, 5, 5, 5, 5, 0, -5,
            -10, -5, 0, 0, 0, 0, -5, -10
    ));

    public static final List<Integer> king = Collections.unmodifiableList(Arrays.asList(
            24, 24, 9, 0, 0, 9, 24, 24,
            16, 14, 7, -3, -3, 7, 14, 16,
            4, -2, -5, -15, -15, -5, -2, 4,
            -10, -15, -20, -25, -25, -20, -15, -10,
            -15, -30, -35, -40, -40, -35, -30, -15,
            -25, -35, -40, -45, -45, -40, -35, -25,
            -22, -35, -40, -40, -40, -40, -35, -22,
            -22, -35, -40, -40, -40, -40, -35, -22
    ));

    public static final List<Integer> kingEndGame = Collections.unmodifiableList(Arrays.asList(
            0, 8, 16, 24, 24, 16, 8, 0,
            8, 16, 24, 32, 32, 24, 16, 8,
            16, 24, 32, 40, 40, 32, 24, 16,
            24, 32, 40, 48, 48, 40, 32, 24,
            24, 32, 40, 48, 48, 40, 32, 24,
            16, 24, 32, 40, 40, 32, 24, 16,
            8, 16, 24, 32, 32, 24, 16, 8,
            0, 8, 16, 24, 24, 16, 8, 0
    ));
}
