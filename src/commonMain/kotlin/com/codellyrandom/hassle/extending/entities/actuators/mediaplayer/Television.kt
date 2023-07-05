package com.codellyrandom.hassle.extending.entities.actuators.mediaplayer

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Television = MediaPlayer<TelevisionState, TelevisionAttributes>

fun HomeAssistantApiClient.Television(objectId: ObjectId): Television =
    MediaPlayer(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            when (desiredState.value) {
                SwitchableValue.ON -> {
                    desiredState.isVolumeMuted?.let { isMuted ->
                        MuteVolumeServiceCommand(entityId, isMuted)
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        SetVolumeServiceCommand(entityId, volumeLevel)
                    } ?: desiredState.source?.let { source ->
                        SetMediaSourceServiceCommand(entityId, source)
                    } ?: TurnOnServiceCommand(entityId)
                }

                SwitchableValue.OFF -> TurnOffServiceCommand(entityId)

                SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

@Serializable
data class TelevisionState(
    override val value: SwitchableValue,
    @SerialName("volume_level")
    val volumeLevel: VolumeLevel? = null,
    @SerialName("is_volume_muted")
    val isVolumeMuted: Mute? = null,
    val source: MediaSource? = null
) : State<SwitchableValue>

@Serializable
data class TelevisionAttributes(
    @SerialName("media_content_id")
    val mediaContentId: MediaContentId,
    @SerialName("media_title")
    val mediaTitle: MediaTitle,
    @SerialName("media_content_type")
    val mediaContentType: MediaContentType,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

val Television.isOn
    get() = actualState.value == SwitchableValue.ON

val Television.isOff
    get() = actualState.value == SwitchableValue.OFF

val Television.isMuted
    get() = actualState.isVolumeMuted == Mute.TRUE

suspend fun Television.turnOn() {
    setDesiredState(TelevisionState(value = SwitchableValue.ON))
}

suspend fun Television.turnOff() {
    setDesiredState(TelevisionState(value = SwitchableValue.OFF))
}

suspend fun Television.setVolumeTo(level: VolumeLevel) {
    setDesiredState(TelevisionState(value = SwitchableValue.ON, volumeLevel = level))
}

suspend fun Television.muteVolume() {
    setDesiredState(TelevisionState(value = SwitchableValue.ON, isVolumeMuted = Mute.TRUE))
}

suspend fun Television.unMuteVolume() {
    setDesiredState(TelevisionState(value = SwitchableValue.ON, isVolumeMuted = Mute.FALSE))
}

suspend fun Television.setSource(source: MediaSource) {
    setDesiredState(TelevisionState(value = SwitchableValue.ON, source = source))
}

fun Television.onTurnedOn(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun Television.onTurnedOff(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
