package khome.core

import khome.values.EntityId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal data class ResolverResponse(val id: Int, val type: ResponseType) : MessageInterface
internal data class StateChangedResponse(val id: Int, val type: ResponseType, val event: StateChangedEventData) :
    MessageInterface

internal data class StateChangedEventData(
    override val eventType: String,
    val data: StateChangedData,
    override val timeFired: Instant,
    override val origin: String
) : MessageInterface, EventDtoInterface

internal data class StateChangedData(val entityId: EntityId, val newState: JsonElement) :
    MessageInterface

interface EventDtoInterface {
    val eventType: String
    val timeFired: Instant
    val origin: String
}

internal data class StateResponse(
    val entityId: EntityId,
    val lastChanged: Instant,
    val state: Any,
    val attributes: JsonElement,
    val lastUpdated: Instant
)

internal data class EventResponse(val id: Int, val type: ResponseType, val event: Event)
internal data class Event(
    override val eventType: String,
    val data: JsonElement,
    override val timeFired: Instant,
    override val origin: String
) : MessageInterface, EventDtoInterface

internal data class ResultResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val error: ErrorResponse?,
    val result: Any?
) : MessageInterface

@Serializable
internal enum class ResponseType {
    @SerialName("event")
    EVENT,

    @SerialName("result")
    RESULT
}
