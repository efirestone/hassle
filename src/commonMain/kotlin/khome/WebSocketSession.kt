package khome

import co.touchlab.kermit.Kermit
import io.ktor.client.features.websocket.ClientWebSocketSession
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import khome.core.mapping.ObjectMapper

internal class WebSocketSession(
    delegate: DefaultClientWebSocketSession,
    val objectMapper: ObjectMapper
) : ClientWebSocketSession by delegate {

    private val logger = Kermit()
    suspend fun callWebSocketApi(message: String) =
        send(message).also { logger.d { "Called hass api with message: $message" } }

    suspend inline fun <reified M : Any> callWebSocketApi(message: M) =
        send(objectMapper.toJson(message))
            .also { logger.d { "Called hass api with message: ${objectMapper.toJson(message)}" } }

    suspend inline fun <reified M : Any> consumeSingleMessage(): M = incoming.receive().asObject()
    inline fun <reified M : Any> Frame.asObject(): M = (this as Frame.Text).toObject()
    inline fun <reified M : Any> Frame.Text.toObject(): M = objectMapper.fromJson(readText())
}
