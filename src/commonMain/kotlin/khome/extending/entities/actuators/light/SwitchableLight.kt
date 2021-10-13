package khome.extending.entities.actuators.light

import khome.HomeAssistantApiClient
import khome.communicating.ServiceCommandResolver
import khome.entities.devices.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.mapSwitchable
import khome.values.ObjectId

typealias SwitchableLight = Actuator<SwitchableState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.SwitchableLight(objectId: ObjectId): SwitchableLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        }
    )
