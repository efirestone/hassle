@file:Suppress("DataClassPrivateConstructor")

package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Mute.Companion::class)
data class Mute private constructor(val value: Boolean) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Mute> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Mute", PrimitiveKind.BOOLEAN)
        override fun deserialize(decoder: Decoder) = Mute(decoder.decodeBoolean())
        override fun serialize(encoder: Encoder, value: Mute) = encoder.encodeBoolean(value.value)

        val TRUE: Mute get() = Mute(true)

        val FALSE: Mute get() = Mute(false)
    }
}
