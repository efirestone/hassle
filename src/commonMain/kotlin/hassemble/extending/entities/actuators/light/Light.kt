package hassemble.extending.entities.actuators.light

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.EntityId
import hassemble.values.FriendlyName
import hassemble.values.ObjectId
import hassemble.values.UserId
import hassemble.values.domain
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Light(
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
