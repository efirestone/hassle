package khome.core

import khome.values.EntityId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class ResolverResponse(val id: Int, val type: ResponseType)
@Serializable
internal data class StateChangedResponse(val id: Int, val type: ResponseType, val event: StateChangedEventData)

@Serializable
internal data class StateChangedEventData(
    override val eventType: String,
    val data: StateChangedData,
    override val timeFired: Instant,
    override val origin: String
) : EventDtoInterface

@Serializable
internal data class StateChangedData(val entityId: EntityId, val newState: JsonElement)

interface EventDtoInterface {
    val eventType: String
    val timeFired: Instant
    val origin: String
}

@Serializable
internal data class StateResponse(
    val entityId: EntityId,
    val lastChanged: Instant,
    val state: JsonObject,
    val attributes: JsonElement,
    val lastUpdated: Instant
)

internal data class EventResponse(val id: Int, val type: ResponseType, val event: Event)
internal data class Event(
    override val eventType: String,
    val data: JsonElement,
    override val timeFired: Instant,
    override val origin: String
) : EventDtoInterface

@Serializable
internal data class ResultResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResponse? = null,
    val result: JsonObject?
)

@Serializable
internal enum class ResponseType {
    @SerialName("event")
    EVENT,

    @SerialName("result")
    RESULT
}
