package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Artist.Companion::class)
data class Artist(val value: String) {
    override fun toString(): String = value

    companion object : KSerializer<Artist> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Artist", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = Artist(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Artist) = encoder.encodeString(value.value)
    }
}

val String.artist
    get() = Artist(this)

val Enum<*>.artist
    get() = Artist(this.name)
