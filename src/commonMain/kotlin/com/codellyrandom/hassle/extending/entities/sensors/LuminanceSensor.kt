package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UnitOfMeasurement
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

fun HomeAssistantApiClient.LuminanceSensor(objectId: ObjectId): LuminanceSensor = Sensor(objectId)

@Serializable
data class LuminanceState(override val value: Double) : State<Double>

@Serializable
data class LuminanceAttributes(
    @SerialName("unit_of_measurement")
    val unitOfMeasurement: UnitOfMeasurement,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
) : Attributes

fun LuminanceSensor.isBrighterThan(value: Double) = measurement.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = measurement.value < value
