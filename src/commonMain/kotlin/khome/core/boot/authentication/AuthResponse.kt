package khome.core.boot.authentication

import khome.core.MessageInterface
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthResponse(
    val type: String,
    val message: String?,
    @SerialName("ha_version")
    val haVersion: String
) : MessageInterface
