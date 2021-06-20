@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Rising.Companion::class)
data class Rising private constructor(val value: Boolean) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Rising> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Rising", PrimitiveKind.BOOLEAN)
        override fun deserialize(decoder: Decoder) = Rising(decoder.decodeBoolean())
        override fun serialize(encoder: Encoder, value: Rising) = encoder.encodeBoolean(value.value)

        val TRUE get(): Rising = Rising(true)

        val FALSE get(): Rising = Rising(false)
    }
}
