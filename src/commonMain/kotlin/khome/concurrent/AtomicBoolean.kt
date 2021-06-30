package khome.concurrent

interface AtomicBoolean {
    fun get(): Boolean
    fun set(newValue: Boolean)
}

expect object AtomicBooleanFactory {
    fun create(initialValue: Boolean): AtomicBoolean
}
