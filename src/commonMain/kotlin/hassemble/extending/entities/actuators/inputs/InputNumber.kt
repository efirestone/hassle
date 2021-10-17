package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.communicating.SetValueServiceCommand
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputNumber = Actuator<InputNumberState, InputNumberAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputNumber(objectId: ObjectId): InputNumber =
    Actuator(
        EntityId.fromPair("input_number".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetValueServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputNumberState(override val value: Double) : State<Double>

@Serializable
data class InputNumberAttributes(
    val initial: Initial,
    val editable: Boolean,
    val min: Min,
    val max: Max,
    val step: Step,
    val mode: Mode,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes
