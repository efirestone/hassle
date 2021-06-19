package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.PersonId
import khome.values.UserId
import khome.values.Zone
import khome.values.domain
import khome.values.zone
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Person = Sensor<PersonState, PersonAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Person(objectId: ObjectId): Person =
    Sensor(EntityId.fromPair("person".domain to objectId))

@Serializable
data class PersonState(override val value: Zone) : State<Zone>

@Serializable
data class PersonAttributes(
    val source: EntityId,
    val id: PersonId,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

val Person.isHome
    get() = measurement.value == "home".zone

val Person.isAway
    get() = measurement.value != "home".zone

inline fun Person.onArrivedHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("home".zone to "not_home".zone, f)

inline fun Person.onLeftHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("not_home".zone to "home".zone, f)
