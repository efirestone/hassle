package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UnitOfMeasurement
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias LuminanceSensor = Sensor<LuminanceState>

fun HomeAssistantApiClient.LuminanceSensor(objectId: ObjectId): LuminanceSensor = Sensor(objectId)

@Serializable
class LuminanceState(
    override val value: Double,
    @SerialName("unit_of_measurement")
    val unitOfMeasurement: UnitOfMeasurement,
    @SerialName("user_id")
    val userId: UserId?,
    @SerialName("last_changed")
    val lastChanged: Instant,
    @SerialName("last_updated")
    val lastUpdated: Instant,
    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
) : State<Double>

fun LuminanceSensor.isBrighterThan(value: Double) = state.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = state.value < value
