package khome.extending.entities.actuators.light

import khome.HomeAssistantApiClient
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.*

typealias RGBWLight = Actuator<RGBWLightState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.RGBWLight(objectId: ObjectId): RGBWLight =
    Light(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                SwitchableValue.OFF -> {
                    ResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.ON -> {
                    desiredState.colorTemp?.let {
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBWLightServiceData(
                                colorTemp = it
                            )
                        )
                    } ?: desiredState.hsColor?.let {
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBWLightServiceData(
                                hsColor = it
                            )
                        )
                    } ?: desiredState.rgbColor?.let {
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBWLightServiceData(
                                rgbColor = it
                            )
                        )
                    } ?: desiredState.brightness?.let {
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBWLightServiceData(
                                brightness = it
                            )
                        )
                    } ?: desiredState.xyColor?.let {
                        ResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBWLightServiceData(
                                xyColor = it
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

data class RGBWLightServiceData(
    private val brightness: Brightness? = null,
    private val hsColor: HSColor? = null,
    private val rgbColor: RGBColor? = null,
    private val xyColor: XYColor? = null,
    private val colorTemp: ColorTemperature? = null
) : DesiredServiceData()

data class RGBWLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    val hsColor: HSColor? = null,
    val rgbColor: RGBColor? = null,
    val xyColor: XYColor? = null,
    val colorTemp: ColorTemperature? = null
) : State<SwitchableValue>

val RGBWLight.isOn
    get() = actualState.value == SwitchableValue.ON

val RGBWLight.isOff
    get() = actualState.value == SwitchableValue.OFF

suspend fun RGBWLight.turnOn() {
    setDesiredState(RGBWLightState(SwitchableValue.ON))
}

suspend fun RGBWLight.turnOff() {
    setDesiredState(RGBWLightState(SwitchableValue.OFF))
}

suspend fun RGBWLight.setBrightness(level: Brightness) {
    setDesiredState(RGBWLightState(SwitchableValue.ON, level))
}

suspend fun RGBWLight.setRGB(red: Int, green: Int, blue: Int) {
    setDesiredState(RGBWLightState(SwitchableValue.ON, rgbColor = RGBColor(red, green, blue)))
}

suspend fun RGBWLight.setHS(hue: Double, saturation: Double) {
    setDesiredState(RGBWLightState(SwitchableValue.ON, hsColor = HSColor(hue, saturation)))
}

suspend fun RGBWLight.setXY(x: Double, y: Double) {
    setDesiredState(RGBWLightState(SwitchableValue.ON, xyColor = XYColor(x, y)))
}

suspend fun RGBWLight.setColorTemperature(temperature: ColorTemperature) {
    when (temperature.unit) {
        ColorTemperature.Unit.MIRED -> setDesiredState(RGBWLightState(SwitchableValue.ON, colorTemp = temperature))
        ColorTemperature.Unit.KELVIN -> callService("turn_on".service, KelvinServiceData(temperature))
    }
}

suspend fun RGBWLight.setColor(name: ColorName) {
    callService("turn_on".service, NamedColorServiceData(name))
}

fun RGBWLight.onTurnedOn(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBWLight.onTurnedOff(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
