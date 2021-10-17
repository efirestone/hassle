package hassemble.extending.events

import hassemble.HomeAssistantApiClient
import hassemble.events.EventHandlerFunction
import hassemble.observability.Switchable
import hassemble.values.EventType

inline fun <reified ED> HomeAssistantApiClient.attachEventHandler(
    eventType: EventType,
    noinline eventHandler: EventHandlerFunction<ED>
): Switchable = attachEventHandler(eventType, ED::class, eventHandler)
