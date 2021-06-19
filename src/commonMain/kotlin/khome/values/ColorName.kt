package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ColorName.Companion::class)
data class ColorName(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<ColorName> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ColorName", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = ColorName(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: ColorName) = encoder.encodeString(value.value)
    }
}

val String.color
    get() = ColorName(this)

val Enum<*>.color
    get() = ColorName(this.name)
