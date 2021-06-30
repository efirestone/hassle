package khome.concurrent

import kotlinx.atomicfu.atomic

class AtomicIntWrapper(initialValue: Int): AtomicInt {
    private val value = atomic(initialValue)

    override fun addAndGet(delta: Int): Int = value.addAndGet(delta)
    override fun incrementAndGet(): Int = value.incrementAndGet()
}

actual object AtomicIntFactory {
    actual fun create(initial: Int): AtomicInt {
        return AtomicIntWrapper(initial)
    }
}
