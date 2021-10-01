package khome.core.boot.authentication

import co.touchlab.kermit.Kermit
import khome.HassSession
import khome.core.Credentials

internal class Authenticator(
    private val hassSession: HassSession,
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
        hassSession.consumeSingleMessage<InitialResponse>()

    private suspend fun consumeAuthenticationResponse() =
        hassSession.consumeSingleMessage<AuthResponse>()

    private suspend fun sendAuthenticationMessage() =
        try {
            hassSession.callWebSocketApi(authRequest).also { logger.i { "Sending authentication message." } }
        } catch (e: Exception) {
            logger.e(e) { "Could not send authentication message" }
        }
}
