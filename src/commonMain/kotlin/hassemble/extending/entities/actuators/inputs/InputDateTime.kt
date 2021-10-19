package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.communicating.SetDateTimeServiceCommand
import hassemble.core.mapping.serializers.default.LocalDateTimeSerializer
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputDateTime = Actuator<InputDateTimeState, InputDateTimeAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputDateTime(objectId: ObjectId): InputDateTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetDateTimeServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputDateTimeState(
    @Serializable(LocalDateTimeSerializer::class)
    override val value: LocalDateTime
) : State<LocalDateTime>

@Serializable
data class InputDateTimeAttributes(
    val editable: Boolean,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes
