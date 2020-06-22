package com.netsensia.rivalchess.util

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.isCheck
import com.netsensia.rivalchess.engine.board.makeMove
import com.netsensia.rivalchess.engine.board.unMakeMove
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel

fun getBitRefFromBoardRef(boardRef: Square) = 63 - 8 * boardRef.yRank - boardRef.xFile

fun getBitRefFromBoardRef(xFile: Int, yRank: Int) = 63 - 8 * yRank - xFile

fun getMoveRefFromEngineMove(move: Int): Move {
    val from = move shr 16 and 63
    val to = move and 63
    val boardRefFrom = Square.fromBitRef(from)
    val boardRefTo = Square.fromBitRef(to)
    val promotionPieceCode = move and PROMOTION_PIECE_TOSQUARE_MASK_FULL
    return if (promotionPieceCode != 0) {
        when (promotionPieceCode) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WQ else SquareOccupant.BQ)
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WR else SquareOccupant.BR)
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WN else SquareOccupant.BN)
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WB else SquareOccupant.BB)
            else -> throw RuntimeException("Unexpected error")
        }
    } else Move(boardRefFrom, boardRefTo, SquareOccupant.NONE)
}

fun getSimpleAlgebraicMoveFromCompactMove(compactMove: Int): String {
    if (compactMove == 0) return "zero"
    val from = compactMove shr 16 and 63
    val to = compactMove and 63
    val move = getMoveRefFromEngineMove(compactMove)
    val pp = if (move.promotedPiece == SquareOccupant.NONE) "" else move.promotedPiece.toChar().toString().trim { it <= ' ' }
    return getSimpleAlgebraicFromBitRef(from) + getSimpleAlgebraicFromBitRef(to) + pp
}

fun getSimpleAlgebraicFromBitRef(bitRef: Int): String {
    val boardRef = Square.fromBitRef(bitRef)
    val a = (boardRef.xFile + 97).toChar()
    return "" + a + (7 - boardRef.yRank + 1)
}

@Throws(IllegalFenException::class, InvalidMoveException::class)
fun getPgnMoveFromCompactMove(move: Int, fen: String?): String {
    val board = EngineBoard()
    board.setBoard(getBoardModel(fen!!))
    var pgnMove = ""
    val to = move and 63
    val from = move ushr 16 and 63
    val promotionPiece = move and PROMOTION_PIECE_TOSQUARE_MASK_FULL
    when (board.getPieceIndex(from)) {
        BITBOARD_WN, BITBOARD_BN -> pgnMove = "N"
        BITBOARD_WK, BITBOARD_BK -> pgnMove = "K"
        BITBOARD_WQ, BITBOARD_BQ -> pgnMove = "Q"
        BITBOARD_WB, BITBOARD_BB -> pgnMove = "B"
        BITBOARD_WR, BITBOARD_BR -> pgnMove = "R"
        BITBOARD_WP, BITBOARD_BP -> {
        }
        else -> throw InvalidMoveException("No piece found on source square")
    }
    var qualifier = ' '
    val legalMoves = board.moveGenerator().generateLegalMoves().moves
    var moveCount = 0
    var legalMove = legalMoves[moveCount] and 0x00FFFFFF
    while (legalMove != 0) {
        val legalMoveTo = legalMove and 63
        val legalMoveFrom = legalMove ushr 16 and 63
        if (legalMoveTo == to) {
            if (board.getPieceIndex(legalMoveFrom) == board.getPieceIndex(from)) {
                if (legalMoveFrom != from) {
                    qualifier = if (legalMoveFrom % 8 == from % 8) ('1'.toInt() + from / 8).toChar()
                    else ('a'.toInt() + (7 - from % 8)).toChar()
                }
            }
        }
        moveCount++
        legalMove = legalMoves[moveCount] and 0x00FFFFFF
    }
    if (qualifier != ' ') pgnMove += qualifier
    if (board.getPieceIndex(to) != -1) {
        if (board.getPieceIndex(from) in arrayOf(BITBOARD_WP, BITBOARD_BP)) {
            pgnMove += ('a'.toInt() + (7 - from % 8)).toChar()
        }
        pgnMove += "x"
    }
    pgnMove += getSimpleAlgebraicFromBitRef(to)
    if (promotionPiece != 0) {
        when (promotionPiece) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> pgnMove += "=Q"
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> pgnMove += "=N"
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> pgnMove += "=B"
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> pgnMove += "=R"
        }
    }
    if (board.makeMove(move)) {
        if (board.isCheck(board.mover)) pgnMove += "+"
        board.unMakeMove()
    }
    return pgnMove
}

fun getEngineMoveFromSimpleAlgebraic(s: String): EngineMove {
    val fromX = s.toUpperCase()[0].toInt() - 65
    val fromY = 7 - (s.toUpperCase()[1].toInt() - 49)
    val toX = s.toUpperCase()[2].toInt() - 65
    val toY = 7 - (s.toUpperCase()[3].toInt() - 49)
    val fromBitRef = getBitRefFromBoardRef(Square.fromCoords(fromX, fromY))
    var toBitRef = getBitRefFromBoardRef(Square.fromCoords(toX, toY))
    val l = s.length
    if (l == 5) {
        when (s.toUpperCase()[4]) {
            'Q' -> toBitRef = toBitRef or PROMOTION_PIECE_TOSQUARE_MASK_QUEEN
            'R' -> toBitRef = toBitRef or PROMOTION_PIECE_TOSQUARE_MASK_ROOK
            'N' -> toBitRef = toBitRef or PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT
            'B' -> toBitRef = toBitRef or PROMOTION_PIECE_TOSQUARE_MASK_BISHOP
        }
    }
    return EngineMove(fromBitRef shl 16 or toBitRef)
}
