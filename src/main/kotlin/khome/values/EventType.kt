package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(EventType.Companion::class)
data class EventType(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<EventType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("EventType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = EventType(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: EventType) = encoder.encodeString(value.value)
    }
}

val String.eventType
    get() = EventType(this)

val Enum<*>.eventType
    get() = EventType(this.name)
