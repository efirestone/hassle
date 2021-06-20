package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ObjectId.Companion::class)
data class ObjectId(val value: String) {

    override fun toString(): String = value

    companion object : KSerializer<ObjectId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ObjectId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = ObjectId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: ObjectId) = encoder.encodeString(value.value)
    }
}

val String.objectId
    get() = ObjectId(this)
