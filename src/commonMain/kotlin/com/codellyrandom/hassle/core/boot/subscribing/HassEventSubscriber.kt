package com.codellyrandom.hassle.core.boot.subscribing

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import com.codellyrandom.hassle.EventHandlerByEventType
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.SubscribeEventsCommand
import com.codellyrandom.hassle.core.ResultResponse

internal class HassEventSubscriber(
    private val session: WebSocketSession,
    private val subscriptions: EventHandlerByEventType,
    private val apiClient: HomeAssistantApiClientImpl
) {
    private val logger = Logger(config = LoggerConfig.default)

    suspend fun subscribe() {
        subscriptions.forEach { entry ->
            SubscribeEventsCommand(eventType = entry.key).also { command -> apiClient.send(command) }
            session.consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.i { "Subscribed to event: ${entry.key}" } }
        }
    }
}
