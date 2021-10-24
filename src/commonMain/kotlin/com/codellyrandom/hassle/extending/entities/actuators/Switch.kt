package com.codellyrandom.hassle.extending.entities.actuators

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.extending.entities.mapSwitchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.PowerConsumption
import com.codellyrandom.hassle.values.UserId
import com.codellyrandom.hassle.values.domain
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
