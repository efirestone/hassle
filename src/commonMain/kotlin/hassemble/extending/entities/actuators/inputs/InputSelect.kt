package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.SelectOptionServiceCommand
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputSelect = Actuator<InputSelectState, InputSelectAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputSelect(objectId: ObjectId): InputSelect =
    Actuator(
        EntityId.fromPair("input_select".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SelectOptionServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputSelectAttributes(
    val options: List<Option>,
    val editable: Boolean,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName? = null,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

@Serializable
data class InputSelectState(override val value: Option) : State<Option>
