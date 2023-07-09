package com.codellyrandom.hassle.extending.entities

import com.codellyrandom.hassle.communicating.TurnOffServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwitchableState(override val value: SwitchableValue) : State<SwitchableValue>

@Serializable
enum class SwitchableValue {
    @SerialName("on")
    ON,

    @SerialName("off")
    OFF,

    @SerialName("unavailable")
    UNAVAILABLE,
}

@Serializable
data class DefaultAttributes(
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
) : Attributes

internal fun mapSwitchable(entityId: EntityId, switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> TurnOnServiceCommand(entityId)
        SwitchableValue.OFF -> TurnOffServiceCommand(entityId)

        SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
    }
