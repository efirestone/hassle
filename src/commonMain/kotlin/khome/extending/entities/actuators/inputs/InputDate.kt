package khome.extending.entities.actuators.inputs

import khome.HomeAssistantApiClient
import khome.communicating.ServiceCommandResolver
import khome.communicating.SetDateTimeServiceCommand
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.*
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

//data class InputDateServiceData(private val date: LocalDate) : DesiredServiceData()
