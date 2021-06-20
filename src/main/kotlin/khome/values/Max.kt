@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Max.Companion::class)
data class Max private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Max> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Max", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Max(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Max) = encoder.encodeDouble(value.value)
    }
}
