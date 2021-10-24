package com.codellyrandom.hassle.communicating

import co.touchlab.kermit.Kermit
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import kotlinx.atomicfu.atomic

internal val CALLER_ID = atomic(0)

internal class HassApiClientImpl(
    private val session: WebSocketSession,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient
) : HassApiClient {
    private val logger = Kermit()

    override suspend fun send(command: Command) {
        command.id = CALLER_ID.getAndIncrement() // has to be called within single thread to prevent race conditions
        objectMapper.toJson(command).let { serializedCommand ->
            session.callWebSocketApi(serializedCommand)
                .also { logger.i { "Called hass api with message: $serializedCommand" } }
        }
    }

    override suspend fun emitEvent(eventType: String, eventData: Any?) {
        httpClient.post<HttpResponse> {
            url { encodedPath = "/api/events/$eventType" }
            body = eventData ?: EmptyContent
        }
    }
}

internal interface HassApiClient {
    // Tell a Home Assistant service to perform a command.
    suspend fun send(command: Command)

    // Emit an event, such as a sensor change event.
    suspend fun emitEvent(eventType: String, eventData: Any?)
}
