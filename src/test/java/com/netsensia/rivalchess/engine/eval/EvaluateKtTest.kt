package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.consts.FEN_START_POS
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.model.util.FenUtils.invertFen
import junit.framework.TestCase
import org.junit.Test

@kotlin.ExperimentalUnsignedTypes
class EvaluateKtTest : TestCase() {

    @Test
    fun testTradePawnBonusWhenMoreMaterial() {
        assertTradePawnBonusScore(FEN_START_POS, 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP1PPP/RNBQKBNR w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/RNBQKBNR w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP3P/RNBQKBNR w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/RNBQKBNR w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP1PPP/1NBQK3 w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP2PP/1NBQK3 w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP3P/1NBQK3 w KQ - 0 1", 0)
        assertTradePawnBonusScore("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/1NBQ3- w KQ - 0 1", 0)

        // bonus for white for each missing black pawn as black is winning in material
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP1PPP/1NBQK3 w KQ - 0 1", 168)
        assertTradePawnBonusScore("rnbqkbnr/5pppp/8/8/8/8/PPPP2PP/1NBQK3 w KQ - 0 1", 252)
        assertTradePawnBonusScore("rnbqkbnr/6pppp/8/8/8/8/PPPP3P/1NBQK3 w KQ - 0 1", 336)
        assertTradePawnBonusScore("rnbqkbnr/7pppp/8/8/8/8/PPPP4/1NBQ3- w KQ - 0 1", 420)
        assertTradePawnBonusScore("rnbqkbnr/8/8/8/8/8/PPPP4/1NBQ3- w KQ - 0 1", 474)

        // the more black is winning by in material, the higher the bonus
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP1PPP/4K3 w KQ - 0 1", 365)
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP2PP/3QK3 w KQ - 0 1", 257)
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP3P/2BQK3 w KQ - 0 1", 227)
    }

    @Test
    fun testExactlyOneBitSet() {
        assertFalse(exactlyOneBitSet(0L))
        for (i in 0..63) assertTrue(exactlyOneBitSet(1L shl i))
    }

    private fun assertTradePawnBonusScore(fen: String, score: Int, flipped: Boolean = true) {
        val ecb = EngineBoard(getBoardModel(fen))
        var materialDifference = materialDifferenceEval(ecb)
        assertEquals(score, tradePawnBonusWhenMoreMaterial(ecb, materialDifference))
        if (!flipped) {
            assertTradePawnBonusScore(invertFen(fen), -score, true)
        }
    }
}