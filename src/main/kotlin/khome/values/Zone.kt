package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Zone.Companion::class)
data class Zone(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<Zone> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Zone", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Zone(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Zone) = encoder.encodeString(value.value)
    }
}

val String.zone
    get() = Zone(this)

val Enum<*>.zone
    get() = Zone(this.name)
