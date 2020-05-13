package com.netsensia.rivalchess.bitboards

import java.util.*

const val RANK_8 = -0x100000000000000L
const val RANK_7 = 0x00FF000000000000L
const val RANK_6 = 0x0000FF0000000000L
const val RANK_5 = 0x000000FF00000000L
const val RANK_4 = 0x00000000FF000000L
const val RANK_3 = 0x0000000000FF0000L
const val RANK_2 = 0x000000000000FF00L
const val RANK_1 = 0x00000000000000FFL

const val FILE_A = 1L shl 7 or (1L shl 15) or (1L shl 23) or (1L shl 31) or (1L shl 39) or (1L shl 47) or (1L shl 55) or (1L shl 63)
const val FILE_B = 1L shl 6 or (1L shl 14) or (1L shl 22) or (1L shl 30) or (1L shl 38) or (1L shl 46) or (1L shl 54) or (1L shl 62)
const val FILE_C = 1L shl 5 or (1L shl 13) or (1L shl 21) or (1L shl 29) or (1L shl 37) or (1L shl 45) or (1L shl 53) or (1L shl 61)
const val FILE_D = 1L shl 4 or (1L shl 12) or (1L shl 20) or (1L shl 28) or (1L shl 36) or (1L shl 44) or (1L shl 52) or (1L shl 60)
const val FILE_E = 1L shl 3 or (1L shl 11) or (1L shl 19) or (1L shl 27) or (1L shl 35) or (1L shl 43) or (1L shl 51) or (1L shl 59)
const val FILE_F = 1L shl 2 or (1L shl 10) or (1L shl 18) or (1L shl 26) or (1L shl 34) or (1L shl 42) or (1L shl 50) or (1L shl 58)
const val FILE_G = 1L shl 1 or (1L shl 9) or (1L shl 17) or (1L shl 25) or (1L shl 33) or (1L shl 41) or (1L shl 49) or (1L shl 57)
const val FILE_H = 1L or (1L shl 8) or (1L shl 16) or (1L shl 24) or (1L shl 32) or (1L shl 40) or (1L shl 48) or (1L shl 56)

const val MIDDLE_FILES_8_BIT: Long = 0xE7
const val NONMID_FILES_8_BIT: Long = 0x18

const val F1G1 = 1L shl 1 or (1L shl 2)
const val G1H1 = 1L or (1L shl 1)
const val A1B1 = 1L shl 7 or (1L shl 6)
const val B1C1 = 1L shl 6 or (1L shl 5)
const val F8G8 = 1L shl 58 or (1L shl 57)
const val G8H8 = 1L shl 57 or (1L shl 56)
const val A8B8 = 1L shl 63 or (1L shl 62)
const val B8C8 = 1L shl 62 or (1L shl 61)

val FILES = Collections.unmodifiableList(Arrays.asList(FILE_H, FILE_G, FILE_F, FILE_E, FILE_D, FILE_C, FILE_B, FILE_A))