package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(UserId.Companion::class)
data class UserId(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<UserId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("UserId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = UserId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: UserId) = encoder.encodeString(value.value)
    }
}
