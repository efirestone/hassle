package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOffServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnLightServiceCommand
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.SwitchableValue.*
import com.codellyrandom.hassle.values.Brightness
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias DimmableLight = Actuator<DimmableLightState, DimmableLightSettableState>

fun HomeAssistantApiClient.DimmableLight(objectId: ObjectId): DimmableLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            desiredState.brightness?.let { brightness ->
                TurnOnLightServiceCommand(
                    entityId,
                    TurnOnLightServiceCommand.ServiceData(brightness = brightness),
                )
            } ?: when (desiredState.value) {
                OFF -> TurnOffServiceCommand(entityId)
                ON -> TurnOnServiceCommand(entityId)

                UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        },
    )

@Serializable
class DimmableLightState(
    override val value: SwitchableValue,
    val brightness: Brightness? = null,

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

data class DimmableLightSettableState(
    val value: SwitchableValue,
    val brightness: Brightness? = null,
)

suspend fun DimmableLight.turnOn() {
    setDesiredState(DimmableLightSettableState(ON))
}

suspend fun DimmableLight.turnOff() {
    setDesiredState(DimmableLightSettableState(OFF))
}

suspend fun DimmableLight.setBrightness(level: Brightness) {
    setDesiredState(DimmableLightSettableState(ON, level))
}
