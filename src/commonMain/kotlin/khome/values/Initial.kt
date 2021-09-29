@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Initial.Companion::class)
data class Initial private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Initial> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Initial", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Initial(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Initial) = encoder.encodeDouble(value.value)
    }
}
