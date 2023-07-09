package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.SetValueServiceCommand
import com.codellyrandom.hassle.core.mapping.serializers.default.RegexSerializer
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputText = Actuator<InputTextState, InputTextSettableState>

fun HomeAssistantApiClient.InputText(objectId: ObjectId): InputText =
    Actuator(
        EntityId.fromPair("input_text".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetValueServiceCommand(entityId, desiredState.value)
        },
    )

@Serializable
class InputTextState(
    override val value: String,
    val editable: Boolean,
    val min: Min,
    val max: Max,
    @Serializable(RegexSerializer::class)
    val pattern: Regex,
    val mode: Mode,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<String>

data class InputTextSettableState(val value: String)
