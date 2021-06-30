package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.onMeasurementValueChangedFrom
import khome.observability.Switchable
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias MotionSensor = Sensor<SwitchableState, MotionSensorAttributes>

@Suppress("FunctionName")
fun KhomeApplication.MotionSensor(objectId: ObjectId): MotionSensor = BinarySensor(objectId)

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
