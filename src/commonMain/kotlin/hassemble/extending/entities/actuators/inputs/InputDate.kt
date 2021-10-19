package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.communicating.SetDateTimeServiceCommand
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputDate = Actuator<InputDateState, InputDateAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputDate(objectId: ObjectId): InputDate =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetDateTimeServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputDateState(override val value: LocalDate) : State<LocalDate>

@Serializable
data class InputDateAttributes(
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
