package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Temperature(val value: Double) {
    internal companion object : KSerializer<Temperature> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Temperature", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Temperature(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Temperature) = encoder.encodeDouble(value.value)
    }
}

val Double.degree
    get() = Temperature(this)

val Int.degree
    get() = Temperature(this.toDouble())
