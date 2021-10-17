package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(FriendlyName.Companion::class)
data class FriendlyName(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<FriendlyName> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("FriendlyName", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): FriendlyName {
            return FriendlyName(decoder.decodeString())
        }
        override fun serialize(encoder: Encoder, value: FriendlyName) = encoder.encodeString(value.value)
    }
}
