package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Option.Companion::class)
data class Option(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<Option> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Option", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Option(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Option) = encoder.encodeString(value.value)
    }
}

val String.option
    get() = Option(this)

val Enum<*>.option
    get() = Option(this.name)
