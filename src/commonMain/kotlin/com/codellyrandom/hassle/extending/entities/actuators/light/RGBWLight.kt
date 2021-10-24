package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*

typealias RGBWLight = Actuator<RGBWLightState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.RGBWLight(objectId: ObjectId): RGBWLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            when (desiredState.value) {
                SwitchableValue.OFF -> TurnOffServiceCommand(entityId)

                SwitchableValue.ON -> {
                    val serviceData = desiredState.colorTemp?.let {
                        when (it.unit) {
                            ColorTemperature.Unit.MIRED -> TurnOnLightServiceCommand.ServiceData(colorTemp = it)
                            ColorTemperature.Unit.KELVIN -> TurnOnLightServiceCommand.ServiceData(kelvin = it)
                        }
                    } ?: desiredState.hsColor?.let {
                        TurnOnLightServiceCommand.ServiceData(hsColor = it)
                    } ?: desiredState.rgbColor?.let {
                        TurnOnLightServiceCommand.ServiceData(rgbColor = it)
                    } ?: desiredState.brightness?.let {
                        TurnOnLightServiceCommand.ServiceData(brightness = it)
                    } ?: desiredState.xyColor?.let {
                        TurnOnLightServiceCommand.ServiceData(xyColor = it)
                    }

                    serviceData?.let {
                        TurnOnLightServiceCommand(entityId, serviceData)
                    } ?: TurnOnServiceCommand(entityId)
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

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

suspend fun RGBWLight.turnOn() = setDesiredState(RGBWLightState(SwitchableValue.ON))

suspend fun RGBWLight.turnOff() = setDesiredState(RGBWLightState(SwitchableValue.OFF))

suspend fun RGBWLight.setBrightness(level: Brightness) = setDesiredState(RGBWLightState(SwitchableValue.ON, level))

suspend fun RGBWLight.setRGB(red: Int, green: Int, blue: Int) =
    setDesiredState(RGBWLightState(SwitchableValue.ON, rgbColor = RGBColor(red, green, blue)))

suspend fun RGBWLight.setHS(hue: Double, saturation: Double) =
    setDesiredState(RGBWLightState(SwitchableValue.ON, hsColor = HSColor(hue, saturation)))

suspend fun RGBWLight.setXY(x: Double, y: Double) =
    setDesiredState(RGBWLightState(SwitchableValue.ON, xyColor = XYColor(x, y)))

suspend fun RGBWLight.setColorTemperature(temperature: ColorTemperature) =
    setDesiredState(RGBWLightState(SwitchableValue.ON, colorTemp = temperature))

suspend fun RGBWLight.setColor(name: ColorName) =
    send(
        TurnOnLightServiceCommand(
            entityId,
            TurnOnLightServiceCommand.ServiceData(colorName = name)
        )
    )

fun RGBWLight.onTurnedOn(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun RGBWLight.onTurnedOff(f: RGBWLight.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
