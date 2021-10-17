package hassemble.extending.entities.sensors

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.Sensor
import hassemble.observability.Switchable
import hassemble.values.EntityId
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.PersonId
import hassemble.values.UserId
import hassemble.values.Zone
import hassemble.values.domain
import hassemble.values.zone
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Person = Sensor<PersonState, PersonAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.Person(objectId: ObjectId): Person =
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
