package khome.core.boot.servicestore

import kotlinx.serialization.Serializable

@Serializable
internal data class ServicesRequest(
    val id: Int,
    val type: String = "get_services"
)
