package khome.core.boot.authentication

import co.touchlab.kermit.Kermit
import khome.KhomeSession
import khome.core.Configuration

internal class AuthenticatorImpl(
    private val khomeSession: KhomeSession,
    configuration: Configuration
) : Authenticator {

    override suspend fun authenticate() =
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
        AuthRequest(accessToken = configuration.accessToken)

    private suspend fun consumeInitialResponse() =
        khomeSession.consumeSingleMessage<InitialResponse>()

    private suspend fun consumeAuthenticationResponse() =
        khomeSession.consumeSingleMessage<AuthResponse>()

    private suspend fun sendAuthenticationMessage() =
        try {
            khomeSession.callWebSocketApi(authRequest).also { logger.i { "Sending authentication message." } }
        } catch (e: Exception) {
            logger.e(e) { "Could not send authentication message" }
        }
}

interface Authenticator {
    suspend fun authenticate()
}
