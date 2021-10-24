package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(MediaPosition.Companion::class)
data class MediaPosition(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<MediaPosition> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MediaPosition", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = MediaPosition(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: MediaPosition) = encoder.encodeDouble(value.value)
    }
}

val Double.position
    get() = MediaPosition(this)

val Int.position
    get() = MediaPosition(this.toDouble())
