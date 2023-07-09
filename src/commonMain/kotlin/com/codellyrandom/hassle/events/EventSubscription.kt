package com.codellyrandom.hassle.events

import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.errorHandling.EventHandlerExceptionHandler
import com.codellyrandom.hassle.observability.Switchable
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

internal class EventSubscription<ED>(
    private val connection: HomeAssistantApiClientImpl,
    private val mapper: ObjectMapper,
    private val eventDataType: KClass<*>,
) {
    private val eventHandler: MutableList<EventHandler<ED>> = mutableListOf()

    fun attachEventHandler(handler: EventHandlerFunction<ED>): Switchable =
        EventHandlerImpl(
            handler,
            EventHandlerExceptionHandler(connection.eventHandlerExceptionHandlerFunction),
        ).also { eventHandler.add(it) }

    @Suppress("UNCHECKED_CAST")
    fun invokeEventHandler(eventData: JsonElement) {
        val mappedEventData: ED = mapper.fromJson(eventData, eventDataType) as ED
        eventHandler.forEach { handler -> handler.handle(mappedEventData) }
    }
}
