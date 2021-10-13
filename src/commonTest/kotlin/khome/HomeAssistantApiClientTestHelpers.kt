package khome

import khome.core.Credentials
import kotlinx.coroutines.*

internal fun withConnection(block: HomeAssistantApiClientImpl.() -> Unit) = runBlocking {
    val credentials = Credentials(
        "Test Server",
        host = "localhost",
        port = 8080,
        "access_token",
        isSecure = false
    )
    val connection = HomeAssistantApiClientImpl(credentials, this)
    block(connection)
}
