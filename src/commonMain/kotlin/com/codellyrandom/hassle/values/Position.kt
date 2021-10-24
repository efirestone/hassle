package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Position.Companion::class)
data class Position(val value: Int) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Position> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Position", PrimitiveKind.INT)
        override fun deserialize(decoder: Decoder) = Position(decoder.decodeInt())
        override fun serialize(encoder: Encoder, value: Position) = encoder.encodeInt(value.value)
    }
}

val Int.pctPosition
    get() = Position(this)
