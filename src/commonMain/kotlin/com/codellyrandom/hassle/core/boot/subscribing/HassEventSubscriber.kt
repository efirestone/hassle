package com.codellyrandom.hassle.core.boot.subscribing

import co.touchlab.kermit.Kermit
import com.codellyrandom.hassle.EventHandlerByEventType
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.SubscribeEventCommand
import com.codellyrandom.hassle.core.ResultResponse

internal class HassEventSubscriber(
    private val session: WebSocketSession,
    private val subscriptions: EventHandlerByEventType,
    private val apiClient: HomeAssistantApiClientImpl
) {
    private val logger = Kermit()

    suspend fun subscribe() {
        subscriptions.forEach { entry ->
            SubscribeEventCommand(eventType = entry.key).also { command -> apiClient.send(command) }
            session.consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.i { "Subscribed to event: ${entry.key}" } }
        }
    }
}
