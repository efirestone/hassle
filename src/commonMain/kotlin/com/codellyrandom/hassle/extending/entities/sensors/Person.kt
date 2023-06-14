package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.Sensor
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.PersonId
import com.codellyrandom.hassle.values.UserId
import com.codellyrandom.hassle.values.Zone
import com.codellyrandom.hassle.values.domain
import com.codellyrandom.hassle.values.zone
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Person = Sensor<PersonState, PersonAttributes>

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
