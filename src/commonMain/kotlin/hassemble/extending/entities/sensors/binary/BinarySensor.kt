package hassemble.extending.entities.sensors.binary

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.Sensor
import hassemble.values.EntityId
import hassemble.values.ObjectId
import hassemble.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.BinarySensor(objectId: ObjectId): Sensor<S, A> =
    Sensor(EntityId.fromPair("binary_sensor".domain to objectId))
