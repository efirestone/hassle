package hassemble.extending.entities.sensors

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.UnitOfMeasurement
import hassemble.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

@Suppress("FunctionName")
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
    override val friendlyName: FriendlyName
) : Attributes

fun LuminanceSensor.isBrighterThan(value: Double) = measurement.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = measurement.value < value
