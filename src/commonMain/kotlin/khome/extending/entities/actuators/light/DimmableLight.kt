package khome.extending.entities.actuators.light

import khome.HomeAssistantApiClient
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.Brightness
import khome.values.ObjectId
import khome.values.service

typealias DimmableLight = Actuator<DimmableLightState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.DimmableLight(objectId: ObjectId): DimmableLight =
    Light(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                SwitchableValue.OFF -> {
                    val resolvedServiceCommand: ResolvedServiceCommand = desiredState.brightness?.let { brightness ->
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = DimmableLightServiceData(
                                brightness
                            )
                        )
                    } ?: ResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                    resolvedServiceCommand
                }
                SwitchableValue.ON -> {
                    desiredState.brightness?.let { brightness ->
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = DimmableLightServiceData(
                                brightness
                            )
                        )
                    } ?: ResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class DimmableLightState(override val value: SwitchableValue, val brightness: Brightness? = null) : State<SwitchableValue>

data class DimmableLightServiceData(private val brightness: Brightness) : DesiredServiceData()

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
