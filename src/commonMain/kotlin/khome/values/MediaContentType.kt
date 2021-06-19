package khome.values

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaContentType {
    @SerialName("music")
    MUSIC,

    @SerialName("tvshow")
    TVSHOW,

    @SerialName("movie")
    MOVIE,

    @SerialName("video")
    VIDEO,

    @SerialName("episode")
    EPISODE,

    @SerialName("channel")
    CHANNEL,

    @SerialName("playlist")
    PLAYLIST
}
