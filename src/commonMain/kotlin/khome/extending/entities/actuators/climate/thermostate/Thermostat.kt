package khome.extending.entities.actuators.climate.thermostate

import khome.HomeAssistantApiClient
import khome.communicating.*
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.actuators.climate.ClimateControl
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Thermostat = Actuator<ThermostatState, ThermostatAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.Thermostat(objectId: ObjectId): Thermostat {
    return ClimateControl(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            when (desiredState.value) {
                ThermostatStateValue.OFF -> TurnOffServiceCommand(entityId)
                ThermostatStateValue.HEAT -> {
                    desiredState.temperature?.let { temperature ->
                        SetTemperatureServiceCommand(
                            entityId,
                            temperature,
                            hvacMode = HvacMode("heat")
                        )
                    } ?: (if (desiredState.presetMode.isNone) null else desiredState.presetMode)?.let { preset ->
                        SetHvacPresetModeServiceCommand(entityId, preset)
                    } ?: TurnOnServiceCommand(entityId)
                }
            }
        }
    )
}

data class ThermostatState(
    override val value: ThermostatStateValue,
    val temperature: Temperature? = null,
    val presetMode: PresetMode = "none".presetMode
) : State<ThermostatStateValue>

data class ThermostatAttributes(
    val hvacModes: List<HvacMode>,
    val presetModes: List<PresetMode>,
    val currentTemperature: Temperature,
    val minTemp: Temperature,
    val maxTemp: Temperature,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val userId: UserId?
) : Attributes

@Serializable
enum class ThermostatStateValue {
    @SerialName("heat")
    HEAT,

    @SerialName("off")
    OFF
}

val Thermostat.isHeating
    get() = actualState.value == ThermostatStateValue.HEAT

val Thermostat.isOn
    get() = isHeating

val Thermostat.isOff
    get() = actualState == ThermostatState(ThermostatStateValue.OFF)

suspend fun Thermostat.turnOff() = setDesiredState(ThermostatState(ThermostatStateValue.OFF))

suspend fun Thermostat.turnOn() = setDesiredState(ThermostatState(ThermostatStateValue.HEAT))

suspend fun Thermostat.setPreset(preset: PresetMode) =
    setDesiredState(ThermostatState(ThermostatStateValue.HEAT, presetMode = preset))

suspend fun Thermostat.setTargetTemperature(temperature: Temperature) =
    setDesiredState(ThermostatState(ThermostatStateValue.HEAT, temperature = temperature))

suspend fun Thermostat.turnOnBoost() = setPreset("boost".presetMode)

fun Thermostat.onTurnedOn(f: Thermostat.(Switchable) -> Unit) =
    onStateValueChangedFrom(ThermostatStateValue.OFF to ThermostatStateValue.HEAT, f)

fun Thermostat.onTurnedOff(f: Thermostat.(Switchable) -> Unit) =
    onStateValueChangedFrom(ThermostatStateValue.HEAT to ThermostatStateValue.OFF, f)
