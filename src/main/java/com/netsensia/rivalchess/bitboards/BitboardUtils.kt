package com.netsensia.rivalchess.bitboards

tailrec fun southFill(bitboard: Long, shiftBy: Int = 8): Long {
    val shiftedAndOrred = bitboard or (bitboard ushr shiftBy)
    return if (shiftBy == 32) shiftedAndOrred else southFill(shiftedAndOrred, shiftBy * 2)
}

tailrec fun northFill(bitboard: Long, shiftBy: Int = 8): Long {
    val shiftedAndOrred = bitboard or (bitboard shl shiftBy)
    return if (shiftBy == 32) shiftedAndOrred else northFill(shiftedAndOrred, shiftBy * 2)
}

fun getBlackPassedPawns(whitePawns: Long, blackPawns: Long): Long {
    return blackPawns and
            northFill(whitePawns or Bitboards.getWhitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()
}

fun getPawnFiles(pawns: Long): Long {
    return southFill(pawns) and Bitboards.RANK_1
}

fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long): Long {
    return whitePawns and
            southFill(blackPawns or Bitboards.getBlackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()
}