package hassemble.extending.entities.actuators.climate

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.EntityId
import hassemble.values.ObjectId
import hassemble.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.ClimateControl(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId.fromPair("climate".domain to objectId), serviceCommandResolver)
