package khome

import khome.core.Credentials

fun withConnection(block: HassConnectionImpl.() -> Unit) {
    val credentials = Credentials(
        "Test Server",
        host = "localhost",
        port = 8080,
        "access_token",
        isSecure = false
    )
    val connection = HassConnectionImpl(credentials)
    block(connection)
}
