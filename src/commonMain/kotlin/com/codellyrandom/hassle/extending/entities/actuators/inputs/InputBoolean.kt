package com.codellyrandom.hassle.extending.entities.actuators.inputs

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

typealias InputBoolean = Actuator<InputBooleanState, SwitchableSettableState>

fun HomeAssistantApiClient.InputBoolean(objectId: ObjectId): InputBoolean =
    Actuator(
        EntityId.fromPair("input_boolean".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        },
    )

@Serializable
class InputBooleanState(
    override val value: SwitchableValue,
    val editable: Boolean,
    val icon: Icon,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<SwitchableValue>
