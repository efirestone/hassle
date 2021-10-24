package com.codellyrandom.hassle.extending.entities.sensors.binary

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias DayTime = Sensor<SwitchableState, DayTimeAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.DayTime(objectId: ObjectId): DayTime = BinarySensor(objectId)

@Serializable
data class DayTimeAttributes(
    val after: Instant,
    val before: Instant,
    @SerialName("next_update")
    val nextUpdate: Instant,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes
