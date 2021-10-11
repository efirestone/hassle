package khome.extending.entities

import khome.HomeAssistantApiClient
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.values.EntityId

/**
 * Base factories
 */

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Sensor(id: EntityId): Sensor<S, A> =
    Sensor(id, S::class, A::class)

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(id, S::class, A::class, serviceCommandResolver)
