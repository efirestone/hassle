package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(PersonId.Companion::class)
data class PersonId(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<PersonId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PersonId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = PersonId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: PersonId) = encoder.encodeString(value.value)
    }
}
