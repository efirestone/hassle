package khome.extending.entities.sensors.binary

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.devices.Sensor
import khome.extending.entities.SwitchableState
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias DayTime = Sensor<SwitchableState, DayTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.DayTime(objectId: ObjectId): DayTime = BinarySensor(objectId)

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
