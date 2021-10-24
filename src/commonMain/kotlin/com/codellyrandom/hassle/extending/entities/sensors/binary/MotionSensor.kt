package com.codellyrandom.hassle.extending.entities.sensors.binary

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.sensors.onMeasurementValueChangedFrom
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias MotionSensor = Sensor<SwitchableState, MotionSensorAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.MotionSensor(objectId: ObjectId): MotionSensor = BinarySensor(objectId)

@Serializable
data class MotionSensorAttributes(
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

inline fun MotionSensor.onMotionAlarm(crossinline f: MotionSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun MotionSensor.onMotionAlarmCleared(crossinline f: MotionSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
