package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Service.Companion::class)
data class Service(val value: String) {
    companion object : KSerializer<Service> {
        fun fromDevice(device: Device): Service =
            Service(device.value)

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Service", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Service(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Service) = encoder.encodeString(value.value)
    }
}

val String.service
    get() = Service(this)
