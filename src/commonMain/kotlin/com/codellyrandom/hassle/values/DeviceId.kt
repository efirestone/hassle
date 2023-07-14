import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The ID of a Home Assistant device.
 *
 * Devices usually represent a physical device and are a grouping of sensors.
 */
@Serializable(DeviceId.Companion::class)
data class DeviceId(val value: String) {

    override fun toString(): String = value

    companion object : KSerializer<DeviceId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("DeviceId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = DeviceId(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: DeviceId) = encoder.encodeString(value.value)
    }
}
