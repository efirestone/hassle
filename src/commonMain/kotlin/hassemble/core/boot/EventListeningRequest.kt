package hassemble.core.boot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventListeningRequest(
    val id: Int,
    val type: String = "subscribe_events",
    @SerialName("event_type")
    val eventType: String
)
