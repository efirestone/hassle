package khome.extending.entities.actuators.light

import khome.HassConnection
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.Brightness
import khome.values.ColorName
import khome.values.HSColor
import khome.values.ObjectId
import khome.values.RGBColor
import khome.values.XYColor
import khome.values.service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias RGBLight = Actuator<RGBLightState, LightAttributes>

@Suppress("FunctionName")
fun HassConnection.RGBLight(objectId: ObjectId): RGBLight =
    Light(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                SwitchableValue.OFF -> {
                    DefaultResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.ON -> {
                    desiredState.hsColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                hsColor = it
                            )
                        )
                    } ?: desiredState.rgbColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                rgbColor = it
                            )
                        )
                    } ?: desiredState.brightness?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                brightness = it
                            )
                        )
                    } ?: desiredState.xyColor?.let {
                        DefaultResolvedServiceCommand(
                            service = "turn_on".service,
                            serviceData = RGBLightServiceData(
                                xyColor = it
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

data class RGBLightServiceData(
    private val brightness: Brightness? = null,
    private val hsColor: HSColor? = null,
    private val rgbColor: RGBColor? = null,
    private val xyColor: XYColor? = null
) : DesiredServiceData()

@Serializable
data class RGBLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    @SerialName("hs_color")
    val hsColor: HSColor? = null,
    @SerialName("rgb_color")
    val rgbColor: RGBColor? = null,
    @SerialName("xy_color")
    val xyColor: XYColor? = null
) : State<SwitchableValue>

val RGBLight.isOn
    get() = actualState.value == SwitchableValue.ON

val RGBLight.isOff
    get() = actualState.value == SwitchableValue.OFF

suspend fun RGBLight.turnOn() {
    setDesiredState(RGBLightState(SwitchableValue.ON))
}

suspend fun RGBLight.turnOff() {
    setDesiredState(RGBLightState(SwitchableValue.OFF))
}

suspend fun RGBLight.setBrightness(level: Brightness) {
    setDesiredState(RGBLightState(SwitchableValue.ON, level))
}

suspend fun RGBLight.setRGB(red: Int, green: Int, blue: Int) {
    setDesiredState(RGBLightState(SwitchableValue.ON, rgbColor = RGBColor(red, green, blue)))
}

suspend fun RGBLight.setHS(hue: Double, saturation: Double) {
    setDesiredState(RGBLightState(SwitchableValue.ON, hsColor = HSColor(hue, saturation)))
}

suspend fun RGBLight.setXY(x: Double, y: Double) {
    setDesiredState(RGBLightState(SwitchableValue.ON, xyColor = XYColor(x, y)))
}

suspend fun RGBLight.setColor(name: ColorName) =
    callService("turn_on".service, NamedColorServiceData(name))

fun RGBLight.onTurnedOn(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBLight.onTurnedOff(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
