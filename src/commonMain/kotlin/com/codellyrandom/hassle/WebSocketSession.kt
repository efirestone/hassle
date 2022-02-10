package com.codellyrandom.hassle

import com.codellyrandom.hassle.core.mapping.ObjectMapper
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

internal class WebSocketSession(
    delegate: DefaultClientWebSocketSession,
    val objectMapper: ObjectMapper
) : ClientWebSocketSession by delegate {
    suspend fun callWebSocketApi(message: String) = send(message)

    suspend inline fun <reified M : Any> callWebSocketApi(message: M) = send(objectMapper.toJson(message))

    suspend inline fun <reified M : Any> consumeSingleMessage(): M = incoming.receive().asObject()
    inline fun <reified M : Any> Frame.asObject(): M = (this as Frame.Text).toObject()
    inline fun <reified M : Any> Frame.Text.toObject(): M = objectMapper.fromJson(readText())
}
