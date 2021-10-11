package khome.extending.entities.actuators.inputs

import khome.HomeAssistantApiClient
import khome.communicating.DesiredServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputSelect = Actuator<InputSelectState, InputSelectAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputSelect(objectId: ObjectId): InputSelect =
    Actuator(
        EntityId.fromPair("input_select".domain to objectId),
        ServiceCommandResolver { desiredState ->
            ResolvedServiceCommand(
                service = "select_option".service,
                serviceData = InputSelectServiceData(
                    desiredState.value
                )
            )
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

data class InputSelectServiceData(val option: Option) : DesiredServiceData()

@Serializable
data class InputSelectState(override val value: Option) : State<Option>
