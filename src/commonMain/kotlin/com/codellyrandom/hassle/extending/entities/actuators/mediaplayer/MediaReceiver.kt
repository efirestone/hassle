package com.codellyrandom.hassle.extending.entities.actuators.mediaplayer

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.*
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.*
import com.codellyrandom.hassle.extending.entities.actuators.onStateValueChangedFrom
import com.codellyrandom.hassle.extending.entities.actuators.stateValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias MediaReceiver = MediaPlayer<MediaReceiverState, MediaReceiverSettableState>

fun HomeAssistantApiClient.MediaReceiver(objectId: ObjectId): MediaReceiver =
    MediaReceiver(EntityId(Domain("media_player"), objectId))

fun HomeAssistantApiClient.MediaReceiver(entityId: EntityId): MediaReceiver =
    Actuator(
        entityId,
        ServiceCommandResolver { storedEntityId, desiredState ->
            when (desiredState.value) {
                IDLE ->
                    desiredState.isVolumeMuted?.let { MuteVolumeServiceCommand(storedEntityId, it) }
                        ?: desiredState.volumeLevel?.let { SetVolumeServiceCommand(storedEntityId, it) }
                        ?: TurnOnServiceCommand(storedEntityId)

                PAUSED ->
                    desiredState.volumeLevel?.let { SetVolumeServiceCommand(storedEntityId, it) }
                        ?: desiredState.mediaPosition?.let { SetSeekPositionServiceCommand(storedEntityId, it) }
                        ?: desiredState.isVolumeMuted?.let { MuteVolumeServiceCommand(storedEntityId, it) }
                        ?: PauseMediaServiceCommand(storedEntityId)

                PLAYING ->
                    desiredState.mediaPosition?.let { SetSeekPositionServiceCommand(storedEntityId, it) }
                        ?: desiredState.isVolumeMuted?.let { MuteVolumeServiceCommand(storedEntityId, it) }
                        ?: desiredState.volumeLevel?.let { SetVolumeServiceCommand(storedEntityId, it) }
                        ?: ResumeMediaServiceCommand(storedEntityId)

                OFF -> TurnOffServiceCommand(storedEntityId)

                UNKNOWN -> throw IllegalStateException("State cannot be changed to UNKNOWN")
                UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        },
    )

@Serializable
class MediaReceiverState(
    override val value: MediaReceiverStateValue,
    @SerialName("volume_level")
    val volumeLevel: VolumeLevel? = null,
    @SerialName("is_volume_muted")
    val isVolumeMuted: Mute? = null,
    @SerialName("media_position")
    val mediaPosition: MediaPosition? = null,
    @SerialName("media_content_id")
    val mediaContentId: MediaContentId? = null,
    @SerialName("media_title")
    val mediaTitle: MediaTitle? = null,
    @SerialName("media_artist")
    val mediaArtist: Artist? = null,
    @SerialName("album_name")
    val mediaAlbumName: AlbumName? = null,
    @SerialName("media_content_type")
    val mediaContentType: MediaContentType? = null,
    @SerialName("media_duration")
    val mediaDuration: MediaDuration? = null,
    @SerialName("media_position_updated_at")
    val mediaPositionUpdatedAt: Instant? = null,
    @SerialName("app_id")
    val appId: AppId? = null,
    @SerialName("app_name")
    val appName: AppName? = null,
    @SerialName("entity_picture")
    val entityPicture: EntityPicture? = null,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<MediaReceiverStateValue>

data class MediaReceiverSettableState(
    val value: MediaReceiverStateValue,
    val volumeLevel: VolumeLevel? = null,
    val isVolumeMuted: Mute? = null,
    val mediaPosition: MediaPosition? = null,
)

@Serializable
enum class MediaReceiverStateValue {
    @SerialName("unknown")
    UNKNOWN,

    @SerialName("unavailable")
    UNAVAILABLE,

    @SerialName("off")
    OFF,

    @SerialName("idle")
    IDLE,

    @SerialName("playing")
    PLAYING,

    @SerialName("paused")
    PAUSED,
}

val MediaReceiver.isOff
    get() = state.value == OFF

val MediaReceiver.isIdle
    get() = state.value == IDLE

val MediaReceiver.isPlaying
    get() = state.value == PLAYING

val MediaReceiver.isOn
    get() = state.value != OFF || state.value != UNAVAILABLE

val MediaReceiver.isPaused
    get() = state.value == PAUSED

suspend fun MediaReceiver.turnOn() = setDesiredState(MediaReceiverSettableState(value = IDLE))

suspend fun MediaReceiver.turnOff() = setDesiredState(MediaReceiverSettableState(value = OFF))

suspend fun MediaReceiver.play() = setDesiredState(MediaReceiverSettableState(value = PLAYING))

suspend fun MediaReceiver.play(contentType: MediaContentType, contentId: MediaContentId) =
    send(PlayMediaServiceCommand(entityId, contentType, contentId))

suspend fun MediaReceiver.pause() = setDesiredState(MediaReceiverSettableState(value = PAUSED))

suspend fun MediaReceiver.setVolumeTo(level: VolumeLevel) {
    if (state.value == UNAVAILABLE || state.value == OFF) {
        throw RuntimeException("Volume can not be set when MediaReceiver is ${state.value}")
    }

    setDesiredState(MediaReceiverSettableState(value = state.value, volumeLevel = level))
}

suspend fun MediaReceiver.muteVolume() =
    setDesiredState(MediaReceiverSettableState(value = state.value, isVolumeMuted = Mute.TRUE))

suspend fun MediaReceiver.unMuteVolume() =
    setDesiredState(MediaReceiverSettableState(value = state.value, isVolumeMuted = Mute.FALSE))

fun MediaReceiver.onPlaybackStarted(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(IDLE to PLAYING, f)

fun MediaReceiver.onPlaybackStopped(f: MediaReceiver.(Switchable) -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PLAYING to IDLE) ||
            stateValueChangedFrom(PLAYING to OFF) ||
            stateValueChangedFrom(PAUSED to OFF) ||
            stateValueChangedFrom(PAUSED to IDLE)
        ) {
            f(this, it)
        }
    }

fun MediaReceiver.onPlaybackPaused(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(PLAYING to PAUSED, f)

fun MediaReceiver.onPlaybackResumed(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(PAUSED to PLAYING, f)

fun MediaReceiver.onTurnedOn(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(UNKNOWN to IDLE, f)

fun MediaReceiver.onTurnedOff(f: MediaReceiver.(Switchable) -> Unit) =
    onStateValueChangedFrom(IDLE to OFF, f)
