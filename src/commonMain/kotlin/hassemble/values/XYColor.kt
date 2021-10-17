package hassemble.values

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(XYColor.Companion::class)
data class XYColor(val x: Double, val y: Double) {
    companion object : KSerializer<XYColor> {
        private val xDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("x", PrimitiveKind.DOUBLE)
        private val yDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("y", PrimitiveKind.DOUBLE)

        @OptIn(InternalSerializationApi::class, kotlinx.serialization.ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            "XYColor",
            StructureKind.LIST,
            xDescriptor,
            yDescriptor
        )

        override fun deserialize(decoder: Decoder): XYColor {
            decoder.decodeStructure(ListSerializer(Double.serializer()).descriptor) {
                val x = decodeDoubleElement(xDescriptor, 0)
                val y = decodeDoubleElement(yDescriptor, 1)
                return XYColor(x, y)
            }
        }

        override fun serialize(encoder: Encoder, value: XYColor) {
            encoder.encodeStructure(ListSerializer(Double.serializer()).descriptor) {
                encodeDoubleElement(xDescriptor, 0, value.x)
                encodeDoubleElement(yDescriptor, 1, value.y)
            }
        }
    }
}
