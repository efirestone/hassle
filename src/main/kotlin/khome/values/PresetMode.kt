package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(PresetMode.Companion::class)
data class PresetMode(val value: String) {
    override fun toString(): String = value

    val isNone
        get() = value == "none"

    companion object : KSerializer<PresetMode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PresetMode", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = PresetMode(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: PresetMode) = encoder.encodeString(value.value)
    }
}

val String.presetMode
    get() = PresetMode(this)
