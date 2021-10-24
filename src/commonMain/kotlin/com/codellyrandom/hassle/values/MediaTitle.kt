package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(MediaTitle.Companion::class)
data class MediaTitle(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<MediaTitle> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MediaTitle", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = MediaTitle(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: MediaTitle) = encoder.encodeString(value.value)
    }
}
