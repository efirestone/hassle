package hassemble.events

import hassemble.errorHandling.EventHandlerExceptionHandler
import hassemble.observability.Switchable
import kotlinx.atomicfu.atomic

typealias EventHandlerFunction<EventData> = (EventData, switchable: Switchable) -> Unit

interface EventHandler<EventData> {
    fun handle(eventData: EventData)
}

internal class EventHandlerImpl<EventData>(
    private val f: EventHandlerFunction<EventData>,
    private val exceptionHandler: EventHandlerExceptionHandler
) : EventHandler<EventData>, Switchable {

    private val enabled = atomic(true)

    override fun enable() { enabled.getAndSet(true) }
    override fun disable() { enabled.getAndSet(false) }
    override fun isEnabled(): Boolean = enabled.value

    override fun handle(eventData: EventData) {
        if (!enabled.value) return
        try {
            f(eventData, this)
        } catch (e: Throwable) {
            exceptionHandler.handleExceptions(e)
        }
    }
}
