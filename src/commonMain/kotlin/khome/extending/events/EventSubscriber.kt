package khome.extending.events

import khome.HomeAssistantApiClient
import khome.events.EventHandlerFunction
import khome.observability.Switchable
import khome.values.EventType

inline fun <reified ED> HomeAssistantApiClient.attachEventHandler(
    eventType: EventType,
    noinline eventHandler: EventHandlerFunction<ED>
): Switchable = attachEventHandler(eventType, ED::class, eventHandler)
