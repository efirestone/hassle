package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Device.Companion::class)
data class Device(val value: String) {
    override fun toString(): String = "$value.device"

    companion object : KSerializer<Device> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Device", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Device(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Device) = encoder.encodeString(value.value)
    }
}

val String.device
    get() = Device(this)

val Enum<*>.device
    get() = Device(this.name)
