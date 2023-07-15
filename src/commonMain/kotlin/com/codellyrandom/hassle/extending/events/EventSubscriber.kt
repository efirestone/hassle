package com.codellyrandom.hassle.extending.events

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.events.EventHandlerFunction
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EventType
import kotlin.reflect.typeOf

inline fun <reified ED> HomeAssistantApiClient.attachEventHandler(
    eventType: EventType,
    noinline eventHandler: EventHandlerFunction<ED>,
): Switchable = attachEventHandler(eventType, typeOf<ED>(), eventHandler)
