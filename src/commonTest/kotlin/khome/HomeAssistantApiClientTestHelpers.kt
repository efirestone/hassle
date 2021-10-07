package khome

import khome.core.Credentials

fun withConnection(block: HomeAssistantApiClientImpl.() -> Unit) {
    val credentials = Credentials(
        "Test Server",
        host = "localhost",
        port = 8080,
        "access_token",
        isSecure = false
    )
    val connection = HomeAssistantApiClientImpl(credentials)
    block(connection)
}
