package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The ID of a Home Assistant config entry.
 */
@Serializable(ConfigEntryId.Companion::class)
data class ConfigEntryId(val value: String) {

    override fun toString(): String = value

    companion object : KSerializer<ConfigEntryId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ConfigEntryId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = ConfigEntryId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: ConfigEntryId) = encoder.encodeString(value.value)
    }
}
