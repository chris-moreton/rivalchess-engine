internal class RivalCLibrary {
    external fun linearScaleC(x: Int, min: Int, max: Int, a: Int, b: Int): Int
    external fun getHighestScoringMoveFromArrayC(theseMoves: IntArray): Int

    companion object {
        init {
            System.loadLibrary("rival")
        }
    }
}