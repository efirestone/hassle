@file:Suppress("DataClassPrivateConstructor")

package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ColorTemperature.Companion::class)
data class ColorTemperature private constructor(val value: Int, val unit: Unit) {
    override fun toString(): String = "$value.${unit.name.lowercase()}"

    companion object : KSerializer<ColorTemperature> {
        fun fromMired(value: Int) = ColorTemperature(value, Unit.MIRED)
        fun fromKelvin(value: Int) = ColorTemperature(value, Unit.KELVIN)

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ColorTemperature", PrimitiveKind.INT)
        override fun deserialize(decoder: Decoder) = ColorTemperature(decoder.decodeInt(), Unit.MIRED)
        override fun serialize(encoder: Encoder, value: ColorTemperature) = encoder.encodeInt(value.value)
    }

    enum class Unit {
        MIRED, KELVIN
    }
}

val Int.mired
    get() = ColorTemperature.fromMired(this)

val Int.kelvin
    get() = ColorTemperature.fromKelvin(this)
