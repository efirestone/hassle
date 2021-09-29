package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Icon.Companion::class)
data class Icon(val value: String) {
    override fun toString(): String = "$value.icon"

    companion object : KSerializer<Icon> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Icon", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Icon(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Icon) = encoder.encodeString(value.value)
    }
}

val String.icon
    get() = Icon(this)
