package khome.extending.entities.actuators.mediaplayer

import khome.HomeAssistantApiClient
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.onStateValueChangedFrom
import khome.observability.Switchable
import khome.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Television = MediaPlayer<TelevisionState, TelevisionAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.Television(objectId: ObjectId): Television =
    MediaPlayer(
        objectId,
        ServiceCommandResolver { desiredState ->
            when (desiredState.value) {
                SwitchableValue.ON -> {
                    desiredState.isVolumeMuted?.let { isMuted ->
                        ResolvedServiceCommand(
                            service = "volume_mute".service,
                            serviceData = TelevisionDesiredServiceData(
                                isVolumeMuted = isMuted
                            )
                        )
                    } ?: desiredState.volumeLevel?.let { volumeLevel ->
                        ResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = TelevisionDesiredServiceData(
                                volumeLevel = volumeLevel
                            )
                        )
                    } ?: desiredState.source?.let { source ->
                        ResolvedServiceCommand(
                            service = "volume_set".service,
                            serviceData = TelevisionDesiredServiceData(
                                source = source
                            )
                        )
                    } ?: ResolvedServiceCommand(
                        service = "turn_on".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

                SwitchableValue.OFF -> {
                    ResolvedServiceCommand(
                        service = "turn_off".service,
                        serviceData = EntityIdOnlyServiceData()
                    )
                }

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

data class TelevisionDesiredServiceData(
    val isVolumeMuted: Mute? = null,
    val volumeLevel: VolumeLevel? = null,
    val source: MediaSource? = null
) : DesiredServiceData()

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
