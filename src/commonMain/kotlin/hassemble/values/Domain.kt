package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Domain.Companion::class)
data class Domain(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<Domain> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Domain", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Domain(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Domain) = encoder.encodeString(value.value)
    }
}

val String.domain
    get() = Domain(this)

val Enum<*>.domain
    get() = Domain(this.name)
