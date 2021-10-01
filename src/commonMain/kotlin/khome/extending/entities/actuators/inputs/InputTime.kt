package khome.extending.entities.actuators.inputs

import io.fluidsonic.time.LocalTime
import khome.HassConnection
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.serializers.default.LocalTimeSerializer
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import khome.values.service
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputTime = Actuator<InputTimeState, InputTimeAttributes>

@Suppress("FunctionName")
fun HassConnection.InputTime(objectId: ObjectId): InputTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_datetime".service,
                serviceData = InputTimeServiceData(desiredState.value)
            )
        }
    )

@Serializable
data class InputTimeState(
    @Serializable(LocalTimeSerializer::class)
    override val value: LocalTime
) : State<LocalTime>

@Serializable
data class InputTimeAttributes(
    val editable: Boolean,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

data class InputTimeServiceData(private val time: LocalTime) : DesiredServiceData()
