package khome

import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.HttpMethod
import khome.core.Credentials
import khome.core.clients.WebSocketClient
import khome.core.mapping.ObjectMapper

internal class HassClient(
    private val credentials: Credentials,
    private val httpClient: WebSocketClient = WebSocketClient(HttpClient(CIO).config { install(WebSockets) }),
    private val objectMapper: ObjectMapper
) {
    private val logger = Kermit()

    private val method = HttpMethod.Get
    private val path = "/api/websocket"

    suspend fun startSession(block: suspend HassSession.() -> Unit) =
        startSessionCatching(block)

    private suspend fun startSessionCatching(block: suspend HassSession.() -> Unit) =
        try {
            when (credentials.isSecure) {
                true -> httpClient.secureWebSocket(
                    method = method,
                    host = credentials.host,
                    port = credentials.port,
                    path = path,
                    block = { block(HassSession(this, objectMapper)) }
                )
                false -> httpClient.webSocket(
                    method = method,
                    host = credentials.host,
                    port = credentials.port,
                    path = path,
                    block = { block(HassSession(this, objectMapper)) }
                )
            }
//        } catch (exception: ConnectException) {
//            logger.e(exception) { "Could not establish a connection to your homeassistant instance." }
        } catch (exception: RuntimeException) {
            logger.e(exception) { "Could not start khome due to: ${exception.message}" }
        }
}
