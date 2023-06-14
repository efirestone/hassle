package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.SelectOptionServiceCommand
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputSelect = Actuator<InputSelectState, InputSelectAttributes>

fun HomeAssistantApiClient.InputSelect(objectId: ObjectId): InputSelect =
    Actuator(
        EntityId.fromPair("input_select".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SelectOptionServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputSelectAttributes(
    val options: List<Option>,
    val editable: Boolean,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName? = null,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

@Serializable
data class InputSelectState(override val value: Option) : State<Option>
