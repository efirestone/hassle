package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Brightness.Companion::class)
data class Brightness(val value: Int) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Brightness> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Brightness", PrimitiveKind.INT)
        override fun deserialize(decoder: Decoder) = Brightness(decoder.decodeInt())
        override fun serialize(encoder: Encoder, value: Brightness) = encoder.encodeInt(value.value)
    }
}

val Int.brightness
    get() = Brightness(this)
