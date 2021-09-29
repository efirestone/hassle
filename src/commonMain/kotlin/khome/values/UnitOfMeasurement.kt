@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(UnitOfMeasurement.Companion::class)
data class UnitOfMeasurement private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<UnitOfMeasurement> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("UnitOfMeasurement", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = UnitOfMeasurement(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: UnitOfMeasurement) = encoder.encodeString(value.value)
    }
}
