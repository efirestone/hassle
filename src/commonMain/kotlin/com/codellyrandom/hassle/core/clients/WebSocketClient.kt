package com.codellyrandom.hassle.core.clients

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod

class WebSocketClient(delegate: HttpClient) {
    private val client = delegate

    suspend fun secureWebSocket(
        method: HttpMethod,
        host: String,
        port: Int,
        path: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) =
        client.wss(
            method = method,
            host = host,
            port = port,
            path = path,
            block = block
        )

    suspend fun webSocket(
        method: HttpMethod,
        host: String,
        port: Int,
        path: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) =
        client.ws(
            method = method,
            host = host,
            port = port,
            path = path,
            block = block
        )
}
