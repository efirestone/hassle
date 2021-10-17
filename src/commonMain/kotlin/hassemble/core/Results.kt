package hassemble.core

import hassemble.values.EntityId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class ResolverResponse(
    // The ID may be null if the command was (incorrectly) sent with a null ID.
    val id: Int?,
    val type: ResponseType
)

@Serializable
internal data class StateChangedResponse(
    val id: Int,
    val type: ResponseType,
    val event: StateChangedEventData
)

@Serializable
internal data class StateChangedEventData(
    @SerialName("event_type")
    override val eventType: String,
    val data: StateChangedData,
    @SerialName("time_fired")
    override val timeFired: Instant,
    override val origin: String
) : EventDtoInterface

@Serializable
internal data class StateChangedData(
    @SerialName("entity_id")
    val entityId: EntityId,
    @SerialName("new_state")
    val newState: JsonElement
)

interface EventDtoInterface {
    val eventType: String
    val timeFired: Instant
    val origin: String
}

@Serializable
internal data class StateResponse(
    @SerialName("entity_id")
    val entityId: EntityId,
    @SerialName("last_changed")
    val lastChanged: Instant,
    val state: JsonObject,
    val attributes: JsonElement,
    @SerialName("last_updated")
    val lastUpdated: Instant
)

@Serializable
internal data class EventResponse(
    val id: Int,
    val type: ResponseType,
    val event: Event
)

@Serializable
internal data class Event(
    @SerialName("event_type")
    override val eventType: String,
    val data: JsonElement,
    @SerialName("time_fired")
    override val timeFired: Instant,
    override val origin: String
) : EventDtoInterface

@Serializable
internal data class ResultResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResponse? = null,
    val result: JsonObject? = null
)

@Serializable
internal enum class ResponseType {
    @SerialName("event")
    EVENT,

    @SerialName("result")
    RESULT
}
