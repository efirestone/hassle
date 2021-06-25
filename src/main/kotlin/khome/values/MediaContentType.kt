package khome.values

import com.google.gson.annotations.SerializedName

enum class MediaContentType {
    @SerializedName("music")
    MUSIC,

    @SerializedName("tvshow")
    TVSHOW,

    @SerializedName("movie")
    MOVIE,

    @SerializedName("video")
    VIDEO,

    @SerializedName("episode")
    EPISODE,

    @SerializedName("channel")
    CHANNEL,

    @SerializedName("playlist")
    PLAYLIST
}
