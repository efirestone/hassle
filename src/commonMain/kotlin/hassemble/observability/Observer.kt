package hassemble.observability

import hassemble.errorHandling.ObserverExceptionHandler
import kotlinx.atomicfu.atomic

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

    private val enabled = atomic(true)

    override fun enable() { enabled.getAndSet(true) }
    override fun disable() { enabled.getAndSet(false) }
    override fun isEnabled(): Boolean = enabled.value

    override fun update(entity: E) {
        if (!enabled.value) return
        try {
            f(entity, this@ObserverImpl)
        } catch (e: Throwable) {
            exceptionHandler.handleExceptions(e)
        }
    }
}
