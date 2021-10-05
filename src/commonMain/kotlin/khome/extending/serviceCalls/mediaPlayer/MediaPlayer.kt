package khome.extending.serviceCalls.mediaPlayer

import khome.HassConnection
import khome.callService
import khome.values.Domain
import khome.values.MediaContentId
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val MEDIA_PLAYER = Domain("media_player")

suspend fun HassConnection.playMedia(mediaContentId: MediaContentId) =
    callService(
        MEDIA_PLAYER,
        Service("play_media"),
        PlayMediaData(mediaContentId)
    )

@Serializable
internal data class PlayMediaData(
    @SerialName("media_content_id")
    val mediaContentId: MediaContentId
)
