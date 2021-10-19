package hassemble.extending.entities.actuators.inputs

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.mapSwitchable
import hassemble.values.EntityId
import hassemble.values.FriendlyName
import hassemble.values.Icon
import hassemble.values.ObjectId
import hassemble.values.UserId
import hassemble.values.domain
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputBoolean = Actuator<SwitchableState, InputBooleanAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.InputBoolean(objectId: ObjectId): InputBoolean =
    Actuator(
        EntityId.fromPair("input_boolean".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        }
    )

@Serializable
data class InputBooleanAttributes(
    val editable: Boolean,
    val icon: Icon,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes
