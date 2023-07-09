package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.SetDateTimeServiceCommand
import com.codellyrandom.hassle.core.mapping.serializers.default.LocalTimeSerializer
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import io.fluidsonic.time.LocalTime
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputTime = Actuator<InputTimeState, InputTimeSettableState>

fun HomeAssistantApiClient.InputTime(objectId: ObjectId): InputTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetDateTimeServiceCommand(entityId, desiredState.value)
        },
    )

@Serializable
class InputTimeState(
    @Serializable(LocalTimeSerializer::class)
    override val value: LocalTime,
    val editable: Boolean,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<LocalTime>

data class InputTimeSettableState(
    val value: LocalTime,
)
