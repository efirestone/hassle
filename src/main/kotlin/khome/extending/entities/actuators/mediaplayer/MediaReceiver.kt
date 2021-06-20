package khome.extending.entities.actuators.mediaplayer

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.IDLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.OFF
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.PAUSED
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.PLAYING
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.UNAVAILABLE
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue.UNKNOWN
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.extending.entities.actuators.stateValueChangedFrom
import khome.observability.Switchable
import khome.values.AlbumName
import khome.values.AppId
import khome.values.AppName
import khome.values.Artist
import khome.values.EntityPicture
import khome.values.FriendlyName
import khome.values.MediaContentId
import khome.values.MediaContentType
import khome.values.MediaDuration
import khome.values.MediaPosition
import khome.values.MediaTitle
import khome.values.Mute
import khome.values.ObjectId
import khome.values.UserId
import khome.values.VolumeLevel
import khome.values.service
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias MediaReceiver = MediaPlayer<MediaReceiverState, MediaReceiverAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MediaReceiver(objectId: ObjectId): MediaReceiver =
    MediaPlayer(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                IDLE -> {
                    desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                PAUSED ->
                    desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: desiredState.mediaPosition?.let { position ->
                        DefaultResolvedServiceCommand(
                            service = "seek_position".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                seekPosition = position
                            )
                        )
                    } ?: desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "media_pause".service,
                        serviceData = EntityIdOnlyServiceData()
                    )

                PLAYING ->
                    desiredState.mediaPosition?.let { position ->
                        DefaultResolvedServiceCommand(
                            service = "seek_position".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                seekPosition = position
                            )
                        )
                    } ?: desiredState.isVolumeMuted?.let { isMuted ->
                        DefaultResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        DefaultResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = MediaReceiverDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: DefaultResolvedServiceCommand(
                        service = "media_play".service,
                        serviceData = EntityIdOnlyServiceData()
                    )

                OFF -> {
                    DefaultResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                UNKNOWN -> throw IllegalStateException("State cannot be changed to UNKNOWN")
                UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
            }
        }
    )

@Serializable
data class MediaReceiverState(
    override val value: MediaReceiverStateValue,
    @SerialName("volume_level")
    val volumeLevel: VolumeLevel? = null,
    @SerialName("is_volume_muted")
    val isVolumeMuted: Mute? = null,
    @SerialName("media_position")
    val mediaPosition: MediaPosition? = null
) : State<MediaReceiverStateValue>

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
    PAUSED
}

@Serializable
data class MediaReceiverAttributes(
    @SerialName("media_content_id")
    val mediaContentId: MediaContentId?,
    @SerialName("media_title")
    val mediaTitle: MediaTitle?,
    @SerialName("media_artist")
    val mediaArtist: Artist? = null,
    @SerialName("album_name")
    val mediaAlbumName: AlbumName? = null,
    @SerialName("media_content_type")
    val mediaContentType: MediaContentType?,
    @SerialName("media_duration")
    val mediaDuration: MediaDuration?,
    @SerialName("media_position_updated_at")
    val mediaPositionUpdatedAt: Instant?,
    @SerialName("app_id")
    val appId: AppId? = null,
    @SerialName("app_name")
    val appName: AppName? = null,
    @SerialName("entity_picture")
    val entityPicture: EntityPicture,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

data class MediaReceiverDesiredServiceData(
    val isVolumeMuted: Mute? = null,
    val volumeLevel: VolumeLevel? = null,
    val seekPosition: MediaPosition? = null
) : DesiredServiceData()

val MediaReceiver.isOff
    get() = actualState.value == OFF

val MediaReceiver.isIdle
    get() = actualState.value == IDLE

val MediaReceiver.isPlaying
    get() = actualState.value == PLAYING

val MediaReceiver.isOn
    get() = actualState.value != OFF || actualState.value != UNAVAILABLE

val MediaReceiver.isPaused
    get() = actualState.value == PAUSED

fun MediaReceiver.turnOn() {
    desiredState = MediaReceiverState(value = IDLE)
}

fun MediaReceiver.turnOff() {
    desiredState = MediaReceiverState(value = OFF)
}

fun MediaReceiver.play() {
    desiredState = MediaReceiverState(value = PLAYING)
}

fun MediaReceiver.pause() {
    desiredState = MediaReceiverState(value = PAUSED)
}

fun MediaReceiver.setVolumeTo(level: VolumeLevel) {
    if (actualState.value == UNAVAILABLE || actualState.value == OFF)
        throw RuntimeException("Volume can not be set when MediaReceiver is ${actualState.value}")

    desiredState = MediaReceiverState(value = actualState.value, volumeLevel = level)
}

fun MediaReceiver.muteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = Mute.TRUE)
}

fun MediaReceiver.unMuteVolume() {
    desiredState = MediaReceiverState(value = actualState.value, isVolumeMuted = Mute.FALSE)
}

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
