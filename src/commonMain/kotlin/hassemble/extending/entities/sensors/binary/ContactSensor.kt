package hassemble.extending.entities.sensors.binary

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.sensors.Sensor
import hassemble.extending.entities.sensors.onMeasurementValueChangedFrom
import hassemble.observability.Switchable
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ContactSensor = Sensor<ContactState, ContactAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.ContactSensor(objectId: ObjectId): ContactSensor = Sensor(objectId)

@Serializable
data class ContactState(override val value: ContactStateValue) : State<ContactStateValue>

@Serializable
enum class ContactStateValue {
    @SerialName("open")
    OPEN,

    @SerialName("closed")
    CLOSED
}

@Serializable
data class ContactAttributes(
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName
) : Attributes

val ContactSensor.isOpen
    get() = measurement.value == ContactStateValue.OPEN

val ContactSensor.isClosed
    get() = measurement.value == ContactStateValue.CLOSED

inline fun ContactSensor.onOpened(crossinline f: ContactSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(ContactStateValue.CLOSED to ContactStateValue.OPEN, f)

inline fun ContactSensor.onClosed(crossinline f: ContactSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(ContactStateValue.OPEN to ContactStateValue.CLOSED, f)
