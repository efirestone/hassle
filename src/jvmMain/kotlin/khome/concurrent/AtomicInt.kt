package khome.concurrent

import java.util.concurrent.atomic.AtomicInteger

class AtomicIntWrapper(initialValue: Int) : AtomicInt {
    private val value = AtomicInteger(initialValue)

    override fun addAndGet(delta: Int): Int = value.addAndGet(delta)
    override fun get() = value.get()
    override fun getAndIncrement(): Int = value.getAndIncrement()
    override fun incrementAndGet(): Int = value.incrementAndGet()
}

actual object AtomicIntFactory {
    actual fun create(initial: Int): AtomicInt {
        return AtomicIntWrapper(initial)
    }
}
