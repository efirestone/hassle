package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.sensors.Sensor
import khome.extending.entities.sensors.onMeasurementValueChangedFrom
import khome.observability.Switchable
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ContactSensor = Sensor<ContactState, ContactAttributes>

@Suppress("FunctionName")
fun KhomeApplication.ContactSensor(objectId: ObjectId): ContactSensor = Sensor(objectId)

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
