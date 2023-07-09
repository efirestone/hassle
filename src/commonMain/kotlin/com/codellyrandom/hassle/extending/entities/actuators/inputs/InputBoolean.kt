package com.codellyrandom.hassle.extending.entities.actuators.inputs

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.extending.entities.mapSwitchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.Icon
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import com.codellyrandom.hassle.values.domain
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputBoolean = Actuator<SwitchableState, InputBooleanAttributes>

fun HomeAssistantApiClient.InputBoolean(objectId: ObjectId): InputBoolean =
    Actuator(
        EntityId.fromPair("input_boolean".domain to objectId),
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        },
    )

@Serializable
data class InputBooleanAttributes(
    val editable: Boolean,
    val icon: Icon,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
) : Attributes
