package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.Sensor
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Person = Sensor<PersonState>

fun HomeAssistantApiClient.Person(objectId: ObjectId): Person =
    Sensor(EntityId.fromPair("person".domain to objectId))

@Serializable
class PersonState(
    override val value: Zone,
    val source: EntityId,
    val id: PersonId,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<Zone>

val Person.isHome
    get() = state.value == "home".zone

val Person.isAway
    get() = state.value != "home".zone

inline fun Person.onArrivedHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("home".zone to "not_home".zone, f)

inline fun Person.onLeftHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("not_home".zone to "home".zone, f)
