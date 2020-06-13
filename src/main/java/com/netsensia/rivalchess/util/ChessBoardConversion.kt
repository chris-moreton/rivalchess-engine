package com.netsensia.rivalchess.util

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.isCheck
import com.netsensia.rivalchess.engine.core.board.makeMove
import com.netsensia.rivalchess.engine.core.board.unMakeMove
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.enums.PromotionPieceMask.Companion.fromValue
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel

inline fun getBitRefFromBoardRef(boardRef: Square) = 63 - 8 * boardRef.yRank - boardRef.xFile

inline fun getBitRefFromBoardRef(xFile: Int, yRank: Int) = 63 - 8 * yRank - xFile

fun getMoveRefFromEngineMove(move: Int): Move {
    val from = move shr 16 and 63
    val to = move and 63
    val boardRefFrom = Square.fromBitRef(from)
    val boardRefTo = Square.fromBitRef(to)
    val promotionPieceCode = move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    return if (promotionPieceCode != 0) {
        when (fromValue(promotionPieceCode)) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WQ else SquareOccupant.BQ)
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WR else SquareOccupant.BR)
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WN else SquareOccupant.BN)
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> Move(boardRefFrom, boardRefTo, if (to >= 56) SquareOccupant.WB else SquareOccupant.BB)
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
    val promotionPiece = move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    when (board.getSquareOccupant(from).piece) {
        Piece.KNIGHT -> pgnMove = "N"
        Piece.KING -> pgnMove = "K"
        Piece.QUEEN -> pgnMove = "Q"
        Piece.BISHOP -> pgnMove = "B"
        Piece.ROOK -> pgnMove = "R"
        Piece.PAWN -> {
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
            if (board.getSquareOccupant(legalMoveFrom).index == board.getSquareOccupant(from).index) {
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
    if (board.getSquareOccupant(to).index != -1) {
        if (board.getSquareOccupant(from).piece == Piece.PAWN) {
            pgnMove += ('a'.toInt() + (7 - from % 8)).toChar()
        }
        pgnMove += "x"
    }
    pgnMove += getSimpleAlgebraicFromBitRef(to)
    if (promotionPiece != 0) {
        when (fromValue(promotionPiece)) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> pgnMove += "=Q"
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> pgnMove += "=N"
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> pgnMove += "=B"
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> pgnMove += "=R"
        }
    }
    if (board.makeMove(EngineMove(move))) {
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
            'Q' -> toBitRef = toBitRef or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value
            'R' -> toBitRef = toBitRef or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.value
            'N' -> toBitRef = toBitRef or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.value
            'B' -> toBitRef = toBitRef or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.value
        }
    }
    return EngineMove(fromBitRef shl 16 or toBitRef)
}
