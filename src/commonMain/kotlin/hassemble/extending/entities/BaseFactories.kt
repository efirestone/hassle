package hassemble.extending.entities

import hassemble.HomeAssistantApiClient
import hassemble.HomeAssistantApiClientImpl
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.entities.devices.Sensor
import hassemble.values.EntityId

/**
 * Base factories
 */

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Sensor(id: EntityId): Sensor<S, A> =
    (this as HomeAssistantApiClientImpl).Sensor(id, S::class, A::class)

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> =
    (this as HomeAssistantApiClientImpl).Actuator(id, S::class, A::class, serviceCommandResolver)
