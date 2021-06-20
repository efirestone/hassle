package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import khome.values.Azimuth
import khome.values.Elevation
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.Rising
import khome.values.UserId
import khome.values.domain
import khome.values.objectId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Sun = Sensor<SunState, SunAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Sun(): Sun =
    Sensor(EntityId.fromPair("sun".domain to "sun".objectId))

@Serializable
data class SunState(override val value: SunValue) : State<SunValue>

@Serializable
enum class SunValue {
    @SerialName("above_horizon")
    ABOVE_HORIZON,

    @SerialName("below_horizon")
    BELOW_HORIZON
}

@Serializable
data class SunAttributes(
    @SerialName("next_dawn")
    val nextDawn: Instant,
    @SerialName("next_dusk")
    val nextDusk: Instant,
    @SerialName("next_midnight")
    val nextMidnight: Instant,
    @SerialName("next_noon")
    val nextNoon: Instant,
    @SerialName("next_rising")
    val nextRising: Instant,
    @SerialName("next_setting")
    val nextSetting: Instant,
    val elevation: Elevation,
    val azimuth: Azimuth,
    val rising: Rising,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName
) : Attributes

val Sun.isAboveHorizon
    get() = measurement.value == SunValue.ABOVE_HORIZON

val Sun.isBelowHorizon
    get() = measurement.value == SunValue.BELOW_HORIZON

val Sun.isRising
    get() = attributes.rising == Rising.TRUE

fun Sun.onSunrise(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON, f)

fun Sun.onSunset(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.ABOVE_HORIZON to SunValue.BELOW_HORIZON, f)
