package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.engine.core.FEN_START_POS
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.model.util.FenUtils.invertFen
import junit.framework.TestCase
import org.junit.Test

class EvaluateKtTest : TestCase() {

    private fun assertTradePawnBonusScore(fen: String, score: Int, flipped: Boolean = true) {
        val ecb = EngineBoard(getBoardModel(fen))
        var bitboards = BitboardData(ecb)
        var materialDifference = materialDifferenceEval(MaterialValues(bitboards))
        assertEquals(score, tradePawnBonusWhenMoreMaterial(bitboards, materialDifference))
        if (!flipped) {
            assertTradePawnBonusScore(invertFen(fen), -score, true)
        }
    }

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
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP1PPP/1NBQK3 w KQ - 0 1", 167)
        assertTradePawnBonusScore("rnbqkbnr/5pppp/8/8/8/8/PPPP2PP/1NBQK3 w KQ - 0 1", 251)
        assertTradePawnBonusScore("rnbqkbnr/6pppp/8/8/8/8/PPPP3P/1NBQK3 w KQ - 0 1", 334)
        assertTradePawnBonusScore("rnbqkbnr/7pppp/8/8/8/8/PPPP4/1NBQ3- w KQ - 0 1", 418)
        assertTradePawnBonusScore("rnbqkbnr/8/8/8/8/8/PPPP4/1NBQ3- w KQ - 0 1", 471)

        // the more black is winning by in material, the higher the bonus
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP1PPP/4K3 w KQ - 0 1", 363)
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP2PP/3QK3 w KQ - 0 1", 255)
        assertTradePawnBonusScore("rnbqkbnr/4pppp/8/8/8/8/PPPP3P/2BQK3 w KQ - 0 1", 226)
    }
}