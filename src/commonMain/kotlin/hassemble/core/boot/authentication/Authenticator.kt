package hassemble.core.boot.authentication

import co.touchlab.kermit.Kermit
import hassemble.WebSocketSession
import hassemble.core.Credentials

internal class Authenticator(
    private val session: WebSocketSession,
    credentials: Credentials
) {

    suspend fun authenticate() =
        consumeInitialResponse()
            .let { initialResponse ->
                when (initialResponse.type) {
                    "auth_required" -> {
                        logger.i { "Authentication required!" }
                        sendAuthenticationMessage()
                        consumeAuthenticationResponse()
                            .let { authResponse ->
                                when (authResponse.type) {
                                    "auth_ok" -> logger.i { "Authenticated successfully to homeassistant version ${authResponse.haVersion}" }
                                    "auth_invalid" -> logger.e { "Authentication failed. Server send: ${authResponse.message}" }
                                }
                            }
                    }
                    "auth_ok" -> logger.i { "Authenticated successfully (no authentication needed)." }
                }
            }

    private val logger = Kermit()
    private val authRequest =
        AuthRequest(accessToken = credentials.accessToken)

    private suspend fun consumeInitialResponse() =
        session.consumeSingleMessage<InitialResponse>()

    private suspend fun consumeAuthenticationResponse() =
        session.consumeSingleMessage<AuthResponse>()

    private suspend fun sendAuthenticationMessage() =
        try {
            session.callWebSocketApi(authRequest).also { logger.i { "Sending authentication message." } }
        } catch (e: Exception) {
            logger.e(e) { "Could not send authentication message" }
        }
}
