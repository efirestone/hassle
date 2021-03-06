package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias RGBLight = Actuator<RGBLightState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.RGBLight(objectId: ObjectId): RGBLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            when (desiredState.value) {
                SwitchableValue.OFF -> TurnOffServiceCommand(entityId)

                SwitchableValue.ON -> {
                    desiredState.hsColor?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(hsColor = it)
                        )
                    } ?: desiredState.rgbColor?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(rgbColor = it)
                        )
                    } ?: desiredState.brightness?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(brightness = it)
                        )
                    } ?: desiredState.xyColor?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(xyColor = it)
                        )
                    } ?: TurnOnServiceCommand(entityId)
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

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
    send(TurnOnLightServiceCommand(entityId, TurnOnLightServiceCommand.ServiceData(colorName = name)))

fun RGBLight.onTurnedOn(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBLight.onTurnedOff(f: RGBLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
