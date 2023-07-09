package com.codellyrandom.hassle.extending.entities.actuators

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias PositionableCover = Actuator<PositionableCoverState, PositionableCoverAttributes>

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Cover(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>,
): Actuator<S, A> = Actuator(EntityId.fromPair("cover".domain to objectId), serviceCommandResolver)

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
data class PositionableCoverState(
    override val value: PositionableCoverValue,
    val currentPosition: Position? = null,
) : State<PositionableCoverValue>

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

@Serializable
data class PositionableCoverAttributes(
    val working: Working,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
) : Attributes

val PositionableCover.isOpen
    get() = actualState.value == PositionableCoverValue.OPEN

val PositionableCover.isClosed
    get() = actualState.value == PositionableCoverValue.CLOSED

val PositionableCover.isWorking
    get() = attributes.working == Working.YES

suspend fun PositionableCover.open() {
    setDesiredState(PositionableCoverState(PositionableCoverValue.OPEN))
}

suspend fun PositionableCover.close() {
    setDesiredState(PositionableCoverState(PositionableCoverValue.CLOSED))
}

suspend fun PositionableCover.setCoverPosition(position: Position) {
    setDesiredState(PositionableCoverState(PositionableCoverValue.OPEN, position))
}

fun PositionableCover.onStartedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.NO &&
            attributes.working == Working.YES
        ) {
            f(this)
        }
    }

fun PositionableCover.onStoppedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.YES &&
            attributes.working == Working.NO
        ) {
            f(this)
        }
    }

fun PositionableCover.onClosed(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.OPEN to PositionableCoverValue.CLOSED, f)

fun PositionableCover.onOpened(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.CLOSED to PositionableCoverValue.OPEN, f)
