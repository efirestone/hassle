package khome.communicating

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class CommandType {
    @SerialName("call_service")
    CALL_SERVICE,

    @SerialName("subscribe_events")
    SUBSCRIBE_EVENTS,

    @SerialName("get_services")
    GET_SERVICES,

    @SerialName("get_states")
    GET_STATES
}
