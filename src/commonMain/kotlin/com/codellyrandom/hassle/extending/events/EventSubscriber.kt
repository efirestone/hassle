package com.codellyrandom.hassle.extending.events

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.events.EventHandlerFunction
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EventType

inline fun <reified ED> HomeAssistantApiClient.attachEventHandler(
    eventType: EventType,
    noinline eventHandler: EventHandlerFunction<ED>
): Switchable = attachEventHandler(eventType, ED::class, eventHandler)
