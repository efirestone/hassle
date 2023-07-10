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

typealias RGBLight = Actuator<RGBLightState, RGBLightSettableState>

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
                            TurnOnLightServiceCommand.ServiceData(hsColor = it),
                        )
                    } ?: desiredState.rgbColor?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(rgbColor = it),
                        )
                    } ?: desiredState.brightness?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(brightness = it),
                        )
                    } ?: desiredState.xyColor?.let {
                        TurnOnLightServiceCommand(
                            entityId,
                            TurnOnLightServiceCommand.ServiceData(xyColor = it),
                        )
                    } ?: TurnOnServiceCommand(entityId)
                }

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        },
    )

@Serializable
class RGBLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,
    @SerialName("hs_color")
    val hsColor: HSColor? = null,
    @SerialName("rgb_color")
    val rgbColor: RGBColor? = null,
    @SerialName("xy_color")
    val xyColor: XYColor? = null,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
    @SerialName("supported_features")
    val supportedFeatures: Int,
) : State<SwitchableValue>

data class RGBLightSettableState(
    val value: SwitchableValue,
    val brightness: Brightness? = null,
    val hsColor: HSColor? = null,
    val rgbColor: RGBColor? = null,
    val xyColor: XYColor? = null,
)

suspend fun RGBLight.turnOn() {
    setDesiredState(RGBLightSettableState(SwitchableValue.ON))
}

suspend fun RGBLight.turnOff() {
    setDesiredState(RGBLightSettableState(SwitchableValue.OFF))
}

suspend fun RGBLight.setBrightness(level: Brightness) {
    setDesiredState(RGBLightSettableState(SwitchableValue.ON, level))
}

suspend fun RGBLight.setRGB(red: Int, green: Int, blue: Int) {
    setDesiredState(RGBLightSettableState(SwitchableValue.ON, rgbColor = RGBColor(red, green, blue)))
}

suspend fun RGBLight.setHS(hue: Double, saturation: Double) {
    setDesiredState(RGBLightSettableState(SwitchableValue.ON, hsColor = HSColor(hue, saturation)))
}

suspend fun RGBLight.setXY(x: Double, y: Double) {
    setDesiredState(RGBLightSettableState(SwitchableValue.ON, xyColor = XYColor(x, y)))
}

suspend fun RGBLight.setColor(name: ColorName) =
    send(TurnOnLightServiceCommand(entityId, TurnOnLightServiceCommand.ServiceData(colorName = name)))
