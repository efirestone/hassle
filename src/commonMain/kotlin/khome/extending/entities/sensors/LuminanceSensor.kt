package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UnitOfMeasurement
import khome.values.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

@Suppress("FunctionName")
fun KhomeApplication.LuminanceSensor(objectId: ObjectId): LuminanceSensor = Sensor(objectId)

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
