package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.stateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.Brightness
import com.codellyrandom.hassle.values.ObjectId

typealias DimmableLight = Actuator<DimmableLightState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.DimmableLight(objectId: ObjectId): DimmableLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            desiredState.brightness?.let { brightness ->
                TurnOnLightServiceCommand(
                    entityId,
                    TurnOnLightServiceCommand.ServiceData(brightness = brightness)
                )
            } ?: when (desiredState.value) {
                SwitchableValue.OFF -> TurnOffServiceCommand(entityId)
                SwitchableValue.ON -> TurnOnServiceCommand(entityId)

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class DimmableLightState(override val value: SwitchableValue, val brightness: Brightness? = null) : State<SwitchableValue>

val DimmableLight.isOn
    get() = actualState.value == SwitchableValue.ON

val DimmableLight.isOff
    get() = actualState.value == SwitchableValue.OFF

suspend fun DimmableLight.turnOn() {
    setDesiredState(DimmableLightState(SwitchableValue.ON))
}

suspend fun DimmableLight.turnOff() {
    setDesiredState(DimmableLightState(SwitchableValue.OFF))
}

suspend fun DimmableLight.setBrightness(level: Brightness) {
    setDesiredState(DimmableLightState(SwitchableValue.ON, level))
}

fun DimmableLight.onTurnedOn(f: DimmableLight.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON))
            f(this, it)
    }

fun DimmableLight.onTurnedOff(f: DimmableLight.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF))
            f(this, it)
    }
