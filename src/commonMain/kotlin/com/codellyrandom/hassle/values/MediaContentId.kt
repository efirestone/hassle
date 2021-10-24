package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(MediaContentId.Companion::class)
data class MediaContentId(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<MediaContentId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MediaContentId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = MediaContentId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: MediaContentId) = encoder.encodeString(value.value)
    }
}
