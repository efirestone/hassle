package khome

import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.HttpMethod
import khome.core.Credentials
import khome.core.clients.WebSocketClient
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class Connection(
    private val credentials: Credentials,
    private val coroutineScope: CoroutineScope,
    private val objectMapper: ObjectMapper,
    private val httpClient: WebSocketClient = WebSocketClient(HttpClient(CIO).config { install(WebSockets) })
) {
    private val logger = Kermit()

    private val method = HttpMethod.Get
    private val path = "/api/websocket"

    fun connect(block: suspend WebSocketSession.() -> Unit) = coroutineScope.connect(block)

    private fun CoroutineScope.connect(block: suspend WebSocketSession.() -> Unit) {
        launch {
            try {
                when (credentials.isSecure) {
                    true -> httpClient.secureWebSocket(
                        method = method,
                        host = credentials.host,
                        port = credentials.port,
                        path = path,
                        block = {
                            block(WebSocketSession(this, objectMapper))
                        }
                    )
                    false -> httpClient.webSocket(
                        method = method,
                        host = credentials.host,
                        port = credentials.port,
                        path = path,
                        block = {
                            block(WebSocketSession(this, objectMapper))
                        }
                    )
                }
//        } catch (exception: ConnectException) {
//            logger.e(exception) { "Could not establish a connection to your Home Assistant instance." }
            } catch (exception: RuntimeException) {
                logger.e(exception) { "Could not start khome due to: ${exception.message}" }
            }
        }
    }
}
