package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(AlbumName.Companion::class)
data class AlbumName(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<AlbumName> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AlbumName", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = AlbumName(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: AlbumName) = encoder.encodeString(value.value)
    }
}

val String.albumName
    get() = AlbumName(this)

val Enum<*>.albumName
    get() = AlbumName(this.name)
