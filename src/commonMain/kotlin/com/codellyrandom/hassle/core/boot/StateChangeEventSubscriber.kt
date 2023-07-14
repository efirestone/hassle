package com.codellyrandom.hassle.core.boot

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.SubscribeEventsCommand
import com.codellyrandom.hassle.core.ResultResponse
import com.codellyrandom.hassle.values.EventType

internal class StateChangeEventSubscriber(
    private val apiClient: HomeAssistantApiClientImpl,
    private val session: WebSocketSession,
) {

    private val logger = Logger(config = LoggerConfig.default)

    suspend fun subscribe() {
        val id = sendEventListenerRequest()
        consumeResultResponse(id).let { resultResponse ->
            when (resultResponse.success) {
                false -> logger.e { "Could not subscribe to state change events" }
                true -> logger.i { "Successfully started listening to state changes" }
            }
        }
    }

    private val subscribeEventsCommand = SubscribeEventsCommand(eventType = EventType("state_changed"))

    private suspend fun sendEventListenerRequest() =
        apiClient.send(subscribeEventsCommand)

    private suspend fun consumeResultResponse(id: Int) =
        session.consumeSingleMessage<ResultResponse>(id)
}
