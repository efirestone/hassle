package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(EntityPicture.Companion::class)
data class EntityPicture(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<EntityPicture> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("EntityPicture", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = EntityPicture(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: EntityPicture) = encoder.encodeString(value.toString())
    }
}
