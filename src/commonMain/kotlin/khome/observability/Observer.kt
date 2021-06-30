package khome.observability

import khome.concurrent.AtomicBoolean
import khome.concurrent.AtomicBooleanFactory
import khome.errorHandling.ObserverExceptionHandler

typealias ObserverFunction<E> = E.(observer: Switchable) -> Unit

internal interface Observer<E> {
    fun update(entity: E)
}

/**
 * A Switchable instance
 *
 * Controls the observer function execution
 * Is enabled by default
 */
interface Switchable {
    /**
     * Returns the current state
     * @return Boolean
     */
    fun isEnabled(): Boolean

    /**
     * Enables the observer function execution
     */
    fun enable()

    /**
     * Disables the observer function execution
     */
    fun disable()
}

interface Observable<E> {
    fun attachObserver(observer: ObserverFunction<E>): Switchable
}

internal class ObserverImpl<E>(
    private val f: ObserverFunction<E>,
    private val exceptionHandler: ObserverExceptionHandler
) : Switchable, Observer<E> {

    private val enabled: AtomicBoolean = AtomicBooleanFactory.create(true)

    override fun enable() = enabled.set(true)
    override fun disable() = enabled.set(false)
    override fun isEnabled(): Boolean = enabled.get()

    override fun update(entity: E) {
        if (!enabled.get()) return
        try {
            f(entity, this@ObserverImpl)
        } catch (e: Throwable) {
            exceptionHandler.handleExceptions(e)
        }
    }
}
