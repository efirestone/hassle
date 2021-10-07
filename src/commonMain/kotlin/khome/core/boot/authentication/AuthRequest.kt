package khome.core.boot.authentication

import khome.core.MessageInterface
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthRequest(
    val type: String = "auth",
    @SerialName("access_token")
    val accessToken: String
) : MessageInterface
