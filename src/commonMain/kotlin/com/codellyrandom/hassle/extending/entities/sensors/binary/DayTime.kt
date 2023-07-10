package com.codellyrandom.hassle.extending.entities.sensors.binary

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias DayTime = Sensor<DayTimeState>

fun HomeAssistantApiClient.DayTime(objectId: ObjectId): DayTime = BinarySensor(objectId)

@Serializable
class DayTimeState(
    override val value: SwitchableValue,
    val after: Instant,
    val before: Instant,
    @SerialName("next_update")
    val nextUpdate: Instant,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
) : State<SwitchableValue>
