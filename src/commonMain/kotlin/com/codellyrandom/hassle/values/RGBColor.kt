package com.codellyrandom.hassle.values

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(RGBColor.Companion::class)
data class RGBColor(val red: Int, val green: Int, val blue: Int) {
    companion object : KSerializer<RGBColor> {
        private val redDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("red", PrimitiveKind.INT)
        private val greenDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("green", PrimitiveKind.INT)
        private val blueDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("blue", PrimitiveKind.INT)

        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            "RGBColor",
            StructureKind.LIST,
            redDescriptor,
            greenDescriptor,
            blueDescriptor
        )

        override fun deserialize(decoder: Decoder): RGBColor {
            return decoder.decodeStructure(ListSerializer(Int.serializer()).descriptor) {
                val red = decodeIntElement(redDescriptor, 0)
                val green = decodeIntElement(greenDescriptor, 1)
                val blue = decodeIntElement(blueDescriptor, 2)
                RGBColor(red, green, blue)
            }
        }

        override fun serialize(encoder: Encoder, value: RGBColor) {
            encoder.encodeStructure(ListSerializer(Int.serializer()).descriptor) {
                encodeIntElement(redDescriptor, 0, value.red)
                encodeIntElement(greenDescriptor, 1, value.green)
                encodeIntElement(blueDescriptor, 2, value.blue)
            }
        }
    }
}
