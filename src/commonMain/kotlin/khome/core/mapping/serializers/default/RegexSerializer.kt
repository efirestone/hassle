package khome.core.mapping.serializers.default

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object RegexSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Regex) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder) = Regex(decoder.decodeString())
}
