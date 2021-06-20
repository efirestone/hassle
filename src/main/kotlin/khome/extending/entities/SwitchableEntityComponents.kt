package khome.extending.entities

import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.entities.Attributes
import khome.entities.State
import khome.values.FriendlyName
import khome.values.UserId
import khome.values.service
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwitchableState(override val value: SwitchableValue) : State<SwitchableValue>

@Serializable
enum class SwitchableValue {
    @SerialName("on")
    ON,

    @SerialName("off")
    OFF,

    @SerialName("unavailable")
    UNAVAILABLE
}

@Serializable
data class DefaultAttributes(
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

fun mapSwitchable(switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> DefaultResolvedServiceCommand(
            service = "turn_on".service,
            serviceData = EntityIdOnlyServiceData()
        )
        SwitchableValue.OFF -> DefaultResolvedServiceCommand(
            service = "turn_off".service,
            serviceData = EntityIdOnlyServiceData()
        )

        SwitchableValue.UNAVAILABLE -> throw IllegalStateException("State cannot be changed to UNAVAILABLE")
    }
