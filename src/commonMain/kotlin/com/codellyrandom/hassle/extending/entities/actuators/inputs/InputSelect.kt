package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.SelectOptionServiceCommand
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputSelect = Actuator<InputSelectState, InputSelectSettableState>

fun HomeAssistantApiClient.InputSelect(objectId: ObjectId): InputSelect =
    Actuator(
        EntityId.fromPair("input_select".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SelectOptionServiceCommand(entityId, desiredState.value)
        },
    )

@Serializable
class InputSelectState(
    override val value: Option,
    val options: List<Option>,
    val editable: Boolean,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName? = null,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<Option>

data class InputSelectSettableState(val value: Option)
