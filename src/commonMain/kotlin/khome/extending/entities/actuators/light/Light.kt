package khome.extending.entities.actuators.light

import khome.HassConnection
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.ColorName
import khome.values.ColorTemperature
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> HassConnection.Light(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId.fromPair("light".domain to objectId), serviceCommandResolver)

@Serializable
data class LightAttributes(
    @SerialName("supported_features")
    val supportedFeatures: Int,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes

data class NamedColorServiceData(val color_name: ColorName) : DesiredServiceData()
data class KelvinServiceData(val kelvin: ColorTemperature) : DesiredServiceData()
