internal class HelloWorld {
    external fun printy()

    companion object {
        init {
            System.loadLibrary("hello")
        }
    }
}