package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.Sensor
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias Sun = Sensor<SunState>

fun HomeAssistantApiClient.Sun(): Sun =
    Sensor(EntityId.fromPair("sun".domain to "sun".objectId))

@Serializable
class SunState(
    override val value: SunValue,

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
    val userId: UserId?,

    @SerialName("last_changed")
    val lastChanged: Instant,

    @SerialName("last_updated")
    val lastUpdated: Instant,

    @SerialName("friendly_name")
    val friendlyName: FriendlyName,
) : State<SunValue>

@Serializable
enum class SunValue {
    @SerialName("above_horizon")
    ABOVE_HORIZON,

    @SerialName("below_horizon")
    BELOW_HORIZON,
}

val Sun.isAboveHorizon
    get() = state.value == SunValue.ABOVE_HORIZON

val Sun.isBelowHorizon
    get() = state.value == SunValue.BELOW_HORIZON

val Sun.isRising
    get() = state.rising == Rising.TRUE

fun Sun.onSunrise(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.BELOW_HORIZON to SunValue.ABOVE_HORIZON, f)

fun Sun.onSunset(f: Sun.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(SunValue.ABOVE_HORIZON to SunValue.BELOW_HORIZON, f)
