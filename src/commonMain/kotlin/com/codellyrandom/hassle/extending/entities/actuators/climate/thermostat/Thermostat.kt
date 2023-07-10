package com.codellyrandom.hassle.extending.entities.actuators.climate.thermostat

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.actuators.climate.ClimateControl
import com.codellyrandom.hassle.extending.entities.actuators.climate.thermostat.ThermostatStateValue.*
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Thermostat = Actuator<ThermostatState, ThermostatSettableState>

fun HomeAssistantApiClient.Thermostat(objectId: ObjectId): Thermostat {
    return ClimateControl(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            when (desiredState.value) {
                OFF -> TurnOffServiceCommand(entityId)
                HEAT -> {
                    desiredState.temperature?.let { temperature ->
                        SetTemperatureServiceCommand(
                            entityId,
                            temperature,
                            hvacMode = HvacMode("heat"),
                        )
                    } ?: (if (desiredState.presetMode.isNone) null else desiredState.presetMode)?.let { preset ->
                        SetHvacPresetModeServiceCommand(entityId, preset)
                    } ?: TurnOnServiceCommand(entityId)
                }
            }
        },
    )
}

@Serializable
class ThermostatState(
    override val value: ThermostatStateValue,
    val temperature: Temperature? = null,
    @SerialName("preset_mode")
    val presetMode: PresetMode = "none".presetMode,
    @SerialName("hvac_modes")
    val hvacModes: List<HvacMode>,
    @SerialName("preset_modes")
    val presetModes: List<PresetMode>,
    @SerialName("current_temperature")
    val currentTemperature: Temperature,
    @SerialName("min_temp")
    val minTemp: Temperature,
    @SerialName("max_temp")
    val maxTemp: Temperature,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
    @SerialName("user_id")
    val userId: UserId?,
) : State<ThermostatStateValue>

data class ThermostatSettableState(
    val value: ThermostatStateValue,
    val temperature: Temperature? = null,
    @SerialName("preset_mode")
    val presetMode: PresetMode = "none".presetMode,
)

@Serializable
enum class ThermostatStateValue {
    @SerialName("heat")
    HEAT,

    @SerialName("off")
    OFF,
}

val Thermostat.isHeating
    get() = state.value == HEAT

val Thermostat.isOn
    get() = isHeating

val Thermostat.isOff
    get() = state.value == OFF

suspend fun Thermostat.turnOff() = setDesiredState(ThermostatSettableState(OFF))

suspend fun Thermostat.turnOn() = setDesiredState(ThermostatSettableState(HEAT))

suspend fun Thermostat.setPreset(preset: PresetMode) =
    setDesiredState(ThermostatSettableState(HEAT, presetMode = preset))

suspend fun Thermostat.setTargetTemperature(temperature: Temperature) =
    setDesiredState(ThermostatSettableState(HEAT, temperature = temperature))

suspend fun Thermostat.turnOnBoost() = setPreset("boost".presetMode)

fun Thermostat.onTurnedOn(f: Thermostat.(Switchable) -> Unit) =
    onStateValueChangedFrom(OFF to HEAT, f)

fun Thermostat.onTurnedOff(f: Thermostat.(Switchable) -> Unit) =
    onStateValueChangedFrom(HEAT to OFF, f)
