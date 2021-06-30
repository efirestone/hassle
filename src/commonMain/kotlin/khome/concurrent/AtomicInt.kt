package khome.concurrent

interface AtomicInt {
    fun addAndGet(delta: Int): Int
    fun incrementAndGet(): Int
}

expect object AtomicIntFactory {
    fun create(initial: Int): AtomicInt
}
