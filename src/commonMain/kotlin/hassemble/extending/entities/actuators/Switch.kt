package hassemble.extending.entities.actuators

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.mapSwitchable
import hassemble.values.EntityId
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.PowerConsumption
import hassemble.values.UserId
import hassemble.values.domain
import kotlinx.datetime.Instant

typealias Switch<reified A> = Actuator<SwitchableState, A>
typealias PowerSwitch = Switch<PowerSwitchAttributes>

@Suppress("FunctionName")
internal inline fun <reified A : Attributes> HomeAssistantApiClient.Switch(objectId: ObjectId): Switch<A> =
    Actuator(
        EntityId.fromPair("switch".domain to objectId),
        ServiceCommandResolver { entityId, switchableState ->
            mapSwitchable(entityId, switchableState.value)
        }
    )

@Suppress("FunctionName")
fun HomeAssistantApiClient.PowerMeasuringSwitch(objectId: ObjectId): PowerSwitch = Switch(objectId)

data class PowerSwitchAttributes(
    val powerConsumption: PowerConsumption,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
