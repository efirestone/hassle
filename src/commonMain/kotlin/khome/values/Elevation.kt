package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Elevation.Companion::class)
data class Elevation(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Elevation> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Elevation", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Elevation(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Elevation) = encoder.encodeDouble(value.value)
    }
}
