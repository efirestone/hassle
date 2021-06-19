package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(HvacMode.Companion::class)
data class HvacMode(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<HvacMode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("HvacMode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = HvacMode(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: HvacMode) = encoder.encodeString(value.value)
    }
}

val String.hvacMode
    get() = HvacMode(this)
