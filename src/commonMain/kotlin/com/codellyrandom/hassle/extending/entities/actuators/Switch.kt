package com.codellyrandom.hassle.extending.entities.actuators

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableSettableState
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.mapSwitchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Switch<S> = Actuator<S, SwitchableSettableState>
typealias PowerSwitch = Actuator<PowerSwitchState, SwitchableSettableState>

internal inline fun <reified S : State<SwitchableValue>> HomeAssistantApiClient.Switch(objectId: ObjectId): Switch<S> =
    Actuator(
        EntityId.fromPair("switch".domain to objectId),
        ServiceCommandResolver { entityId, switchableState ->
            mapSwitchable(entityId, switchableState.value)
        },
    )

@Suppress("FunctionName")
fun HomeAssistantApiClient.PowerMeasuringSwitch(objectId: ObjectId): PowerSwitch = Switch(objectId)

@Serializable
class PowerSwitchState(
    override val value: SwitchableValue,

    @SerialName("power_consumption")
    val powerConsumption: PowerConsumption,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<SwitchableValue>
