package com.codellyrandom.hassle.extending.entities.actuators

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.CloseCoverServiceCommand
import com.codellyrandom.hassle.communicating.OpenCoverServiceCommand
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.SetCoverPositionServiceCommand
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias PositionableCover = Actuator<PositionableCoverState, PositionableCoverSettableState>

@Suppress("FunctionName")
internal fun HomeAssistantApiClient.Cover(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<PositionableCoverSettableState>,
): PositionableCover = Actuator(EntityId.fromPair("cover".domain to objectId), serviceCommandResolver)

fun HomeAssistantApiClient.PositionableCover(objectId: ObjectId): PositionableCover =
    Cover(
        objectId,
        ServiceCommandResolver { entityId, state ->
            when (state.value) {
                PositionableCoverValue.OPEN -> state.currentPosition?.let { position ->
                    SetCoverPositionServiceCommand(entityId, position)
                } ?: OpenCoverServiceCommand(entityId)

                PositionableCoverValue.CLOSED -> CloseCoverServiceCommand(entityId)
            }
        },
    )

@Serializable
class PositionableCoverState(
    override val value: PositionableCoverValue,
    @SerialName("current_position")
    val currentPosition: Position? = null,

    val working: Working,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
) : State<PositionableCoverValue>

data class PositionableCoverSettableState(
    val value: PositionableCoverValue,
    val currentPosition: Position? = null,
)

@Serializable
enum class PositionableCoverValue {
    @SerialName("open")
    OPEN,

    @SerialName("closed")
    CLOSED,
}

@Serializable
enum class Working {
    @SerialName("Yes")
    YES,

    @SerialName("No")
    NO,
}

val PositionableCover.isOpen
    get() = state.value == PositionableCoverValue.OPEN

val PositionableCover.isClosed
    get() = state.value == PositionableCoverValue.CLOSED

val PositionableCover.isWorking
    get() = state.working == Working.YES

suspend fun PositionableCover.open() {
    setDesiredState(PositionableCoverSettableState(PositionableCoverValue.OPEN))
}

suspend fun PositionableCover.close() {
    setDesiredState(PositionableCoverSettableState(PositionableCoverValue.CLOSED))
}

suspend fun PositionableCover.setCoverPosition(position: Position) {
    setDesiredState(PositionableCoverSettableState(PositionableCoverValue.OPEN, position))
}

fun PositionableCover.onStartedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].working == Working.NO &&
            state.working == Working.YES
        ) {
            f(this)
        }
    }

fun PositionableCover.onStoppedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].working == Working.YES &&
            state.working == Working.NO
        ) {
            f(this)
        }
    }

fun PositionableCover.onClosed(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.OPEN to PositionableCoverValue.CLOSED, f)

fun PositionableCover.onOpened(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.CLOSED to PositionableCoverValue.OPEN, f)
