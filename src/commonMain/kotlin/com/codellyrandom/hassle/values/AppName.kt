package com.codellyrandom.hassle.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(AppName.Companion::class)
data class AppName(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<AppName> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AppName", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = AppName(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: AppName) = encoder.encodeString(value.value)
    }
}

val String.appName
    get() = AppName(this)

val Enum<*>.appName
    get() = AppName(this.name)
