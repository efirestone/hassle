@file:Suppress("DataClassPrivateConstructor")

package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Step.Companion::class)
data class Step private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<Step> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Step", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder) = Step(decoder.decodeDouble())
        override fun serialize(encoder: Encoder, value: Step) = encoder.encodeDouble(value.value)
    }
}
