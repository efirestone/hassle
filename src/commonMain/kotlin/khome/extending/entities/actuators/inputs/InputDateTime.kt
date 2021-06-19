package khome.extending.entities.actuators.inputs

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.serializers.default.LocalDateTimeSerializer
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
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias InputDateTime = Actuator<InputDateTimeState, InputDateTimeAttributes>

@Suppress("FunctionName")
fun KhomeApplication.InputDateTime(objectId: ObjectId): InputDateTime =
    Actuator(
        EntityId.fromPair("input_datetime".domain to objectId),
        ServiceCommandResolver { desiredState ->
            DefaultResolvedServiceCommand(
                service = "set_datetime".service,
                serviceData = InputDateTimeServiceData(
                    desiredState.value
                )
            )
        }
    )

@Serializable
data class InputDateTimeState(
    @Serializable(LocalDateTimeSerializer::class)
    override val value: LocalDateTime
) : State<LocalDateTime>

@Serializable
data class InputDateTimeAttributes(
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

data class InputDateTimeServiceData(private val datetime: LocalDateTime) : DesiredServiceData()
