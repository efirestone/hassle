package khome.concurrent

import kotlinx.atomicfu.atomic

class AtomicBooleanWrapper(initialValue: Boolean) : AtomicBoolean {
    private val value = atomic(initialValue)

    override fun get(): Boolean = value.value
    override fun set(newValue: Boolean) {
        value.getAndSet(newValue)
    }
}

actual object AtomicBooleanFactory {
    actual fun create(initialValue: Boolean): AtomicBoolean {
        return AtomicBooleanWrapper(initialValue)
    }
}
