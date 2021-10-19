package hassemble.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(MediaSource.Companion::class)
data class MediaSource(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<MediaSource> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("MediaSource", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = MediaSource(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: MediaSource) = encoder.encodeString(value.value)
    }
}

val String.mediaSource
    get() = MediaSource(this)

val Enum<*>.mediaSource
    get() = MediaSource(this.name)
