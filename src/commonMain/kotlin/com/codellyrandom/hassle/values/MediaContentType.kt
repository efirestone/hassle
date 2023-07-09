package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(MediaContentType.Companion::class)
enum class MediaContentType {
    MUSIC,
    TVSHOW,
    MOVIE,
    VIDEO,
    EPISODE,
    CHANNEL,
    PLAYLIST,
    ;

    companion object : KSerializer<MediaContentType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MediaContentType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = valueOf(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: MediaContentType) = encoder.encodeString(value.name)

        fun valueOf(string: String): MediaContentType {
            val uppercaseString = string.uppercase()
            return values().firstOrNull { it.name == uppercaseString }
                ?: throw IllegalArgumentException("$string is not a valid MediaContentType")
        }
    }
}
