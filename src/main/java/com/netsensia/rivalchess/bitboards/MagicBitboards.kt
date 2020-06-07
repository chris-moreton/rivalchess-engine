package com.netsensia.rivalchess.bitboards

import com.netsensia.rivalchess.bitboards.util.squareList

object MagicBitboards {
    @JvmField
    val magicMovesRook: Array<LongArray>
    @JvmField
    val magicMovesBishop: Array<LongArray>
    @JvmField
    val bishopVars: MagicVars
    @JvmField
    val rookVars: MagicVars

    var occupancyVariation: LongArray?
    var occupancyAttackSet: LongArray?


    private fun generateOccupancyVariationsAndDatabase(isRook: Boolean) {
        var bitRef: Int
        var mask: Long
        var setBitsInMask: List<Int>
        var bitCount: Int
        bitRef = 0
        while (bitRef <= 63) {
            mask = if (isRook) occupancyMaskRook[bitRef] else occupancyMaskBishop[bitRef]
            setBitsInMask = squareList(mask).toList()
            bitCount = java.lang.Long.bitCount(mask)
            calculateOccupancyAttackSets(isRook, bitRef, setBitsInMask, bitCount)
            setMagicMoves(isRook, bitRef, bitCount)
            bitRef++
        }
    }

    private fun setMagicMoves(isRook: Boolean, bitRef: Int, bitCount: Int) {
        val variations: Int
        var i: Int
        var validMoves: Long
        variations = (1L shl bitCount).toInt()
        i = 0
        while (i < variations) {
            validMoves = 0
            if (isRook) {
                setMagicMovesForRooks(bitRef, i, validMoves)
            } else {
                setMagicMovesForBishop(bitRef, i, validMoves)
            }
            i++
        }
    }

    private fun setMagicMovesForBishop(bitRef: Int, i: Int, validMoves: Long) {
        var validMovesShadow = validMoves
        val magicIndex: Int
        magicIndex = (occupancyVariation!![i] * magicNumberBishop[bitRef] ushr magicNumberShiftsBishop[bitRef]).toInt()
        validMovesShadow = setMagicMovesForNorthWestDiagonal(bitRef, i, validMovesShadow)
        validMovesShadow = setMagicMovesForSouthEastDiagonal(bitRef, i, validMovesShadow)
        validMovesShadow = setMagicMovesForNorthEastDiagonal(bitRef, i, validMovesShadow)
        validMovesShadow = setMagicMovesForSouthWestDiagonal(bitRef, i, validMovesShadow)
        magicMovesBishop[bitRef][magicIndex] = validMovesShadow
    }

    private fun setMagicMovesForSouthWestDiagonal(bitRef: Int, i: Int, validMoves: Long): Long {
        var validMovesShadow = validMoves
        var j: Int
        j = bitRef - 7
        while (j % 8 != 0 && j >= 0) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) {
                break
            }
            j -= 7
        }
        return validMovesShadow
    }

    private fun setMagicMovesForNorthEastDiagonal(bitRef: Int, i: Int, validMoves: Long): Long {
        var validMoves = validMoves
        var j: Int
        j = bitRef + 7
        while (j % 8 != 7 && j <= 63) {
            validMoves = validMoves or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) {
                break
            }
            j += 7
        }
        return validMoves
    }

    private fun setMagicMovesForSouthEastDiagonal(bitRef: Int, i: Int, validMoves: Long): Long {
        var validMovesShadow = validMoves
        var j: Int
        j = bitRef - 9
        while (j % 8 != 7 && j >= 0) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) {
                break
            }
            j -= 9
        }
        return validMovesShadow
    }

    private fun setMagicMovesForNorthWestDiagonal(bitRef: Int, i: Int, validMoves: Long): Long {
        var validMovesShadow = validMoves
        var j: Int
        j = bitRef + 9
        while (j % 8 != 0 && j <= 63) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) {
                break
            }
            j += 9
        }
        return validMovesShadow
    }

    private fun setMagicMovesForRooks(bitRef: Int, i: Int, validMoves: Long) {
        var validMovesShadow = validMoves
        val magicIndex: Int
        var j: Int
        magicIndex = (occupancyVariation!![i] * magicNumberRook[bitRef] ushr magicNumberShiftsRook[bitRef]).toInt()
        j = bitRef + 8
        while (j <= 63) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) break
            j += 8
        }
        j = bitRef - 8
        while (j >= 0) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) break
            j -= 8
        }
        j = bitRef + 1
        while (j % 8 != 0) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) break
            j++
        }
        j = bitRef - 1
        while (j % 8 != 7 && j >= 0) {
            validMovesShadow = validMovesShadow or (1L shl j)
            if (occupancyVariation!![i] and (1L shl j) != 0L) break
            j--
        }
        magicMovesRook[bitRef][magicIndex] = validMovesShadow
    }

    private fun calculateOccupancyAttackSets(isRook: Boolean, bitRef: Int, setBitsInMask: List<Int>, bitCount: Int) {
        val variationCount: Int
        var i: Int
        var setBitsInIndex: List<Int>

        // How many possibilities are there for occupancy patterns for this piece on this square
        // e.g. For a bishop on a8, there are 7 squares to move to, 7^2 = 64 possible variations of
        // how those squares could be occupied.
        variationCount = (1L shl bitCount).toInt()
        i = 0
        while (i < variationCount) {
            occupancyVariation!![i] = 0
            // find bits set in index "i" and map them to bits in the 64 bit "occupancyVariation"
            setBitsInIndex = squareList(i.toLong()).toList()
            for (setBitInIndex in setBitsInIndex) {
                occupancyVariation!![i] = occupancyVariation!![i] or (1L shl setBitsInMask[setBitInIndex])
                // e.g. if setBitsInIndex[0] == 3 then the third bit (position 4) is set in counter "i"
                // so we add the third relevant bit in the mask to this occupancyVariation
                // the third relevant bit in the mask is found by setBitsInMask[3]
            }

            // multiple variations can share the same attack set (possible moves),
            // so find this here because we can use it to allow clashes for hash keys
            val j = if (isRook) calculateOccupancyAttackSetsRook(bitRef, i) else calculateOccupancyAttackSetsBishop(bitRef, i)
            if (j >= 0 && j <= 63) {
                occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
            }
            i++
        }
    }

    private fun calculateOccupancyAttackSetsBishop(bitRef: Int, i: Int): Int {
        var j: Int
        j = bitRef + 9
        while (j % 8 != 7 && j % 8 != 0 && j <= 55 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j += 9
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef - 9
        while (j % 8 != 7 && j % 8 != 0 && j >= 8 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j -= 9
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef + 7
        while (j % 8 != 7 && j % 8 != 0 && j <= 55 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j += 7
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef - 7
        while (j % 8 != 7 && j % 8 != 0 && j >= 8 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j -= 7
        }
        return j
    }

    private fun calculateOccupancyAttackSetsRook(bitRef: Int, i: Int): Int {
        var j: Int
        j = bitRef + 8
        while (j <= 55 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j += 8
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef - 8
        while (j >= 8 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j -= 8
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef + 1
        while (j % 8 != 7 && j % 8 != 0 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j++
        }
        if (j >= 0 && j <= 63) occupancyAttackSet!![i] = occupancyAttackSet!![i] or (1L shl j)
        j = bitRef - 1
        while (j % 8 != 7 && j % 8 != 0 && j >= 0 && occupancyVariation!![i] and (1L shl j) == 0L) {
            j--
        }
        return j
    }

    @JvmField
    val occupancyMaskRook = longArrayOf(
            0x101010101017eL, 0x202020202027cL, 0x404040404047aL, 0x8080808080876L, 0x1010101010106eL, 0x2020202020205eL, 0x4040404040403eL, 0x8080808080807eL, 0x1010101017e00L, 0x2020202027c00L, 0x4040404047a00L, 0x8080808087600L, 0x10101010106e00L, 0x20202020205e00L, 0x40404040403e00L, 0x80808080807e00L, 0x10101017e0100L, 0x20202027c0200L, 0x40404047a0400L, 0x8080808760800L, 0x101010106e1000L, 0x202020205e2000L, 0x404040403e4000L, 0x808080807e8000L, 0x101017e010100L, 0x202027c020200L, 0x404047a040400L, 0x8080876080800L, 0x1010106e101000L, 0x2020205e202000L, 0x4040403e404000L, 0x8080807e808000L, 0x1017e01010100L, 0x2027c02020200L, 0x4047a04040400L, 0x8087608080800L, 0x10106e10101000L, 0x20205e20202000L, 0x40403e40404000L, 0x80807e80808000L, 0x17e0101010100L, 0x27c0202020200L, 0x47a0404040400L, 0x8760808080800L, 0x106e1010101000L, 0x205e2020202000L, 0x403e4040404000L, 0x807e8080808000L, 0x7e010101010100L, 0x7c020202020200L, 0x7a040404040400L, 0x76080808080800L, 0x6e101010101000L, 0x5e202020202000L, 0x3e404040404000L, 0x7e808080808000L, 0x7e01010101010100L, 0x7c02020202020200L, 0x7a04040404040400L, 0x7608080808080800L, 0x6e10101010101000L, 0x5e20202020202000L, 0x3e40404040404000L, 0x7e80808080808000L
    )
    @JvmField
    val occupancyMaskBishop = longArrayOf(
            0x40201008040200L, 0x402010080400L, 0x4020100a00L, 0x40221400L, 0x2442800L, 0x204085000L, 0x20408102000L, 0x2040810204000L, 0x20100804020000L, 0x40201008040000L, 0x4020100a0000L, 0x4022140000L, 0x244280000L, 0x20408500000L, 0x2040810200000L, 0x4081020400000L, 0x10080402000200L, 0x20100804000400L, 0x4020100a000a00L, 0x402214001400L, 0x24428002800L, 0x2040850005000L, 0x4081020002000L, 0x8102040004000L, 0x8040200020400L, 0x10080400040800L, 0x20100a000a1000L, 0x40221400142200L, 0x2442800284400L, 0x4085000500800L, 0x8102000201000L, 0x10204000402000L, 0x4020002040800L, 0x8040004081000L, 0x100a000a102000L, 0x22140014224000L, 0x44280028440200L, 0x8500050080400L, 0x10200020100800L, 0x20400040201000L, 0x2000204081000L, 0x4000408102000L, 0xa000a10204000L, 0x14001422400000L, 0x28002844020000L, 0x50005008040200L, 0x20002010080400L, 0x40004020100800L, 0x20408102000L, 0x40810204000L, 0xa1020400000L, 0x142240000000L, 0x284402000000L, 0x500804020000L, 0x201008040200L, 0x402010080400L, 0x2040810204000L, 0x4081020400000L, 0xa102040000000L, 0x14224000000000L, 0x28440200000000L, 0x50080402000000L, 0x20100804020000L, 0x40201008040200L
    )
    @JvmField
    val magicNumberRook = longArrayOf(
            -0x5e7ffddf7fbffdd0L, 0x40100040022000L, 0x80088020001002L, 0x80080280841000L, 0x4200042010460008L, 0x4800a0003040080L, 0x400110082041008L, 0x8000a041000880L, 0x10138001a080c010L, 0x804008200480L, 0x10011012000c0L, 0x22004128102200L, 0x200081201200cL, 0x202a001048460004L, 0x81000100420004L, 0x4000800380004500L, 0x208002904001L, 0x90004040026008L, 0x208808010002001L, 0x2002020020704940L, -0x7fb7fefff7eefffbL, 0x6820808004002200L, 0xa80040008023011L, 0xb1460000811044L, 0x4204400080008ea0L, -0x4ffdbffe7fdffe7cL, 0x2020200080100380L, 0x10080080100080L, 0x2204080080800400L, 0xa40080360080L, 0x2040604002810b1L, 0x8c218600004104L, -0x7e7fffbfffbfe000L, 0x488c402000401001L, 0x4018a00080801004L, 0x1230002105001008L, -0x76fb7ff7ff7ffc00L, 0x42000c42003810L, 0x8408110400b012L, 0x18086182000401L, 0x2240088020c28000L, 0x1001201040c004L, 0xa02008010420020L, 0x10003009010060L, 0x4008008008014L, 0x80020004008080L, 0x282020001008080L, 0x50000181204a0004L, 0x102042111804200L, 0x40002010004001c0L, 0x19220045508200L, 0x20030010060a900L, 0x8018028040080L, 0x88240002008080L, 0x10301802830400L, 0x332a4081140200L, 0x8080010a601241L, 0x1008010400021L, 0x4082001007241L, 0x211009001200509L, -0x7feaffeffdbbe7ffL, 0x801000804000603L, 0xc0900220024a401L, 0x1000200608243L
    )
    @JvmField
    val magicNumberBishop = longArrayOf(
            0x2910054208004104L, 0x2100630a7020180L, 0x5822022042000000L, 0x2ca804a100200020L, 0x204042200000900L, 0x2002121024000002L, -0x7fbfbefbdfdfff18L, -0x7ed5fdfdfafef7c0L, -0x7ffae7ee7bf7ffb8L, 0x1001c20208010101L, 0x1001080204002100L, 0x1810080489021800L, 0x62040420010a00L, 0x5028043004300020L, -0x3ff7f5bbfd9faffeL, 0x8a00a0104220200L, 0x940000410821212L, 0x1808024a280210L, 0x40c0422080a0598L, 0x4228020082004050L, 0x200800400e00100L, 0x20b001230021040L, 0x90a0201900c00L, 0x4940120a0a0108L, 0x20208050a42180L, 0x1004804b280200L, 0x2048020024040010L, 0x102c04004010200L, 0x20408204c002010L, 0x2411100020080c1L, 0x102a008084042100L, 0x941030000a09846L, 0x244100800400200L, 0x4000901010080696L, 0x280404180020L, 0x800042008240100L, 0x220008400088020L, 0x4020182000904c9L, 0x23010400020600L, 0x41040020110302L, 0x412101004020818L, -0x7fddf7f5f6bfbdf8L, 0x1401210240484800L, 0x22244208010080L, 0x1105040104000210L, 0x2040088800c40081L, -0x7e7b7efdadfffc00L, 0x4004610041002200L, 0x40201a444400810L, 0x4611010802020008L, -0x7ffff4fbfefbfbfeL, 0x20004821880a00L, -0x7dffffdfddbbff00L, 0x9431801010068L, 0x1040c20806108040L, 0x804901403022a40L, 0x2400202602104000L, 0x208520209440204L, 0x40c000022013020L, 0x2000104000420600L, 0x400000260142410L, 0x800633408100500L, 0x2404080a1410L, 0x138200122002900L
    )
    @JvmField
    val magicNumberShiftsRook = intArrayOf(
            52, 53, 53, 53, 53, 53, 53, 52, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 52, 53, 53, 53, 53, 53, 53, 52
    )
    @JvmField
    val magicNumberShiftsBishop = intArrayOf(
            58, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 58
    )



    init {
        magicMovesRook = Array(64) { LongArray(4096) }
        magicMovesBishop = Array(64) { LongArray(1024) }
        occupancyVariation = LongArray(4096)
        occupancyAttackSet = LongArray(4096)
        generateOccupancyVariationsAndDatabase(false)
        generateOccupancyVariationsAndDatabase(true)
        occupancyVariation = null
        occupancyAttackSet = null
        rookVars = MagicVars(magicMovesRook, occupancyMaskRook, magicNumberRook, magicNumberShiftsRook)
        bishopVars = MagicVars(magicMovesBishop, occupancyMaskBishop, magicNumberBishop, magicNumberShiftsBishop)
    }
}