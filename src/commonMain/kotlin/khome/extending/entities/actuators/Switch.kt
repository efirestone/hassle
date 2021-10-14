package khome.extending.entities.actuators

import khome.HomeAssistantApiClient
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.mapSwitchable
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.PowerConsumption
import khome.values.UserId
import khome.values.domain
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
