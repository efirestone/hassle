package khome.concurrent

class AtomicBooleanWrapper(initialValue: Boolean): AtomicBoolean {
    private val value = java.util.concurrent.atomic.AtomicBoolean(initialValue)

    override fun get(): Boolean = value.get()
    override fun set(newValue: Boolean) = value.set(newValue)
}

actual object AtomicBooleanFactory {
    actual fun create(initialValue: Boolean): AtomicBoolean {
        return AtomicBooleanWrapper(initialValue)
    }
}
