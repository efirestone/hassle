package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Azimuth.Companion::class)
data class Azimuth(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Azimuth> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Azimuth", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Azimuth(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Azimuth) = encoder.encodeDouble(value.value)
    }
}

val Double.azimuth
    get() = Azimuth(this)

val Int.azimuth
    get() = Azimuth(this.toDouble())
