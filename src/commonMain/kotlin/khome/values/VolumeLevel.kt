package khome.values

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class VolumeLevel(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KSerializer<VolumeLevel> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("VolumeLevel", PrimitiveKind.DOUBLE)
        override fun deserialize(decoder: Decoder): VolumeLevel {
            val volume = decoder.decodeDouble()
            check(volume >= 0) { "Volume can not be a negative value" }
            check(volume <= 100) { "Volume can not be greater than 100(percent)" }
            return VolumeLevel(volume)
        }
        override fun serialize(encoder: Encoder, value: VolumeLevel) {
            return encoder.encodeDouble(value.value / 100)
        }
    }
}

val Double.pctVolume
    get() = VolumeLevel(this)

val Int.pctVolume
    get() = VolumeLevel(this.toDouble())
