package hassemble.extending.entities.sensors.binary

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.SwitchableValue
import hassemble.extending.entities.sensors.onMeasurementValueChangedFrom
import hassemble.observability.Switchable
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.UserId
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
