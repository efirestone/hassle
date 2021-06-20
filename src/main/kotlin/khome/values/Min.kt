@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Min.Companion::class)
data class Min private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Min> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Min", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Min(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Min) = encoder.encodeDouble(value.value)
    }
}
