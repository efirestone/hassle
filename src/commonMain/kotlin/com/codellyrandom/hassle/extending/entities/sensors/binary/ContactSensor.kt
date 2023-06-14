package com.codellyrandom.hassle.extending.entities.sensors.binary

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.sensors.Sensor
import com.codellyrandom.hassle.extending.entities.sensors.onMeasurementValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias ContactSensor = Sensor<ContactState, ContactAttributes>

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
