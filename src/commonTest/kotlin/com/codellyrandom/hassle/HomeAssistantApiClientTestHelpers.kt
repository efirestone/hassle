package com.codellyrandom.hassle

import com.codellyrandom.hassle.core.Credentials
import kotlinx.coroutines.runBlocking

internal fun withConnection(block: HomeAssistantApiClientImpl.() -> Unit) = runBlocking {
    val credentials = Credentials(
        "Test Server",
        host = "localhost",
        port = 8080,
        "access_token",
        isSecure = false,
    )
    val connection = HomeAssistantApiClientImpl(credentials, this)
    block(connection)
}
