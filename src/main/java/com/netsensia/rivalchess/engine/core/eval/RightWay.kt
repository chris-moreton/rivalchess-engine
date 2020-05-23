package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.DARK_SQUARES
import com.netsensia.rivalchess.bitboards.LIGHT_SQUARES
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.SquareOccupant

data class RightWaySquares(
        val h1: Int,
        val h2: Int,
        val h3: Int,
        val g2: Int,
        val g3: Int,
        val f1: Int,
        val f2: Int,
        val f3: Int,
        val f4: Int
)

fun scoreRightWayPositions(
        board: EngineBoard,
        rightWaySquares: RightWaySquares,
        isWhite: Boolean,
        cornerColour: Colour
): Int {
    val offset = if (isWhite) 0 else 6
    val friendlyPawns = board.getBitboard(BitboardType.fromIndex(SquareOccupant.WP.index + offset))
    val friendlyRooks = board.getBitboard(BitboardType.fromIndex(SquareOccupant.WR.index + offset))
    val friendlyKnights = board.getBitboard(BitboardType.fromIndex(SquareOccupant.WN.index + offset))
    val friendlyBishops = board.getBitboard(BitboardType.fromIndex(SquareOccupant.WB.index + offset))

    if (board.allPiecesBitboard and (1L shl rightWaySquares.h1) != 0L ||
            friendlyRooks and (1L shl rightWaySquares.f1) == 0L) {
        return 0
    }
    return  if (friendlyPawns and (1L shl rightWaySquares.f2) != 0L) {
        if (friendlyPawns and (1L shl rightWaySquares.g2) != 0L) {
            checkForPositionsAOrD(friendlyPawns, friendlyKnights, friendlyBishops, rightWaySquares, cornerColour)
        } else {
            checkForPositionsBOrC(friendlyPawns, friendlyBishops, rightWaySquares)
        }
    } else {
        if (friendlyPawns and (1L shl rightWaySquares.f4) != 0L) {
            checkForPositionE(friendlyPawns, friendlyKnights, rightWaySquares)
        } else {
            checkForPositionFOrH(friendlyPawns, rightWaySquares)
        }
    } / 4
}

fun checkForPositionFOrH(friendlyPawns: Long, rightWaySquares: RightWaySquares) =
        if (friendlyPawns and (1L shl rightWaySquares.f3) != 0L && friendlyPawns and (1L shl rightWaySquares.g2) != 0L)
            (if (friendlyPawns and (1L shl rightWaySquares.h2) != 0L) -10 // (F)
            else (if (friendlyPawns and (1L shl rightWaySquares.h3) != 0L) -30 else 0)) // (H)
        else 0

fun checkForPositionE(friendlyPawns: Long, friendlyKnights: Long, rightWaySquares: RightWaySquares): Int {
    var safety = 0
    if (friendlyPawns and (1L shl rightWaySquares.g2) != 0L
            && friendlyPawns and (1L shl rightWaySquares.h2) != 0L) {
        // (E)
        safety += 80
        if (friendlyPawns and (1L shl rightWaySquares.h2) != 0L
                && friendlyKnights and (1L shl rightWaySquares.f3) != 0L) {
            safety += 40
        }
    }
    return safety
}

fun checkForPositionsBOrC(friendlyPawns: Long, friendlyBishops: Long, rightWaySquares: RightWaySquares) =
        if (friendlyPawns and (1L shl rightWaySquares.g3) != 0L) {
            (if (friendlyPawns and (1L shl rightWaySquares.h2) != 0L) {
                (if (friendlyBishops and (1L shl rightWaySquares.g2) != 0L) {
                    100 // (B)
                } else 0)
            } else {
                (if (friendlyPawns and (1L shl rightWaySquares.h3) != 0L
                        && friendlyBishops and (1L shl rightWaySquares.g2) != 0L) {
                    70 // (C)
                } else 0)
            })
        } else 0

fun checkForPositionsAOrD(
        friendlyPawns: Long,
        friendlyKnights: Long,
        friendlyBishops: Long,
        rightWaySquares: RightWaySquares,
        cornerColour: Colour
) = if (friendlyPawns and (1L shl rightWaySquares.h2) != 0L) {
    120 // (A)
} else {
    checkForPositionD(friendlyPawns, friendlyKnights, friendlyBishops, rightWaySquares, cornerColour)
}

fun checkForPositionD(
        friendlyPawns: Long,
        friendlyKnights: Long,
        friendlyBishops: Long,
        rightWaySquares: RightWaySquares,
        cornerColour: Colour)
        : Int {
    var safety = 0
    if (friendlyPawns and (1L shl rightWaySquares.h3) != 0L
            && friendlyKnights and (1L shl rightWaySquares.f3) != 0L) {
        // (D)
        safety += 70
        // check for bishop of same colour as h3
        val bits = if (cornerColour == Colour.WHITE) LIGHT_SQUARES else DARK_SQUARES
        if (bits and friendlyBishops != 0L) {
            safety -= 30
        }
    }
    return safety
}

fun getWhiteKingRightWayScore(engineBoard: EngineBoard): Int {
    return if (engineBoard.getWhiteKingSquare() == 1 || engineBoard.getWhiteKingSquare() == 8) {
        scoreRightWayPositions(engineBoard,
                RightWaySquares(0, 8, 16, 9, 17, 2, 10, 18, 26), true,
                Colour.WHITE)
    } else 0
}

fun getBlackKingRightWayScore(engineBoard: EngineBoard): Int {
    return if (engineBoard.getBlackKingSquare() == 57 || engineBoard.getBlackKingSquare() == 48) {
        scoreRightWayPositions(engineBoard,
                RightWaySquares(56, 48, 40, 49, 41, 58, 50, 42, 34), false,
                Colour.BLACK)
    } else 0
}