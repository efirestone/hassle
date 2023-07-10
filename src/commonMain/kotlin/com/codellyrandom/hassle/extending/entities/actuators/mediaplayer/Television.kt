package com.codellyrandom.hassle.extending.entities.actuators.mediaplayer

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Television = MediaPlayer<TelevisionState, TelevisionSettableState>

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
        },
    )

@Serializable
class TelevisionState(
    override val value: SwitchableValue,
    @SerialName("volume_level")
    val volumeLevel: VolumeLevel? = null,
    @SerialName("is_volume_muted")
    val isVolumeMuted: Mute? = null,
    val source: MediaSource? = null,
    @SerialName("media_content_id")
    val mediaContentId: MediaContentId,
    @SerialName("media_title")
    val mediaTitle: MediaTitle,
    @SerialName("media_content_type")
    val mediaContentType: MediaContentType,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<SwitchableValue>

data class TelevisionSettableState(
    val value: SwitchableValue,
    val volumeLevel: VolumeLevel? = null,
    val isVolumeMuted: Mute? = null,
    val source: MediaSource? = null,
)

val Television.isOn
    get() = state.value == SwitchableValue.ON

val Television.isOff
    get() = state.value == SwitchableValue.OFF

val Television.isMuted
    get() = state.isVolumeMuted == Mute.TRUE

suspend fun Television.turnOn() {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.ON))
}

suspend fun Television.turnOff() {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.OFF))
}

suspend fun Television.setVolumeTo(level: VolumeLevel) {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.ON, volumeLevel = level))
}

suspend fun Television.muteVolume() {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.ON, isVolumeMuted = Mute.TRUE))
}

suspend fun Television.unMuteVolume() {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.ON, isVolumeMuted = Mute.FALSE))
}

suspend fun Television.setSource(source: MediaSource) {
    setDesiredState(TelevisionSettableState(value = SwitchableValue.ON, source = source))
}

fun Television.onTurnedOn(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

fun Television.onTurnedOff(f: Television.(Switchable) -> Unit) =
    onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
