package khome.concurrent

import kotlinx.atomicfu.atomic

class AtomicIntWrapper(initialValue: Int) : AtomicInt {
    private val value = atomic(initialValue)

    override fun addAndGet(delta: Int) = value.addAndGet(delta)
    override fun get() = value.value
    override fun getAndIncrement() = value.getAndIncrement()
    override fun incrementAndGet() = value.incrementAndGet()
}

actual object AtomicIntFactory {
    actual fun create(initial: Int): AtomicInt {
        return AtomicIntWrapper(initial)
    }
}
