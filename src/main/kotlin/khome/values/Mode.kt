package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Mode.Companion::class)
data class Mode(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<Mode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Mode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Mode(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Mode) = encoder.encodeString(value.value)
    }
}

val String.mode
    get() = Mode(this)
