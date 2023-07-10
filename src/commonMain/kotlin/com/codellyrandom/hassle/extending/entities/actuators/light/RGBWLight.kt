package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOffServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnLightServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias RGBWLight = Actuator<RGBWLightState, RGBWLightSettableState>

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
        },
    )

@Serializable
data class RGBWLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    @SerialName("hs_color")
    val hsColor: HSColor? = null,
    @SerialName("rgb_color")
    val rgbColor: RGBColor? = null,
    @SerialName("xy_color")
    val xyColor: XYColor? = null,
    @SerialName("color_temp")
    val colorTemp: ColorTemperature? = null,

    @SerialName("supported_features")
    val supportedFeatures: Int,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<SwitchableValue>

data class RGBWLightSettableState(
    val value: SwitchableValue,
    val brightness: Brightness? = null,
    val hsColor: HSColor? = null,
    val rgbColor: RGBColor? = null,
    val xyColor: XYColor? = null,
    val colorTemp: ColorTemperature? = null,
)

suspend fun RGBWLight.turnOn() = setDesiredState(RGBWLightSettableState(SwitchableValue.ON))

suspend fun RGBWLight.turnOff() = setDesiredState(RGBWLightSettableState(SwitchableValue.OFF))

suspend fun RGBWLight.setBrightness(level: Brightness) = setDesiredState(RGBWLightSettableState(SwitchableValue.ON, level))

suspend fun RGBWLight.setRGB(red: Int, green: Int, blue: Int) =
    setDesiredState(RGBWLightSettableState(SwitchableValue.ON, rgbColor = RGBColor(red, green, blue)))

suspend fun RGBWLight.setHS(hue: Double, saturation: Double) =
    setDesiredState(RGBWLightSettableState(SwitchableValue.ON, hsColor = HSColor(hue, saturation)))

suspend fun RGBWLight.setXY(x: Double, y: Double) =
    setDesiredState(RGBWLightSettableState(SwitchableValue.ON, xyColor = XYColor(x, y)))

suspend fun RGBWLight.setColorTemperature(temperature: ColorTemperature) =
    setDesiredState(RGBWLightSettableState(SwitchableValue.ON, colorTemp = temperature))

suspend fun RGBWLight.setColor(name: ColorName) =
    send(
        TurnOnLightServiceCommand(
            entityId,
            TurnOnLightServiceCommand.ServiceData(colorName = name),
        ),
    )
