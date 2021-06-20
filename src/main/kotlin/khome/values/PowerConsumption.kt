@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class PowerConsumption private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    internal companion object : KSerializer<PowerConsumption> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PowerConsumption", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = PowerConsumption(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: PowerConsumption) = encoder.encodeDouble(value.value)
    }
}
