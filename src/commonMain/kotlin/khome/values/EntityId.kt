package khome.values

import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The EntityId of an [Actuator] or [Sensor]
 *
 * The entity id is not a member of an [Actuator] or [Sensor] but is used
 * as a key in the registries.
 *
 * @property domain the domain that the entity belongs to e.g. cover, light, sensor
 * @property objectId the object id of an entity
 */
@Serializable(EntityId.Companion::class)
data class EntityId(val domain: Domain, val objectId: ObjectId) {

    /**
     * The EntityId's string representation.
     *
     * Equals the value that is used to communicate with
     * home assistant.
     */
    override fun toString(): String = "${domain.value}.${objectId.value}"

    companion object : KSerializer<EntityId> {

        fun fromPair(pair: Pair<Domain, ObjectId>) =
            fromString("${pair.first}.${pair.second}")

        fun fromString(value: String): EntityId {
            val parts = value.split(".")
            check(parts.size == 2) { "EntityId has wrong format. Correct format is: \"domain.objectId\"" }
            val (domain, id) = parts
            return EntityId(Domain(domain), ObjectId(id))
        }

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("EntityId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder) = fromString(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: EntityId) = encoder.encodeString(value.toString())
    }
}
