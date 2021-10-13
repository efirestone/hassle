package khome.extending.entities.sensors.binary

import khome.HomeAssistantApiClient
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.values.EntityId
import khome.values.ObjectId
import khome.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.BinarySensor(objectId: ObjectId): Sensor<S, A> =
    Sensor(EntityId.fromPair("binary_sensor".domain to objectId))
