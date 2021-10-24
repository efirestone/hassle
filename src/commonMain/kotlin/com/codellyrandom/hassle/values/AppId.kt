package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(AppId.Companion::class)
data class AppId(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<AppId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AppId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = AppId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: AppId) = encoder.encodeString(value.value)
    }
}

val String.appId
    get() = AppId(this)

val Enum<*>.appId
    get() = AppId(this.name)
