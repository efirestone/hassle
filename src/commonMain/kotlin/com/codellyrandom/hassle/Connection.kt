package com.codellyrandom.hassle

import com.codellyrandom.hassle.core.Credentials
import com.codellyrandom.hassle.core.clients.WebSocketClient
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class Connection(
    private val credentials: Credentials,
    private val coroutineScope: CoroutineScope,
    private val objectMapper: ObjectMapper,
    private val exceptionHandler: (Throwable) -> Unit,
    private val httpClient: WebSocketClient = WebSocketClient(HttpClient().config { install(WebSockets) })
) {
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
            } catch (exception: Throwable) {
                exceptionHandler(exception)
            }
        }
    }
}
