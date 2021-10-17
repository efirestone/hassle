package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.communicating.SetValueServiceCommand
import hassemble.core.mapping.serializers.default.RegexSerializer
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputText = Actuator<InputTextState, InputTextAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputText(objectId: ObjectId): InputText =
    Actuator(
        EntityId.fromPair("input_text".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetValueServiceCommand(entityId, desiredState.value)
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
