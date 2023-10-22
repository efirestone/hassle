package com.codellyrandom.hassle.core.mapping.serializers.default

import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = LocalTime.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.toString())
}
