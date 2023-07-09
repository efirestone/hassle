package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.SetDateTimeServiceCommand
import com.codellyrandom.hassle.core.mapping.serializers.default.LocalDateTimeSerializer
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputDateTime = Actuator<InputDateTimeState, InputDateTimeAttributes>

fun HomeAssistantApiClient.InputDateTime(objectId: ObjectId): InputDateTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            SetDateTimeServiceCommand(entityId, desiredState.value)
        },
    )

@Serializable
data class InputDateTimeState(
    @Serializable(LocalDateTimeSerializer::class)
    override val value: LocalDateTime,
) : State<LocalDateTime>

@Serializable
data class InputDateTimeAttributes(
    val editable: Boolean,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
) : Attributes
