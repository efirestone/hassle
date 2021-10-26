package com.codellyrandom.hassle.core.boot

import co.touchlab.kermit.Kermit
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.SubscribeEventsCommand
import com.codellyrandom.hassle.core.ResultResponse
import com.codellyrandom.hassle.values.EventType

internal class StateChangeEventSubscriber(
    private val apiClient: HomeAssistantApiClientImpl,
    private val session: WebSocketSession
) {

    private val logger = Kermit()

    suspend fun subscribe() {
        sendEventListenerRequest()
        consumeResultResponse().let { resultResponse ->
            when (resultResponse.success) {
                false -> logger.e { "Could not subscribe to state change events" }
                true -> logger.i { "Successfully started listening to state changes" }
            }
        }
    }

    private val subscribeEventsCommand = SubscribeEventsCommand(eventType = EventType("state_changed"))

    private suspend fun sendEventListenerRequest() =
        apiClient.send(subscribeEventsCommand)

    private suspend fun consumeResultResponse() =
        session.consumeSingleMessage<ResultResponse>()
}
