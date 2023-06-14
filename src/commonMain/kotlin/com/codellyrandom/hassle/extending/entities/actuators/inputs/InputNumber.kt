package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.SetValueServiceCommand
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputNumber = Actuator<InputNumberState, InputNumberAttributes>

fun HomeAssistantApiClient.InputNumber(objectId: ObjectId): InputNumber =
    Actuator(
        EntityId.fromPair("input_number".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetValueServiceCommand(entityId, desiredState.value)
        }
    )

@Serializable
data class InputNumberState(override val value: Double) : State<Double>

@Serializable
data class InputNumberAttributes(
    val initial: Initial,
    val editable: Boolean,
    val min: Min,
    val max: Max,
    val step: Step,
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
