package com.codellyrandom.hassle.extending.entities

import com.codellyrandom.hassle.communicating.TurnOffServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.values.EntityId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class SwitchableSettableState(val value: SwitchableValue)

@Serializable
enum class SwitchableValue {
    @SerialName("on")
    ON,

    @SerialName("off")
    OFF,

    @SerialName("unavailable")
    UNAVAILABLE,
}

internal fun mapSwitchable(entityId: EntityId, switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> TurnOnServiceCommand(entityId)
        SwitchableValue.OFF -> TurnOffServiceCommand(entityId)

        SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
    }
