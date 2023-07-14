package com.codellyrandom.hassle

import com.codellyrandom.hassle.core.mapping.ObjectMapper
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class WebSocketSession(
    delegate: DefaultClientWebSocketSession,
    val objectMapper: ObjectMapper,
) : ClientWebSocketSession by delegate {

    suspend fun callWebSocketApi(message: String) = send(message)

    suspend inline fun <reified M : Any> callWebSocketApi(message: M) = send(objectMapper.toJson(message))

    suspend inline fun <reified M : Any> consumeInitialMessage(): M = incoming.receive().asObject()

    suspend inline fun <reified M : Any> consumeSingleMessage(id: Int): M = incoming.receiveAsFlow()
        .first {
            val commandId = (it as? Frame.Text)
                ?.let { textFrame -> Json.parseToJsonElement(textFrame.readText()) }
                ?.let { json -> json.jsonObject["id"]?.jsonPrimitive?.intOrNull }
            return@first commandId == id
        }.let {
            (it as Frame.Text).toObject()
        }

    inline fun <reified M : Any> Frame.asObject(): M = (this as Frame.Text).toObject()
    inline fun <reified M : Any> Frame.Text.toObject(): M = objectMapper.fromJson(readText())
}
