package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.serializers.default.RegexSerializer
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.Max
import khome.values.Min
import khome.values.Mode
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import khome.values.service
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputText = Actuator<InputTextState, InputTextAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputText(objectId: ObjectId): InputText =
    Actuator(
        EntityId.fromPair("input_text".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_value".service,
                serviceData = SettableStateValueServiceData(
                    desiredState.value
                )
            )
        }
    )

@Serializable
data class InputTextState(override val value: String) : State<String>

@Serializable
data class InputTextAttributes(
    val editable: Boolean,
    val min: Min,
    val max: Max,
    @Serializable(RegexSerializer::class)
    val pattern: Regex,
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
