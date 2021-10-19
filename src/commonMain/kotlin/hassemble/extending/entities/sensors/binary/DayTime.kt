package hassemble.extending.entities.sensors.binary

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.SwitchableState
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.UserId
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
