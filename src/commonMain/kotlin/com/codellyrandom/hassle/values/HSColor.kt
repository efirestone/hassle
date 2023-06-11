package com.codellyrandom.hassle.values

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

@Serializable(HSColor.Companion::class)
data class HSColor(val hue: Double, val saturation: Double) {

    companion object : KSerializer<HSColor> {
        private val hueDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("hue", PrimitiveKind.DOUBLE)
        private val saturationDescriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("brightness", PrimitiveKind.DOUBLE)

        @OptIn(InternalSerializationApi::class, kotlinx.serialization.ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            "HSColor",
            StructureKind.LIST,
            hueDescriptor,
            saturationDescriptor
        )

        override fun deserialize(decoder: Decoder): HSColor {
            return decoder.decodeStructure(ListSerializer(Double.serializer()).descriptor) {
                val hue = decodeDoubleElement(hueDescriptor, 0)
                val saturation = decodeDoubleElement(saturationDescriptor, 1)
                HSColor(hue, saturation)
            }
        }

        override fun serialize(encoder: Encoder, value: HSColor) {
            encoder.encodeStructure(ListSerializer(Double.serializer()).descriptor) {
                encodeDoubleElement(hueDescriptor, 0, value.hue)
                encodeDoubleElement(hueDescriptor, 1, value.saturation)
            }
        }
    }
}
